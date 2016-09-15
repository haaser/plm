package de.ivz.plm.agent.statistic.transfomer;

import de.ivz.plm.agent.statistic.collector.StatisticCollector;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EjbStatisticTransformer - Zur Bytecode-Instrumentierung speziell für EJBs 1.x und 3.0
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class EjbStatisticTransformer implements ClassFileTransformer {

    private static final Logger log = Logger.getLogger(EjbStatisticTransformer.class.getName());

    /* Reguläre Methoden bei EJB 1.x */
    public static final String[] ejb1Methods = new String[]{"ejbCreate", "ejbRemove", "ejbActivate", "ejbPassivate", "ejbStore", "ejbLoad", "ejbPostCreate", "setSessionContext", "setEntityContext", "unsetEntityContext", "setMessageDrivenContext"};
    /* Reguläre Schnittstellen bei EJB 1.x */
    public static final String[] ejb1Interfaces = new String[]{"javax.ejb.SessionBean", "javax.ejb.EntityBean", "javax.ejb.MessageDrivenBean", "javax.jms.MessageListener"};
    /* Reguläre Annotationen bei EJB 3.0 */
    public static final String[] ejb3Annotations = new String[]{"@javax.ejb.Stateless", "@javax.ejb.Stateful", "@javax.ejb.MessageDriven", "@javax.ejb.Singleton", "@javax.persistence.Entity"};

    private String classNameRegex;

    /**
    * Konstruktor, welcher einen regulären Ausdruck für die spätere Filterung annimmt
    * @param classNameRegex der gesetzt werden soll
    */
    public EjbStatisticTransformer(String classNameRegex) {
        this.classNameRegex = classNameRegex;
        log.info("successfully initialized - classNameRegex: " + this.classNameRegex);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        String normalizedClassName = null;
        try {
            normalizedClassName = className.replace('/', '.');
            // Die Klassen soll nur dann transformiert werden, wenn dessen vollqualifizierender Name dem regulären Ausdruck entspricht?
            if (normalizedClassName.matches(classNameRegex)) {
                // Bereite den ClassPool vor - erst den JVM-Standard und zusätzlich noch den Context-Classloader (JBoss/Tomcat)
                ClassPool classPool = new ClassPool();
                classPool.appendClassPath(new ByteArrayClassPath(normalizedClassName, byteCode));
                classPool.appendClassPath(new LoaderClassPath(loader));
                try {
                    // Lade die originale Klasse
                    CtClass ctClass = classPool.get(normalizedClassName);
                    if (!hasField(ctClass, "__plm") || ctClass.isFrozen()) {
                        // Nur weiter machen, wenn es sich um eine echte Klasse handelt
                        if (!(ctClass.isPrimitive() || ctClass.isEnum() || ctClass.isAnnotation() || ctClass.isInterface() || ctClass.isArray())) {
                            boolean isClassModified = false;
                            // Überprüfe die Klassen anhand der regulären Merkmale für EJB 1.x und 3.0
                            boolean isEjb1 = hasInterface(ctClass, ejb1Interfaces);
                            boolean isEjb3 = hasAnnotation(ctClass, ejb3Annotations);
                            // Wenn noch keine EJB-Qualifikation vorliegt, überprüfe diese Eigenschaften an allen vererbenden Klassen
                            if (!isEjb1 && !isEjb3) {
                                CtClass sCtClass = ctClass.getSuperclass();
                                isEjb1 = hasInterface(sCtClass, ejb1Interfaces);
                                isEjb3 = hasAnnotation(sCtClass, ejb3Annotations);
                            }
                            // Wenn die Klasse Eigendchaften gemäß EJB 1.x oder 3.x aufweist, dann muss diese instrumentiert werden
                            if (isEjb1 || isEjb3) {
                                for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                                    if (!Modifier.isAbstract(ctMethod.getModifiers()) && (isEjb3 || isEjb1 && !matches(ctMethod.getName(), ejb1Methods))) {
                                        try {
                                            ctMethod.addLocalVariable("__plmDuration", CtClass.longType);
                                            ctMethod.insertBefore("{ de.ivz.plm.agent.statistic.collector.StatisticCollector.callIn(\"" + ctClass.getName() + "\"); __plmDuration = System.currentTimeMillis(); }");
                                            ctMethod.insertAfter("{ __plmDuration = System.currentTimeMillis() - __plmDuration; de.ivz.plm.agent.statistic.collector.StatisticCollector.callOut(\"" + ctClass.getName() + "\"); de.ivz.plm.agent.statistic.collector.StatisticCollector.update(\"" + ctClass.getName() + "\", \"" + ctMethod.getMethodInfo().toString() + "\", __plmDuration); }");
                                            isClassModified = true;
                                            // Registriere die Klasse bei der Instrumentierung, wenn der StatisticCollector so eingestellt ist
                                            if (StatisticCollector.fullreg()) {
                                                StatisticCollector.update(ctClass.getName(), ctMethod.getMethodInfo().toString(), -1l);
                                            }
                                        } catch (CannotCompileException e) {
                                            log.log(Level.SEVERE, "could not transform method '" + ctMethod.getMethodInfo().toString() + "' of class '" + normalizedClassName + "'", e);
                                        }
                                    }
                                }
                            } else {
                                log.fine("class '" + normalizedClassName + "' skipped by: ejb-conditions");
                            }
                            // Wenn die Klasse modifiziert wurde, dann muss diese markiert und kompiliert werden
                            if (isClassModified) {
                                try {
                                    CtField plmField = new CtField(CtClass.booleanType, "__plm", ctClass);
                                    plmField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
                                    ctClass.addField(plmField, CtField.Initializer.constant(false));
                                    byteCode = ctClass.toBytecode();
                                    log.info("class '" + normalizedClassName + "' successfully tagged and transformed");
                                } catch (Exception e) {
                                    log.log(Level.SEVERE, "could not compile class '" + normalizedClassName + "'", e);
                                }
                            }
                        } else {
                            log.fine("class '" + normalizedClassName + "' skipped by: type (primitive:" + ctClass.isPrimitive() + ", array:" + ctClass.isArray() + ", enum:" + ctClass.isEnum() + ", annotation:" + ctClass.isAnnotation() + ", interface:" + ctClass.isInterface() + ")");
                        }
                    } else {
                        log.fine("class '" + normalizedClassName + "' already transformed (plm:" + hasField(ctClass, "__plm") + ", modified:" + ctClass.isModified() + ", frozen:" + ctClass.isFrozen() + ")");
                    }
                } catch (NotFoundException e) {
                    log.log(Level.SEVERE, "could not load class '" + normalizedClassName + "' from class-pool", e);
                }
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "could not inspect and transform class '" + normalizedClassName + "'", t);
        }
        return byteCode;
    }

    /**
     * Überprüft die Klasse, ob ein Feld deklariert ist
     * @param ctClass die Klasse
     * @param name der Name des Feldes
     * @return on die Klasse das Feld aufweist
     */
    private boolean hasField(CtClass ctClass, String name) {
        if (ctClass != null && name != null) {
            try {
                ctClass.getDeclaredField(name);
                return true;
            } catch (NotFoundException e) {
                // ignore
            }
        }
        return false;
    }

    /**
     * Überprüft die Klasse, ob diese mindestens eine Annotaion aus einer gegeben Liste aufweist
     * @param ctClass die Klasse
     * @param anNames Liste von Namen der Annotationen
     * @return ob die Klasse eine solche Annotation aufweist
     */
    private boolean hasAnnotation(CtClass ctClass, String[] anNames) {
        if (ctClass != null && anNames != null && anNames.length > 0) {
            try {
                for (Object obj : ctClass.getAnnotations()) {
                    for (String anName : anNames) {
                        if (anName != null && anName.equals(obj.toString())) {
                            return true;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // ignore - could not load annotations
                // trying to compare plain message with annotation name
                for (String anName : anNames) {
                    if (anName != null && (anName.equals(e.getMessage()) || anName.equals("@" + e.getMessage()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Überprüft die Klasse, ob diese mindestens eine Schnittselle aus einer gegeben Liste implementiert
     * @param ctClass die Klasse
     * @param inNames Liste von Namen der Schnittsellen
     * @return ob die Klasse eine solche Schnittselle aufweist
     */
    private boolean hasInterface(CtClass ctClass, String[] inNames) {
        if (ctClass != null && inNames != null) {
            try {
                for (CtClass iObj : ctClass.getInterfaces()) {
                    for (String inName : inNames) {
                        if (inName != null && inName.equals(iObj.getName())) {
                            return true;
                        }
                    }
                }
            } catch (NotFoundException e) {
                // ignore - could not load annotations
                // trying to compare plain message with annotation name
                for (String inName : inNames) {
                    if (inName != null && inName.equals(e.getMessage())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Überprüft einen String, ob dieser in einer vorgegeben Liste vorhanden ist
     * @param name der String
     * @param names die Liste an Strings
     * @return on der String in der Liste vorhanden ist
     */
    private boolean matches(String name, String[] names) {
        if (name != null && names != null) {
            for (String n : names) {
                if (n != null && n.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}

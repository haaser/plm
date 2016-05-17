package de.ivz.plm.agent.statistic.transfomer;

import de.ivz.plm.agent.statistic.collector.StatisticCollector;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EjbStatisticTransformer implements ClassFileTransformer {

    private static final Logger log = Logger.getLogger(EjbStatisticTransformer.class.getName());

    public static final String[] ejb1Methods = new String[]{"ejbCreate", "ejbRemove", "ejbActivate", "ejbPassivate", "ejbStore", "ejbLoad", "ejbPostCreate", "setSessionContext", "setEntityContext", "unsetEntityContext", "setMessageDrivenContext"};
    public static final String[] ejb1Interfaces = new String[]{"javax.ejb.SessionBean", "javax.ejb.EntityBean", "javax.ejb.MessageDrivenBean", "javax.jms.MessageListener"};
    public static final String[] ejb3Annotations = new String[]{"@javax.ejb.Stateless", "@javax.ejb.Stateful", "@javax.ejb.MessageDriven", "@javax.ejb.Singleton", "@javax.persistence.Entity"};

    private String classNameRegex;

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
            if (normalizedClassName.matches(classNameRegex)) {
                // prepare classpool - jvm-default & context-classloader (jboss/tomcat)
                ClassPool classPool = new ClassPool();
                classPool.appendClassPath(new ByteArrayClassPath(normalizedClassName, byteCode));
                classPool.appendClassPath(new LoaderClassPath(loader));
                //if (loader.getParent() != null) {
                //    classPool.appendClassPath(new LoaderClassPath(loader.getParent()));
                //}
                try {
                    // load original class
                    CtClass ctClass = classPool.get(normalizedClassName);
                    if (!hasField(ctClass, "__plm") || ctClass.isFrozen()) {
                        // only apply if class is a real class
                        if (!(ctClass.isPrimitive() || ctClass.isEnum() || ctClass.isAnnotation() || ctClass.isInterface() || ctClass.isArray())) {
                            boolean isClassModified = false;
                            // check interfaces and annotations
                            boolean isEjb1 = hasInterface(ctClass, ejb1Interfaces);
                            boolean isEjb3 = hasAnnotation(ctClass, ejb3Annotations);
                            // no match? check extended classes!
                            if (!isEjb1 && !isEjb3) {
                                CtClass sCtClass = ctClass.getSuperclass();
                                isEjb1 = hasInterface(sCtClass, ejb1Interfaces);
                                isEjb3 = hasAnnotation(sCtClass, ejb3Annotations);
                            }
                            if (isEjb1 || isEjb3) {
                                for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                                    if (!Modifier.isAbstract(ctMethod.getModifiers()) && (isEjb3 || isEjb1 && !matches(ctMethod.getName(), ejb1Methods))) {
                                        try {
                                            ctMethod.addLocalVariable("__plmDuration", CtClass.longType);
                                            ctMethod.insertBefore("{ de.ivz.plm.agent.statistic.collector.StatisticCollector.callIn(\"" + ctClass.getName() + "\"); __plmDuration = System.currentTimeMillis(); }");
                                            ctMethod.insertAfter("{ __plmDuration = System.currentTimeMillis() - __plmDuration; de.ivz.plm.agent.statistic.collector.StatisticCollector.callOut(\"" + ctClass.getName() + "\"); de.ivz.plm.agent.statistic.collector.StatisticCollector.update(\"" + ctClass.getName() + "\", \"" + ctMethod.getMethodInfo().toString() + "\", __plmDuration); }");
                                            isClassModified = true;
                                            // initial collector registration
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

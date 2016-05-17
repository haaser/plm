package de.ivz.plm.agent.statistic.transfomer;

import de.ivz.plm.agent.statistic.collector.StatisticCollector;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassStatisticTransformer implements ClassFileTransformer {

    private static final Logger log = Logger.getLogger(ClassStatisticTransformer.class.getName());

    private String classNameRegex;

    public ClassStatisticTransformer(String classNameRegex) {
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
                            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                                if (!Modifier.isAbstract(ctMethod.getModifiers())) {
                                    try {
                                        ctMethod.addLocalVariable("__plmDuration", CtClass.longType);
                                        ctMethod.insertBefore("{ de.ivz.plm.agent.statistic.collector.StatisticCollector.in(\"" + ctClass.getName() + "\"); __plmDuration = System.currentTimeMillis(); }");
                                        ctMethod.insertAfter("{ __plmDuration = System.currentTimeMillis() - __plmDuration; de.ivz.plm.agent.statistic.collector.StatisticCollector.out(\"" + ctClass.getName() + "\"); de.ivz.plm.agent.statistic.collector.StatisticCollector.update(\"" + ctClass.getName() + "\", \"" + ctMethod.getMethodInfo().toString() + "\", __plmDuration); }");
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
}

package com.keven1z.core.hook;

import com.keven1z.core.utils.*;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.io.FileUtils.writeStringToFile;


public class HookTransformer implements ClassFileTransformer {
    Set<String> reTransformClasses = new HashSet<>();
    private final String scanMode;
    private final boolean isCleanupShell;
    private final Instrumentation instrumentation;
    public final static String MODE_FULL = "full";
    public HookTransformer(Instrumentation instrumentation, String scanMode, boolean isCleanupShell) {
        this.instrumentation = instrumentation;
        this.scanMode = scanMode;
        this.isCleanupShell = isCleanupShell;
        this.instrumentation.addTransformer(this, true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        /* 若className为空，不进行任何操作，直接返回 */
        if (null == className) {
            return classfileBuffer;
        }
        if (classBeingRedefined == null) {
            return classfileBuffer;
        }
        if (!reTransformClasses.contains(className)) {
            return classfileBuffer;
        }
        try {
            byte[] originalClassBytes = getOriginalClassBytes(className, loader);
            if (originalClassBytes == null) {
                return classfileBuffer;
            }
            if (originalClassBytes.length == classfileBuffer.length || compareBytecodeLineCount(originalClassBytes, classfileBuffer)) {
                return classfileBuffer;
            }
            System.out.println("[!] 发现注入的类：" + className);
            String originalCLassPath = dumpClassIfNecessary(className + ".original", Decompiler.getDecompilerString(originalClassBytes, className));
            String dangerCLassPath = dumpClassIfNecessary(className + ".danger", Decompiler.getDecompilerString(classfileBuffer, className));
            if (originalCLassPath != null && dangerCLassPath != null) {
                System.out.println("[!] 原始类保存在"+originalCLassPath);
                System.out.println("[!] 注入类保存在"+dangerCLassPath);
            }
            /*
             * 清理注入的类
             */
            if (isCleanupShell) {
                return originalClassBytes;
            }
        } catch (Throwable throwable) {
        }
        return classfileBuffer;

    }


    private static String dumpClassIfNecessary(String className, String data) {
        final File dumpClassFile = new File("./danger-class-dump/" + className + ".java");
        final File classPath = new File(dumpClassFile.getParent());
        // 创建类所在的包路径
        if (!classPath.mkdirs() && !classPath.exists()) {
            return null;
        }
        // 将类字节码写入文件
        try {
            writeStringToFile(dumpClassFile, data, Charset.defaultCharset());
            return dumpClassFile.getAbsolutePath();
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * hook已经加载的类，或者是回滚已经加载的类
     */
    public void reTransform() {
        List<Class<?>> loadedClasses = findForReTransform();
        for (Class<?> clazz : loadedClasses) {
            try {
                reTransformClasses.add(ClassUtils.normalizeClass(clazz.getName()));
                instrumentation.retransformClasses(clazz);
            } catch (Throwable ignored) {
            }
        }
    }

    public static boolean compareBytecodeLineCount(byte[] originalClassBytes, byte[] currentClassBytes) {
        // 检查输入是否为空
        if (originalClassBytes == null || currentClassBytes == null) {
            throw new IllegalArgumentException("字节码数组不能为空");
        }

        // 计算原始字节码的行数
        int originLineCount = calculateLineCount(originalClassBytes);

        // 计算当前字节码的行数
        int currentLineCount = calculateLineCount(currentClassBytes);

        // 比较行数
        return originLineCount == currentLineCount;
    }

    /**
     * 计算字节码的行数
     */
    private static int calculateLineCount(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        LineNumberCounterClassVisitor counterClassVisitor = new LineNumberCounterClassVisitor();
        classReader.accept(counterClassVisitor, ClassReader.SKIP_DEBUG);
        return counterClassVisitor.getLineCount();
    }


    public boolean isJdkInternalClass(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        return packageName.startsWith("java.") ||
                packageName.startsWith("javax.") ||
                packageName.startsWith("jakarta.");
    }

    /**
     * 查找已加载到内存中hook点
     */
    public List<Class<?>> findForReTransform() {
        final List<Class<?>> classes = new ArrayList<>();

        Class<?>[] loadedClasses = this.instrumentation.getAllLoadedClasses();
        for (Class<?> clazz : loadedClasses) {
            TransformerProtector.instance.enterProtecting();
            try {
                String normalizeClass = ClassUtils.normalizeClass(clazz.getName());
                // 跳过不需要处理的类
                if (shouldSkipClass(clazz, normalizeClass)) {
                    continue;
                }

                // 根据扫描模式决定是否添加类
                if (MODE_FULL.equals(scanMode)) {
                    classes.add(clazz);
                } else {
                    addClassIfJdkInternal(clazz, classes);
                }
            } catch (Exception ignored) {
            } finally {
                TransformerProtector.instance.exitProtecting();
            }
        }
        return classes;
    }

    /**
     * 判断是否需要跳过当前类
     */
    private boolean shouldSkipClass(Class<?> clazz, String normalizeClass) {
        return ClassUtils.isComeFromSMSSFamily(normalizeClass, clazz.getClassLoader()) // 跳过 IAST 家族的类
                || !instrumentation.isModifiableClass(clazz) // 跳过不可修改的类
                || clazz.getName().startsWith("jdk.internal.reflect") // 跳过反射相关的类
                || clazz.getName().startsWith("java.lang.invoke") // 跳过方法句柄相关的类
                || ClassUtils.shouldSkipProxyClass(clazz.getName()) // 跳过代理类
                || normalizeClass.matches(".*\\$\\d+"); // 跳过匿名内部类
    }

    /**
     * 如果是 JDK 内部类或其接口是 JDK 内部类，则添加到列表中
     */
    private void addClassIfJdkInternal(Class<?> clazz, List<Class<?>> classes) {
        if (isJdkInternalClass(clazz)) {
            classes.add(clazz);
            return;
        }

        // 检查接口是否是 JDK 内部类
        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (isJdkInternalClass(anInterface)) {
                classes.add(clazz);
                break;
            }
        }
    }

    private byte[] getOriginalClassBytes(String className, ClassLoader loader) throws Exception {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        InputStream inputStream = loader.getResourceAsStream(className + ".class");
        if (inputStream == null) {
            return null;
        }
        // 将字节码流读取为字节数组
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


}

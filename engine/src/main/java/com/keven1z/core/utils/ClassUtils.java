package com.keven1z.core.utils;



/**
 * @author keven1z
 * @since 2023/02/06
 */
public class ClassUtils {
    private static final String[] IGNORE_OBJECT_CLASS = new String[]{"java.lang.Object", "java.lang.Cloneable", "java.io.Serializable", "java.lang.Iterable"};
    private static final String SMSS_FAMILY_CLASS_RES_PREFIX = "com/keven1z/";



    public static String normalizeClass(String className) {
        return className.replace(".", "/");
    }


    public static boolean isComeFromSMSSFamily(final String internalClassName, final ClassLoader loader) {

        if (null != internalClassName
                && isSMSSPrefix(internalClassName)) {
            return true;
        }
        return null != loader
                && isSMSSPrefix(normalizeClass(loader.getClass().getName()));
    }
    public static boolean shouldSkipProxyClass(String className) {
        return className.contains("$Proxy") || className.contains("Proxy$");
    }
    private static boolean isSMSSPrefix(String className) {
        return className.startsWith(SMSS_FAMILY_CLASS_RES_PREFIX);
    }
}

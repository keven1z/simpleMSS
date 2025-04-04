package com.keven1z;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import static com.keven1z.ModuleLoader.*;

/**
 * @author keven1z
 * @since 2023/02/21
 */

public class ModuleContainer implements Module {

    private static final String CLASS_OF_CORE_ENGINE = "com.keven1z.core.EngineBoot";
    private final Object engineObject;
    private final String scanLevel;
    private final boolean isCleanupShell;

    public ModuleContainer(String jarName, String scanLevel, boolean isCleanupShell) {
        try {
            File originFile = new File(baseDirectory + File.separator + jarName);
            ModuleLoader.classLoader = new SimpleMemShellClassLoader(originFile.getAbsolutePath());
            Class<?> classOfEngine = classLoader.loadClass(CLASS_OF_CORE_ENGINE);
            engineObject = classOfEngine.getDeclaredConstructor().newInstance();
            this.scanLevel = scanLevel;
            this.isCleanupShell = isCleanupShell;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize module jar: " + jarName, t);
        }
    }

    public void start(Instrumentation inst) throws Exception {
        Method method = engineObject.getClass().getMethod("start", Instrumentation.class, String.class, Boolean.class);
        method.invoke(engineObject, inst, scanLevel, isCleanupShell);
    }

    public void shutdown() throws Throwable {
        try {
            if (engineObject != null) {
                Method method = engineObject.getClass().getMethod("shutdown");
                method.invoke(engineObject);
            }
        } catch (Throwable t) {
            System.err.println("[SimpleMemShell] Failed to shutdown module");
            throw t;
        }
    }

}

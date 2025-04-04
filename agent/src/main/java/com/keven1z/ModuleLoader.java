package com.keven1z;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * @author keven1z
 * @since 2023/02/21
 */
public class ModuleLoader {
    public static final String ENGINE_JAR = "smss-engine.jar";

    public static String baseDirectory;

    private static ModuleLoader instance;
    public static SimpleMemShellClassLoader classLoader;
    public static String projectVersion;
    public static String buildTime;
    public static String gitCommit;
    private final ModuleContainer engineContainer;


    // ModuleLoader 为 classloader加载的，不能通过getProtectionDomain()的方法获得JAR路径
    static {
        // juli
        try {
            Class<?> clazz = Class.forName("java.nio.file.FileSystems");
            clazz.getMethod("getDefault").invoke(null);
        } catch (Throwable t) {
            // ignore
        }
        Class<?> clazz = ModuleLoader.class;
        // path值示例：　file:/opt/apache-tomcat-xxx/rasp/rasp.jar!/com/fuxi/javaagent/Agent.class
        String path = Objects.requireNonNull(clazz.getResource("/" + clazz.getName().replace(".", "/") + ".class")).getPath();
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }
        if (path.contains("!")) {
            path = path.substring(0, path.indexOf("!"));
        }
        try {
            baseDirectory = URLDecoder.decode(new File(path).getParent(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            baseDirectory = new File(path).getParent();
        }
    }

    private ModuleLoader(String scriptName, boolean isDebug) {
        engineContainer = new ModuleContainer(ENGINE_JAR, scriptName, isDebug);
    }

    public void start(Instrumentation inst) throws Exception {
        engineContainer.start(inst);

    }

    public static void readVersion() throws IOException {
        Class<?> clazz = Agent.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = Objects.requireNonNull(clazz.getResource(className)).toString();
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();
        projectVersion = attr.getValue("Project-Version");
        buildTime = attr.getValue("Build-Time");
        gitCommit = attr.getValue("Git-Commit");

        projectVersion = (projectVersion == null ? "UNKNOWN" : projectVersion);
        buildTime = (buildTime == null ? "UNKNOWN" : buildTime);
        gitCommit = (gitCommit == null ? "UNKNOWN" : gitCommit);
    }

    /**
     *
     * @param inst   {@link java.lang.instrument.Instrumentation}
     */
    public static synchronized void load(String scanLevel, boolean isCleanupShell, Instrumentation inst) throws Throwable {
            readVersion();
            if (instance != null) {
                release();
            }
            instance = new ModuleLoader(scanLevel, isCleanupShell);
            instance.start(inst);

    }

    public static synchronized void release() {
        try {
            if (instance != null) {
                instance = null;
            }
            if (classLoader != null) {
                classLoader.closeIfPossible();
            }
        } catch (Throwable throwable) {
            // ignore
        }
    }
}

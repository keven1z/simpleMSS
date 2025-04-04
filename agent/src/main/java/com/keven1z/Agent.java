package com.keven1z;


import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import static com.keven1z.ModuleLoader.release;

/**
 * @author keven1z
 * @since 2023/5/16
 */
public class Agent {
    public static String projectVersion;
    public static String buildTime;
    public static String gitCommit;

    public static void agentmain(String agentArgs, Instrumentation inst) {
        String[] agentArgArr = agentArgs.split(",");
        if (agentArgArr.length < 2) {
            System.err.println("[SMSS] Missing scan parameters,parameters length is " + agentArgArr.length);
            throw new RuntimeException("[SMSS] Missing scan parameters");
        }
        init(agentArgArr[0],  Boolean.parseBoolean(agentArgArr[1]), inst);
    }

    /**
     * attack 机制加载 agent
     *
     * @param scanLevel 扫描等级
     * @param inst    {@link Instrumentation}
     */
    public static synchronized void init(String scanLevel, boolean isCleanupShell, Instrumentation inst) {
        try {
            JarFileHelper.addJarToBootstrap(inst);
            ModuleLoader.load(scanLevel, isCleanupShell, inst);
        } catch (Throwable e) {
            PrintStream err = System.err;
            e.printStackTrace(err);
            release();
        }
    }
}

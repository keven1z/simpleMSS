package com.keven1z.core;


import com.keven1z.core.hook.HookTransformer;
import java.lang.instrument.Instrumentation;


/**
 * 引擎加载类
 *
 * @author keven1z
 * @since 2023/02/21
 */
public class EngineController {

    public void start(Instrumentation inst, String scanMode,boolean isCleanupShell) throws Exception {
        banner();
        loadTransform(inst,scanMode,isCleanupShell);
    }

    private void loadTransform(Instrumentation instrumentation, String scanMode, boolean isCleanupShell) {
        HookTransformer hookTransformer = new HookTransformer(instrumentation,scanMode,isCleanupShell);
        hookTransformer.reTransform();
    }
    /**
     * 打印banner信息
     */
    private void banner() {
        String s = "   _____   __  __   _____   _____ \n" +
                " / ____| |  \\/  | / ____| / ____|\n" +
                "| (___   | \\  / | | (___  | (___  \n" +
                " \\___ \\  | |\\/| |  \\___ \\  \\___ \\ \n" +
                " ____) | | |  | |  ____) | ____) |\n" +
                "|_____/  |_|  |_| |_____/ |_____/ \n";
        System.out.println(s);
    }
}

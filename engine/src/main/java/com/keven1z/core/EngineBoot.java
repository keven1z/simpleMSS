package com.keven1z.core;


import com.keven1z.JarFileHelper;
import net.bytebuddy.agent.VirtualMachine;
import org.apache.commons.cli.*;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import java.lang.instrument.Instrumentation;
import java.util.List;

import static com.keven1z.Agent.*;
import static com.keven1z.ModuleLoader.readVersion;

/**
 * @author keven1z
 * @since 2025/01/21
 */
public class EngineBoot {
    EngineController engineController = null;
    public final static String MODE_QUICK = "quick";
    public final static String MODE_FULL = "full";

    public void start(Instrumentation inst, String scanLevel, Boolean isCleanupShell) {
        try {
            engineController = new EngineController();
            engineController.start(inst, scanLevel, isCleanupShell);
        } catch (Exception e) {
            throw new RuntimeException("Engine load error," + e.getMessage());
        }
    }
    /**
     * @param targetJvmPid 目标进程id
     * @param agentJarPath 扫描java应用路径
     * @param mode         扫描模式
     */
    private static Boolean attachAgent(final String targetJvmPid,
                                       final String agentJarPath,
                                       final String mode,
                                       final boolean isCleanup) throws Exception {

        VirtualMachine machine = null;
        try {
            System.out.println("Start scanning,Scan mode:" + mode+",pid:"+targetJvmPid);
            String stringBuilder = mode + "," + isCleanup;
            machine = VirtualMachine.ForHotSpot.attach(targetJvmPid);
            machine.loadAgent(agentJarPath, stringBuilder);
            System.out.println("Finish scanning,Please check the scan result in the application console logs");
        } finally {
            if (null != machine) {
                machine.detach();
            }
        }
        return true;
    }
    public static String keepFirstAndLast25(String input) {
        if (input == null || input.length() <= 50) {
            return input;
        }
        String first25 = input.substring(0, 25);
        String last25 = input.substring(input.length() - 25);
        return first25 + "..." + last25;
    }
    private static void printJavaProcesses(){
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        // 打印表头
        printTableHeader();

        List<OSProcess> processes = os.getProcesses();

        for (OSProcess process : processes) {
            if (process.getName().toLowerCase().contains("java")) {
                printProcessRow(process.getProcessID(),process.getName(), keepFirstAndLast25(process.getCommandLine()));
            }
        }
        // 打印表尾
        printTableFooter();


    }
    /**
     * 打印表头
     */
    private static void printTableHeader() {
        System.out.println("\n+------------+----------------------+----------------------+");
        System.out.println("| process ID | process name          |cmd line ");
        System.out.println("+------------+----------------------+----------------------+");
    }

    /**
     * 打印进程信息行
     */
    private static void printProcessRow(int processId, String processName,String cmdLine) {
        String row = String.format(
                "| %-10d | %-20s | %-30s |",
                processId,
                processName,
                cmdLine
        );
        System.out.println(row);
    }

    /**
     * 打印表尾
     */
    private static void printTableFooter() {
        System.out.println("+------------+----------------------+----------------------+");
    }


    public static void main(String[] args) {
        try {
            Options options = createOptions();
            HelpFormatter helpFormatter = new HelpFormatter();
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("v")) {
                readVersion();
                System.out.println("Version:       " + projectVersion + "\n" +
                        "Build Time:    " + buildTime + "\n" +
                        "Git Commit ID: " + gitCommit);
            } else if (cmd.hasOption("h")) {
                helpFormatter.printHelp("java -jar smss-engine.jar", options, true);
            } else if (cmd.hasOption("p")) {
                String mode = MODE_FULL.equals(cmd.getOptionValue("m")) ? MODE_FULL : MODE_QUICK;
                String agentPath = JarFileHelper.getAgentPath();
                if (System.getProperty("os.name").startsWith("Windows")) {
                    agentPath = agentPath.substring(1);
                    agentPath = agentPath.replace("/", "\\");
                }
                boolean isCleanup = cmd.hasOption("c");
                attachAgent(cmd.getOptionValue("p"), agentPath, mode, isCleanup);
            } else if (cmd.hasOption("l")) {
                printJavaProcesses();
            }
            else {
                helpFormatter.printHelp("java -jar smss-engine.jar", options, true);
            }
        } catch (Throwable e) {
            System.err.println("Failed to Attach,reason:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "print options information");
        options.addOption("v", "version", false, "print the version of simpleMemShellScanner");
        options.addOption("m", "mode", true, "scan mode: quick and full,default quick");
        options.addOption("p", "pid", true, "scan jvm pid");
        options.addOption("c", "cleanup", false, "cleanup memshell");
        options.addOption("l", "list", false, "list all java processes");
        return options;
    }
}

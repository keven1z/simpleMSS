package com.keven1z.core.utils;
import com.strobel.assembler.metadata.ArrayTypeLoader;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.languages.BytecodeOutputOptions;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Decompiler {

    public static String getDecompilerString(byte[] bytes, String className) throws Exception {
        DecompilerSettings settings = new DecompilerSettings();
        settings.setBytecodeOutputOptions(BytecodeOutputOptions.createVerbose());
        if (settings.getJavaFormattingOptions() == null) {
            settings.setJavaFormattingOptions(JavaFormattingOptions.createDefault());
        }
        settings.setShowDebugLineNumbers(true);
        DecompilationOptions decompilationOptions = new DecompilationOptions();
        decompilationOptions.setSettings(settings);
        decompilationOptions.setFullDecompilation(true);
        ArrayTypeLoader typeLoader = new ArrayTypeLoader(bytes);
        MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
        className = className.replace(".", "/");
        TypeReference type = metadataSystem.lookupType(className);
        DecompilerProvider newProvider = new DecompilerProvider();
        newProvider.setDecompilerReferences(settings, decompilationOptions);
        newProvider.setType(type.resolve());
        newProvider.generateContent();
        return newProvider.getTextContent();
    }

    private static String matchStringByRegularExpression(String line, int lineNumber) {
        String regex = ".*\\/\\*[E|S]L:" + lineNumber + "\\*\\/.*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group().trim().replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
        }
        return "";
    }
}

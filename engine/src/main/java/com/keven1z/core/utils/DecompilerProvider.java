package com.keven1z.core.utils;

import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import java.io.StringWriter;

public class DecompilerProvider {
    private DecompilerSettings settings;
    private DecompilationOptions decompilationOptions;
    private TypeDefinition type;

    private String textContent = "";

    public void generateContent() {
        final StringWriter stringwriter = new StringWriter();
        PlainTextOutput plainTextOutput = new PlainTextOutput(stringwriter) {
            @Override
            public void writeDefinition(String text, Object definition, boolean isLocal) {
                super.writeDefinition(text, definition, isLocal);
            }

            @Override
            public void writeReference(String text, Object reference, boolean isLocal) {
                super.writeReference(text, reference, isLocal);
            }
        };
        plainTextOutput.setUnicodeOutputEnabled(decompilationOptions.getSettings().isUnicodeOutputEnabled());
        settings.getLanguage().decompileType(type, plainTextOutput, decompilationOptions);
        textContent = stringwriter.toString();
    }

    public String getTextContent() {
        return textContent;
    }

    public void setDecompilerReferences(DecompilerSettings settings, DecompilationOptions decompilationOptions) {
        this.settings = settings;
        this.decompilationOptions = decompilationOptions;
    }

    public void setType(TypeDefinition type) {
        this.type = type;
    }
}

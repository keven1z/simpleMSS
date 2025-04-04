package com.keven1z.core.hook;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LineNumberCounterClassVisitor extends ClassVisitor {
    private int lineNumber;
    public LineNumberCounterClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                lineNumber++;
            }
        };
    }

    public int getLineCount() {
        return this.lineNumber;
    }
}

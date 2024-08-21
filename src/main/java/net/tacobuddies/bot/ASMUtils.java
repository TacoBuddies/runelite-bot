package net.tacobuddies.bot;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ASMUtils {
    public static void printMethod(MethodNode method) {
        Printer printer = new Textifier();
        TraceMethodVisitor mp = new TraceMethodVisitor(printer);
        for(AbstractInsnNode node : method.instructions) {
            node.accept(mp);
            StringWriter sw = new StringWriter();
            printer.print(new PrintWriter(sw));
            printer.getText().clear();
            System.out.print(sw);
        }
    }

    public static int findLdc(AbstractInsnNode[] instructions, String search, int skip) {
        for(int i = 0; i < instructions.length; i++) {
            AbstractInsnNode node = instructions[i];
            if(node.getOpcode() == Opcodes.LDC) {
                LdcInsnNode ldc = (LdcInsnNode) node;
                if(ldc.cst.toString().equals(search)) {
                    if(skip > 0) {
                        skip--;
                        continue;
                    }

                    return i;
                }
            }
        }

        return -1;
    }

    public static int findMethodInsnNode(AbstractInsnNode[] instructions, int opcode, String owner, String name, String desc) {
        for(int i = 0; i < instructions.length; i++) {
            if(instructions[i] instanceof MethodInsnNode) {
                MethodInsnNode node = (MethodInsnNode) instructions[i];
                if(node.getOpcode() == opcode && node.name.equals(name) && node.owner.equals(owner) && node.desc.equals(desc)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static int findOpcode(AbstractInsnNode[] instructions, int opcode) {
        return findOpcode(instructions, opcode, 0);
    }

    public static int findOpcode(AbstractInsnNode[] instructions, int opcode, int start) {
        for(int i = start; i < instructions.length; i++) {
            if(instructions[i].getOpcode() == opcode) {
                return i;
            }
        }
        return -1;
    }
}

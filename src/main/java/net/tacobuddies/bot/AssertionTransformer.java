package net.tacobuddies.bot;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@Slf4j
public class AssertionTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;

        if(className.equals("net/runelite/client/externalplugins/ExternalPluginManager")) {
            try {
                ClassNode cn = parseClassFile(classfileBuffer);
                MethodNode method = cn.methods.stream()
                        .filter(m -> m.name.equals("loadBuiltin"))
                        .findFirst()
                        .orElse(null);

                if(method == null) {
                    log.warn("Unable to find ExternalPluginManager.loadBuiltin");
                    return classfileBuffer;
                }

                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new FieldInsnNode(Opcodes.PUTSTATIC, "net/runelite/client/externalplugins/ExternalPluginManager", "builtinExternals", "[Ljava/lang/Class;"));
                list.add(new InsnNode(Opcodes.RETURN));
                method.instructions = list;

                byteCode = writeClassFile(cn);
            } catch(Exception e) {
                log.error("Error patching ExternalPluginManager.loadBuiltin", e);
            }
        }

        if(className.equals("net/runelite/client/RuneLite")) {
            try {
                ClassNode cn = parseClassFile(classfileBuffer);
                MethodNode method = cn.methods.stream()
                        .filter(m -> m.name.equals("main"))
                        .findFirst()
                        .orElse(null);

                if(method == null) {
                    log.warn("Unable to find RuneLite.main");
                    return classfileBuffer;
                }

                AbstractInsnNode[] instructions = method.instructions.toArray();
                int ldcIndex = findLdc(instructions, "developer-mode", 1);
                int loadIndex = findPattern(instructions, ldcIndex, Opcodes.ILOAD, Opcodes.IFEQ);
                method.instructions.set(instructions[loadIndex], new InsnNode(Opcodes.ICONST_0));

                byteCode = writeClassFile(cn);
            } catch(Exception e) {
                log.error("Error patching RuneLite.main", e);
            }
        }

        return byteCode;
    }

    private ClassNode parseClassFile(byte[] buffer) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(buffer);
        reader.accept(node, 0);
        return node;
    }

    private byte[] writeClassFile(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private int findLdc(AbstractInsnNode[] instructions, String search, int skip) {
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

    private int findPattern(AbstractInsnNode[] instructions, int start, int... opcodes) {
        outer: for(int i = start; i < instructions.length; i++) {
            AbstractInsnNode node = instructions[i];
            if(node.getOpcode() == opcodes[0]) {
                if(opcodes.length > 1) {
                    for (int j = 1; j < opcodes.length; j++) {
                        if(instructions[i + j].getOpcode() != opcodes[j]) {
                            continue outer;
                        }
                    }
                }
                return i;
            }
        }

        return -1;
    }
}

package net.tacobuddies.bot;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@Slf4j
public class LauncherTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;

        if(className.equals("net/runelite/launcher/JvmLauncher")) {
            try {
                ClassNode cn = parseClassFile(classfileBuffer);
                MethodNode method = cn.methods.stream()
                        .filter(m -> m.name.equals("launch"))
                        .findFirst()
                        .orElse(null);

                if(method == null) {
                    log.warn("Unable to find JvmLauncher.launch");
                    return classfileBuffer;
                }

                AbstractInsnNode[] instructions = method.instructions.toArray();
                int ldcIndex = ASMUtils.findLdc(instructions, "-cp", 0);

                VarInsnNode load = (VarInsnNode) instructions[ldcIndex - 1];
                int varIndex = load.var;

                String agentPath = Agent.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
                list.add(new LdcInsnNode("-javaagent:" + agentPath));
                list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
                list.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(instructions[ldcIndex - 1], list);

                if(System.getProperties().containsKey("net.tacobuddies.login")) {
                    list = new InsnList();
                    list.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
                    list.add(new LdcInsnNode("-Dnet.tacobuddies.login=" + System.getProperty("net.tacobuddies.login")));
                    list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
                    list.add(new InsnNode(Opcodes.POP));
                    method.instructions.insertBefore(instructions[ldcIndex - 1], list);
                }

                if(System.getProperties().containsKey("net.tacobuddies.smartMouse")) {
                    list = new InsnList();
                    list.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
                    list.add(new LdcInsnNode("-Dnet.tacobuddies.smartMouse=" + System.getProperty("net.tacobuddies.smartMouse")));
                    list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
                    list.add(new InsnNode(Opcodes.POP));
                    method.instructions.insertBefore(instructions[ldcIndex - 1], list);
                }

                if(System.getProperties().containsKey("net.tacobuddies.cache")) {
                    list = new InsnList();
                    list.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
                    list.add(new LdcInsnNode("-Dnet.tacobuddies.cache=" + System.getProperty("net.tacobuddies.cache")));
                    list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
                    list.add(new InsnNode(Opcodes.POP));
                    method.instructions.insertBefore(instructions[ldcIndex - 1], list);
                }

                int addAllIndex = ASMUtils.findMethodInsnNode(instructions, Opcodes.INVOKEINTERFACE,
                        "java/util/List", "addAll", "(Ljava/util/Collection;)Z");

                list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
                list.add(new LdcInsnNode("-Xmx2048m"));
                list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
                list.add(new InsnNode(Opcodes.POP));
                method.instructions.insert(instructions[addAllIndex + 1], list);

                byteCode = writeClassFile(cn);
            } catch(Exception e) {
                log.error("Error patching JvmLauncher.launch", e);
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
}

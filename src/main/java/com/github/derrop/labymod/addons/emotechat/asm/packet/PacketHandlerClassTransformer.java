package com.github.derrop.labymod.addons.emotechat.asm.packet;


import com.github.derrop.labymod.addons.emotechat.asm.PredicateClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PacketHandlerClassTransformer implements PredicateClassTransformer {

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals("bcy") || name.equals("net.minecraft.client.network.NetHandlerPlayClient");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        try {
            return this.transform(basicClass, name.equals("bcy"));
        } catch (Exception exception) {
            exception.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transform(byte[] basicClass, boolean obfuscated) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String name = obfuscated ? "a" : "addToSendQueue";
        String desc = obfuscated ? "(Lff;)V" : "(Lnet/minecraft/network/Packet;)V";

        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                this.transformAddToSendQueue(method, obfuscated);
            }
        }


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private void transformAddToSendQueue(MethodNode method, boolean obfuscated) {
        String desc = obfuscated ? "(Lff;)V" : "(Lnet/minecraft/network/Packet;)Z";

        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/derrop/labymod/addons/emotechat/asm/packet/PacketHandler", "handlePacket", desc, false));

        method.instructions.insert(method.instructions.getFirst(), list);
    }

}

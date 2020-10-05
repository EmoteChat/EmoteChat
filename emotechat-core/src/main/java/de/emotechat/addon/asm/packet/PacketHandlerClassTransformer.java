package de.emotechat.addon.asm.packet;

import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PacketHandlerClassTransformer extends PredicateClassNodeTransformer {

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals("bcy") || name.equals("net.minecraft.client.network.NetHandlerPlayClient");
    }

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        try {
            this.transform(node, name.equals("bcy"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void transform(ClassNode node, boolean obfuscated) {
        String name = obfuscated ? "a" : "sendPacket";
        String desc = obfuscated ? "(Lff;)V" : "(Lnet/minecraft/network/Packet;)V";

        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                this.transformAddToSendQueue(method, obfuscated);
            }
        }
    }

    private void transformAddToSendQueue(MethodNode method, boolean obfuscated) {
        String desc = obfuscated ? "(Lff;)V" : "(Lnet/minecraft/network/Packet;)Z";

        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/emotechat/addon/asm/packet/PacketHandler", "handlePacket", desc, false));

        method.instructions.insert(method.instructions.getFirst(), list);
    }

}

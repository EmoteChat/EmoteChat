package de.emotechat.addon.asm.packet;

import de.emotechat.addon.adapter.mappings.ClassMapping;
import de.emotechat.addon.adapter.mappings.Mappings;
import de.emotechat.addon.adapter.mappings.MethodMapping;
import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PacketHandlerClassTransformer extends PredicateClassNodeTransformer {

    private static final ClassMapping NET_HANDLER_PLAY_CLIENT_MAPPING = Mappings.ACTIVE_MAPPINGS.getNetHandlerPlayClientMapping();

    private static final MethodMapping SEND_PACKET_MAPPING = Mappings.ACTIVE_MAPPINGS.getSendPacketMapping();

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals(NET_HANDLER_PLAY_CLIENT_MAPPING.getObfuscatedName())
                || name.equals(NET_HANDLER_PLAY_CLIENT_MAPPING.getName());
    }

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        try {
            this.transform(node, name.equals(NET_HANDLER_PLAY_CLIENT_MAPPING.getObfuscatedName()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void transform(ClassNode node, boolean obfuscated) {
        String name = obfuscated ? SEND_PACKET_MAPPING.getObfuscatedName() : SEND_PACKET_MAPPING.getName();
        String desc = obfuscated ? SEND_PACKET_MAPPING.getObfuscatedDesc() : SEND_PACKET_MAPPING.getDesc();

        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                this.transformAddToSendQueue(method, obfuscated);
            }
        }
    }

    private void transformAddToSendQueue(MethodNode method, boolean obfuscated) {
        String desc = obfuscated ? SEND_PACKET_MAPPING.getObfuscatedDesc() : "(Lnet/minecraft/network/Packet;)Z";

        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/emotechat/addon/asm/packet/PacketHandler", "handlePacket", desc, false));

        method.instructions.insert(method.instructions.getFirst(), list);
    }

}

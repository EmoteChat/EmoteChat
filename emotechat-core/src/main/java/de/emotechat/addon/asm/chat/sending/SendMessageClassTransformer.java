package de.emotechat.addon.asm.chat.sending;

import de.emotechat.addon.adapter.mappings.Mappings;
import de.emotechat.addon.adapter.mappings.MethodMapping;
import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SendMessageClassTransformer extends PredicateClassNodeTransformer {

    private static final MethodMapping SEND_CHAT_MESSAGE_MAPPING = Mappings.ACTIVE_MAPPINGS.getSendChatMessageMapping();

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals("net.labymod.ingamechat.GuiChatCustom")
                || transformedName.equals("net.labymod.ingamechat.GuiChatCustom");
    }

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        try {
            for (MethodNode method : node.methods) {
                if (method.name.equals(SEND_CHAT_MESSAGE_MAPPING.getName())
                        || method.name.equals(SEND_CHAT_MESSAGE_MAPPING.getObfuscatedName())
                        && method.desc.equals(SEND_CHAT_MESSAGE_MAPPING.getDesc())
                        || method.desc.equals(SEND_CHAT_MESSAGE_MAPPING.getObfuscatedDesc())) {
                    this.transformSendChatMessage(method);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void transformSendChatMessage(MethodNode method) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "de/emotechat/addon/asm/chat/sending/SendMessageHandler",
                "handleMessage",
                "(Ljava/lang/String;)Ljava/lang/String;",
                false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 1));

        method.instructions.insertBefore(method.instructions.getFirst(), list);
    }
}

package de.emotechat.addon.asm.packet;

import com.google.common.base.Preconditions;
import de.emotechat.addon.adapter.EmoteChatAdapter;
import de.emotechat.addon.adapter.mappings.Mappings;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class PacketHandler {

    private static Field messageField;

    private static ChatModifier chatModifier;

    private static EmoteChatAdapter emoteChatAdapter;

    public static void setChatModifier(ChatModifier chatModifier) {
        PacketHandler.chatModifier = chatModifier;
    }

    public static void setEmoteChatAdapter(EmoteChatAdapter emoteChatAdapter) {
        PacketHandler.emoteChatAdapter = emoteChatAdapter;
        PacketHandler.messageField = ReflectionHelper.findField(emoteChatAdapter.getChatPacketClass(), Mappings.ACTIVE_MAPPINGS.getChatPacketMessageFieldNames());
    }

    public static void handlePacket(Packet<?> packet) {
        String message = PacketHandler.emoteChatAdapter.getChatPacketMessage(packet);

        if (PacketHandler.chatModifier == null || message == null) {
            return;
        }

        if (!PacketHandler.chatModifier.shouldReplace(message)) {
            return;
        }

        String result = PacketHandler.chatModifier.replaceMessage(message);
        Preconditions.checkNotNull(result);

        try {
            PacketHandler.messageField.set(packet, result);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

}

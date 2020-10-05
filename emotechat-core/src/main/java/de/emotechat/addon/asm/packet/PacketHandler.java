package de.emotechat.addon.asm.packet;

import com.google.common.base.Preconditions;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class PacketHandler {

    private static final Field FIELD = ReflectionHelper.findField(CPacketChatMessage.class, "message", "a", "field_149440_a");

    private static ChatModifier chatModifier;

    public static void setChatModifier(ChatModifier chatModifier) {
        PacketHandler.chatModifier = chatModifier;
    }

    public static void handlePacket(Packet<?> packet) {
        if (chatModifier == null || !(packet instanceof CPacketChatMessage)) {
            return;
        }

        CPacketChatMessage message = (CPacketChatMessage) packet;
        if (!chatModifier.shouldReplace(message.getMessage())) {
            return;
        }

        String result = chatModifier.replaceMessage(message.getMessage());
        Preconditions.checkNotNull(result);

        try {
            FIELD.set(message, result);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

}

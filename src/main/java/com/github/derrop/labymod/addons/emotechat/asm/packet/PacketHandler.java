package com.github.derrop.labymod.addons.emotechat.asm.packet;

import com.google.common.base.Preconditions;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class PacketHandler {

    private static final Field FIELD = ReflectionHelper.findField(C01PacketChatMessage.class, "message", "a", "field_149440_a");

    private static ChatModifier chatModifier;

    public static void setChatModifier(ChatModifier chatModifier) {
        PacketHandler.chatModifier = chatModifier;
    }

    public static void handlePacket(Packet<?> packet) {
        if (chatModifier == null || !(packet instanceof C01PacketChatMessage)) {
            return;
        }

        C01PacketChatMessage message = (C01PacketChatMessage) packet;
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

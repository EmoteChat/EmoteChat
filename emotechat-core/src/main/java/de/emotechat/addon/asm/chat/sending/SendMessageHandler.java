package de.emotechat.addon.asm.chat.sending;

import com.google.common.base.Preconditions;

public class SendMessageHandler {

    private static ChatModifier chatModifier;

    public static void setChatModifier(ChatModifier chatModifier) {
        SendMessageHandler.chatModifier = chatModifier;
    }

    public static String handleMessage(String message) {
        if (SendMessageHandler.chatModifier == null || message == null) {
            return message;
        }

        if (!SendMessageHandler.chatModifier.shouldReplace(message)) {
            return message;
        }

        String result = SendMessageHandler.chatModifier.replaceMessage(message);
        Preconditions.checkNotNull(result);

        return result;
    }
}

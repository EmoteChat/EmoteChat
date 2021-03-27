package de.emotechat.addon.asm.chat.sending;

public interface ChatModifier {

    String replaceMessage(String message);

    boolean shouldReplace(String message);

}

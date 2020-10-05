package com.github.emotechat.addon.asm.packet;

public interface ChatModifier {

    String replaceMessage(String message);

    boolean shouldReplace(String message);

}

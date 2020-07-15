package com.github.derrop.labymod.addons.emotechat.asm.packet;

public interface ChatModifier {

    String replaceMessage(String message);

    boolean shouldReplace(String message);

}

package com.github.derrop.labymod.addons.emotechat.asm;

public interface ChatModifier {

    String replaceMessage(String message);

    boolean shouldReplace(String message);

}

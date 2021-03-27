package de.emotechat.addon.adapter.mappings;


import net.labymod.main.Source;

import java.util.Arrays;

public enum Mappings {
    V18(
            Source.ABOUT_MC_VERSION.startsWith("1.8"),
            new ClassMapping("net.minecraft.client.gui.GuiUtilRenderComponents", "avu"),
            new MethodMapping(
                    "sendChatMessage",
                    "b",
                    "(Ljava/lang/String;Z)V",
                    "(Ljava/lang/String;Z)V"
            ),
            new MethodMapping(
                    "func_178908_a",
                    "a",
                    "(Lnet/minecraft/util/IChatComponent;ILnet/minecraft/client/gui/FontRenderer;ZZ)Ljava/util/List;",
                    "(Leu;ILavn;ZZ)Ljava/util/List;"
            ),
            "de/emotechat/addon/adapter/v18/V18ChatWidthCalculator"
    ),
    V112(
            Source.ABOUT_MC_VERSION.startsWith("1.12"),
            new ClassMapping("net.minecraft.client.gui.GuiUtilRenderComponents", "bjc"),
            new MethodMapping(
                    "sendChatMessage",
                    "b",
                    "(Ljava/lang/String;Z)V",
                    "(Ljava/lang/String;Z)V"
            ),
            new MethodMapping(
                    "splitText",
                    "a",
                    "(Lnet/minecraft/util/text/ITextComponent;ILnet/minecraft/client/gui/FontRenderer;ZZ)Ljava/util/List;",
                    "(Lhh;ILbip;ZZ)Ljava/util/List;"
            ),
            "de/emotechat/addon/adapter/v112/V112ChatWidthCalculator"
    );

    public static final Mappings ACTIVE_MAPPINGS = Arrays.stream(Mappings.values())
            .filter(Mappings::isActive)
            .findFirst()
            .orElse(null);

    private final boolean active;

    private final ClassMapping guiUtilRenderComponentsMapping;

    private final MethodMapping sendChatMessageMapping;

    private final MethodMapping splitTextMapping;

    private final String chatWidthCalculatorClassName;

    Mappings(boolean active,
             ClassMapping guiUtilRenderComponentsMapping,
             MethodMapping sendChatMessageMapping,
             MethodMapping splitTextMapping,
             String chatWidthCalculatorClassName) {
        this.active = active;
        this.guiUtilRenderComponentsMapping = guiUtilRenderComponentsMapping;
        this.sendChatMessageMapping = sendChatMessageMapping;
        this.splitTextMapping = splitTextMapping;
        this.chatWidthCalculatorClassName = chatWidthCalculatorClassName;
    }

    public boolean isActive() {
        return this.active;
    }

    public ClassMapping getGuiUtilRenderComponentsMapping() {
        return this.guiUtilRenderComponentsMapping;
    }

    public MethodMapping getSendChatMessageMapping() {
        return this.sendChatMessageMapping;
    }

    public MethodMapping getSplitTextMapping() {
        return this.splitTextMapping;
    }

    public String getChatWidthCalculatorClassName() {
        return this.chatWidthCalculatorClassName;
    }
}


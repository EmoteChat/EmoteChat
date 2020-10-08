package de.emotechat.addon.adapter.mappings;


import net.labymod.main.Source;

import java.util.Arrays;

public enum Mappings {
    V18(
            Source.ABOUT_MC_VERSION.startsWith("1.8"),
            new ClassMapping("net.minecraft.client.network.NetHandlerPlayClient", "bcy"),
            new MethodMapping("addToSendQueue", "a", "(Lnet/minecraft/network/Packet;)V", "(Lff;)V"),
            new ClassMapping("net.minecraft.client.gui.GuiUtilRenderComponents", "avu"),
            new MethodMapping(
                    "func_178908_a",
                    "a",
                    "(Lnet.minecraft.util.IChatComponent;ILnet.minecraft.client.gui.FontRenderer;ZZ)Ljava/util/List;",
                    "(Leu;ILavn;ZZ)Ljava/util/List;"
            ),
            "de/emotechat/addon/adapter/v18/V18ChatWidthCalculator",
            new String[]{"message", "a", "field_149440_a"}
    ),
    V112(
            Source.ABOUT_MC_VERSION.startsWith("1.12"),
            new ClassMapping("net.minecraft.client.network.NetHandlerPlayClient", "bcy"),
            new MethodMapping("sendPacket", "a", "(Lnet/minecraft/network/Packet;)V", "(Lff;)V"),
            new ClassMapping("net.minecraft.client.gui.GuiUtilRenderComponents", "avu"),
            new MethodMapping(
                    "func_178908_a",
                    "a",
                    "(Lnet.minecraft.util.text.ITextComponent;ILnet.minecraft.client.gui.FontRenderer;ZZ)Ljava/util/List;",
                    "(Leu;ILavn;ZZ)Ljava/util/List;"
            ),
            "de/emotechat/addon/adapter/v112/V112ChatWidthCalculator",
            new String[]{"message", "a", "field_149440_a"}
    );

    public static final Mappings ACTIVE_MAPPINGS = Arrays.stream(Mappings.values())
            .filter(Mappings::isActive)
            .findFirst()
            .orElse(null);

    private final boolean active;

    private final ClassMapping netHandlerPlayClientMapping;

    private final MethodMapping sendPacketMapping;

    private final ClassMapping guiUtilRenderComponentsMapping;

    private final MethodMapping calculateLinesMapping;

    private final String chatWidthCalculatorClassName;

    private final String[] chatPacketMessageFieldNames;

    Mappings(boolean active,
             ClassMapping netHandlerPlayClientMapping,
             MethodMapping sendPacketMapping,
             ClassMapping guiUtilRenderComponentsMapping,
             MethodMapping calculateLinesMapping,
             String chatWidthCalculatorClassName,
             String[] chatPacketMessageFieldNames) {
        this.active = active;
        this.netHandlerPlayClientMapping = netHandlerPlayClientMapping;
        this.sendPacketMapping = sendPacketMapping;
        this.guiUtilRenderComponentsMapping = guiUtilRenderComponentsMapping;
        this.calculateLinesMapping = calculateLinesMapping;
        this.chatWidthCalculatorClassName = chatWidthCalculatorClassName;
        this.chatPacketMessageFieldNames = chatPacketMessageFieldNames;
    }

    public boolean isActive() {
        return active;
    }

    public ClassMapping getNetHandlerPlayClientMapping() {
        return netHandlerPlayClientMapping;
    }

    public MethodMapping getSendPacketMapping() {
        return sendPacketMapping;
    }

    public ClassMapping getGuiUtilRenderComponentsMapping() {
        return guiUtilRenderComponentsMapping;
    }

    public MethodMapping getCalculateLinesMapping() {
        return calculateLinesMapping;
    }

    public String getChatWidthCalculatorClassName() {
        return chatWidthCalculatorClassName;
    }

    public String[] getChatPacketMessageFieldNames() {
        return chatPacketMessageFieldNames;
    }

}


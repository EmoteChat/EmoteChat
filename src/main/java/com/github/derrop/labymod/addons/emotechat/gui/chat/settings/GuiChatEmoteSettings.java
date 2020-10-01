package com.github.derrop.labymod.addons.emotechat.gui.chat.settings;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiChatEmoteSettings extends GuiChatCustom {

    private final int EMOTE_SIZE = 15;

    private final int MAX_ROW_AMOUNT = 5;

    private final EmoteChatAddon addon;

    private final Scrollbar scrollbar = new Scrollbar(EMOTE_SIZE);

    private boolean canScroll;

    public GuiChatEmoteSettings(String defaultText, EmoteChatAddon addon) {
        super(defaultText);
        this.addon = addon;
    }

    public void initGui() {
        super.initGui();
        this.scrollbar.setPosition(this.width - 8, this.height - 155, this.width - 3, this.height - 20);
        this.scrollbar.update(this.addon.getSavedEmotes().size() / (MAX_ROW_AMOUNT + 1) + 1);
        this.scrollbar.setSpeed(EMOTE_SIZE);
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.canScroll) {
            this.scrollbar.mouseInput();
            int i = Mouse.getEventDWheel();
            if (i != 0) {
                this.mc.ingameGUI.getChatGUI().resetScroll();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.scrollbar.calc();

        drawRect(this.width - 100, this.height - 155, this.width - 2, this.height - 16, -2147483648);
        drawRect(this.width - 6, this.height - 145, this.width - 5, this.height - 20, -2147483648);
        drawRect(this.width - 7, (int) this.scrollbar.getTop(), this.width - 4, (int) (this.scrollbar.getTop() + this.scrollbar.getBarLength()), 2147483647);

        this.drawString(LabyModCore.getMinecraft().getFontRenderer(), "Emotes", this.width - 100, this.height - 165, -1);

        this.canScroll = mouseX > this.width - 100 && mouseX < this.width - 2 && mouseY > this.height - 150 && mouseY < this.height - 16;
        int row = 0;
        int column = 0;

        List<BTTVEmote> emotes = new ArrayList<>(this.addon.getSavedEmotes().values());

        Runnable tooltipDrawRunnable = () -> {
        };

        for (BTTVEmote emote : emotes) {
            if (this.isEmoteShown(column)) {
                ResourceLocation emoteTexture = emote.getTextureLocation();
                Minecraft.getMinecraft().getTextureManager().bindTexture(emoteTexture);

                LabyMod.getInstance().getDrawUtils().drawTexture(
                        this.width - 98 + row * EMOTE_SIZE,
                        (int) ((double) (this.height - 152 + column * EMOTE_SIZE) + this.scrollbar.getScrollY()),
                        256,
                        256,
                        EMOTE_SIZE - 1,
                        EMOTE_SIZE - 1
                );

                if (this.isEmoteHovered(mouseX, mouseY, row, column)) {
                    tooltipDrawRunnable = () -> LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, emote.getName());
                }
            }

            ++row;
            if (row > MAX_ROW_AMOUNT) {
                row = 0;
                ++column;
            }
        }

        tooltipDrawRunnable.run();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
        int row = 0;
        int column = 0;

        List<BTTVEmote> emotes = new ArrayList<>(this.addon.getSavedEmotes().values());

        for (BTTVEmote emote : emotes) {
            if (this.isEmoteShown(column)) {
                if (this.isEmoteHovered(mouseX, mouseY, row, column)) {
                    String text = this.inputField.getText();

                    this.inputField.setText(text + (text.endsWith(" ") || text.isEmpty() ? "" : " ") + Constants.EMOTE_WRAPPER + emote.getName() + Constants.EMOTE_WRAPPER + " ");
                    LabyModCore.getMinecraft().playSound(SettingsElement.BUTTON_PRESS_SOUND, 2.0F);
                    break;
                }
            }

            ++row;
            if (row > MAX_ROW_AMOUNT) {
                row = 0;
                ++column;
            }
        }
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
        super.mouseReleased(mouseX, mouseY, state);
    }

    private boolean isEmoteShown(int column) {
        double emoteHeight = column * EMOTE_SIZE + this.scrollbar.getScrollY();

        return emoteHeight > -3.0D
                && emoteHeight < 123.0D;
    }

    private boolean isEmoteHovered(int mouseX, int mouseY, int row, int column) {
        int emoteX = this.width - 98 + row * EMOTE_SIZE;
        int emoteY = (int) ((double) (this.height - 152 + column * EMOTE_SIZE) + this.scrollbar.getScrollY());

        return mouseX > emoteX
                && mouseX < emoteX + EMOTE_SIZE
                && mouseY > emoteY
                && mouseY < emoteY + EMOTE_SIZE;
    }

}

package de.emotechat.addon.gui.chat.menu;

import de.emotechat.addon.Constants;
import de.emotechat.addon.EmoteChatAddon;
import de.emotechat.addon.bttv.BTTVEmote;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.manager.TooltipHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GuiChatEmoteMenu extends GuiChatCustom {

    private static final int EMOTE_SIZE = 15;

    private static final int MAX_ROW_AMOUNT = 6;

    private static final int SETTINGS_ICON_SIZE = LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT;

    private static final int SETTINGS_ICON_HOVER_INCREASE = 1;

    private static final Field OPENED_ADDON_SETTINGS_FIELD;

    static {
        Field openedAddonSettingsField = null;

        try {
            openedAddonSettingsField = LabyModAddonsGui.class.getDeclaredField("openedAddonSettings");
            openedAddonSettingsField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        OPENED_ADDON_SETTINGS_FIELD = openedAddonSettingsField;
    }

    private final EmoteChatAddon addon;

    private final Scrollbar scrollbar = new Scrollbar(EMOTE_SIZE + 1);

    private boolean canScroll;

    public GuiChatEmoteMenu(String defaultText, EmoteChatAddon addon) {
        super(defaultText);
        this.addon = addon;
    }

    public void initGui() {
        super.initGui();
        this.scrollbar.setPosition(this.width - 8, this.height - 150, this.width - 3, this.height - 20);
        this.scrollbar.update((int) Math.ceil(this.addon.getSavedEmotes().size() / (double) MAX_ROW_AMOUNT) - 1);
        this.scrollbar.setSpeed(EMOTE_SIZE + 1);
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
        drawRect(this.width - 6, this.height - 150, this.width - 5, this.height - 20, -2147483648);
        drawRect(this.width - 7, (int) this.scrollbar.getTop(), this.width - 4, (int) (this.scrollbar.getTop() + this.scrollbar.getBarLength()), 2147483647);

        this.drawString(LabyModCore.getMinecraft().getFontRenderer(), "Emotes", this.width - 100, this.height - 165, -1);

        int iconX = this.width - SETTINGS_ICON_SIZE - 2;
        int iconY = this.height - 165;

        boolean iconHovered = this.isSettingsIconHovered(mouseX, mouseY, iconX, iconY);

        if (iconHovered) {
            TooltipHelper.getHelper().pointTooltip(mouseX, mouseY, 0, "Addon settings");
        }

        int size = SETTINGS_ICON_SIZE + (iconHovered ? SETTINGS_ICON_HOVER_INCREASE * 2 : 0);

        this.mc.getTextureManager().bindTexture(ModTextures.BUTTON_ADVANCED);
        LabyMod.getInstance().getDrawUtils().drawTexture(
                iconX - (iconHovered ? SETTINGS_ICON_HOVER_INCREASE : 0),
                iconY - (iconHovered ? SETTINGS_ICON_HOVER_INCREASE : 0),
                256,
                256,
                size,
                size,
                1F
        );

        this.canScroll = mouseX > this.width - 100 && mouseX < this.width - 2 && mouseY > this.height - 150 && mouseY < this.height - 16;
        int row = 0;
        int column = 0;

        List<BTTVEmote> emotes = new ArrayList<>(this.addon.getSavedEmotes().values());

        String hoveredEmoteName = null;

        for (BTTVEmote emote : emotes) {
            if (this.isEmoteShown(column)) {
                ResourceLocation emoteTexture = emote.getTextureLocation();
                Minecraft.getMinecraft().getTextureManager().bindTexture(emoteTexture);

                LabyMod.getInstance().getDrawUtils().drawTexture(
                        this.width - 98 + row * EMOTE_SIZE,
                        this.height - 152 + column * EMOTE_SIZE + this.scrollbar.getScrollY(),
                        256,
                        256,
                        EMOTE_SIZE - 1,
                        EMOTE_SIZE - 1
                );

                if (this.isEmoteHovered(mouseX, mouseY, row, column)) {
                    hoveredEmoteName = emote.getName();
                }
            }

            ++row;
            if (row >= MAX_ROW_AMOUNT) {
                row = 0;
                ++column;
            }
        }

        if (hoveredEmoteName != null) {
            LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, hoveredEmoteName);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int iconX = this.width - SETTINGS_ICON_SIZE - 2;
        int iconY = this.height - 165;

        if (this.isSettingsIconHovered(mouseX, mouseY, iconX, iconY)) {
            LabyModCore.getMinecraft().playSound(SettingsElement.BUTTON_PRESS_SOUND, 2.0F);
            this.openAddonSettings();
            return;
        }

        this.scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
        int row = 0;
        int column = 0;

        List<BTTVEmote> emotes = new ArrayList<>(this.addon.getSavedEmotes().values());

        for (BTTVEmote emote : emotes) {
            if (this.isEmoteShown(column)) {
                if (this.isEmoteHovered(mouseX, mouseY, row, column)) {
                    String text = this.inputField.getText();

                    this.inputField.setText(
                            text + (text.endsWith(" ") || text.isEmpty() ? "" : " ")
                                    + Constants.EMOTE_WRAPPER + emote.getName() + Constants.EMOTE_WRAPPER + " "
                    );
                    LabyModCore.getMinecraft().playSound(SettingsElement.BUTTON_PRESS_SOUND, 2.0F);
                    break;
                }
            }

            ++row;
            if (row >= MAX_ROW_AMOUNT) {
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

    private void openAddonSettings() {
        AddonInfoManager.getInstance().init();
        AddonInfo addonInfo = AddonInfoManager.getInstance().getAddonInfoMap().get(this.addon.about.uuid);

        LabyModAddonsGui addonsGui = new LabyModAddonsGui(super.mc.currentScreen);
        try {
            OPENED_ADDON_SETTINGS_FIELD.set(addonsGui, addonInfo.getAddonElement());
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        super.mc.displayGuiScreen(addonsGui);
    }

    private boolean isEmoteShown(int column) {
        double emoteHeight = column * EMOTE_SIZE + this.scrollbar.getScrollY();

        return emoteHeight > -3.0D
                && emoteHeight < 123.0D;
    }

    private boolean isEmoteHovered(int mouseX, int mouseY, int row, int column) {
        int emoteX = this.width - 98 + row * EMOTE_SIZE;
        double emoteY = this.height - 152 + column * EMOTE_SIZE + this.scrollbar.getScrollY();

        return mouseX > emoteX
                && mouseX < emoteX + EMOTE_SIZE
                && mouseY > emoteY
                && mouseY < emoteY + EMOTE_SIZE;
    }

    private boolean isSettingsIconHovered(int mouseX, int mouseY, int iconX, int iconY) {
        return mouseX > iconX
                && mouseX < (iconX + SETTINGS_ICON_SIZE)
                && mouseY > iconY
                && mouseY < (iconY + SETTINGS_ICON_SIZE);
    }

}

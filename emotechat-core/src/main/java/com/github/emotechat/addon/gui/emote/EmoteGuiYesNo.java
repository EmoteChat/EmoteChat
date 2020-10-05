package com.github.emotechat.addon.gui.emote;

import com.github.emotechat.addon.Constants;
import com.github.emotechat.addon.bttv.BTTVEmote;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

public class EmoteGuiYesNo extends GuiYesNo {

    private static final int TOP_OFFSET = 70;
    private static final int DISTANCE = 5;

    private final ControlElement.IconData iconData;

    public EmoteGuiYesNo(BTTVEmote emote, GuiYesNoCallback callback, int parentButtonClickedId) {
        super(callback, "Do you want to add " + emote.getName() + " to your list of emotes?", "", parentButtonClickedId);
        this.iconData = emote.asIconData();
    }

    @Override
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);

        String text = super.messageLine1;
        int width = LabyModCore.getMinecraft().getFontRenderer().getStringWidth(text);
        int x = (super.width - width) / 2 - Constants.CHAT_EMOTE_SIZE - DISTANCE;
        int y = TOP_OFFSET - (LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT / 2);

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.iconData.getTextureIcon());

        LabyMod.getInstance().getDrawUtils().drawTexture(x, y, 256, 256, Constants.CHAT_EMOTE_SIZE, Constants.CHAT_EMOTE_SIZE, 1F);
    }

}

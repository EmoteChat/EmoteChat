package de.emotechat.addon.gui.emote;

import de.emotechat.addon.Constants;
import de.emotechat.addon.bttv.BTTVEmote;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

public class EmoteGuiYesNo extends GuiYesNo {

    private static final int TOP_OFFSET = 70;

    private final ControlElement.IconData iconData;

    public EmoteGuiYesNo(BTTVEmote emote, String title, String subTitle, GuiYesNoCallback callback, int parentButtonClickedId) {
        super(callback, title, subTitle, parentButtonClickedId);
        this.iconData = emote.asIconData();
    }

    @Override
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);

        int textY = TOP_OFFSET - (LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT / 2);

        int x = (super.width / 2) - (Constants.SETTINGS_EMOTE_PREVIEW_SIZE / 2);
        double y = textY - Constants.SETTINGS_EMOTE_PREVIEW_SIZE;

        super.mc.getTextureManager().bindTexture(this.iconData.getTextureIcon());
        LabyMod.getInstance().getDrawUtils().drawTexture(
                x,
                y,
                256,
                256,
                Constants.SETTINGS_EMOTE_PREVIEW_SIZE,
                Constants.SETTINGS_EMOTE_PREVIEW_SIZE,
                1F
        );
    }
}

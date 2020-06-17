package com.github.derrop.labymod.addons.emotechat.gui;


import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;

import java.util.Collection;

public class EmoteList extends DropDownMenu<BTTVEmote> {

    private static final int EMOTE_SIZE = 10;

    private boolean emotesSelectable = true;

    public EmoteList(String title, int x, int y, int width, int height) {
        super(title, x, y, width, height);
        super.setEntryDrawer((Object object, int entityX, int entityY, String trimmedEntry) -> {
            BTTVEmote emote = (BTTVEmote) object;

            LabyMod.getInstance().getDrawUtils().drawImageUrl(emote.getURL(1), entityX - (double) (EMOTE_SIZE / 4), entityY - (double) (EMOTE_SIZE / 4), 256, 256, EMOTE_SIZE, EMOTE_SIZE);
            LabyMod.getInstance().getDrawUtils().drawString(emote.getName(), entityX + (EMOTE_SIZE * 1.5), entityY);
        });
    }

    public EmoteList(String title) {
        this(title, 0, 0, 0, 0);
    }

    public void update(Collection<BTTVEmote> emotes) {
        super.clear();
        BTTVEmote[] emoteArray = emotes.toArray(new BTTVEmote[0]);
        super.fill(emoteArray);

        if (emoteArray.length > 0) {
            super.setSelected(emoteArray[0]);
        }
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        if (!this.emotesSelectable && super.getHoverSelected() != null) {
            return false;
        }

        return super.onClick(mouseX, mouseY, mouseButton);
    }

    public boolean isEmotesSelectable() {
        return emotesSelectable;
    }

    public void setEmotesSelectable(boolean emotesSelectable) {
        this.emotesSelectable = emotesSelectable;
    }

}

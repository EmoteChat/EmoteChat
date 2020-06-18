package com.github.derrop.labymod.addons.emotechat.gui.emote;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;

import java.util.Collection;

public class EmoteDropDownMenu extends DropDownMenu<BTTVEmote> {

    public EmoteDropDownMenu(String title, int x, int y, int width, int height) {
        super(title, x, y, width, height);
        super.setEntryDrawer((Object object, int entityX, int entityY, String trimmedEntry) -> {
            BTTVEmote emote = (BTTVEmote) object;

            LabyMod.getInstance().getDrawUtils().drawImageUrl(
                    emote.getURL(3),
                    entityX - (double) (Constants.SETTINGS_EMOTE_SIZE / 4), entityY - (double) (Constants.SETTINGS_EMOTE_SIZE / 4),
                    256, 256,
                    Constants.SETTINGS_EMOTE_SIZE, Constants.SETTINGS_EMOTE_SIZE
            );
            LabyMod.getInstance().getDrawUtils().drawString(emote.getName(), entityX + (Constants.SETTINGS_EMOTE_SIZE * 1.5), entityY);
        });
    }

    public EmoteDropDownMenu(String title) {
        this(title, 0, 0, 0, 0);
    }

    public void update(Collection<BTTVEmote> emotes) {
        super.clear();
        if (emotes.isEmpty()) {
            return;
        }

        BTTVEmote[] emoteArray = emotes.toArray(new BTTVEmote[0]);
        super.fill(emoteArray);

        super.setSelected(emoteArray[0]);
    }

}

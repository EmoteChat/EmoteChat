package com.github.derrop.labymod.addons.emotechat.gui.emote;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;

import java.lang.reflect.Field;
import java.util.Collection;

public class EmoteDropDownMenu extends DropDownMenu<BTTVEmote> {

    private static final Field SCROLLBAR_FIELD;

    static {
        Field scrollbarField = null;

        try {
            scrollbarField = DropDownMenu.class.getDeclaredField("scrollbar");
            scrollbarField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        SCROLLBAR_FIELD = scrollbarField;
    }

    public EmoteDropDownMenu(String title, int x, int y, int width, int height) {
        super(title, x, y, width, height);
        super.setEntryDrawer((Object object, int entityX, int entityY, String trimmedEntry) -> {
            BTTVEmote emote = (BTTVEmote) object;

            LabyMod.getInstance().getDrawUtils().drawImageUrl(
                    emote.getImageURL(3),
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

        Scrollbar scrollbar = this.getScrollbar();
        if (scrollbar != null) {
            scrollbar.update(emotes.size());
        }

        if (emotes.isEmpty()) {
            return;
        }

        BTTVEmote[] emoteArray = emotes.toArray(new BTTVEmote[0]);
        super.fill(emoteArray);

        super.setSelected(emoteArray[0]);
    }

    private Scrollbar getScrollbar() {
        try {
            return (Scrollbar) SCROLLBAR_FIELD.get(this);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

}

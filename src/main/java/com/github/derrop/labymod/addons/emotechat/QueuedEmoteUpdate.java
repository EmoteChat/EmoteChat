package com.github.derrop.labymod.addons.emotechat;

import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteDropDownMenu;

import java.util.List;

public class QueuedEmoteUpdate {

    private final EmoteDropDownMenu menu;
    private final List<BTTVEmote> emotes;

    public QueuedEmoteUpdate(EmoteDropDownMenu menu, List<BTTVEmote> emotes) {
        this.menu = menu;
        this.emotes = emotes;
    }

    public EmoteDropDownMenu getMenu() {
        return this.menu;
    }

    public List<BTTVEmote> getEmotes() {
        return this.emotes;
    }

    public void apply() {
        this.menu.update(this.emotes);
    }

}

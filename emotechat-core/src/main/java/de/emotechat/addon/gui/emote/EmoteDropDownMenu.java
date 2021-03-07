package de.emotechat.addon.gui.emote;


import de.emotechat.addon.Constants;
import de.emotechat.addon.bttv.BTTVEmote;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

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

    private List<BTTVEmote> emoteList;

    private final boolean highlightSelected;

    public EmoteDropDownMenu(boolean highlightSelected, String title, int x, int y, int width, int height) {
        super(title, x, y, width, height);
        this.highlightSelected = highlightSelected;
        super.setEntryDrawer((Object object, int entityX, int entityY, String trimmedEntry) -> {
            BTTVEmote emote = (BTTVEmote) object;

            BTTVEmote selected = super.getSelected();
            boolean highlight = this.highlightSelected && selected != null && selected.getGlobalId() != null
                    && emote.getGlobalId() != null && selected.getGlobalId().equals(emote.getGlobalId());

            Minecraft.getMinecraft().getTextureManager().bindTexture(emote.asIconData().getTextureIcon());

            LabyMod.getInstance().getDrawUtils().drawTexture(
                    entityX - (double) (Constants.SETTINGS_EMOTE_SIZE / 4), entityY - (double) (Constants.SETTINGS_EMOTE_SIZE / 4),
                    256, 256,
                    Constants.SETTINGS_EMOTE_SIZE, Constants.SETTINGS_EMOTE_SIZE
            );
            LabyMod.getInstance().getDrawUtils().fontRenderer
                    .drawString(emote.getName(), (float) (entityX + (Constants.SETTINGS_EMOTE_SIZE * 1.5)), (float) entityY, highlight ? 16777120 : 16777215, true);
        });

        try {
            Field listField = DropDownMenu.class.getDeclaredField("list");
            listField.setAccessible(true);

            this.emoteList = (List<BTTVEmote>) listField.get(this);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            exception.printStackTrace();
        }
    }

    public EmoteDropDownMenu(boolean highlightSelected, String title) {
        this(highlightSelected, title, 0, 0, 0, 0);
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

    public List<BTTVEmote> getEmoteList() {
        return emoteList;
    }

}

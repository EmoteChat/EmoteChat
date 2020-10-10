package de.emotechat.addon.gui.element;

import de.emotechat.addon.Constants;
import de.emotechat.addon.bttv.BTTVEmote;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.ingamegui.Module;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class PreviewedDropDownElement extends DropDownElement<BTTVEmote> {

    private final Supplier<BTTVEmote> emoteSupplier;

    public PreviewedDropDownElement(Module module, String displayName, String attribute, DropDownMenu dropDownMenu, DrowpDownLoadValue<BTTVEmote> loadValue, Supplier<BTTVEmote> emoteSupplier) {
        super(module, displayName, attribute, dropDownMenu, loadValue);
        this.emoteSupplier = emoteSupplier;
    }

    public PreviewedDropDownElement(String diplayName, String configEntryName, DropDownMenu dropDownMenu, IconData iconData, DrowpDownLoadValue<BTTVEmote> loadValue, Supplier<BTTVEmote> emoteSupplier) {
        super(diplayName, configEntryName, dropDownMenu, iconData, loadValue);
        this.emoteSupplier = emoteSupplier;
    }

    public PreviewedDropDownElement(String diplayName, DropDownMenu dropDownMenu, Supplier<BTTVEmote> emoteSupplier) {
        super(diplayName, dropDownMenu);
        this.emoteSupplier = emoteSupplier;
    }

    public PreviewedDropDownElement(String configEntryName, DropDownMenu dropDownMenu, IconData iconData, DrowpDownLoadValue<BTTVEmote> loadValue, Supplier<BTTVEmote> emoteSupplier) {
        super(configEntryName, dropDownMenu, iconData, loadValue);
        this.emoteSupplier = emoteSupplier;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        BTTVEmote emote = this.emoteSupplier.get();
        if (emote == null) {
            return;
        }

        int previewX = x - Constants.SETTINGS_EMOTE_PREVIEW_SIZE - 30;

        Minecraft.getMinecraft().getTextureManager().bindTexture(emote.getTextureLocation());
        LabyMod.getInstance().getDrawUtils().drawTexture(previewX, y, 256, 256, Constants.SETTINGS_EMOTE_PREVIEW_SIZE, Constants.SETTINGS_EMOTE_PREVIEW_SIZE);
    }
}

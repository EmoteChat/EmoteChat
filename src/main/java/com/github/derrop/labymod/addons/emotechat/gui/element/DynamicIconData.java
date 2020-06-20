package com.github.derrop.labymod.addons.emotechat.gui.element;

import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.util.ResourceLocation;

public class DynamicIconData extends ControlElement.IconData {

    private final String identifier;
    private final String url;

    public DynamicIconData(String identifier, String url) {
        this.identifier = identifier;
        this.url = url;
    }

    @Override
    public boolean hasTextureIcon() {
        return true;
    }

    @Override
    public boolean hasMaterialIcon() {
        return false;
    }

    @Override
    public ResourceLocation getTextureIcon() {
        return LabyMod.getInstance().getDynamicTextureManager().getTexture(identifier, url);
    }
}

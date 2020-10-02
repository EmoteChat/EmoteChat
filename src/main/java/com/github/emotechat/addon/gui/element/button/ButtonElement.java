package com.github.emotechat.addon.gui.element.button;

import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class ButtonElement extends ControlElement {

    protected final GuiButton button = new GuiButton(-2, 0, 0, 0, 20, "");

    protected boolean enabled = true;

    private Runnable clickListener;

    public ButtonElement(String text) {
        this(null, null, text);
    }

    public ButtonElement(String displayName, IconData iconData, String text) {
        super(displayName, iconData);
        this.button.displayString = text;
    }

    public String getText() {
        return this.button.displayString;
    }

    public void setText(String text) {
        this.button.displayString = text;
    }

    public void setClickListener(Runnable handler) {
        this.clickListener = handler;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.clickListener != null && this.button.mousePressed(this.mc, mouseX, mouseY)) {
            this.button.playPressSound(super.mc.getSoundHandler());
            this.clickListener.run();
        }
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        if (super.displayName != null) {
            LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, Color.GRAY.getRGB());
        }

        int buttonWidth = super.displayName == null ? maxX - x : super.mc.fontRendererObj.getStringWidth(this.button.displayString) + 20;

        this.button.setWidth(buttonWidth);
        this.button.enabled = this.enabled;

        LabyModCore.getMinecraft().setButtonXPosition(this.button, maxX - buttonWidth - 2);
        LabyModCore.getMinecraft().setButtonYPosition(this.button, y + 1);

        LabyModCore.getMinecraft().drawButton(this.button, mouseX, mouseY);
    }

}

package com.github.derrop.labymod.addons.emotechat.gui.element.button;


public class TimedButtonElement extends ButtonElement {

    private final long disabledTime;

    private long nextEnabledTime = -1;

    public TimedButtonElement(String displayName, IconData iconData, String text, long disabledTime) {
        super(displayName, iconData, text);
        this.disabledTime = disabledTime;
    }

    public TimedButtonElement(String text, long disabledTime) {
        super(text);
        this.disabledTime = disabledTime;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.button.mousePressed(this.mc, mouseX, mouseY)) {
            this.nextEnabledTime = System.currentTimeMillis() + disabledTime;
            this.enabled = false;
        }
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        if (this.nextEnabledTime != -1 && System.currentTimeMillis() >= this.nextEnabledTime) {
            this.nextEnabledTime = -1;
            this.enabled = true;
        }
    }

}

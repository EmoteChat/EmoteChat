package de.emotechat.addon.gui.element;


import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.BooleanElement;

import java.lang.reflect.Field;

public class ModifiableBooleanElement extends BooleanElement {

    private static final Field VALUE_FIELD;

    static {
        Field valueField = null;

        try {
            valueField = BooleanElement.class.getDeclaredField("currentValue");
            valueField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        VALUE_FIELD = valueField;
    }

    private final ValueProvider<Boolean> provider;

    public ModifiableBooleanElement(String displayName, LabyModAddon addon, IconData iconData, String attributeName, ValueProvider<Boolean> provider) {
        super(displayName, addon, iconData, attributeName, provider.get());
        this.provider = provider;
    }

    public void setValue(boolean value) {
        try {
            VALUE_FIELD.set(this, value);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        this.setValue(this.provider.get());
        super.draw(x, y, maxX, maxY, mouseX, mouseY);
    }

    public interface ValueProvider<T> {

        T get();
    }
}

package de.emotechat.addon.bttv;

import com.google.gson.annotations.SerializedName;
import de.emotechat.addon.gui.element.AnimatedIconData;
import de.emotechat.addon.gui.element.DynamicIconData;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.util.ResourceLocation;

public class BTTVEmote {

    private static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

    private BTTVGlobalId globalId;

    @SerializedName("id")
    private String bttvId;

    @SerializedName("code")
    private String name;

    private String imageType;

    private transient ControlElement.IconData iconData;

    public BTTVEmote(BTTVGlobalId globalId, String bttvId, String name, String imageType) {
        this.globalId = globalId;
        this.bttvId = bttvId;
        this.name = name;
        this.imageType = imageType;
    }

    public String getImageURL(int size) {
        return String.format(EMOTE_IMAGE_ENDPOINT, this.bttvId, size);
    }

    public ControlElement.IconData asIconData() {
        if (this.iconData != null) {
            return this.iconData;
        }

        if ("gif".equals(this.imageType)) {
            return this.iconData = AnimatedIconData.create(this.bttvId, this.getImageURL(3));
        }
        return this.iconData = new DynamicIconData(this.bttvId, this.getImageURL(3));
    }

    public ResourceLocation getTextureLocation() {
        return this.asIconData().getTextureIcon();
    }

    public boolean isComplete() {
        return this.bttvId != null && this.name != null
                && this.globalId != null && this.globalId.getEmoteName() != null && this.globalId.getEmoteId() != null;
    }

    public BTTVGlobalId getGlobalId() {
        return this.globalId;
    }

    public void setGlobalId(BTTVGlobalId globalId) {
        this.globalId = globalId;
    }

    public String getBttvId() {
        return this.bttvId;
    }

    public void setBttvId(String bttvId) {
        this.bttvId = bttvId;
        this.iconData = null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageType() {
        return this.imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}

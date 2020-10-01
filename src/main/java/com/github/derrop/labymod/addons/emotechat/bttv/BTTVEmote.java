package com.github.derrop.labymod.addons.emotechat.bttv;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.gui.element.AnimatedIconData;
import com.github.derrop.labymod.addons.emotechat.gui.element.DynamicIconData;
import com.google.gson.annotations.SerializedName;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BTTVEmote {

    private static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

    private static final Map<String, BTTVEmote> EMOTE_CACHE = new ConcurrentHashMap<>();

    private String originalName;

    public BTTVEmote(String id, String name, String originalName, String imageType) {
        this.id = id;
        this.name = name;
        this.originalName = originalName;
        this.imageType = imageType;
    }

    private String id;

    @SerializedName("code")
    private String name;

    private String imageType;

    public static BTTVEmote getByGlobalIdentifier(String globalIdentifier) {
        if (!EMOTE_CACHE.containsKey(globalIdentifier)) {
            BTTVEmote toFill = new BTTVEmote("", "", "", "");

            EMOTE_CACHE.put(globalIdentifier, toFill);
            fillEmoteAsync(toFill, globalIdentifier);
        }
        return EMOTE_CACHE.get(globalIdentifier);
    }

    private transient ControlElement.IconData iconData;

    private static void fillEmoteAsync(BTTVEmote toFill, String globalIdentifier) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            BackendEmoteInfo emoteInfo = BackendEmoteInfo.retrieveInfoByGlobalIdentifier(globalIdentifier);

            if (emoteInfo != null) {
                toFill.id = emoteInfo.getBttvId();
                toFill.name = emoteInfo.getName();
                toFill.originalName = emoteInfo.getName();
                toFill.imageType = emoteInfo.getImageType();
                toFill.iconData = null;
            }
        });
    }

    public String getImageURL(int size) {
        return String.format(EMOTE_IMAGE_ENDPOINT, this.id, size);
    }

    public ControlElement.IconData asIconData() {
        if (this.iconData != null) {
            return this.iconData;
        }

        if ("gif".equals(this.imageType)) {
            return this.iconData = AnimatedIconData.create(this.id, this.getImageURL(3));
        }
        return this.iconData = new DynamicIconData(this.id, this.getImageURL(3));
    }

    public ResourceLocation getTextureLocation() {
        return this.asIconData().getTextureIcon();
    }

    public boolean isComplete() {
        return this.id != null && this.name != null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

}

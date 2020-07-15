package com.github.derrop.labymod.addons.emotechat.bttv;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.gui.element.AnimatedIconData;
import com.github.derrop.labymod.addons.emotechat.gui.element.DynamicIconData;
import com.google.gson.annotations.SerializedName;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BTTVEmote {

    private static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

    private static final String EMOTE_INFO_ENDPOINT = "https://api.betterttv.net/3/emotes/%s";

    private static final Map<String, BTTVEmote> EMOTE_CACHE = new ConcurrentHashMap<>();

    public static BTTVEmote getById(String id) {
        if (!EMOTE_CACHE.containsKey(id)) {
            EMOTE_CACHE.put(id, new BTTVEmote(id, "", ""));
            cacheEmoteAsync(id);
        }
        return EMOTE_CACHE.get(id);
    }

    private static void cacheEmoteAsync(String id) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            try {
                URLConnection urlConnection = new URL(String.format(EMOTE_INFO_ENDPOINT, id)).openConnection();

                urlConnection.setUseCaches(true);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.setRequestProperty("User-Agent", "LabyMod Emote addon");

                urlConnection.connect();

                try (InputStream inputStream = urlConnection.getInputStream(); Reader reader = new InputStreamReader(inputStream)) {
                    BTTVEmote emote = Constants.GSON.fromJson(reader, BTTVEmote.class);
                    EMOTE_CACHE.put(id, emote);
                }
            } catch (IOException ignored) {
            }
        });
    }

    private String id;

    @SerializedName("code")
    private String name;

    private String imageType;

    private transient ControlElement.IconData iconData;

    public BTTVEmote(String id, String name, String imageType) {
        this.id = id;
        this.name = name;
        this.imageType = imageType;
    }

    public String getImageURL(int size) {
        return String.format(EMOTE_IMAGE_ENDPOINT, this.id, size);
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

    public String getImageType() {
        return imageType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

}

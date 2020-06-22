package com.github.derrop.labymod.addons.emotechat.bttv;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.google.gson.annotations.SerializedName;
import net.labymod.main.LabyMod;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            cacheEmoteAsync(id);
            EMOTE_CACHE.put(id, new BTTVEmote(id, ""));
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

                try (InputStream inputStream = urlConnection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
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

    public BTTVEmote(String id, String code) {
        this.id = id;
        this.name = code;
    }

    public String getImageURL(int size) {
        return String.format(EMOTE_IMAGE_ENDPOINT, this.id, size);
    }

    public ResourceLocation getTextureLocation() {
        return LabyMod.getInstance().getDynamicTextureManager().getTexture(this.id, this.getImageURL(3));
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

}

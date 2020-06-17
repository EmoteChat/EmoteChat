package com.github.derrop.labymod.addons.emotechat.bttv;


import com.google.gson.annotations.SerializedName;

public class BTTVEmote {

    private static final String EMOTE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

    private String id;

    @SerializedName("code")
    private String name;

    public BTTVEmote(String id, String code) {
        this.id = id;
        this.name = code;
    }

    public String getURL(int size) {
        return String.format(EMOTE_ENDPOINT, this.id, size);
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

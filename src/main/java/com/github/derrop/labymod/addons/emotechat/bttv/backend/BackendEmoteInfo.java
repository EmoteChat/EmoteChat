package com.github.derrop.labymod.addons.emotechat.bttv.backend;


public class BackendEmoteInfo {

    private final String bttvId;

    private final String imageType;

    private final String globalIdentifier;

    public BackendEmoteInfo(String bttvId, String imageType, String globalIdentifier) {
        this.bttvId = bttvId;
        this.imageType = imageType;
        this.globalIdentifier = globalIdentifier;
    }

    public String getBttvId() {
        return bttvId;
    }

    public String getName() {
        return this.globalIdentifier.split("\\+")[0];
    }

    public String getImageType() {
        return imageType;
    }

    public String getGlobalIdentifier() {
        return globalIdentifier;
    }

}

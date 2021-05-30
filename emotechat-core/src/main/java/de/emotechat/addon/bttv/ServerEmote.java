package de.emotechat.addon.bttv;

/**
 * Extra class for server sent emotes as they aren't stored the same as in the config.
 */
public class ServerEmote {

    private final BTTVGlobalId globalId;
    private final String bttvId;
    private final String name;
    private final String imageType;

    public ServerEmote(BTTVGlobalId globalId, String bttvId, String name, String imageType) {
        this.globalId = globalId;
        this.bttvId = bttvId;
        this.name = name;
        this.imageType = imageType;
    }

    public BTTVEmote toBTTVEmote() {
        return new BTTVEmote(this.globalId, this.bttvId, this.name, this.imageType);
    }

    public BTTVGlobalId getGlobalId() {
        return this.globalId;
    }

    public String getBttvId() {
        return this.bttvId;
    }

    public String getName() {
        return this.name;
    }

    public String getImageType() {
        return this.imageType;
    }
}

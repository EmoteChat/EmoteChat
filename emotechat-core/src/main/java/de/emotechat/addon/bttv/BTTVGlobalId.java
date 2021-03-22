package de.emotechat.addon.bttv;

import java.io.Serializable;
import java.util.Objects;

public class BTTVGlobalId implements Serializable {

    private String emoteName;
    private String emoteId;

    public BTTVGlobalId() {
    }

    public BTTVGlobalId(String emoteName, String emoteId) {
        this.emoteName = emoteName;
        this.emoteId = emoteId;
    }

    private boolean isValid() {
        if (this.emoteName.isEmpty() || this.emoteId.isEmpty()) {
            return false;
        }

        for (char c : this.emoteName.toCharArray()) {
            if (!Character.isLowerCase(c)) {
                return false;
            }
        }

        for (char c : this.emoteId.toCharArray()) {
            if (!Character.isUpperCase(c)) {
                return false;
            }
        }

        return true;
    }

    public static BTTVGlobalId parse(String idSplitter, String rawId) {
        if (!idSplitter.isEmpty()) {
            int index = rawId.lastIndexOf(idSplitter);
            if (index == -1) {
                return null;
            }

            BTTVGlobalId id = new BTTVGlobalId(rawId.substring(0, index), rawId.substring(index + 1));
            return id.isValid() ? id : null;
        }

        if (rawId.isEmpty()) {
            return null;
        }

        int splitter = -1;
        boolean foundLower = false;

        char[] chars = rawId.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLowerCase(chars[i])) {
                foundLower = true;
            }

            if (Character.isUpperCase(chars[i]) && splitter == -1) {
                if (!foundLower) {
                    return null;
                }
                splitter = i;
            }

            if (Character.isLowerCase(chars[i]) && splitter != -1) {
                return null;
            }
        }

        if (splitter == -1) {
            return null;
        }

        return new BTTVGlobalId(rawId.substring(0, splitter), rawId.substring(splitter));
    }

    public String getEmoteName() {
        return this.emoteName;
    }

    public void setEmoteName(String emoteName) {
        this.emoteName = emoteName;
    }

    public String getEmoteId() {
        return this.emoteId;
    }

    public void setEmoteId(String emoteId) {
        this.emoteId = emoteId;
    }

    public String toString(String idSplitter) {
        return this.emoteName + idSplitter + this.emoteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BTTVGlobalId that = (BTTVGlobalId) o;
        return Objects.equals(emoteName, that.emoteName) &&
                Objects.equals(emoteId, that.emoteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emoteName, emoteId);
    }
}

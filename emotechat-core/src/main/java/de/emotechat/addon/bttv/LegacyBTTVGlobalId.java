package de.emotechat.addon.bttv;

import de.emotechat.addon.Constants;

import java.util.Objects;
import java.util.regex.Pattern;

public class LegacyBTTVGlobalId extends BTTVGlobalId {

    private static final Pattern EMOTE_PATTERN = Pattern.compile(Constants.EMOTE_WRAPPER + "[A-Za-z0-9:]+\\+[A-Za-z0-9]{5}" + Constants.EMOTE_WRAPPER);

    private final String name;
    private final String bttvId;

    public LegacyBTTVGlobalId(String name, String bttvId) {
        this.name = name;
        this.bttvId = bttvId;
    }

    public static BTTVGlobalId parseLegacy(String rawId) {
        if (!EMOTE_PATTERN.matcher(rawId).matches()) {
            return null;
        }

        String[] split = rawId.substring(1, rawId.length() - 1).split("\\+");
        if (split.length != 2) {
            return null;
        }

        return new LegacyBTTVGlobalId(split[0], split[1]);
    }

    public String getName() {
        return this.name;
    }

    public String getBttvId() {
        return this.bttvId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LegacyBTTVGlobalId that = (LegacyBTTVGlobalId) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(bttvId, that.bttvId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, bttvId);
    }
}

package de.emotechat.addon.bttv;

import de.emotechat.addon.Constants;

import java.util.regex.Pattern;

public class LegacyBTTVGlobalId extends BTTVGlobalId {

    private static final Pattern EMOTE_PATTERN = Pattern.compile(Constants.EMOTE_WRAPPER + "[A-Za-z0-9]+\\+[A-Za-z0-9]{5}" + Constants.EMOTE_WRAPPER);

    public LegacyBTTVGlobalId(String emoteName, String emoteId) {
        super(emoteName, emoteId);
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
}

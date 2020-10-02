package com.github.emotechat.addon.bttv;


import com.github.emotechat.addon.Constants;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class BTTVSearch {

    private static final String SEARCH_ENDPOINT = "https://api.betterttv.net/3/emotes/shared/search?query=%s&offset=%d&limit=%d";

    private static final Type EMOTES_TYPE_TOKEN = new TypeToken<List<BTTVEmote>>() {
    }.getType();

    private final String query;

    private final int offset;

    private final int limit;

    private final int connectTimeout;

    private final int readTimeout;

    public BTTVSearch(String query, int offset, int limit, int connectTimeout, int readTimeout) {
        this.query = query;
        this.offset = offset;
        this.limit = limit;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public List<BTTVEmote> execute() throws IOException {
        URLConnection urlConnection = new URL(String.format(SEARCH_ENDPOINT, this.query, this.offset, this.limit)).openConnection();

        urlConnection.setUseCaches(true);
        urlConnection.setConnectTimeout(this.connectTimeout);
        urlConnection.setReadTimeout(this.readTimeout);
        urlConnection.setRequestProperty("User-Agent", "LabyMod Emote addon");

        urlConnection.connect();

        try (InputStream inputStream = urlConnection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
            return Constants.GSON.fromJson(reader, EMOTES_TYPE_TOKEN);
        }
    }

    public static class Builder {

        private String query;

        private int offset = 0;

        private int limit = 50;

        private int connectTimeout = 3000;

        private int readTimeout = 3000;

        public Builder(String query) {
            this.query = query;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public String query() {
            return this.query;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public int offset() {
            return this.offset;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public int limit() {
            return this.limit;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public int connectTimeout() {
            return this.connectTimeout;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public int readTimeout() {
            return this.readTimeout;
        }

        public BTTVSearch build() {
            return new BTTVSearch(this.query, this.offset, this.limit, this.connectTimeout, this.readTimeout);
        }

    }

}

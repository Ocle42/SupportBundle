package com.ocle_42.supportbundle.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonUtil() {
    }

    public static String toPrettyJson(JsonElement element) {
        return GSON.toJson(element);
    }
}

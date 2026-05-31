package com.ocle_42.supportbundle.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;

public final class TextUtil {
    private TextUtil() {
    }

    public static String applyTemplate(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public static String createReportText(JsonObject report) {
        StringBuilder builder = new StringBuilder();
        builder.append("SupportBundle report\n");
        builder.append("====================\n\n");
        append(builder, report, "timestamp", "Timestamp");
        append(builder, report, "minecraft_version", "Minecraft");
        append(builder, report, "neoforge_version", "NeoForge");
        append(builder, report, "supportbundle_version", "SupportBundle");
        append(builder, report, "mod_count", "Mod count");

        if (report.has("environment") && report.get("environment").isJsonObject()) {
            JsonObject environment = report.getAsJsonObject("environment");
            builder.append("\nEnvironment\n");
            append(builder, environment, "loader_info", "Loader");
            append(builder, environment, "java_version", "Java");
            append(builder, environment, "jvm_vendor", "JVM vendor");
            append(builder, environment, "current_screen", "Current screen");
            append(builder, environment, "singleplayer", "Singleplayer");
            append(builder, environment, "multiplayer", "Multiplayer");
            append(builder, environment, "server_brand", "Server brand");
        }

        if (report.has("keybind_conflicts") && report.get("keybind_conflicts").isJsonArray()) {
            JsonArray conflicts = report.getAsJsonArray("keybind_conflicts");
            builder.append("\nKeybind conflicts: ").append(conflicts.size()).append('\n');
        }
        return builder.toString();
    }

    public static String archiveReadme() {
        return "This zip was created locally by SupportBundle.\n"
                + "It contains diagnostic information intended for modpack authors or server staff.\n"
                + "Review the files before sharing them. Logs and crash reports are sanitized before inclusion.\n"
                + "No files are uploaded automatically.\n";
    }

    private static void append(StringBuilder builder, JsonObject object, String key, String label) {
        if (!object.has(key)) {
            return;
        }
        JsonElement value = object.get(key);
        if (value.isJsonPrimitive()) {
            builder.append(label).append(": ").append(value.getAsString()).append('\n');
        }
    }
}

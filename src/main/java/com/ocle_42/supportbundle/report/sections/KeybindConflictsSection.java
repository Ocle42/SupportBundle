package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public final class KeybindConflictsSection {
    private KeybindConflictsSection() {
    }

    public static JsonArray collect(Minecraft minecraft) {
        Map<String, List<KeyMapping>> byKey = new LinkedHashMap<>();
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            if (!mapping.isUnbound()) {
                byKey.computeIfAbsent(mapping.saveString(), ignored -> new ArrayList<>()).add(mapping);
            }
        }

        JsonArray conflicts = new JsonArray();
        for (Map.Entry<String, List<KeyMapping>> entry : byKey.entrySet()) {
            if (entry.getValue().size() <= 1) {
                continue;
            }

            JsonObject conflict = new JsonObject();
            conflict.addProperty("key", entry.getKey());
            conflict.addProperty("translated_key", entry.getValue().getFirst().getTranslatedKeyMessage().getString());
            JsonArray actions = new JsonArray();
            for (KeyMapping mapping : entry.getValue()) {
                JsonObject action = new JsonObject();
                action.addProperty("category", I18n.get(mapping.getCategory()));
                action.addProperty("name", I18n.get(mapping.getName()));
                action.addProperty("translation_key", mapping.getName());
                actions.add(action);
            }
            conflict.add("actions", actions);
            conflicts.add(conflict);
        }
        return conflicts;
    }

    public static String toText(JsonArray conflicts) {
        StringBuilder builder = new StringBuilder();
        builder.append("Keybind conflicts\n");
        builder.append("=================\n\n");
        if (conflicts.isEmpty()) {
            builder.append("No conflicts found.\n");
            return builder.toString();
        }

        for (int i = 0; i < conflicts.size(); i++) {
            JsonObject conflict = conflicts.get(i).getAsJsonObject();
            builder.append(conflict.get("translated_key").getAsString()).append(" (").append(conflict.get("key").getAsString()).append(")\n");
            JsonArray actions = conflict.getAsJsonArray("actions");
            for (int j = 0; j < actions.size(); j++) {
                JsonObject action = actions.get(j).getAsJsonObject();
                builder.append("- ").append(action.get("category").getAsString()).append(": ").append(action.get("name").getAsString()).append('\n');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}

package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;

public final class ResourcePacksSection {
    private ResourcePacksSection() {
    }

    public static JsonArray collect(Minecraft minecraft) {
        JsonArray packs = new JsonArray();
        for (Pack pack : minecraft.getResourcePackRepository().getSelectedPacks()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", pack.getId());
            json.addProperty("title", pack.getTitle().getString());
            json.addProperty("required", pack.isRequired());
            json.addProperty("fixed_position", pack.isFixedPosition());
            packs.add(json);
        }
        return packs;
    }

    public static String toText(JsonArray packs) {
        StringBuilder builder = new StringBuilder();
        builder.append("Active resource packs\n");
        builder.append("=====================\n\n");
        if (packs.isEmpty()) {
            builder.append("No active packs reported.\n");
            return builder.toString();
        }

        for (int i = 0; i < packs.size(); i++) {
            JsonObject pack = packs.get(i).getAsJsonObject();
            builder.append("- ").append(value(pack, "id")).append(" (").append(value(pack, "title")).append(")");
            if (pack.has("required") && pack.get("required").getAsBoolean()) {
                builder.append(" [required]");
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private static String value(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsString() : "unknown";
    }
}

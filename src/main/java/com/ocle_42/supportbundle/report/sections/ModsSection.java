package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Comparator;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

public final class ModsSection {
    private ModsSection() {
    }

    public static JsonArray collect() {
        JsonArray mods = new JsonArray();
        ModList.get().getMods().stream()
                .sorted(Comparator.comparing(IModInfo::getModId))
                .forEach(info -> mods.add(toJson(info)));
        return mods;
    }

    private static JsonObject toJson(IModInfo info) {
        JsonObject mod = new JsonObject();
        try {
            mod.addProperty("mod_id", safe(info.getModId()));
            mod.addProperty("display_name", safe(info.getDisplayName()));
            mod.addProperty("version", info.getVersion() == null ? "unknown" : info.getVersion().toString());
        } catch (Exception exception) {
            mod.addProperty("error", exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
        return mod;
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}

package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

public final class OptionsSection {
    private OptionsSection() {
    }

    public static JsonObject collect(Minecraft minecraft) {
        Options options = minecraft.options;
        JsonObject json = new JsonObject();
        json.addProperty("gui_scale", options.guiScale().get());
        json.addProperty("render_distance", options.renderDistance().get());
        json.addProperty("simulation_distance", options.simulationDistance().get());
        json.addProperty("graphics_mode", options.graphicsMode().get().toString());
        json.addProperty("fullscreen", options.fullscreen().get());
        json.addProperty("language", options.languageCode);

        JsonObject volumes = new JsonObject();
        for (SoundSource source : SoundSource.values()) {
            volumes.addProperty(source.getName(), options.getSoundSourceVolume(source));
        }
        json.add("sound_volumes", volumes);
        return json;
    }

    public static String toText(JsonObject options) {
        StringBuilder builder = new StringBuilder();
        builder.append("Options summary\n");
        builder.append("================\n\n");
        append(builder, options, "gui_scale", "GUI scale");
        append(builder, options, "render_distance", "Render distance");
        append(builder, options, "simulation_distance", "Simulation distance");
        append(builder, options, "graphics_mode", "Graphics mode");
        append(builder, options, "fullscreen", "Fullscreen");
        append(builder, options, "language", "Language");
        if (options.has("sound_volumes") && options.get("sound_volumes").isJsonObject()) {
            builder.append("\nSound volumes\n");
            JsonObject volumes = options.getAsJsonObject("sound_volumes");
            for (String key : volumes.keySet()) {
                builder.append("- ").append(key).append(": ").append(volumes.get(key).getAsString()).append('\n');
            }
        }
        return builder.toString();
    }

    private static void append(StringBuilder builder, JsonObject object, String key, String label) {
        if (object.has(key)) {
            builder.append(label).append(": ").append(object.get(key).getAsString()).append('\n');
        }
    }
}

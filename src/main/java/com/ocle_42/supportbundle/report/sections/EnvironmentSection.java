package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonObject;
import com.ocle_42.supportbundle.SupportBundleConstants;
import com.ocle_42.supportbundle.config.SupportBundleConfig;
import com.ocle_42.supportbundle.privacy.PrivacySanitizer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.fml.ModList;

public final class EnvironmentSection {
    private EnvironmentSection() {
    }

    public static JsonObject collect(Minecraft minecraft, PrivacySanitizer sanitizer) {
        JsonObject environment = new JsonObject();
        environment.addProperty("supportbundle_version", modVersion(SupportBundleConstants.MOD_ID));
        environment.addProperty("minecraft_version", SharedConstants.getCurrentVersion().getName());
        environment.addProperty("neoforge_version", neoforgeVersion());
        environment.addProperty("loader", "NeoForge");
        environment.addProperty("loader_info", "NeoForge " + neoforgeVersion());
        environment.addProperty("java_version", System.getProperty("java.version", "unknown"));
        environment.addProperty("jvm_vendor", System.getProperty("java.vendor", "unknown"));

        if (SupportBundleConfig.INCLUDE_OS_JVM_INFO.getAsBoolean()) {
            JsonObject os = new JsonObject();
            os.addProperty("name", System.getProperty("os.name", "unknown"));
            os.addProperty("version", System.getProperty("os.version", "unknown"));
            os.addProperty("arch", System.getProperty("os.arch", "unknown"));
            environment.add("os", os);
        } else {
            environment.addProperty("os", "disabled");
        }

        Runtime runtime = Runtime.getRuntime();
        JsonObject memory = new JsonObject();
        memory.addProperty("allocated_bytes", runtime.totalMemory());
        memory.addProperty("max_bytes", runtime.maxMemory());
        memory.addProperty("free_bytes", runtime.freeMemory());
        environment.add("memory", memory);

        environment.addProperty("current_screen", minecraft.screen == null ? "none" : minecraft.screen.getClass().getName());
        environment.addProperty("world_loaded", minecraft.level != null);
        environment.addProperty("singleplayer", minecraft.isSingleplayer());
        environment.addProperty("multiplayer", minecraft.getCurrentServer() != null);
        if (minecraft.level != null) {
            environment.addProperty("dimension", minecraft.level.dimension().location().toString());
        }

        ClientPacketListener connection = minecraft.getConnection();
        environment.addProperty("server_brand", connection == null ? "not connected" : sanitizer.sanitize(connection.serverBrand()));

        ServerData serverData = minecraft.getCurrentServer();
        if (SupportBundleConfig.INCLUDE_SERVER_ADDRESS.getAsBoolean() && serverData != null) {
            environment.addProperty("server_address", sanitizer.sanitize(serverData.ip));
        } else {
            environment.addProperty("server_address", "disabled");
        }

        if (SupportBundleConfig.INCLUDE_USERNAME.getAsBoolean()) {
            environment.addProperty("username", sanitizer.sanitize(minecraft.getUser().getName()));
        } else {
            environment.addProperty("username", "disabled");
        }

        return environment;
    }

    public static String neoforgeVersion() {
        return modVersion("neoforge");
    }

    private static String modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }
}

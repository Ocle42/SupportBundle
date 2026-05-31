package com.ocle_42.supportbundle.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ocle_42.supportbundle.config.SupportBundleConfig;
import com.ocle_42.supportbundle.privacy.PrivacySanitizer;
import com.ocle_42.supportbundle.report.sections.EnvironmentSection;
import com.ocle_42.supportbundle.report.sections.KeybindConflictsSection;
import com.ocle_42.supportbundle.report.sections.ModsSection;
import com.ocle_42.supportbundle.report.sections.OptionsSection;
import com.ocle_42.supportbundle.report.sections.ResourcePacksSection;
import com.ocle_42.supportbundle.util.FileUtil;
import com.ocle_42.supportbundle.util.JsonUtil;
import com.ocle_42.supportbundle.util.TextUtil;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class SupportReportGenerator {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SupportBundle Report Writer");
        thread.setDaemon(true);
        return thread;
    });

    private SupportReportGenerator() {
    }

    public static CompletableFuture<ReportResult> createAsync(Minecraft minecraft) {
        SupportReport report = capture(minecraft);
        return CompletableFuture.supplyAsync(() -> SupportReportWriter.write(report), EXECUTOR);
    }

    public static String createMessagePreview(Minecraft minecraft, String reportFileName) {
        Map<String, String> values = createMessageValues(minecraft);
        values.put("report_file", reportFileName);
        return TextUtil.applyTemplate(SupportBundleConfig.MESSAGE_TEMPLATE.get(), values);
    }

    private static SupportReport capture(Minecraft minecraft) {
        Path gameDirectory = minecraft.gameDirectory.toPath();
        Path reportsDirectory = FileUtil.resolveReportsDirectory(gameDirectory, SupportBundleConfig.REPORTS_FOLDER.get());
        String username = minecraft.getUser() == null ? "" : minecraft.getUser().getName();
        ServerData serverData = minecraft.getCurrentServer();
        String serverAddress = serverData == null ? "" : serverData.ip;
        PrivacySanitizer sanitizer = PrivacySanitizer.fromConfig(
                SupportBundleConfig.INCLUDE_USERNAME.getAsBoolean() ? "" : username,
                SupportBundleConfig.INCLUDE_SERVER_ADDRESS.getAsBoolean() ? "" : serverAddress,
                !SupportBundleConfig.INCLUDE_SERVER_ADDRESS.getAsBoolean(),
                List.copyOf(SupportBundleConfig.EXTRA_SANITIZER_PATTERNS.get()));

        JsonObject reportJson = new JsonObject();
        reportJson.addProperty("timestamp", Instant.now().toString());

        JsonObject environment = safeObject("environment", sanitizer, () -> EnvironmentSection.collect(minecraft, sanitizer));
        reportJson.add("environment", environment);
        copyRoot(environment, reportJson, "supportbundle_version");
        copyRoot(environment, reportJson, "minecraft_version");
        copyRoot(environment, reportJson, "neoforge_version");
        copyRoot(environment, reportJson, "loader");
        copyRoot(environment, reportJson, "java_version");
        copyRoot(environment, reportJson, "jvm_vendor");
        copyRoot(environment, reportJson, "os");

        JsonArray mods = safeArray("mods", sanitizer, ModsSection::collect);
        reportJson.addProperty("mod_count", mods.size());
        reportJson.add("mods", mods);

        JsonObject options = safeObject("options", sanitizer, () -> OptionsSection.collect(minecraft));
        reportJson.add("options", options);

        JsonArray resourcePacks = SupportBundleConfig.COLLECT_RESOURCE_PACKS.getAsBoolean()
                ? safeArray("resource_packs", sanitizer, () -> ResourcePacksSection.collect(minecraft))
                : new JsonArray();
        reportJson.add("resource_packs", resourcePacks);

        JsonArray keybindConflicts = SupportBundleConfig.COLLECT_KEYBIND_CONFLICTS.getAsBoolean()
                ? safeArray("keybind_conflicts", sanitizer, () -> KeybindConflictsSection.collect(minecraft))
                : new JsonArray();
        reportJson.add("keybind_conflicts", keybindConflicts);

        LinkedHashMap<String, String> textEntries = new LinkedHashMap<>();
        textEntries.put("report.txt", TextUtil.createReportText(reportJson));
        textEntries.put("mods.json", JsonUtil.toPrettyJson(mods));
        textEntries.put("keybind-conflicts.txt", KeybindConflictsSection.toText(keybindConflicts));
        textEntries.put("resource-packs.txt", ResourcePacksSection.toText(resourcePacks));
        textEntries.put("options-summary.txt", OptionsSection.toText(options));
        textEntries.put("readme.txt", TextUtil.archiveReadme());

        return new SupportReport(
                gameDirectory,
                reportsDirectory,
                SupportBundleConfig.ZIP_PREFIX.get(),
                SupportBundleConfig.COLLECT_LATEST_LOG.getAsBoolean(),
                SupportBundleConfig.LATEST_LOG_MAX_KB.getAsInt(),
                SupportBundleConfig.COLLECT_CRASH_REPORTS.getAsBoolean(),
                List.copyOf(SupportBundleConfig.ADDITIONAL_FILES.get()),
                sanitizer,
                reportJson,
                mods,
                textEntries,
                SupportBundleConfig.MESSAGE_TEMPLATE.get(),
                createMessageValues(minecraft));
    }

    private static Map<String, String> createMessageValues(Minecraft minecraft) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("minecraft_version", SharedConstants.getCurrentVersion().getName());
        values.put("neoforge_version", EnvironmentSection.neoforgeVersion());
        ServerData serverData = minecraft.getCurrentServer();
        values.put("modpack", serverData == null || serverData.name == null || serverData.name.isBlank() ? "unknown" : serverData.name);
        values.put("report_file", "not created yet");
        return values;
    }

    private static JsonObject safeObject(String name, PrivacySanitizer sanitizer, ReportSection<JsonObject> section) {
        try {
            return section.collect();
        } catch (Exception exception) {
            JsonObject error = new JsonObject();
            error.addProperty("error", sanitizer.sanitize(exception.getClass().getSimpleName() + ": " + exception.getMessage()));
            error.addProperty("section", name);
            return error;
        }
    }

    private static JsonArray safeArray(String name, PrivacySanitizer sanitizer, ReportSection<JsonArray> section) {
        try {
            return section.collect();
        } catch (Exception exception) {
            JsonArray array = new JsonArray();
            JsonObject error = new JsonObject();
            error.addProperty("error", sanitizer.sanitize(exception.getClass().getSimpleName() + ": " + exception.getMessage()));
            error.addProperty("section", name);
            array.add(error);
            return array;
        }
    }

    private static void copyRoot(JsonObject source, JsonObject target, String key) {
        if (source.has(key)) {
            target.add(key, source.get(key));
        }
    }
}

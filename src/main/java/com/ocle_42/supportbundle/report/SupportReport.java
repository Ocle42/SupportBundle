package com.ocle_42.supportbundle.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ocle_42.supportbundle.privacy.PrivacySanitizer;
import com.ocle_42.supportbundle.util.TextUtil;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SupportReport {
    private final Path gameDirectory;
    private final Path reportsDirectory;
    private final String zipPrefix;
    private final boolean collectLatestLog;
    private final int latestLogMaxKiB;
    private final boolean collectCrashReports;
    private final List<String> additionalFiles;
    private final PrivacySanitizer sanitizer;
    private final JsonObject reportJson;
    private final JsonArray modsJson;
    private final LinkedHashMap<String, String> textEntries;
    private final String discordTemplate;
    private final Map<String, String> messageValues;

    public SupportReport(
            Path gameDirectory,
            Path reportsDirectory,
            String zipPrefix,
            boolean collectLatestLog,
            int latestLogMaxKiB,
            boolean collectCrashReports,
            List<String> additionalFiles,
            PrivacySanitizer sanitizer,
            JsonObject reportJson,
            JsonArray modsJson,
            LinkedHashMap<String, String> textEntries,
            String discordTemplate,
            Map<String, String> messageValues) {
        this.gameDirectory = gameDirectory;
        this.reportsDirectory = reportsDirectory;
        this.zipPrefix = zipPrefix;
        this.collectLatestLog = collectLatestLog;
        this.latestLogMaxKiB = latestLogMaxKiB;
        this.collectCrashReports = collectCrashReports;
        this.additionalFiles = List.copyOf(additionalFiles);
        this.sanitizer = sanitizer;
        this.reportJson = reportJson;
        this.modsJson = modsJson;
        this.textEntries = textEntries;
        this.discordTemplate = discordTemplate;
        this.messageValues = Map.copyOf(messageValues);
    }

    public Path gameDirectory() {
        return gameDirectory;
    }

    public Path reportsDirectory() {
        return reportsDirectory;
    }

    public String zipPrefix() {
        return zipPrefix;
    }

    public boolean collectLatestLog() {
        return collectLatestLog;
    }

    public int latestLogMaxKiB() {
        return latestLogMaxKiB;
    }

    public boolean collectCrashReports() {
        return collectCrashReports;
    }

    public List<String> additionalFiles() {
        return additionalFiles;
    }

    public PrivacySanitizer sanitizer() {
        return sanitizer;
    }

    public JsonObject reportJson() {
        return reportJson;
    }

    public JsonArray modsJson() {
        return modsJson;
    }

    public LinkedHashMap<String, String> textEntries() {
        return textEntries;
    }

    public String createDiscordMessage(String reportFileName) {
        Map<String, String> values = new LinkedHashMap<>(messageValues);
        values.put("report_file", reportFileName);
        return TextUtil.applyTemplate(discordTemplate, values);
    }
}

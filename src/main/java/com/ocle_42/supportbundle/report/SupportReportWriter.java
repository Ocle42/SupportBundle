package com.ocle_42.supportbundle.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ocle_42.supportbundle.report.sections.CrashReportsSection;
import com.ocle_42.supportbundle.report.sections.LogsSection;
import com.ocle_42.supportbundle.util.FileUtil;
import com.ocle_42.supportbundle.util.JsonUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class SupportReportWriter {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final int MAX_ADDITIONAL_FILE_BYTES = 512 * 1024;

    private SupportReportWriter() {
    }

    public static ReportResult write(SupportReport report) {
        try {
            Files.createDirectories(report.reportsDirectory());
            String fileName = FileUtil.safeFileName(report.zipPrefix()) + "-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".zip";
            Path zipPath = report.reportsDirectory().resolve(fileName);

            if (report.collectLatestLog()) {
                LogsSection.capture(report.gameDirectory(), report.sanitizer(), report.latestLogMaxKiB()).ifPresent(captured -> {
                    report.reportJson().add("latest_log", captured.metadata());
                    report.textEntries().put(captured.entryName(), captured.content());
                });
            }

            if (report.collectCrashReports()) {
                CrashReportsSection.capture(report.gameDirectory(), report.sanitizer()).ifPresent(captured -> {
                    report.reportJson().add("crash_report", captured.metadata());
                    report.textEntries().put(captured.entryName(), captured.content());
                });
            }

            JsonArray additionalFiles = addAdditionalFiles(report);
            report.reportJson().add("additional_files", additionalFiles);
            report.reportJson().addProperty("zip_file", fileName);
            report.textEntries().put("report.json", JsonUtil.toPrettyJson(report.reportJson()));

            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipPath), StandardCharsets.UTF_8)) {
                addText(zip, "report.json", report.textEntries().get("report.json"));
                for (var entry : report.textEntries().entrySet()) {
                    if (!"report.json".equals(entry.getKey()) && !"mods.json".equals(entry.getKey())) {
                        addText(zip, entry.getKey(), entry.getValue());
                    }
                }
                addText(zip, "mods.json", JsonUtil.toPrettyJson(report.modsJson()));
                addAdditionalTextEntries(zip, report);
            }

            return ReportResult.success(zipPath, report.createDiscordMessage(fileName));
        } catch (Exception exception) {
            return ReportResult.failure(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage(), exception);
        }
    }

    private static JsonArray addAdditionalFiles(SupportReport report) {
        JsonArray results = new JsonArray();
        for (String configuredPath : report.additionalFiles()) {
            JsonObject status = new JsonObject();
            status.addProperty("path", configuredPath);
            try {
                Path source = FileUtil.resolveAllowedFile(report.gameDirectory(), configuredPath);
                if (!Files.isRegularFile(source)) {
                    status.addProperty("status", "missing");
                } else if (Files.size(source) > MAX_ADDITIONAL_FILE_BYTES) {
                    status.addProperty("status", "skipped_too_large");
                    status.addProperty("max_bytes", MAX_ADDITIONAL_FILE_BYTES);
                } else {
                    status.addProperty("status", "included");
                    status.addProperty("entry", "additional-files/" + FileUtil.zipPath(configuredPath));
                }
            } catch (Exception exception) {
                status.addProperty("status", "error");
                status.addProperty("message", report.sanitizer().sanitize(exception.getMessage()));
            }
            results.add(status);
        }
        return results;
    }

    private static void addAdditionalTextEntries(ZipOutputStream zip, SupportReport report) throws IOException {
        for (String configuredPath : report.additionalFiles()) {
            try {
                Path source = FileUtil.resolveAllowedFile(report.gameDirectory(), configuredPath);
                if (!Files.isRegularFile(source) || Files.size(source) > MAX_ADDITIONAL_FILE_BYTES) {
                    continue;
                }
                String content = Files.readString(source, StandardCharsets.UTF_8);
                addText(zip, "additional-files/" + FileUtil.zipPath(configuredPath), report.sanitizer().sanitize(content));
            } catch (Exception ignored) {
            }
        }
    }

    private static void addText(ZipOutputStream zip, String entryName, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}

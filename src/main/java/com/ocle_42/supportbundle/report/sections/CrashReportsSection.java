package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonObject;
import com.ocle_42.supportbundle.privacy.PrivacySanitizer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class CrashReportsSection {
    private static final int MAX_CRASH_REPORT_BYTES = 256 * 1024;

    private CrashReportsSection() {
    }

    public static Optional<CapturedFile> capture(Path gameDirectory, PrivacySanitizer sanitizer) {
        JsonObject metadata = new JsonObject();
        Path crashReports = gameDirectory.resolve("crash-reports");
        metadata.addProperty("path", "crash-reports");

        if (!Files.isDirectory(crashReports)) {
            metadata.addProperty("status", "missing");
            return Optional.of(new CapturedFile("crash-reports.txt", "No crash-reports directory was found.\n", metadata));
        }

        try (Stream<Path> files = Files.list(crashReports)) {
            Optional<Path> newest = files.filter(Files::isRegularFile)
                    .max(Comparator.comparingLong(CrashReportsSection::lastModified));
            if (newest.isEmpty()) {
                metadata.addProperty("status", "empty");
                return Optional.of(new CapturedFile("crash-reports.txt", "No crash reports were found.\n", metadata));
            }

            Path file = newest.get();
            byte[] bytes = Files.readAllBytes(file);
            boolean trimmed = bytes.length > MAX_CRASH_REPORT_BYTES;
            int offset = trimmed ? bytes.length - MAX_CRASH_REPORT_BYTES : 0;
            String content = new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8);
            metadata.addProperty("status", "included");
            metadata.addProperty("file", file.getFileName().toString());
            metadata.addProperty("trimmed", trimmed);
            return Optional.of(new CapturedFile(
                    "crash-reports.txt",
                    "Latest crash report: " + file.getFileName() + "\n\n" + sanitizer.sanitize(content),
                    metadata));
        } catch (Exception exception) {
            metadata.addProperty("status", "error");
            metadata.addProperty("message", sanitizer.sanitize(exception.getMessage()));
            return Optional.of(new CapturedFile("crash-reports.txt", "Could not read crash reports: " + sanitizer.sanitize(exception.getMessage()) + "\n", metadata));
        }
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }

    public record CapturedFile(String entryName, String content, JsonObject metadata) {
    }
}

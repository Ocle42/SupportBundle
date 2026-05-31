package com.ocle_42.supportbundle.report.sections;

import com.google.gson.JsonObject;
import com.ocle_42.supportbundle.privacy.PrivacySanitizer;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class LogsSection {
    private LogsSection() {
    }

    public static Optional<CapturedFile> capture(Path gameDirectory, PrivacySanitizer sanitizer, int maxKiB) {
        JsonObject metadata = new JsonObject();
        Path latestLog = gameDirectory.resolve("logs").resolve("latest.log");
        metadata.addProperty("path", "logs/latest.log");

        if (!Files.isRegularFile(latestLog)) {
            metadata.addProperty("status", "missing");
            return Optional.of(new CapturedFile("latest.log.txt", "logs/latest.log was not found.\n", metadata));
        }

        try {
            String tail = readTail(latestLog, maxKiB * 1024L);
            metadata.addProperty("status", "included");
            metadata.addProperty("tail_max_kib", maxKiB);
            metadata.addProperty("source_size_bytes", Files.size(latestLog));
            return Optional.of(new CapturedFile("latest.log.txt", sanitizer.sanitize(tail), metadata));
        } catch (Exception exception) {
            metadata.addProperty("status", "error");
            metadata.addProperty("message", sanitizer.sanitize(exception.getMessage()));
            return Optional.of(new CapturedFile("latest.log.txt", "Could not read logs/latest.log: " + sanitizer.sanitize(exception.getMessage()) + "\n", metadata));
        }
    }

    private static String readTail(Path file, long maxBytes) throws IOException {
        try (RandomAccessFile access = new RandomAccessFile(file.toFile(), "r")) {
            long length = access.length();
            long offset = Math.max(0, length - maxBytes);
            access.seek(offset);
            byte[] bytes = new byte[(int) (length - offset)];
            access.readFully(bytes);
            String text = new String(bytes, StandardCharsets.UTF_8);
            return offset == 0 ? text : "[trimmed to last " + maxBytes + " bytes]\n" + text;
        }
    }

    public record CapturedFile(String entryName, String content, JsonObject metadata) {
    }
}

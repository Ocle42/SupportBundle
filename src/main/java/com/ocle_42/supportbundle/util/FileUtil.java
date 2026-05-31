package com.ocle_42.supportbundle.util;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.Util;

public final class FileUtil {
    private FileUtil() {
    }

    public static Path resolveReportsDirectory(Path gameDirectory, String configuredFolder) {
        Path folder = gameDirectory.resolve(configuredFolder.replace('\\', '/')).normalize();
        if (!folder.startsWith(gameDirectory.normalize())) {
            return gameDirectory.resolve("supportbundle").resolve("reports").normalize();
        }
        return folder;
    }

    public static Path resolveAllowedFile(Path gameDirectory, String configuredPath) {
        Path normalizedRoot = gameDirectory.normalize();
        Path file = normalizedRoot.resolve(configuredPath.replace('\\', '/')).normalize();
        if (!file.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("Path leaves the game directory");
        }
        return file;
    }

    public static void openFolder(Path folder) {
        try {
            Files.createDirectories(folder);
            Util.getPlatform().openFile(folder.toFile());
        } catch (Exception ignored) {
        }
    }

    public static String safeFileName(String value) {
        String sanitized = value == null ? "supportbundle" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? "supportbundle" : sanitized;
    }

    public static String zipPath(String configuredPath) {
        return configuredPath.replace('\\', '/').replaceAll("^/+", "");
    }
}

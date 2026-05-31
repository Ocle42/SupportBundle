package com.ocle_42.supportbundle.report;

import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record ReportResult(boolean success, Component statusMessage, Optional<Path> reportPath, Optional<String> discordMessage, Optional<Throwable> error) {
    public static ReportResult success(Path reportPath, String discordMessage) {
        return new ReportResult(
                true,
                Component.translatable("supportbundle.status.report_created", reportPath.getFileName().toString()),
                Optional.of(reportPath),
                Optional.of(discordMessage),
                Optional.empty());
    }

    public static ReportResult failure(String message, @Nullable Throwable error) {
        return new ReportResult(
                false,
                Component.translatable("supportbundle.status.report_failed", message),
                Optional.empty(),
                Optional.empty(),
                Optional.ofNullable(error));
    }
}

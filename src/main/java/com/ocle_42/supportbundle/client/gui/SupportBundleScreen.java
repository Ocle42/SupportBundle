package com.ocle_42.supportbundle.client.gui;

import com.ocle_42.supportbundle.client.SupportBundleClient;
import com.ocle_42.supportbundle.config.SupportBundleConfig;
import com.ocle_42.supportbundle.report.ReportResult;
import com.ocle_42.supportbundle.report.SupportReportGenerator;
import com.ocle_42.supportbundle.util.FileUtil;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class SupportBundleScreen extends Screen {
    private static final int BUTTON_WIDTH = 210;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TEXT_WIDTH = 300;

    @Nullable
    private final Screen parent;
    @Nullable
    private Path lastReportPath;
    @Nullable
    private String lastDiscordMessage;
    private Component status = Component.translatable("supportbundle.status.ready");
    private boolean creating;

    public SupportBundleScreen(@Nullable Screen parent) {
        super(Component.translatable("supportbundle.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        int y = Math.max(78, this.height / 4 + 16);

        Button createButton = Button.builder(Component.translatable("supportbundle.button.create_report"), button -> createReport())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        createButton.active = !this.creating;
        this.addRenderableWidget(createButton);

        this.addRenderableWidget(Button.builder(Component.translatable("supportbundle.button.open_folder"), button -> openReportsFolder())
                .bounds(x, y + 24, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("supportbundle.button.copy_message"), button -> copyDiscordMessage())
                .bounds(x, y + 48, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        addConfiguredLinks(y + 76);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                .bounds(x, this.height - 32, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void addConfiguredLinks(int y) {
        if (!SupportBundleConfig.SHOW_LINKS_IN_GUI.getAsBoolean()) {
            return;
        }

        int buttonCount = 0;
        buttonCount += SupportBundleConfig.SUPPORT_URL.get().isBlank() ? 0 : 1;
        buttonCount += SupportBundleConfig.ISSUE_TRACKER_URL.get().isBlank() ? 0 : 1;
        buttonCount += SupportBundleConfig.DISCORD_URL.get().isBlank() ? 0 : 1;
        if (buttonCount == 0) {
            return;
        }

        int linkWidth = 96;
        int totalWidth = buttonCount * linkWidth + (buttonCount - 1) * 4;
        int x = this.width / 2 - totalWidth / 2;
        if (!SupportBundleConfig.SUPPORT_URL.get().isBlank()) {
            this.addRenderableWidget(Button.builder(Component.translatable("supportbundle.button.support"), SupportBundleClient.linkButton(SupportBundleConfig.SUPPORT_URL.get()))
                    .bounds(x, y, linkWidth, BUTTON_HEIGHT)
                    .build());
            x += linkWidth + 4;
        }
        if (!SupportBundleConfig.ISSUE_TRACKER_URL.get().isBlank()) {
            this.addRenderableWidget(Button.builder(Component.translatable("supportbundle.button.issues"), SupportBundleClient.linkButton(SupportBundleConfig.ISSUE_TRACKER_URL.get()))
                    .bounds(x, y, linkWidth, BUTTON_HEIGHT)
                    .build());
            x += linkWidth + 4;
        }
        if (!SupportBundleConfig.DISCORD_URL.get().isBlank()) {
            this.addRenderableWidget(Button.builder(Component.translatable("supportbundle.button.discord"), SupportBundleClient.linkButton(SupportBundleConfig.DISCORD_URL.get()))
                    .bounds(x, y, linkWidth, BUTTON_HEIGHT)
                    .build());
        }
    }

    private void createReport() {
        this.creating = true;
        this.status = Component.translatable("supportbundle.status.creating");
        refreshWidgets();

        SupportBundleClient.createReport(this::handleReportResult);
    }

    private void handleReportResult(ReportResult result) {
        this.creating = false;
        this.status = result.statusMessage();
        this.lastReportPath = result.reportPath().orElse(null);
        this.lastDiscordMessage = result.discordMessage().orElse(null);
        refreshWidgets();
    }

    private void openReportsFolder() {
        Path reportsDirectory = SupportBundleClient.reportsDirectory();
        FileUtil.openFolder(reportsDirectory);
        this.status = Component.translatable("supportbundle.status.folder_opened");
    }

    private void copyDiscordMessage() {
        Minecraft minecraft = Minecraft.getInstance();
        String message = this.lastDiscordMessage;
        if (message == null) {
            String fileName = this.lastReportPath == null ? "not created yet" : this.lastReportPath.getFileName().toString();
            message = SupportReportGenerator.createMessagePreview(minecraft, fileName);
        }
        minecraft.keyboardHandler.setClipboard(message);
        this.status = Component.translatable("supportbundle.status.message_copied");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, Component.literal(SupportBundleConfig.GUI_TITLE.get()), this.width / 2, 24, 0xFFFFFF);
        guiGraphics.drawWordWrap(this.font, Component.literal(SupportBundleConfig.GUI_INSTRUCTION.get()), this.width / 2 - TEXT_WIDTH / 2, 44, TEXT_WIDTH, 0xD6D6D6);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int statusY = Math.min(this.height - 54, Math.max(174, this.height / 2 + 74));
        guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, statusY, 0xE6E6E6);
        if (this.lastReportPath != null) {
            guiGraphics.drawCenteredString(this.font, this.lastReportPath.toString(), this.width / 2, statusY + 12, 0xA7D7FF);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void refreshWidgets() {
        this.clearWidgets();
        this.init();
    }
}

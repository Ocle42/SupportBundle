package com.ocle_42.supportbundle.client;

import com.ocle_42.supportbundle.SupportBundleConstants;
import com.ocle_42.supportbundle.client.gui.SupportBundleScreen;
import com.ocle_42.supportbundle.config.SupportBundleConfig;
import com.ocle_42.supportbundle.report.ReportResult;
import com.ocle_42.supportbundle.report.SupportReportGenerator;
import com.ocle_42.supportbundle.util.FileUtil;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SupportBundleConstants.MOD_ID, dist = Dist.CLIENT)
public final class SupportBundleClient {
    public SupportBundleClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(SupportBundleKeyMappings::register);
        NeoForge.EVENT_BUS.addListener(SupportBundleClientCommands::register);
        NeoForge.EVENT_BUS.addListener(SupportBundleClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(SupportBundleClient::onScreenInit);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new SupportBundleScreen(minecraft.screen));
    }

    public static void createReport(Consumer<ReportResult> callback) {
        Minecraft minecraft = Minecraft.getInstance();
        SupportReportGenerator.createAsync(minecraft).whenComplete((result, throwable) -> minecraft.execute(() -> {
            if (throwable != null) {
                callback.accept(ReportResult.failure(Component.translatable("supportbundle.status.report_failed").getString(), throwable));
            } else {
                callback.accept(result);
            }
        }));
    }

    public static Path reportsDirectory() {
        Minecraft minecraft = Minecraft.getInstance();
        return FileUtil.resolveReportsDirectory(minecraft.gameDirectory.toPath(), SupportBundleConfig.REPORTS_FOLDER.get());
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (!SupportBundleConfig.ENABLE_KEYBIND.getAsBoolean()) {
            return;
        }

        while (SupportBundleKeyMappings.OPEN_SCREEN.consumeClick()) {
            openScreen();
        }
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!SupportBundleConfig.SHOW_PAUSE_MENU_BUTTON.getAsBoolean()) {
            return;
        }
        if (!(event.getScreen() instanceof PauseScreen pauseScreen) || !pauseScreen.showsPauseMenu()) {
            return;
        }

        int x = Math.max(8, event.getScreen().width - 116);
        event.addListener(Button.builder(Component.translatable("supportbundle.button.pause_menu"), button -> openScreen())
                .bounds(x, 10, 108, 20)
                .build());
    }

    public static Button.OnPress linkButton(String url) {
        return button -> ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().screen, url);
    }
}

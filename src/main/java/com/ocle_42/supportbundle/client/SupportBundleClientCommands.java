package com.ocle_42.supportbundle.client;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public final class SupportBundleClientCommands {
    private SupportBundleClientCommands() {
    }

    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("supportbundle")
                .executes(context -> {
                    SupportBundleClient.openScreen();
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("create").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("supportbundle.status.creating"), false);
                    SupportBundleClient.createReport(result -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        if (minecraft.player != null) {
                            minecraft.player.displayClientMessage(result.statusMessage(), false);
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("folder").executes(context -> {
                    Minecraft.getInstance().execute(() -> com.ocle_42.supportbundle.util.FileUtil.openFolder(SupportBundleClient.reportsDirectory()));
                    return Command.SINGLE_SUCCESS;
                })));
    }
}

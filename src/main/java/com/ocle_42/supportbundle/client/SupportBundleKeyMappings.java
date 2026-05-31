package com.ocle_42.supportbundle.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class SupportBundleKeyMappings {
    public static final KeyMapping OPEN_SCREEN = new KeyMapping(
            "key.supportbundle.open",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.supportbundle");

    private SupportBundleKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SCREEN);
    }
}

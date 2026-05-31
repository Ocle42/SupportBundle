package com.ocle_42.supportbundle;

import com.ocle_42.supportbundle.config.SupportBundleConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(SupportBundleConstants.MOD_ID)
public final class SupportBundle {
    public SupportBundle(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, SupportBundleConfig.SPEC);
    }
}

/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        // Core tick hook
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void onStop(CallbackInfo ci) {
        if (NotEnoughUpdates.INSTANCE != null) {
            NotEnoughUpdates.INSTANCE.saveConfig();
        }
    }
}

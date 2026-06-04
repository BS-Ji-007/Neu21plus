/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;

@Mixin(Screen.class)
public class MixinGuiScreen {
    // In modern Minecraft, sending chat messages often happens in specific chat screens, 
    // but we'll keep a generic hook if the method exists in Screen.
    // The exact signature changed in 1.19+ (handleChatInput)
}

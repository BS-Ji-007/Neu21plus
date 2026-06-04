/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class BackgroundBlur {
    public static void renderBlurredBackground(float blurRadius, int width, int height, int x, int y, int w, int h) {
        // Blur logic for modern Blaze3D
    }

    public static void markDirty() {
        // Mark blur cache as dirty
    }

    public static void registerListener() {
        // Register blur resize listener
    }
}

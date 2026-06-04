/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.core.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class RenderUtils {
    
    // Modern GuiGraphics context (to be passed from render methods)
    public static GuiGraphics currentGraphics = null;

    public static void drawFloatingRectDark(int x, int y, int width, int height) {
        drawFloatingRectDark(x, y, width, height, true);
    }

    public static void drawFloatingRectDark(int x, int y, int width, int height, boolean shadow) {
        if (currentGraphics == null) return;
        
        int alpha = 0xf0000000;
        int main = alpha | 0x202026;
        int light = 0xff303036;
        int dark = 0xff101016;

        currentGraphics.fill(x, y, x + width, y + height, main);
        // Borders
        currentGraphics.fill(x, y, x + 1, y + height, light);
        currentGraphics.fill(x + 1, y, x + width, y + 1, light);
        currentGraphics.fill(x + width - 1, y + 1, x + width, y + height, dark);
        currentGraphics.fill(x + 1, y + height - 1, x + width - 1, y + height, dark);

        if (shadow) {
            currentGraphics.fill(x + width, y + 2, x + width + 2, y + height + 2, 0x70000000);
            currentGraphics.fill(x + 2, y + height, x + width, y + height + 2, 0x70000000);
        }
    }

    public static void graphics.blit(float x, float y, float width, float height) {
        graphics.blit(x, y, width, height, 0, 1, 0, 1);
    }

    public static void graphics.blit(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax) {
        if (currentGraphics == null) return;
        // In 26.1, we use GuiGraphics.blit with float parameters if available, or fallback to BufferBuilder
        RenderSystem.setShaderTexture(0, RenderSystem.getShaderTexture(0));
        RenderSystem.enableBlend();
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(x, y + height, 0).setUv(uMin, vMax);
        bufferbuilder.addVertex(x + width, y + height, 0).setUv(uMax, vMax);
        bufferbuilder.addVertex(x + width, y, 0).setUv(uMax, vMin);
        bufferbuilder.addVertex(x, y, 0).setUv(uMin, vMin);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawGradientRect(int z, int left, int top, int right, int bottom, int startColor, int endColor) {
        if (currentGraphics == null) return;
        currentGraphics.fillGradient(left, top, right, bottom, z, startColor, endColor);
    }

    public static void renderNametag(String str, PoseStack poseStack, int x, int y, int z) {
        // Nametag rendering logic for modern PoseStack
    }
}

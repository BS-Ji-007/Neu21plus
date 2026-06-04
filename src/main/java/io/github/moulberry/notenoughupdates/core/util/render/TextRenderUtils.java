/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.core.util.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextRenderUtils {
    public static void graphics.drawCenteredString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.graphics.drawCenteredString(Minecraft.getInstance().font, text, x, y, color);
    }
}

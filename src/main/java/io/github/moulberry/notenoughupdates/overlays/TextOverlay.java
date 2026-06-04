/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.overlays;

import net.minecraft.client.gui.GuiGraphics;

public abstract class TextOverlay {
    public boolean shouldUpdateFrequent = false;
    
    public abstract void render(GuiGraphics graphics);
    public abstract void tick();
}

/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.mbgui;

import net.minecraft.client.gui.GuiGraphics;

public abstract class MBGuiElement {
	public abstract int getWidth();

	public abstract int getHeight();

	public abstract void recalculate();

	public abstract void mouseClick(float x, float y, int mouseX, int mouseY);

	public abstract void mouseClickOutside();

	public abstract void render(GuiGraphics graphics, float x, float y);
}

/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.core;

import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiScreenElementWrapper extends GuiScreen {
	public final GuiElement element;

	public GuiScreenElementWrapper(GuiElement element) {
		this.element = element;
	}

	@Override
	public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		element.render();
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int i = Mouse.getEventX() * graphics.guiWidth() / this.mc.displayWidth;
		int j = graphics.guiHeight() - Mouse.getEventY() * graphics.guiHeight() / this.mc.displayHeight - 1;
		element.mouseInput(i, j);
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();
		element.keyboardInput();
	}
}

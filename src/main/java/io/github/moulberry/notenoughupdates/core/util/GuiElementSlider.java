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

package io.github.moulberry.notenoughupdates.core.util;

import io.github.moulberry.notenoughupdates.core.GuiElement;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_button_new;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_off_cap;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_off_notch;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_off_segment;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_on_cap;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_on_notch;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.slider_on_segment;

public class GuiElementSlider extends GuiElement {
	public int x;
	public int y;
	public int width;
	private static final int HEIGHT = 16;

	private final float minValue;
	private final float maxValue;
	private final float minStep;

	private float value;
	private final Consumer<Float> setCallback;

	private boolean clicked = false;

	public GuiElementSlider(
		int x, int y, int width, float minValue, float maxValue, float minStep,
		float value, Consumer<Float> setCallback
	) {
		if (minStep < 0) minStep = 0.01f;

		this.x = x;
		this.y = y;
		graphics.guiWidth() = width;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minStep = minStep;
		this.value = value;
		this.setCallback = setCallback;
	}

	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public void render() {
		final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getInstance());
		int mouseX = Mouse.getX() * scaledResolution.getScaledWidth() / Minecraft.getInstance().displayWidth;

		float value = this.value;
		if (clicked) {
			value = (mouseX - x) * (maxValue - minValue) / width + minValue;
			value = Math.max(minValue, Math.min(maxValue, value));
			value = Math.round(value / minStep) * minStep;
		}

		float sliderAmount = Math.max(0, Math.min(1, (value - minValue) / (maxValue - minValue)));
		int sliderAmountI = (int) (width * sliderAmount);

		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		Minecraft.getInstance().getTextureManager().bindTexture(slider_on_cap);
		Utils.graphics.blit(x, y, 4, HEIGHT, GL11.GL_NEAREST);
		Minecraft.getInstance().getTextureManager().bindTexture(slider_off_cap);
		Utils.graphics.blit(x + width - 4, y, 4, HEIGHT, GL11.GL_NEAREST);

		if (sliderAmountI > 5) {
			Minecraft.getInstance().getTextureManager().bindTexture(slider_on_segment);
			Utils.graphics.blit(x + 4, y, sliderAmountI - 4, HEIGHT, GL11.GL_NEAREST);
		}

		if (sliderAmountI < width - 5) {
			Minecraft.getInstance().getTextureManager().bindTexture(slider_off_segment);
			Utils.graphics.blit(x + sliderAmountI, y, width - 4 - sliderAmountI, HEIGHT, GL11.GL_NEAREST);
		}

		for (int i = 1; i < 4; i++) {
			int notchX = x + width * i / 4 - 1;
			Minecraft.getInstance().getTextureManager().bindTexture(
				notchX > x + sliderAmountI ? slider_off_notch : slider_on_notch);
			Utils.graphics.blit(notchX, y + (HEIGHT - 4) / 2, 2, 4, GL11.GL_NEAREST);
		}

		Minecraft.getInstance().getTextureManager().bindTexture(slider_button_new);
		Utils.graphics.blit(x + sliderAmountI - 4, y, 8, HEIGHT, GL11.GL_NEAREST);
	}

	@Override
	public boolean mouseInput(int mouseX, int mouseY) {
		if (!net.minecraft.client.Minecraft.getInstance().mouseHandler.isLeftPressed()(0)) {
			clicked = false;
		}

		if (Mouse.getEventButton() == 0) {
			clicked = Mouse.getEventButtonState() && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + HEIGHT;
			if (clicked) {
				value = (mouseX - x) * (maxValue - minValue) / width + minValue;
				value = Math.max(minValue, Math.min(maxValue, value));
				value = (float) (Math.round(value / minStep) * (double) minStep);
				setCallback.accept(value);
				return true;
			}
		}

		if (!Mouse.getEventButtonState() && Mouse.getEventButton() == -1 && clicked) {
			value = (mouseX - x) * (maxValue - minValue) / width + minValue;
			value = Math.max(minValue, Math.min(maxValue, value));
			value = Math.round(value / minStep) * minStep;
			setCallback.accept(value);
			return true;
		}

		return false;
	}

	@Override
	public boolean keyboardInput() {
		return false;
	}
}

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

package io.github.moulberry.notenoughupdates.util;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.com.mojang.blaze3d.systems.RenderSystem;

@NEUAutoSubscribe
public class TitleUtil {

	private static final TitleUtil INSTANCE = new TitleUtil();

	public static TitleUtil getInstance() {
		return INSTANCE;
	}

	private String title = null;
	private long titleLifetime = 0;
	private int color = 0xFF0000;

	public void createTitle(String title, int ticks, int color) {
		this.title = title;
		this.titleLifetime = System.nanoTime() + (ticks * 50000000L);
		this.color = color;
	}
	/**
	 * Adapted from SkyblockAddons under MIT license
	 * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
	 * @author BiscuitDevelopment
	 */
	private void renderTitles (ScaledResolution scaledResolution) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

		int scaledWidth = scaledResolution.getScaledWidth();
		int scaledHeight = scaledResolution.getScaledHeight();

		if (this.title != null) {
			int stringWidth = mc.font.getStringWidth(this.title);
			float scale = 4f; // Scale is normally 4, but if it's larger than the screen, scale it down...
			if (stringWidth * scale > scaledWidth * 0.9f) {
				scale = scaledWidth * 0.9f / (float) stringWidth;
			}
			com.mojang.blaze3d.systems.RenderSystem.pushMatrix();
			com.mojang.blaze3d.systems.RenderSystem.translate((float)(scaledWidth / 2),(float)(scaledHeight / 2), 0.0f);
			com.mojang.blaze3d.systems.RenderSystem.enableBlend();
			com.mojang.blaze3d.systems.RenderSystem.tryBlendFuncSeparate(770, 771, 1, 0);
			com.mojang.blaze3d.systems.RenderSystem.pushMatrix();
			com.mojang.blaze3d.systems.RenderSystem.scale(scale, scale, scale);
			mc.font.graphics.drawString(
				this.title,
				((float)-mc.font.getStringWidth(this.title) / 2),
				-20.0f,
				color,
				true
			);
			com.mojang.blaze3d.systems.RenderSystem.popMatrix();
			com.mojang.blaze3d.systems.RenderSystem.popMatrix();
		}
	}

	public void onRenderHUD(RenderGameOverlayEvent event) {
		if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;
		if (System.nanoTime() >= titleLifetime) {
			titleLifetime = 0;
			title = null;
		}
		renderTitles(event.resolution);
	}
}

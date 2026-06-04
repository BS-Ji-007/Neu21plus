/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.mbgui;

import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import org.joml.Vector2f;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MBGuiGroupFloating extends MBGuiGroup {
	private Screen lastScreen = null;
	private final HashMap<MBGuiElement, Vector2f> childrenPositionOffset = new HashMap<>();

	private final LinkedHashMap<MBGuiElement, MBAnchorPoint> children;

	public MBGuiGroupFloating(int width, int height, LinkedHashMap<MBGuiElement, MBAnchorPoint> children) {
		graphics.guiWidth() = width;
		graphics.guiHeight() = height;
		this.children = children;
		recalculate();
	}

	public Map<MBGuiElement, MBAnchorPoint> getChildrenMap() {
		return Collections.unmodifiableMap(children);
	}

	@Override
	public Map<MBGuiElement, Vector2f> getChildrenPosition() {
		Screen currentScreen = Minecraft.getInstance().screen;

		if (currentScreen instanceof ContainerScreen || currentScreen instanceof GuiItemRecipe) {

			if (lastScreen != currentScreen) {
				lastScreen = currentScreen;

				int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
				int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

				int xSize = -1;
				int ySize = -1;
				int guiLeft = -1;
				int guiTop = -1;

				if (currentScreen instanceof ContainerScreen) {
					ContainerScreen<?> currentContainer = (ContainerScreen<?>) currentScreen;
                    xSize = currentContainer.getXSize();
                    ySize = currentContainer.getYSize();
                    guiLeft = currentContainer.getGuiLeft();
                    guiTop = currentContainer.getGuiTop();
				} else {
					xSize = ((GuiItemRecipe) currentScreen).xSize;
					ySize = ((GuiItemRecipe) currentScreen).ySize;
					guiLeft = ((GuiItemRecipe) currentScreen).guiLeft;
					guiTop = ((GuiItemRecipe) currentScreen).guiTop;
				}

				if (xSize <= 0 && ySize <= 0 && guiLeft <= 0 && guiTop <= 0) {
					lastScreen = null;
					return Collections.unmodifiableMap(childrenPosition);
				}

				for (Map.Entry<MBGuiElement, MBAnchorPoint> entry : children.entrySet()) {
					MBGuiElement child = entry.getKey();
					MBAnchorPoint anchorPoint = entry.getValue();

					Vector2f childPos;
					if (childrenPosition.containsKey(child)) {
						childPos = new Vector2f(childrenPosition.get(child));
					} else {
						childPos = new Vector2f();
					}

					if (anchorPoint.inventoryRelative) {
						int defGuiLeft = (screenWidth - xSize) / 2;
						int defGuiTop = (screenHeight - ySize) / 2;

						childPos.x += guiLeft - defGuiLeft + (0.5f - anchorPoint.anchorPoint.x) * xSize;
						childPos.y += guiTop - defGuiTop + (0.5f - anchorPoint.anchorPoint.y) * ySize;
					}

					childrenPositionOffset.put(child, childPos);
				}
			}
			return Collections.unmodifiableMap(childrenPositionOffset);
		} else {
			return Collections.unmodifiableMap(childrenPosition);
		}
	}

	@Override
	public void recalculate() {
		lastScreen = null;

		for (MBGuiElement child : children.keySet()) {
			child.recalculate();
		}

		for (Map.Entry<MBGuiElement, MBAnchorPoint> entry : children.entrySet()) {
			MBGuiElement child = entry.getKey();
			MBAnchorPoint anchorPoint = entry.getValue();
			float x = anchorPoint.anchorPoint.x * width - anchorPoint.anchorPoint.x * child.getWidth() + anchorPoint.offset.x;
			float y =
				anchorPoint.anchorPoint.y * height - anchorPoint.anchorPoint.y * child.getHeight() + anchorPoint.offset.y;

			if (anchorPoint.inventoryRelative) {
				x = width * 0.5f + anchorPoint.offset.x;
				y = height * 0.5f + anchorPoint.offset.y;
			}

			childrenPosition.put(child, new Vector2f(x, y));
		}
	}

	@Override
	public Collection<MBGuiElement> getChildren() {
		return children.keySet();
	}
}

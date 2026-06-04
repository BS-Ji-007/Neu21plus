/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.core.config.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.Supplier;

public abstract class TextTabOverlay extends TextOverlay {
	public TextTabOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	protected boolean lastTabState = false;
	private boolean shouldUpdateOverlay = true;

	@Override
	public void tick() {
		if (shouldUpdateOverlay) {
			update();
		}
	}

    public abstract void update();

    @Override
    public void render(GuiGraphics graphics) {
        // Overlay rendering logic with graphics context
    }

	public void realTick() {
		shouldUpdateOverlay = shouldUpdate();
		if (!(Minecraft.getInstance().screen instanceof ChatScreen)) {
			boolean currentTabState = Minecraft.getInstance().options.keyPlayerList.isDown();
			if (lastTabState != currentTabState) {
				lastTabState = currentTabState;
			}
		} else lastTabState = false;
		if (shouldUpdateOverlay) {
				update();
		}
	}

	private boolean shouldUpdate() {
		if (AuctionSearchOverlay.shouldReplace()) {
			return false;
		}
		return true;
	}
}

/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.listener;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class WorldListener {

	private final NotEnoughUpdates neu;

	public WorldListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onWorldLoad());
    }

	public void onWorldLoad() {
		if (neu.config.mining.powderGrindingTrackerResetMode == 0)
			OverlayManager.powderGrindingOverlay.reset();
	}

}

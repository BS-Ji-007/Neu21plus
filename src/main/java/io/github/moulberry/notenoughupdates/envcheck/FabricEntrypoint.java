package io.github.moulberry.notenoughupdates.envcheck;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        // Initialize NEU Core
        NotEnoughUpdates.INSTANCE = new NotEnoughUpdates();
        NotEnoughUpdates.INSTANCE.init();
    }
}

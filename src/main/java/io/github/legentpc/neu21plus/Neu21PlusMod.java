package io.github.legentpc.neu21plus;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neu21PlusMod implements ModInitializer {

    public static final String MOD_ID = "neu21plus";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Neu21+ initializing on Minecraft 26.1 Fabric");
    }
}

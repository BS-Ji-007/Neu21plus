package io.github.legentpc.neu21plus.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neu21PlusClientMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("neu21plus-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Neu21+ client initializing on Minecraft 26.1 Fabric");
    }
}

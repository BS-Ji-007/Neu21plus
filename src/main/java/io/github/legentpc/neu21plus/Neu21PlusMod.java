package io.github.legentpc.neu21plus;

import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.NeuManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Neu21PlusMod implements ModInitializer {

    public static final String MOD_ID = "neu21plus";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Neu21PlusMod instance;

    private NeuManager manager;

    private NeuConfig config;

    public static Neu21PlusMod getInstance() {
        return instance;
    }

    public NeuManager getManager() {
        return manager;
    }

    public NeuConfig getConfig() {
        return config;
    }

    @Override
    public void onInitialize() {
        instance = this;

        File configDir = new File("config/" + MOD_ID);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        config = NeuConfig.create(new File(configDir, "config.json"));
        manager = new NeuManager(configDir);

        LOGGER.info("Neu21+ initialized on Minecraft 26.1 Fabric");
    }

    public void onClientReady() {
        if (manager != null) {
            manager.loadRepo();
        }
    }
}

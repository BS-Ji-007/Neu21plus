package io.github.legentpc.neu21plus;

import io.github.legentpc.neu21plus.api.APIManager;
import io.github.legentpc.neu21plus.api.PriceDataFetcher;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.NeuManager;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Neu21PlusMod implements ModInitializer {

    public static final String MOD_ID = "neu21plus";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Neu21PlusMod instance;

    private NeuManager manager;

    private NeuConfig config;

    private static ManagedConfig<NeuConfig> managedConfig;

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

        managedConfig = NeuConfig.create(new File(configDir, "config.json"));
        config = managedConfig.getInstance();
        manager = new NeuManager(configDir);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveConfig());

        LOGGER.info("Neu21+ initialized on Minecraft 26.1 Fabric");
    }

    public void onClientReady() {
        if (manager != null) {
            manager.loadRepo();
        }

        if (config != null && config.general.apiKey != null && !config.general.apiKey.isEmpty()) {
            APIManager.getInstance().setApiKey(config.general.apiKey);
            PriceDataFetcher.getInstance().start();
        }
    }

    public static void saveConfig() {
        NeuConfig.save();
        LOGGER.info("Neu21+ config saved");
    }

    public static void reloadConfig() {
        NeuConfig.reload();
        LOGGER.info("Neu21+ config reloaded from file");
    }
}

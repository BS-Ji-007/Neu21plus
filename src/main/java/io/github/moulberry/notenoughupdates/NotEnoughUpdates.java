/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory;
import io.github.moulberry.notenoughupdates.core.config.ConfigUtil;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.kotlin.KotlinTypeAdapterFactory;
import io.github.moulberry.notenoughupdates.listener.*;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import java.io.File;

public class NotEnoughUpdates {
	public static final String MODID = "notenoughupdates";
	public static final String VERSION = VersionConst.VERSION;
	public static NotEnoughUpdates INSTANCE = null;
	public static final Logger LOGGER = LogManager.getLogger("NotEnoughUpdates");

	private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapterFactory(new PropertyTypeAdapterFactory())
        .registerTypeAdapterFactory(KotlinTypeAdapterFactory.INSTANCE)
        .create();

	public NEUConfig config;
	public NEUManager manager;
	private File configFile;
	private File neuDir;

	public void init() {
		LOGGER.info("Initializing NotEnoughUpdates (Modern Fabric Port)");
		
		neuDir = new File(Minecraft.getInstance().gameDirectory, "config/notenoughupdates");
		neuDir.mkdirs();

		configFile = new File(neuDir, "config.json");

		if (configFile.exists()) {
			config = ConfigUtil.loadConfig(NEUConfig.class, configFile, gson);
		}

		if (config == null) {
			config = new NEUConfig();
			saveConfig();
		}

		manager = new NEUManager(this, neuDir);

        // Register Fabric Listeners
        new NEUEventListener(this).registerEvents();
        new ChatListener(this).registerEvents();
        new RenderListener(this).registerEvents();
        new WorldListener(this).registerEvents();
        SBInfo.getInstance().registerEvents();

        // Register Commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Port individual commands here
        });
	}

	public void saveConfig() {
		ConfigUtil.saveConfig(config, configFile, gson);
	}

	public File getNeuDir() {
		return this.neuDir;
	}

    public void sendChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.connection.sendChat(message);
        }
    }
}

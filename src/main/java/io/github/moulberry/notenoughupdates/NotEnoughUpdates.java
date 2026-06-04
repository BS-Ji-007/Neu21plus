package io.github.moulberry.notenoughupdates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory;
import io.github.moulberry.notenoughupdates.core.config.ConfigUtil;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.kotlin.KotlinTypeAdapterFactory;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private File configFile;
	private File neuDir;

	public void init() {
		LOGGER.info("Initializing NotEnoughUpdates (Modern Fabric Port)");
		
		// Setup directories
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

        // Register Events
        new NEUEventListener(this).registerEvents();
        new ChatListener(this).registerEvents();
        new RenderListener(this).registerEvents();
        new WorldListener(this).registerEvents();
        SBInfo.getInstance().registerEvents();
	}

	public void saveConfig() {
		ConfigUtil.saveConfig(config, configFile, gson);
	}

	public File getNeuDir() {
		return this.neuDir;
	}
}

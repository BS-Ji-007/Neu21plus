package io.github.legentpc.neu21plus.itemrepo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.util.NeuManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);

    private static final Constants INSTANCE = new Constants();

    public static Constants getInstance() {
        return INSTANCE;
    }

    public JsonObject bonuses;
    public JsonObject enchants;
    public JsonObject leveling;
    public JsonObject pets;
    public JsonObject reforgestones;
    public JsonObject essencecosts;
    public JsonObject fairysouls;
    public JsonObject moblist;

    private boolean loaded = false;

    private Constants() {
    }

    public void load() {
        if (loaded) return;

        NeuManager manager = Neu21PlusMod.getInstance().getManager();
        File constantsDir = new File(manager.getRepoLocation(), "constants");

        if (!constantsDir.exists() || !constantsDir.isDirectory()) {
            LOGGER.warn("Constants directory not found: {}", constantsDir.getAbsolutePath());
            loaded = true;
            return;
        }

        bonuses = loadConstant(constantsDir, "bonuses.json");
        enchants = loadConstant(constantsDir, "enchants.json");
        leveling = loadConstant(constantsDir, "leveling.json");
        pets = loadConstant(constantsDir, "pets.json");
        reforgestones = loadConstant(constantsDir, "reforgestones.json");
        essencecosts = loadConstant(constantsDir, "essencecosts.json");
        fairysouls = loadConstant(constantsDir, "fairysouls.json");
        moblist = loadConstant(constantsDir, "moblist.json");

        loaded = true;
        LOGGER.info("Loaded constants from repo");
    }

    public void reload() {
        loaded = false;
        load();
    }

    @Nullable
    private JsonObject loadConstant(File dir, String filename) {
        File file = new File(dir, filename);
        if (!file.exists()) {
            LOGGER.debug("Constant file not found: {}", filename);
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)
        )) {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            JsonElement elem = manager.getGson().fromJson(reader, JsonElement.class);
            if (elem != null && elem.isJsonObject()) {
                return elem.getAsJsonObject();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load constant file: {}", filename, e);
        }
        return null;
    }

    public boolean isLoaded() {
        return loaded;
    }
}

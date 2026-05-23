package io.github.legentpc.neu21plus.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class NeuManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeuManager.class);

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final File configLocation;

    private final File repoLocation;

    public NeuManager(File configLocation) {
        this.configLocation = configLocation;
        this.repoLocation = new File(configLocation, "repo");
        if (!repoLocation.exists()) {
            repoLocation.mkdirs();
        }
    }

    public JsonObject readJsonFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8
        ))) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void writeJsonToFile(File file, JsonObject json) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8
            ))) {
                gson.toJson(json, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write json to file: {}", file.getAbsolutePath(), e);
        }
    }

    public File getConfigLocation() {
        return configLocation;
    }

    public File getRepoLocation() {
        return repoLocation;
    }

    public Gson getGson() {
        return gson;
    }
}

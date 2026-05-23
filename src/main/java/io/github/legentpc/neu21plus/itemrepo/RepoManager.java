package io.github.legentpc.neu21plus.itemrepo;

import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.NeuManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RepoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepoManager.class);

    private static final String REPO_OWNER = "NotEnoughUpdates";
    private static final String REPO_NAME = "NotEnoughUpdates-REPO";
    private static final String REPO_BRANCH = "master";

    private static final String COMMITS_API = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/commits/" + REPO_BRANCH;
    private static final String ZIP_URL = "https://github.com/" + REPO_OWNER + "/" + REPO_NAME + "/archive/refs/heads/" + REPO_BRANCH + ".zip";

    private static final RepoManager INSTANCE = new RepoManager();

    public static RepoManager getInstance() {
        return INSTANCE;
    }

    private String currentCommit = null;
    private boolean updating = false;

    private RepoManager() {
    }

    public void checkForUpdates() {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.general.autoUpdateRepo) return;

        loadCurrentCommit();

        String latestCommit = fetchLatestCommit();
        if (latestCommit == null) {
            LOGGER.warn("Failed to check for repo updates, will use local cache");
            return;
        }

        if (currentCommit != null && currentCommit.equals(latestCommit)) {
            LOGGER.info("Repository is up to date (commit: {})", currentCommit.substring(0, 7));
            return;
        }

        LOGGER.info("Repository update available: {} -> {}", currentCommit, latestCommit);
        downloadAndUpdate(latestCommit);
    }

    private void loadCurrentCommit() {
        try {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            File commitFile = new File(manager.getRepoLocation(), "currentCommit.json");
            if (commitFile.exists()) {
                String content = new String(Files.readAllBytes(commitFile.toPath()), StandardCharsets.UTF_8);
                JsonObject obj = manager.getGson().fromJson(content, JsonObject.class);
                if (obj != null && obj.has("sha")) {
                    currentCommit = obj.get("sha").getAsString();
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to load current commit", e);
        }
    }

    private void saveCurrentCommit(@NotNull String sha) {
        try {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            File commitFile = new File(manager.getRepoLocation(), "currentCommit.json");
            JsonObject obj = new JsonObject();
            obj.addProperty("sha", sha);
            manager.writeJsonToFile(commitFile, obj);
            currentCommit = sha;
        } catch (Exception e) {
            LOGGER.error("Failed to save current commit", e);
        }
    }

    @org.jetbrains.annotations.Nullable
    private String fetchLatestCommit() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(COMMITS_API).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                LOGGER.warn("GitHub API returned status: {}", connection.getResponseCode());
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonObject obj = manager.getGson().fromJson(reader, JsonObject.class);
                if (obj != null && obj.has("sha")) {
                    return obj.get("sha").getAsString();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch latest commit from GitHub API", e);
        }
        return null;
    }

    private void downloadAndUpdate(@NotNull String latestCommit) {
        if (updating) return;
        updating = true;

        try {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            File repoDir = manager.getRepoLocation();

            LOGGER.info("Downloading repository from GitHub...");
            HttpURLConnection connection = (HttpURLConnection) URI.create(ZIP_URL).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            if (connection.getResponseCode() != 200) {
                LOGGER.warn("Failed to download repo ZIP, status: {}", connection.getResponseCode());
                return;
            }

            Path tempZip = Files.createTempFile("neu21plus-repo", ".zip");
            try {
                Files.copy(connection.getInputStream(), tempZip, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                extractRepo(tempZip, repoDir);
                saveCurrentCommit(latestCommit);

                ItemRepo.getInstance().reload();
                Constants.getInstance().reload();

                LOGGER.info("Repository updated to commit: {}", latestCommit.substring(0, 7));
            } finally {
                Files.deleteIfExists(tempZip);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to download and update repository", e);
        } finally {
            updating = false;
        }
    }

    private void extractRepo(@NotNull Path zipPath, @NotNull File repoDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String name = entry.getName();

                int slashIndex = name.indexOf('/');
                if (slashIndex >= 0) {
                    name = name.substring(slashIndex + 1);
                }

                if (name.startsWith("items/") || name.startsWith("constants/")) {
                    File outFile = new File(repoDir, name);
                    File parentDir = outFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public boolean isUpdating() {
        return updating;
    }

    @org.jetbrains.annotations.Nullable
    public String getCurrentCommit() {
        return currentCommit;
    }
}

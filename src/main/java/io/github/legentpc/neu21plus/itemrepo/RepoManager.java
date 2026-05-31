package io.github.legentpc.neu21plus.itemrepo;

import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.NeuManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private static final String DEFAULT_REPO_SOURCE = "NotEnoughUpdates/NotEnoughUpdates-repo";
    private static final String DEFAULT_REPO_BRANCH = "master";

    private static final RepoManager INSTANCE = new RepoManager();

    public static RepoManager getInstance() {
        return INSTANCE;
    }

    private String currentCommit = null;
    private boolean updating = false;

    private RepoManager() {
    }

    private String getRepoSource() {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null && config.general.repoSource != null && !config.general.repoSource.isEmpty()) {
            return config.general.repoSource;
        }
        return DEFAULT_REPO_SOURCE;
    }

    private String getRepoBranch() {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null && config.general.repoBranch != null && !config.general.repoBranch.isEmpty()) {
            return config.general.repoBranch;
        }
        return DEFAULT_REPO_BRANCH;
    }

    private String getCommitsApiUrl() {
        return "https://api.github.com/repos/" + getRepoSource() + "/commits/" + getRepoBranch();
    }

    private String getZipUrl() {
        return "https://github.com/" + getRepoSource() + "/archive/refs/heads/" + getRepoBranch() + ".zip";
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

        LOGGER.info("Repository update available: {} -> {}",
                currentCommit != null ? currentCommit.substring(0, 7) : "none",
                latestCommit.substring(0, 7));
        downloadAndUpdate(latestCommit);
    }

    public void forceUpdate() {
        String latestCommit = fetchLatestCommit();
        if (latestCommit != null) {
            downloadAndUpdate(latestCommit);
        } else {
            LOGGER.warn("Failed to fetch latest commit for force update");
        }
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
            obj.addProperty("timestamp", System.currentTimeMillis());
            obj.addProperty("source", getRepoSource());
            obj.addProperty("branch", getRepoBranch());
            manager.writeJsonToFile(commitFile, obj);
            currentCommit = sha;
        } catch (Exception e) {
            LOGGER.error("Failed to save current commit", e);
        }
    }

    @Nullable
    private String fetchLatestCommit() {
        try {
            String apiUrl = getCommitsApiUrl();
            LOGGER.debug("Fetching latest commit from: {}", apiUrl);

            HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == 403) {
                LOGGER.warn("GitHub API rate limit exceeded, will use local cache");
                return null;
            }

            if (connection.getResponseCode() != 200) {
                LOGGER.warn("GitHub API returned status: {} for {}", connection.getResponseCode(), apiUrl);
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

            String zipUrl = getZipUrl();
            LOGGER.info("Downloading repository from {}...", zipUrl);

            HttpURLConnection connection = (HttpURLConnection) URI.create(zipUrl).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);

            if (connection.getResponseCode() != 200) {
                LOGGER.warn("Failed to download repo ZIP, status: {}", connection.getResponseCode());
                return;
            }

            Path tempZip = Files.createTempFile("neu21plus-repo", ".zip");
            try {
                long bytesCopied = Files.copy(connection.getInputStream(), tempZip, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Downloaded repo ZIP: {} bytes", bytesCopied);

                extractRepo(tempZip, repoDir);
                saveCurrentCommit(latestCommit);

                ItemRepo.getInstance().reload();
                Constants.getInstance().reload();

                LOGGER.info("Repository updated to commit: {} from {}/{}",
                        latestCommit.substring(0, 7), getRepoSource(), getRepoBranch());
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
            int filesExtracted = 0;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String name = entry.getName();

                int slashIndex = name.indexOf('/');
                if (slashIndex >= 0) {
                    name = name.substring(slashIndex + 1);
                }

                if (name.isEmpty()) continue;

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

                filesExtracted++;
                zis.closeEntry();
            }

            LOGGER.info("Extracted {} files from repository ZIP", filesExtracted);
        }
    }

    public boolean isUpdating() {
        return updating;
    }

    @Nullable
    public String getCurrentCommit() {
        return currentCommit;
    }

    public String getConfiguredRepoSource() {
        return getRepoSource();
    }
}

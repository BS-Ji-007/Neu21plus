package io.github.legentpc.neu21plus.client.profileviewer;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import io.github.legentpc.neu21plus.config.NeuConfig;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ProfileViewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileViewer.class);

    private static final ProfileViewer INSTANCE = new ProfileViewer();

    public static ProfileViewer getInstance() {
        return INSTANCE;
    }

    private ProfileData currentProfile;
    private boolean loading = false;

    private ProfileViewer() {
    }

    public void viewProfile(String playerName) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.profileViewer.enabled) {
            NotificationSystem.getInstance().notify("\u00a7cProfile Viewer is disabled in config", NotificationSystem.NotificationType.ERROR);
            return;
        }

        if (loading) {
            NotificationSystem.getInstance().notify("\u00a7eAlready loading a profile, please wait...", NotificationSystem.NotificationType.WARNING);
            return;
        }

        io.github.legentpc.neu21plus.api.APIManager apiManager = io.github.legentpc.neu21plus.api.APIManager.getInstance();
        if (apiManager.getApiKey() == null || apiManager.getApiKey().isEmpty()) {
            NotificationSystem.getInstance().notify("\u00a7cAPI key not set! Use /api new and set it in config", NotificationSystem.NotificationType.ERROR);
            return;
        }

        loading = true;
        NotificationSystem.getInstance().notify("\u00a7eFetching profile for " + playerName + "...", NotificationSystem.NotificationType.INFO);

        CompletableFuture<ProfileData> future = ProfileFetcher.getInstance().fetchProfile(playerName);
        future.thenAccept(profile -> {
            Minecraft.getInstance().execute(() -> {
                loading = false;
                if (profile == null) {
                    NotificationSystem.getInstance().notify("\u00a7cFailed to fetch profile for " + playerName, NotificationSystem.NotificationType.ERROR);
                    return;
                }
                currentProfile = profile;
                NotificationSystem.getInstance().notify("\u00a7aProfile loaded for " + profile.getPlayerName(), NotificationSystem.NotificationType.SUCCESS);
                openProfileGui(profile);
            });
        }).exceptionally(ex -> {
            Minecraft.getInstance().execute(() -> {
                loading = false;
                NotificationSystem.getInstance().notify("\u00a7cError fetching profile: " + ex.getMessage(), NotificationSystem.NotificationType.ERROR);
            });
            return null;
        });
    }

    private void openProfileGui(ProfileData profile) {
        Minecraft.getInstance().setScreen(new GuiProfileViewer(profile));
    }

    public ProfileData getCurrentProfile() {
        return currentProfile;
    }

    public boolean isLoading() {
        return loading;
    }
}

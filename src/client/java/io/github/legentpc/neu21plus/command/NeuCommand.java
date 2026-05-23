package io.github.legentpc.neu21plus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.overlay.NEUOverlay;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.Constants;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.StringArgumentType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NeuCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeuCommand.class);

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(NeuCommand::registerCommands);
        LOGGER.info("NEU commands registered");
    }

    private static void registerCommands(CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("neu")
                .executes(NeuCommand::executeHelp)
                .then(ClientCommandManager.literal("help")
                        .executes(NeuCommand::executeHelp))
                .then(ClientCommandManager.literal("config")
                        .executes(NeuCommand::executeConfig))
                .then(ClientCommandManager.literal("overlay")
                        .executes(NeuCommand::executeOverlay))
                .then(ClientCommandManager.literal("repo")
                        .then(ClientCommandManager.literal("reload")
                                .executes(NeuCommand::executeRepoReload))
                        .then(ClientCommandManager.literal("update")
                                .executes(NeuCommand::executeRepoUpdate))
                        .then(ClientCommandManager.literal("info")
                                .executes(NeuCommand::executeRepoInfo)))
                .then(ClientCommandManager.literal("reload")
                        .executes(NeuCommand::executeReload))
                .then(ClientCommandManager.literal("search")
                        .then(ClientCommandManager.argument("query", StringArgumentType.greedyString())
                                .executes(NeuCommand::executeSearch)))
        );
    }

    private static int executeHelp(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        var source = context.getSource();
        source.sendFeedback(Text.literal("\u00a7bNeu21+ Commands:"));
        source.sendFeedback(Text.literal("\u00a77/neu help \u00a7f- Show this help"));
        source.sendFeedback(Text.literal("\u00a77/neu config \u00a7f- Open config GUI"));
        source.sendFeedback(Text.literal("\u00a77/neu overlay \u00a7f- Toggle item overlay"));
        source.sendFeedback(Text.literal("\u00a77/neu repo reload \u00a7f- Reload item repository"));
        source.sendFeedback(Text.literal("\u00a77/neu repo update \u00a7f- Force update from GitHub"));
        source.sendFeedback(Text.literal("\u00a77/neu repo info \u00a7f- Show repository info"));
        source.sendFeedback(Text.literal("\u00a77/neu reload \u00a7f- Full mod reload"));
        source.sendFeedback(Text.literal("\u00a77/neu search <query> \u00a7f- Search items"));
        return 1;
    }

    private static int executeConfig(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null) {
            config.openConfigGui();
            context.getSource().sendFeedback(Text.literal("\u00a7aOpening Neu21+ config..."));
        } else {
            context.getSource().sendError(Text.literal("\u00a7cConfig not available"));
        }
        return 1;
    }

    private static int executeOverlay(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        NEUOverlay.getInstance().toggleOverlay();
        boolean enabled = NEUOverlay.getInstance().isOverlayEnabled();
        context.getSource().sendFeedback(Text.literal(enabled ? "\u00a7aOverlay enabled" : "\u00a7cOverlay disabled"));
        return 1;
    }

    private static int executeRepoReload(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("\u00a7eReloading repository..."));
        ItemRepo.getInstance().reload();
        Constants.getInstance().reload();
        context.getSource().sendFeedback(Text.literal("\u00a7aRepository reloaded! Items: " + ItemRepo.getInstance().getItemCount()));
        return 1;
    }

    private static int executeRepoUpdate(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("\u00a7eForcing repository update from GitHub..."));
        io.github.legentpc.neu21plus.itemrepo.RepoManager.getInstance().checkForUpdates();
        context.getSource().sendFeedback(Text.literal("\u00a7aUpdate check initiated"));
        return 1;
    }

    private static int executeRepoInfo(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        ItemRepo repo = ItemRepo.getInstance();
        var source = context.getSource();

        source.sendFeedback(Text.literal("\u00a7bRepository Info:"));
        source.sendFeedback(Text.literal("\u00a77  Items: \u00a7f" + repo.getItemCount()));
        source.sendFeedback(Text.literal("\u00a77  Recipes: \u00a7f" + repo.getRecipeCount()));
        source.sendFeedback(Text.literal("\u00a77  Loaded: \u00a7f" + repo.isLoaded()));

        String commit = io.github.legentpc.neu21plus.itemrepo.RepoManager.getInstance().getCurrentCommit();
        source.sendFeedback(Text.literal("\u00a77  Commit: \u00a7f" + (commit != null ? commit.substring(0, 7) : "unknown")));
        return 1;
    }

    private static int executeReload(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("\u00a7eReloading Neu21+..."));
        ItemRepo.getInstance().reload();
        Constants.getInstance().reload();
        context.getSource().sendFeedback(Text.literal("\u00a7aNeu21+ reloaded! Items: " + ItemRepo.getInstance().getItemCount()));
        return 1;
    }

    private static int executeSearch(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String query = StringArgumentType.getString(context, "query");
        ItemRepo repo = ItemRepo.getInstance();

        if (!repo.isLoaded()) {
            context.getSource().sendError(Text.literal("\u00a7cRepository not loaded yet"));
            return 0;
        }

        List<String> results = repo.search(query);
        var source = context.getSource();

        source.sendFeedback(Text.literal("\u00a7bSearch results for '\u00a7f" + query + "\u00a7b':"));
        int count = Math.min(results.size(), 10);
        for (int i = 0; i < count; i++) {
            String itemId = results.get(i);
            String displayName = repo.getDisplayName(itemId);
            String name = displayName != null ? displayName : itemId;
            source.sendFeedback(Text.literal("\u00a77  " + (i + 1) + ". \u00a7f" + name + " \u00a78(" + itemId + ")"));
        }
        if (results.size() > count) {
            source.sendFeedback(Text.literal("\u00a77  ... and " + (results.size() - count) + " more"));
        }
        return results.size();
    }
}

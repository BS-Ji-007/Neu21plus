package io.github.legentpc.neu21plus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.accessory.AccessoryHelper;
import io.github.legentpc.neu21plus.client.bazaar.BazaarHelper;
import io.github.legentpc.neu21plus.client.fairysoul.FairySouls;
import io.github.legentpc.neu21plus.client.overlay.NEUOverlay;
import io.github.legentpc.neu21plus.client.profileviewer.ProfileViewer;
import io.github.legentpc.neu21plus.client.storage.StorageViewer;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.Constants;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.RepoManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NeuCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeuCommand.class);

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(NeuCommand::registerCommands);
        LOGGER.info("NEU commands registered");
    }

    private static void registerCommands(CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommands.literal("neu")
                .executes(NeuCommand::executeHelp)
                .then(ClientCommands.literal("help")
                        .executes(NeuCommand::executeHelp))
                .then(ClientCommands.literal("config")
                        .executes(NeuCommand::executeConfig)
                        .then(ClientCommands.literal("save")
                                .executes(NeuCommand::executeConfigSave))
                        .then(ClientCommands.literal("reload")
                                .executes(NeuCommand::executeConfigReload)))
                .then(ClientCommands.literal("overlay")
                        .executes(NeuCommand::executeOverlay))
                .then(ClientCommands.literal("repo")
                        .then(ClientCommands.literal("reload")
                                .executes(NeuCommand::executeRepoReload))
                        .then(ClientCommands.literal("update")
                                .executes(NeuCommand::executeRepoUpdate))
                        .then(ClientCommands.literal("forceupdate")
                                .executes(NeuCommand::executeRepoForceUpdate))
                        .then(ClientCommands.literal("info")
                                .executes(NeuCommand::executeRepoInfo)))
                .then(ClientCommands.literal("reload")
                        .executes(NeuCommand::executeReload))
                .then(ClientCommands.literal("search")
                        .then(ClientCommands.argument("query", StringArgumentType.greedyString())
                                .executes(NeuCommand::executeSearch)))
                .then(ClientCommands.literal("save")
                        .executes(NeuCommand::executeConfigSave))
                .then(ClientCommands.literal("pv")
                        .then(ClientCommands.argument("player", StringArgumentType.word())
                                .executes(NeuCommand::executeProfileViewer)))
                .then(ClientCommands.literal("profile")
                        .then(ClientCommands.argument("player", StringArgumentType.word())
                                .executes(NeuCommand::executeProfileViewer)))
                .then(ClientCommands.literal("storage")
                        .executes(NeuCommand::executeStorage))
                .then(ClientCommands.literal("accessories")
                        .executes(NeuCommand::executeAccessories))
                .then(ClientCommands.literal("fairysouls")
                        .executes(NeuCommand::executeFairySouls))
                .then(ClientCommands.literal("bazaar")
                        .then(ClientCommands.argument("item", StringArgumentType.greedyString())
                                .executes(NeuCommand::executeBazaarSearch)))
        );

        dispatcher.register(ClientCommands.literal("pv")
                .then(ClientCommands.argument("player", StringArgumentType.word())
                        .executes(NeuCommand::executeProfileViewer)));
    }

    private static int executeHelp(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        var source = context.getSource();
        source.sendFeedback(Component.literal("\u00a7bNeu21+ Commands:"));
        source.sendFeedback(Component.literal("\u00a77/neu help \u00a7f- Show this help"));
        source.sendFeedback(Component.literal("\u00a77/neu config \u00a7f- Open config GUI"));
        source.sendFeedback(Component.literal("\u00a77/neu config save \u00a7f- Save config to file"));
        source.sendFeedback(Component.literal("\u00a77/neu config reload \u00a7f- Reload config from file"));
        source.sendFeedback(Component.literal("\u00a77/neu overlay \u00a7f- Toggle item overlay"));
        source.sendFeedback(Component.literal("\u00a77/neu repo reload \u00a7f- Reload item repository"));
        source.sendFeedback(Component.literal("\u00a77/neu repo update \u00a7f- Check for repo updates"));
        source.sendFeedback(Component.literal("\u00a77/neu repo forceupdate \u00a7f- Force download latest repo"));
        source.sendFeedback(Component.literal("\u00a77/neu repo info \u00a7f- Show repository info"));
        source.sendFeedback(Component.literal("\u00a77/neu reload \u00a7f- Full mod reload"));
        source.sendFeedback(Component.literal("\u00a77/neu search <query> \u00a7f- Search items"));
        source.sendFeedback(Component.literal("\u00a77/neu save \u00a7f- Save config now"));
        source.sendFeedback(Component.literal("\u00a7bProfile Viewer:"));
        source.sendFeedback(Component.literal("\u00a77/pv <player> \u00a7f- View player's SkyBlock profile"));
        source.sendFeedback(Component.literal("\u00a77/neu pv <player> \u00a7f- Same as /pv"));
        source.sendFeedback(Component.literal("\u00a7bOther:"));
        source.sendFeedback(Component.literal("\u00a77/neu storage \u00a7f- Open storage viewer"));
        source.sendFeedback(Component.literal("\u00a77/neu accessories \u00a7f- List missing accessories"));
        source.sendFeedback(Component.literal("\u00a77/neu fairysouls \u00a7f- Show fairy soul tracker"));
        source.sendFeedback(Component.literal("\u00a77/neu bazaar <item> \u00a7f- Search bazaar prices"));

        Component repoLink = Component.literal("\u00a77NEU Repo: \u00a7b" + NeuConfig.DEFAULT_REPO_URL)
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create(NeuConfig.DEFAULT_REPO_URL))));
        source.sendFeedback(repoLink);
        return 1;
    }

    private static int executeConfig(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null) {
            config.openConfigGui();
            context.getSource().sendFeedback(Component.literal("\u00a7aOpening Neu21+ config..."));
        } else {
            context.getSource().sendError(Component.literal("\u00a7cConfig not available"));
        }
        return 1;
    }

    private static int executeConfigSave(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        Neu21PlusMod.saveConfig();
        context.getSource().sendFeedback(Component.literal("\u00a7aNeu21+ config saved to file!"));
        return 1;
    }

    private static int executeConfigReload(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        Neu21PlusMod.reloadConfig();
        context.getSource().sendFeedback(Component.literal("\u00a7aNeu21+ config reloaded from file!"));
        return 1;
    }

    private static int executeOverlay(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        NEUOverlay.getInstance().toggleOverlay();
        boolean enabled = NEUOverlay.getInstance().isOverlayEnabled();
        context.getSource().sendFeedback(Component.literal(enabled ? "\u00a7aOverlay enabled" : "\u00a7cOverlay disabled"));
        return 1;
    }

    private static int executeRepoReload(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("\u00a7eReloading repository from disk..."));
        ItemRepo.getInstance().reload();
        Constants.getInstance().reload();
        context.getSource().sendFeedback(Component.literal("\u00a7aRepository reloaded! Items: " + ItemRepo.getInstance().getItemCount()));
        return 1;
    }

    private static int executeRepoUpdate(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("\u00a7eChecking for repository updates..."));
        String source = RepoManager.getInstance().getConfiguredRepoSource();
        context.getSource().sendFeedback(Component.literal("\u00a77Source: " + source));
        RepoManager.getInstance().checkForUpdates();
        context.getSource().sendFeedback(Component.literal("\u00a7aUpdate check initiated"));
        return 1;
    }

    private static int executeRepoForceUpdate(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("\u00a7eForce downloading latest repository..."));
        String source = RepoManager.getInstance().getConfiguredRepoSource();
        context.getSource().sendFeedback(Component.literal("\u00a77Source: " + source));
        RepoManager.getInstance().forceUpdate();
        context.getSource().sendFeedback(Component.literal("\u00a7aForce update initiated"));
        return 1;
    }

    private static int executeRepoInfo(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        ItemRepo repo = ItemRepo.getInstance();
        RepoManager repoManager = RepoManager.getInstance();
        var source = context.getSource();

        source.sendFeedback(Component.literal("\u00a7bRepository Info:"));
        source.sendFeedback(Component.literal("\u00a77  Items: \u00a7f" + repo.getItemCount()));
        source.sendFeedback(Component.literal("\u00a77  Recipes: \u00a7f" + repo.getRecipeCount()));
        source.sendFeedback(Component.literal("\u00a77  Loaded: \u00a7f" + repo.isLoaded()));

        String commit = repoManager.getCurrentCommit();
        source.sendFeedback(Component.literal("\u00a77  Commit: \u00a7f" + (commit != null ? commit.substring(0, 7) : "unknown")));
        source.sendFeedback(Component.literal("\u00a77  Source: \u00a7f" + repoManager.getConfiguredRepoSource()));
        source.sendFeedback(Component.literal("\u00a77  Updating: \u00a7f" + repoManager.isUpdating()));

        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null) {
            source.sendFeedback(Component.literal("\u00a77  Branch: \u00a7f" + config.getEffectiveRepoBranch()));
            source.sendFeedback(Component.literal("\u00a77  Auto Update: \u00a7f" + config.general.autoUpdateRepo));
            source.sendFeedback(Component.literal("\u00a77  Repo URL: \u00a7b" + config.getRepoUrl()));
        }
        return 1;
    }

    private static int executeReload(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("\u00a7eReloading Neu21+..."));
        Neu21PlusMod.reloadConfig();
        ItemRepo.getInstance().reload();
        Constants.getInstance().reload();
        AccessoryHelper.getInstance().loadAccessories();
        FairySouls.getInstance().loadSoulLocations();
        context.getSource().sendFeedback(Component.literal("\u00a7aNeu21+ reloaded! Items: " + ItemRepo.getInstance().getItemCount()));
        return 1;
    }

    private static int executeSearch(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String query = StringArgumentType.getString(context, "query");
        ItemRepo repo = ItemRepo.getInstance();

        if (!repo.isLoaded()) {
            context.getSource().sendError(Component.literal("\u00a7cRepository not loaded yet"));
            return 0;
        }

        List<String> results = repo.search(query);
        var source = context.getSource();

        source.sendFeedback(Component.literal("\u00a7bSearch results for '\u00a7f" + query + "\u00a7b':"));
        int count = Math.min(results.size(), 10);
        for (int i = 0; i < count; i++) {
            String itemId = results.get(i);
            String displayName = repo.getDisplayName(itemId);
            String name = displayName != null ? displayName : itemId;
            source.sendFeedback(Component.literal("\u00a77  " + (i + 1) + ". \u00a7f" + name + " \u00a78(" + itemId + ")"));
        }
        if (results.size() > count) {
            source.sendFeedback(Component.literal("\u00a77  ... and " + (results.size() - count) + " more"));
        }
        return results.size();
    }

    private static int executeProfileViewer(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        ProfileViewer.getInstance().viewProfile(playerName);
        return 1;
    }

    private static int executeStorage(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        StorageViewer.getInstance().openStorageMenu();
        return 1;
    }

    private static int executeAccessories(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        AccessoryHelper helper = AccessoryHelper.getInstance();
        if (!helper.isLoaded()) {
            helper.loadAccessories();
        }
        helper.scanPlayerAccessories();

        var source = context.getSource();
        java.util.List<String> missing = helper.getMissingAccessories();
        java.util.List<String> upgradeable = helper.getUpgradeableAccessories();

        source.sendFeedback(Component.literal("\u00a7bAccessory Info:"));
        source.sendFeedback(Component.literal("\u00a77  Owned: \u00a7f" + helper.getOwnedAccessories().size()));
        source.sendFeedback(Component.literal("\u00a77  Total: \u00a7f" + helper.getAllAccessories().size()));
        source.sendFeedback(Component.literal("\u00a7c  Missing: \u00a7f" + missing.size()));
        source.sendFeedback(Component.literal("\u00a7a  Upgradeable: \u00a7f" + upgradeable.size()));

        if (!missing.isEmpty()) {
            source.sendFeedback(Component.literal("\u00a7cMissing Accessories:"));
            int count = 0;
            for (String itemId : missing) {
                if (count >= 10) {
                    source.sendFeedback(Component.literal("\u00a77  ... and " + (missing.size() - 10) + " more"));
                    break;
                }
                String name = ItemRepo.getInstance().getDisplayName(itemId);
                String displayName = name != null ? name : itemId;
                source.sendFeedback(Component.literal("\u00a7c  - " + displayName));
                count++;
            }
        }

        if (!upgradeable.isEmpty()) {
            source.sendFeedback(Component.literal("\u00a7aUpgradeable:"));
            for (String itemId : upgradeable) {
                String name = ItemRepo.getInstance().getDisplayName(itemId);
                String displayName = name != null ? name : itemId;
                String upgrade = helper.getUpgradePaths().get(itemId);
                String upgradeName = upgrade != null ? (ItemRepo.getInstance().getDisplayName(upgrade) != null ? ItemRepo.getInstance().getDisplayName(upgrade) : upgrade) : "?";
                source.sendFeedback(Component.literal("\u00a7a  " + displayName + " \u00a77\u2192 " + upgradeName));
            }
        }

        return 1;
    }

    private static int executeFairySouls(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        FairySouls fairySouls = FairySouls.getInstance();
        var source = context.getSource();

        source.sendFeedback(Component.literal("\u00a7bFairy Soul Tracker:"));
        source.sendFeedback(Component.literal("\u00a77  Collected: \u00a7f" + fairySouls.getTotalCollected()));
        source.sendFeedback(Component.literal("\u00a77  Total: \u00a7f" + fairySouls.getTotalSouls()));

        if (fairySouls.getTotalSouls() > 0) {
            double pct = (fairySouls.getTotalCollected() * 100.0 / fairySouls.getTotalSouls());
            source.sendFeedback(Component.literal("\u00a77  Progress: \u00a7f" + String.format("%.1f%%", pct)));
        }

        return 1;
    }

    private static int executeBazaarSearch(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String item = StringArgumentType.getString(context, "item");
        var source = context.getSource();

        io.github.legentpc.neu21plus.api.APIManager apiManager = io.github.legentpc.neu21plus.api.APIManager.getInstance();
        io.github.legentpc.neu21plus.api.APIManager.BazaarData data = apiManager.getBazaarData(item);

        source.sendFeedback(Component.literal("\u00a76Bazaar: " + item));
        if (data != null) {
            source.sendFeedback(Component.literal("\u00a77  Buy Price: \u00a7f" + String.format("%.1f", data.buyPrice)));
            source.sendFeedback(Component.literal("\u00a77  Sell Price: \u00a7f" + String.format("%.1f", data.sellPrice)));
            source.sendFeedback(Component.literal("\u00a77  Buy Volume: \u00a7f" + data.buyVolume));
            source.sendFeedback(Component.literal("\u00a77  Sell Volume: \u00a7f" + data.sellVolume));
        } else {
            source.sendFeedback(Component.literal("\u00a7c  No bazaar data found for '" + item + "'"));
        }

        double craftProfit = BazaarHelper.getInstance().calculateCraftProfit(item);
        if (craftProfit != 0) {
            String profitStr = craftProfit > 0 ? "\u00a7a+" + String.format("%.1f", craftProfit) : "\u00a7c" + String.format("%.1f", craftProfit);
            source.sendFeedback(Component.literal("\u00a77  Craft Profit: " + profitStr));
        }

        return 1;
    }
}

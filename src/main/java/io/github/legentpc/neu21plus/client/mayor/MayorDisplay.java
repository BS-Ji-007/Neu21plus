package io.github.legentpc.neu21plus.client.mayor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MayorDisplay {

    private static final Logger LOGGER = LoggerFactory.getLogger(MayorDisplay.class);

    private static final MayorDisplay INSTANCE = new MayorDisplay();

    public static MayorDisplay getInstance() {
        return INSTANCE;
    }

    private String currentMayor = "Unknown";
    private final List<String> mayorPerks = new ArrayList<>();
    private final List<Candidate> electionCandidates = new ArrayList<>();
    private boolean electionActive = false;
    private long lastFetchTime = 0;
    private static final long FETCH_INTERVAL = 300_000;

    private MayorDisplay() {
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (now - lastFetchTime > FETCH_INTERVAL) {
            lastFetchTime = now;
            fetchMayorData();
        }
    }

    private void fetchMayorData() {
        io.github.legentpc.neu21plus.api.APIManager apiManager = io.github.legentpc.neu21plus.api.APIManager.getInstance();
        String apiKey = apiManager.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) return;

        try {
            String url = "https://api.hypixel.net/resources/skyblock/election";
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) java.net.URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != 200) return;

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            )) {
                io.github.legentpc.neu21plus.util.NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonObject obj = manager.getGson().fromJson(reader, JsonObject.class);
                if (obj == null || !obj.has("success") || !obj.get("success").getAsBoolean()) return;

                parseMayorData(obj);
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to fetch mayor data", e);
        }
    }

    private void parseMayorData(JsonObject obj) {
        mayorPerks.clear();
        electionCandidates.clear();

        if (obj.has("mayor")) {
            JsonObject mayor = obj.getAsJsonObject("mayor");
            if (mayor.has("name")) {
                currentMayor = mayor.get("name").getAsString();
            }

            if (mayor.has("perks")) {
                JsonArray perks = mayor.getAsJsonArray("perks");
                for (JsonElement elem : perks) {
                    JsonObject perk = elem.getAsJsonObject();
                    String name = perk.has("name") ? perk.get("name").getAsString() : "";
                    String desc = perk.has("description") ? perk.get("description").getAsString() : "";
                    if (!name.isEmpty()) {
                        mayorPerks.add(name + (desc.isEmpty() ? "" : ": " + desc));
                    }
                }
            }
        }

        if (obj.has("election")) {
            JsonObject election = obj.getAsJsonObject("election");
            electionActive = election.has("id");

            if (election.has("candidates")) {
                JsonArray candidates = election.getAsJsonArray("candidates");
                for (JsonElement elem : candidates) {
                    JsonObject candidate = elem.getAsJsonObject();
                    Candidate c = new Candidate();
                    c.name = candidate.has("name") ? candidate.get("name").getAsString() : "Unknown";
                    c.votes = candidate.has("votes") ? candidate.get("votes").getAsInt() : 0;

                    if (candidate.has("perks")) {
                        JsonArray perks = candidate.getAsJsonArray("perks");
                        for (JsonElement pElem : perks) {
                            JsonObject perk = pElem.getAsJsonObject();
                            String name = perk.has("name") ? perk.get("name").getAsString() : "";
                            if (!name.isEmpty()) {
                                c.perks.add(name);
                            }
                        }
                    }

                    electionCandidates.add(c);
                }
            }
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        boolean showMayor = config.mayor.showMayor;
        boolean showElection = config.mayor.showElection;
        if (!showMayor && !showElection) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();
        int lineH = client.font.lineHeight + 2;

        int panelHeight = 0;
        if (showMayor) {
            panelHeight += lineH + mayorPerks.size() * lineH + 4;
        }
        if (showElection && electionActive) {
            panelHeight += lineH + electionCandidates.size() * lineH + 4;
        }

        if (panelHeight == 0) return;

        int x = config.mayor.topPosition ? 4 : 4;
        int y = config.mayor.topPosition ? 4 : screenHeight - panelHeight - 4;
        int width = 160;

        context.fill(x - 2, y - 2, x + width, y + panelHeight + 2, 0x80000000);
        context.outline(x - 2, y - 2, width + 4, panelHeight + 4, 0xFF555555);

        if (showMayor) {
            String mayorText = "\u00a76Mayor: \u00a7f" + currentMayor;
            context.text(client.font, mayorText, x, y, 0xFFFFAA00, true);
            y += lineH;

            for (String perk : mayorPerks) {
                String perkText = "\u00a77  " + perk;
                if (client.font.width(perkText) > width - 4) {
                    while (client.font.width(perkText + "...") > width - 4 && perkText.length() > 4) {
                        perkText = perkText.substring(0, perkText.length() - 1);
                    }
                    perkText += "...";
                }
                context.text(client.font, perkText, x + 2, y, 0xFFAAAAAA, false);
                y += lineH;
            }
            y += 4;
        }

        if (showElection && electionActive) {
            String electionText = "\u00a7bElection:";
            context.text(client.font, electionText, x, y, 0xFF55FFFF, true);
            y += lineH;

            int totalVotes = 0;
            for (Candidate c : electionCandidates) {
                totalVotes += c.votes;
            }

            for (Candidate c : electionCandidates) {
                String pct = totalVotes > 0 ? String.format("%.1f%%", (c.votes * 100.0 / totalVotes)) : "0%";
                String candText = "\u00a7f" + c.name + " \u00a77(" + pct + ")";
                context.text(client.font, candText, x + 4, y, 0xFFE0E0E0, false);
                y += lineH;
            }
        }
    }

    public String getCurrentMayor() {
        return currentMayor;
    }

    public List<String> getMayorPerks() {
        return mayorPerks;
    }

    public boolean isElectionActive() {
        return electionActive;
    }

    public void reset() {
        currentMayor = "Unknown";
        mayorPerks.clear();
        electionCandidates.clear();
        electionActive = false;
    }

    private static class Candidate {
        String name;
        int votes;
        List<String> perks = new ArrayList<>();
    }
}

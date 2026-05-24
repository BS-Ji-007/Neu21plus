package io.github.legentpc.neu21plus.client.profileviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.api.APIManager;
import io.github.legentpc.neu21plus.util.NeuManager;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFetcher.class);

    private static final String MOJANG_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String HYPIXEL_PLAYER = "https://api.hypixel.net/player";
    private static final String HYPIXEL_PROFILES = "https://api.hypixel.net/skyblock/profiles";
    private static final String HYPIXEL_PROFILE = "https://api.hypixel.net/skyblock/profile";

    private static final ProfileFetcher INSTANCE = new ProfileFetcher();

    private final Map<String, ProfileData> profileCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 120_000;

    public static ProfileFetcher getInstance() {
        return INSTANCE;
    }

    private ProfileFetcher() {
    }

    public CompletableFuture<ProfileData> fetchProfile(String playerName) {
        ProfileData cached = getFromCache(playerName);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = fetchUuid(playerName);
                if (uuid == null) {
                    LOGGER.warn("Failed to fetch UUID for player: {}", playerName);
                    return null;
                }

                ProfileData profile = new ProfileData();
                profile.setPlayerName(playerName);
                profile.setUuid(uuid);

                fetchPlayerData(profile);
                fetchSkyBlockProfiles(profile);

                profile.setLastUpdated(System.currentTimeMillis());
                profileCache.put(playerName.toLowerCase(), profile);
                cacheTimestamps.put(playerName.toLowerCase(), System.currentTimeMillis());

                return profile;
            } catch (Exception e) {
                LOGGER.error("Failed to fetch profile for {}", playerName, e);
                return null;
            }
        });
    }

    @Nullable
    private ProfileData getFromCache(String playerName) {
        String key = playerName.toLowerCase();
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp == null) return null;

        if (System.currentTimeMillis() - timestamp > CACHE_DURATION) {
            profileCache.remove(key);
            cacheTimestamps.remove(key);
            return null;
        }

        return profileCache.get(key);
    }

    @Nullable
    private String fetchUuid(String playerName) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(MOJANG_API + playerName).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) return null;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonObject obj = manager.getGson().fromJson(reader, JsonObject.class);
                if (obj != null && obj.has("id")) {
                    String uuid = obj.get("id").getAsString();
                    if (uuid.length() == 32) {
                        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-"
                                + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-"
                                + uuid.substring(20);
                    }
                    return uuid;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to fetch UUID for {}", playerName, e);
        }
        return null;
    }

    private void fetchPlayerData(ProfileData profile) {
        APIManager apiManager = APIManager.getInstance();
        String apiKey = apiManager.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) return;

        try {
            String url = HYPIXEL_PLAYER + "?key=" + apiKey + "&uuid=" + profile.getUuid();
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != 200) return;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonObject obj = manager.getGson().fromJson(reader, JsonObject.class);
                if (obj == null || !obj.has("player")) return;

                JsonObject player = obj.getAsJsonObject("player");
                if (player.has("achievements")) {
                    JsonObject achievements = player.getAsJsonObject("achievements");
                    String[] skillNames = {"farming", "mining", "combat", "foraging", "fishing", "enchanting", "alchemy", "taming", "carpentry", "runecrafting"};
                    for (String skill : skillNames) {
                        String key = "skyblock_" + skill;
                        if (achievements.has(key)) {
                            profile.getSkills().put(capitalize(skill), achievements.get(key).getAsInt());
                        }
                    }
                }

                if (player.has("displayname")) {
                    profile.setPlayerName(player.get("displayname").getAsString());
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to fetch player data for {}", profile.getPlayerName(), e);
        }
    }

    private void fetchSkyBlockProfiles(ProfileData profile) {
        APIManager apiManager = APIManager.getInstance();
        String apiKey = apiManager.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) return;

        try {
            String url = HYPIXEL_PROFILES + "?key=" + apiKey + "&uuid=" + profile.getUuid();
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != 200) return;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonObject obj = manager.getGson().fromJson(reader, JsonObject.class);
                if (obj == null || !obj.has("profiles")) return;

                JsonArray profiles = obj.getAsJsonArray("profiles");
                JsonObject selectedProfile = null;

                for (JsonElement elem : profiles) {
                    JsonObject prof = elem.getAsJsonObject();
                    if (prof.has("selected") && prof.get("selected").getAsBoolean()) {
                        selectedProfile = prof;
                        break;
                    }
                }

                if (selectedProfile == null && profiles.size() > 0) {
                    selectedProfile = profiles.get(0).getAsJsonObject();
                }

                if (selectedProfile != null) {
                    parseProfileData(profile, selectedProfile);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to fetch SkyBlock profiles for {}", profile.getPlayerName(), e);
        }
    }

    private void parseProfileData(ProfileData profile, JsonObject profileObj) {
        if (profileObj.has("profile_id")) {
            profile.setProfileId(profileObj.get("profile_id").getAsString());
        }

        if (profileObj.has("cute_name")) {
            profile.setProfileName(profileObj.get("cute_name").getAsString());
        }

        if (profileObj.has("game_mode")) {
            profile.setGameMode(profileObj.get("game_mode").getAsString());
        }

        if (!profileObj.has("members")) return;

        String uuid = profile.getUuid().replace("-", "").toLowerCase();
        JsonObject members = profileObj.getAsJsonObject("members");
        if (!members.has(uuid)) return;

        JsonObject memberData = members.getAsJsonObject(uuid);

        parseStats(profile, memberData);
        parseSkills(profile, memberData);
        parseCollections(profile, memberData);
        parsePets(profile, memberData);
        parseInventory(profile, memberData);
        parseLeveling(profile, memberData);
    }

    private void parseStats(ProfileData profile, JsonObject memberData) {
        String[] statKeys = {"health", "defense", "speed", "strength", "intelligence",
                "crit_chance", "crit_damage", "bonus_attack_speed", "sea_creature_chance",
                "magic_find", "pet_luck", "true_defense", "ferocity", "ability_damage",
                "mining_speed", "mining_fortune", "farming_fortune", "foraging_fortune"};

        for (String key : statKeys) {
            if (memberData.has(key)) {
                profile.getStats().put(capitalize(key.replace("_", " ")), memberData.get(key).getAsDouble());
            }
        }
    }

    private void parseSkills(ProfileData profile, JsonObject memberData) {
        String[] skillKeys = {"experience_skill_farming", "experience_skill_mining",
                "experience_skill_combat", "experience_skill_foraging", "experience_skill_fishing",
                "experience_skill_enchanting", "experience_skill_alchemy", "experience_skill_taming",
                "experience_skill_carpentry", "experience_skill_runecrafting"};

        String[] skillNames = {"Farming", "Mining", "Combat", "Foraging", "Fishing",
                "Enchanting", "Alchemy", "Taming", "Carpentry", "Runecrafting"};

        for (int i = 0; i < skillKeys.length; i++) {
            if (memberData.has(skillKeys[i])) {
                float xp = memberData.get(skillKeys[i]).getAsFloat();
                profile.getSkillXp().put(skillNames[i], xp);
            }
        }
    }

    private void parseCollections(ProfileData profile, JsonObject memberData) {
        if (memberData.has("collection")) {
            JsonObject collections = memberData.getAsJsonObject("collection");
            for (Map.Entry<String, JsonElement> entry : collections.entrySet()) {
                profile.getCollections().put(entry.getKey(), entry.getValue().getAsLong());
            }
        }
    }

    private void parsePets(ProfileData profile, JsonObject memberData) {
        if (memberData.has("pets")) {
            JsonArray petsArray = memberData.getAsJsonArray("pets");
            for (JsonElement elem : petsArray) {
                JsonObject petObj = elem.getAsJsonObject();
                ProfileData.PetData pet = new ProfileData.PetData();

                if (petObj.has("tier")) pet.setRarity(petObj.get("tier").getAsString());
                if (petObj.has("level")) pet.setLevel(petObj.get("level").getAsInt());
                if (petObj.has("xp")) pet.setXp(petObj.get("xp").getAsFloat());
                if (petObj.has("maxXp")) pet.setMaxXp(petObj.get("maxXp").getAsFloat());
                if (petObj.has("heldItem")) pet.setHeldItem(petObj.get("heldItem").getAsString());

                String type = petObj.has("type") ? petObj.get("type").getAsString() : "Unknown";
                pet.setInternalName(type);
                pet.setName(formatPetName(type));

                profile.getPets().add(pet);
            }
        }
    }

    private void parseInventory(ProfileData profile, JsonObject memberData) {
        parseItemArray(memberData, "inv_contents", profile.getInventory());
        parseItemArray(memberData, "inv_armor", profile.getArmor());
        parseItemArray(memberData, "talisman_bag", profile.getAccessories());
        parseItemArray(memberData, "ender_chest_contents", profile.getEnderChest());
    }

    private void parseItemArray(JsonObject memberData, String key, java.util.List<ProfileData.InventoryItem> items) {
        if (!memberData.has(key)) return;
        JsonObject data = memberData.getAsJsonObject(key);
        if (!data.has("data")) return;

        try {
            String encoded = data.get("data").getAsString();
            byte[] bytes = java.util.Base64.getDecoder().decode(encoded);
            parseInventoryBytes(bytes, items);
        } catch (Exception e) {
            LOGGER.debug("Failed to parse inventory data for key: {}", key, e);
        }
    }

    private void parseInventoryBytes(byte[] bytes, java.util.List<ProfileData.InventoryItem> items) {
        try {
            java.io.DataInputStream dataInput = new java.io.DataInputStream(
                    new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(bytes))
            );
            net.minecraft.nbt.CompoundTag rootTag = net.minecraft.nbt.NbtIo.read(dataInput);
            net.minecraft.nbt.ListTag itemList = rootTag.getListOrEmpty("i");
            for (int i = 0; i < itemList.size(); i++) {
                net.minecraft.nbt.CompoundTag itemTag = itemList.getCompoundOrEmpty(i);
                if (itemTag.isEmpty()) continue;

                ProfileData.InventoryItem item = new ProfileData.InventoryItem();
                String id = itemTag.getStringOr("id", "");
                if (!id.isEmpty()) {
                    item.setInternalName(id);
                }
                byte count = itemTag.getByteOr("Count", (byte) 1);
                item.setCount(count);

                net.minecraft.nbt.CompoundTag tag = itemTag.getCompoundOrEmpty("tag");
                if (!tag.isEmpty()) {
                    net.minecraft.nbt.CompoundTag display = tag.getCompoundOrEmpty("display");
                    if (!display.isEmpty()) {
                        String name = display.getStringOr("Name", "");
                        if (!name.isEmpty()) {
                            item.setDisplayName(name);
                        }
                    }
                    net.minecraft.nbt.CompoundTag extra = tag.getCompoundOrEmpty("ExtraAttributes");
                    if (!extra.isEmpty()) {
                        String extraId = extra.getStringOr("id", "");
                        if (!extraId.isEmpty()) {
                            item.setInternalName(extraId);
                        }
                        String rarity = extra.getStringOr("rarity", "");
                        if (!rarity.isEmpty()) {
                            item.setRarity(rarity);
                        }
                    }
                }
                items.add(item);
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse inventory NBT", e);
        }
    }

    private void parseLeveling(ProfileData profile, JsonObject memberData) {
        if (memberData.has("leveling")) {
            JsonObject leveling = memberData.getAsJsonObject("leveling");
            if (leveling.has("experience")) {
                double xp = leveling.get("experience").getAsDouble();
                profile.setSkyblockLevel(calculateSkyblockLevel(xp));
            }
        }
    }

    private int calculateSkyblockLevel(double xp) {
        int level = 0;
        double remaining = xp;
        double[] xpPerLevel = {0, 50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500, 5000};
        for (int i = 1; i < xpPerLevel.length; i++) {
            double needed = xpPerLevel[i] - xpPerLevel[i - 1];
            if (remaining >= needed) {
                remaining -= needed;
                level++;
            } else {
                break;
            }
        }
        if (remaining > 0) {
            level += (int) (remaining / 5000);
        }
        return level;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private String formatPetName(String type) {
        String[] parts = type.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public void clearCache() {
        profileCache.clear();
        cacheTimestamps.clear();
    }
}

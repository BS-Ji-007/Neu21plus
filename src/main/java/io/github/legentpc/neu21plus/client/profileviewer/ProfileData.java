package io.github.legentpc.neu21plus.client.profileviewer;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileData {

    private String playerName;
    private String uuid;
    private String profileName;
    private String profileId;
    private long lastUpdated = 0;

    private final Map<String, Integer> skills = new HashMap<>();
    private final Map<String, Float> skillXp = new HashMap<>();
    private final Map<String, Long> collections = new HashMap<>();
    private final Map<String, Double> stats = new HashMap<>();

    private final List<PetData> pets = new ArrayList<>();
    private final List<InventoryItem> inventory = new ArrayList<>();
    private final List<InventoryItem> armor = new ArrayList<>();
    private final List<InventoryItem> accessories = new ArrayList<>();
    private final List<InventoryItem> enderChest = new ArrayList<>();
    private final List<InventoryItem> storage = new ArrayList<>();

    private double networth = 0;
    private int skyblockLevel = 0;
    private String gameMode = "normal";

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }

    public Map<String, Float> getSkillXp() {
        return skillXp;
    }

    public Map<String, Long> getCollections() {
        return collections;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public List<PetData> getPets() {
        return pets;
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }

    public List<InventoryItem> getArmor() {
        return armor;
    }

    public List<InventoryItem> getAccessories() {
        return accessories;
    }

    public List<InventoryItem> getEnderChest() {
        return enderChest;
    }

    public List<InventoryItem> getStorage() {
        return storage;
    }

    public double getNetworth() {
        return networth;
    }

    public void setNetworth(double networth) {
        this.networth = networth;
    }

    public int getSkyblockLevel() {
        return skyblockLevel;
    }

    public void setSkyblockLevel(int skyblockLevel) {
        this.skyblockLevel = skyblockLevel;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public static class PetData {
        private String name;
        private String internalName;
        private int level;
        private String rarity;
        private float xp;
        private float maxXp;
        private String heldItem;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInternalName() {
            return internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getRarity() {
            return rarity;
        }

        public void setRarity(String rarity) {
            this.rarity = rarity;
        }

        public float getXp() {
            return xp;
        }

        public void setXp(float xp) {
            this.xp = xp;
        }

        public float getMaxXp() {
            return maxXp;
        }

        public void setMaxXp(float maxXp) {
            this.maxXp = maxXp;
        }

        public String getHeldItem() {
            return heldItem;
        }

        public void setHeldItem(String heldItem) {
            this.heldItem = heldItem;
        }
    }

    public static class InventoryItem {
        private String internalName;
        private String displayName;
        private String rarity;
        private int count;
        private JsonObject rawNbt;

        public String getInternalName() {
            return internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getRarity() {
            return rarity;
        }

        public void setRarity(String rarity) {
            this.rarity = rarity;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public JsonObject getRawNbt() {
            return rawNbt;
        }

        public void setRawNbt(JsonObject rawNbt) {
            this.rawNbt = rawNbt;
        }
    }
}

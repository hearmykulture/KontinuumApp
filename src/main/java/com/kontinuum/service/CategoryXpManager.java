package com.kontinuum.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kontinuum.model.ObjectiveCategory;
import com.kontinuum.model.XpTracker;
import com.kontinuum.ui.LevelUpPopup;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class CategoryXpManager {
    private static final int MAX_XP_PER_CATEGORY = 600_000;
    private static final String XP_DATA_FILE = "xp_data.json";

    private final EnumMap<ObjectiveCategory, XpTracker> categoryXp = new EnumMap<>(ObjectiveCategory.class);
    private final EnumMap<ObjectiveCategory, Integer> categoryLevels = new EnumMap<>(ObjectiveCategory.class);

    // Dependency for completed objectives count
    private CategoryCompletionManager completionManager;

    public CategoryXpManager() {
        for (ObjectiveCategory category : ObjectiveCategory.values()) {
            categoryXp.put(category, new XpTracker(MAX_XP_PER_CATEGORY));
            categoryLevels.put(category, 1); // Default to level 1
        }
        loadXpData();
    }

    // Setter for dependency injection
    public void setCompletionManager(CategoryCompletionManager completionManager) {
        this.completionManager = completionManager;
    }

    public LevelUpInfo addXp(ObjectiveCategory category, int xp) {
        XpTracker tracker = categoryXp.get(category);
        if (tracker == null) return null;

        int oldXp = tracker.getXp();
        int oldLevel = calculateLevelForXp(oldXp);

        tracker.addXp(xp);
        int newXp = tracker.getXp();
        int newLevel = calculateLevelForXp(newXp);
        categoryLevels.put(category, newLevel);
        saveXpData();

        if (newLevel > oldLevel) {
            return new LevelUpInfo(category, newLevel, newXp, xp);
        }
        return null;
    }


    public void removeXp(ObjectiveCategory category, int xp) {
        XpTracker tracker = categoryXp.get(category);
        if (tracker != null) {
            tracker.subtractXp(xp);
            categoryLevels.put(category, calculateLevelForXp(tracker.getXp()));
            saveXpData();
        }
    }

    public XpTracker getTracker(ObjectiveCategory category) {
        return categoryXp.get(category);
    }

    public int getCategoryLevel(ObjectiveCategory category) {
        return calculateLevelForXp(getTracker(category).getXp());
    }

    public int getXpToNextLevel(ObjectiveCategory category) {
        int currentXp = getTracker(category).getXp();
        int currentLevel = calculateLevelForXp(currentXp);
        int xpForNext = xpRequiredForLevel(currentLevel + 1);
        return Math.max(0, xpForNext - currentXp);
    }

    public double getCategoryProgressPercentage(ObjectiveCategory category) {
        int currentXp = getTracker(category).getXp();
        int currentLevel = calculateLevelForXp(currentXp);
        int xpForCurrent = xpRequiredForLevel(currentLevel);
        int xpForNext = xpRequiredForLevel(currentLevel + 1);

        return (double) (currentXp - xpForCurrent) / (xpForNext - xpForCurrent);
    }

    public int getTotalLevelCapped() {
        int totalXp = categoryXp.values().stream().mapToInt(XpTracker::getXp).sum();
        int maxTotalXp = MAX_XP_PER_CATEGORY * categoryXp.size();
        double gamma = 2.0;

        double ratio = (double) totalXp / maxTotalXp;
        double rawLevel = 100 * Math.pow(ratio, 1.0 / gamma);
        return (int) Math.round(Math.max(1, Math.min(100, rawLevel)));
    }

    public int getTotalXp() {
        return categoryXp.values().stream().mapToInt(XpTracker::getXp).sum();
    }

    // XP Curve: Level 1 = 0 XP, Level 2 = 100 XP, Level 3 = 250 XP, etc.
    private int calculateLevelForXp(int xp) {
        int level = 1;
        while (xp >= xpRequiredForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    private int xpRequiredForLevel(int level) {
        return (int) Math.round(100 * Math.pow(level - 1, 1.5));
    }

    private void saveXpData() {
        Map<ObjectiveCategory, Integer> xpMap = new EnumMap<>(ObjectiveCategory.class);
        for (Map.Entry<ObjectiveCategory, XpTracker> entry : categoryXp.entrySet()) {
            xpMap.put(entry.getKey(), entry.getValue().getXp());
        }
        try (Writer writer = new FileWriter(XP_DATA_FILE)) {
            new Gson().toJson(xpMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadXpData() {
        File file = new File(XP_DATA_FILE);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<ObjectiveCategory, Integer>>() {}.getType();
            Map<ObjectiveCategory, Integer> xpMap = new Gson().fromJson(reader, type);
            if (xpMap != null) {
                for (Map.Entry<ObjectiveCategory, Integer> entry : xpMap.entrySet()) {
                    XpTracker tracker = categoryXp.get(entry.getKey());
                    if (tracker != null) {
                        tracker.setXp(entry.getValue());
                        categoryLevels.put(entry.getKey(), calculateLevelForXp(entry.getValue()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper class to pass level-up data to UI
    public static class LevelUpInfo {
        public final ObjectiveCategory category;
        public final int newLevel;
        public final int newTotalXp;
        public final int xpGained;

        public LevelUpInfo(ObjectiveCategory category, int newLevel, int newTotalXp, int xpGained) {
            this.category = category;
            this.newLevel = newLevel;
            this.newTotalXp = newTotalXp;
            this.xpGained = xpGained;
        }

    }

}

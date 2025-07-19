package com.kontinuum.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kontinuum.model.ObjectiveCategory;
import com.kontinuum.model.XpTracker;

import java.io.*;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

public class CategoryXpManager {
    private static final int MAX_XP_PER_CATEGORY = 600_000;
    private static final String XP_DATA_FILE = "xp_data.json";

    private final EnumMap<ObjectiveCategory, XpTracker> categoryXp = new EnumMap<>(ObjectiveCategory.class);

    public CategoryXpManager() {
        // Initialize XP trackers for all categories
        for (ObjectiveCategory category : ObjectiveCategory.values()) {
            categoryXp.put(category, new XpTracker(MAX_XP_PER_CATEGORY));
        }
        // Load saved XP data (if exists)
        loadXpData();
    }

    public void addXp(ObjectiveCategory category, int xp) {
        XpTracker tracker = categoryXp.get(category);
        if (tracker != null) {
            tracker.addXp(xp);
            saveXpData();
        }
    }

    public void removeXp(ObjectiveCategory category, int xp) {
        XpTracker tracker = categoryXp.get(category);
        if (tracker != null) {
            tracker.subtractXp(xp);
            saveXpData();
        }
    }

    public XpTracker getTracker(ObjectiveCategory category) {
        return categoryXp.get(category);
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

    // Save the XP data to a JSON file
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

    // Load XP data from JSON file and update trackers
    private void loadXpData() {
        File file = new File(XP_DATA_FILE);
        if (!file.exists()) {
            // No saved file; skip loading
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<ObjectiveCategory, Integer>>() {}.getType();
            Map<ObjectiveCategory, Integer> xpMap = new Gson().fromJson(reader, type);
            if (xpMap != null) {
                for (Map.Entry<ObjectiveCategory, Integer> entry : xpMap.entrySet()) {
                    XpTracker tracker = categoryXp.get(entry.getKey());
                    if (tracker != null) {
                        tracker.setXp(entry.getValue());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

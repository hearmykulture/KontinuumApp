package com.kontinuum.model;

import java.time.LocalDate;
import java.util.List;

public class Mission {
    public String id;
    public String title;
    public MissionTier rarity;
    public int xpReward;
    public int timesCompleted;
    public String category;
    public LocalDate lastCompletedDate;
    public boolean isAccepted;
    public boolean isCompleted;
    public List<String> conditions;

    public Mission() {} // For Jackson

    public Mission(String id, String title, MissionTier rarity, int xpReward, String category, List<String> conditions) {
        this.id = id;
        this.title = title;
        this.rarity = rarity;
        this.xpReward = xpReward;
        this.timesCompleted = 0;
        this.category = category;
        this.lastCompletedDate = null;
        this.isAccepted = false;
        this.isCompleted = false;
        this.conditions = conditions;
    }
}

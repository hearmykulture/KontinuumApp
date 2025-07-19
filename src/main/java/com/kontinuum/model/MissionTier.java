package com.kontinuum.model;

import javafx.scene.paint.Color;

public enum MissionTier {
    COMMON("Common", Color.LIGHTGRAY, 50, 25, 50, "Everyday tasks with low effort."),
    UNCOMMON("Uncommon", Color.GREEN, 30, 50, 100, "More effort, better XP."),
    RARE("Rare", Color.BLUE, 15, 100, 200, "Requires focus and time."),
    VERY_RARE("Very Rare", Color.PURPLE, 4, 200, 400, "High-effort, high-reward."),
    CHALLENGE("Challenge", Color.RED, 1, 500, 1000, "Peak difficulty or willpower.");

    public final String name;
    public final Color color;
    public final int frequency; // % frequency weight
    public final int minXp;
    public final int maxXp;
    public final String description;

    MissionTier(String name, Color color, int frequency, int minXp, int maxXp, String description) {
        this.name = name;
        this.color = color;
        this.frequency = frequency;
        this.minXp = minXp;
        this.maxXp = maxXp;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}

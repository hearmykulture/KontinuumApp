package com.kontinuum.model;

public class XpTracker {
    private int xp;
    private int level;
    private final int maxXp;
    private final int maxLevel = 100;
    private final double gamma = 2.0;

    public XpTracker(int maxXp) {
        this.xp = 0;
        this.maxXp = maxXp;
        this.level = 1;
    }

    public void addXp(int amount) {
        setXp(Math.min(maxXp, xp + amount));
    }

    public void subtractXp(int amount) {
        setXp(Math.max(0, xp - amount));
    }

    public void setXp(int newXp) {
        this.xp = newXp;
        recalculateLevel();
    }

    private void recalculateLevel() {
        double ratio = (double) xp / maxXp;
        double rawLevel = maxLevel * Math.pow(ratio, 1.0 / gamma);
        level = (int) Math.max(1, Math.min(maxLevel, Math.floor(rawLevel)));
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public int getXpToNextLevel() {
        if (level >= maxLevel) return 0;
        return getXpForLevel(level + 1) - xp;
    }

    /**
     * Calculate the XP required to reach a specific level,
     * based on the nonlinear formula used for leveling.
     */
    public int getXpForLevel(int targetLevel) {
        double ratio = (double) targetLevel / maxLevel;
        return (int) (maxXp * Math.pow(ratio, gamma));
    }

}

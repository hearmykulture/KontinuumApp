package com.kontinuum.model;

import java.time.LocalDate;

public class MissedObjectiveTracker {
    private int currentStreak = 0;
    private LocalDate lastEvaluated = null;

    public void evaluateDay(int missedCount, LocalDate date) {
        if (lastEvaluated != null && lastEvaluated.equals(date)) return;

        if (missedCount > 0) currentStreak++;
        else currentStreak = 0;

        lastEvaluated = date;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void reset() {
        currentStreak = 0;
    }
}

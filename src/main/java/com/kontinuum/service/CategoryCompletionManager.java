package com.kontinuum.service;

import com.kontinuum.model.Objective;
import com.kontinuum.model.ObjectiveCategory;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CategoryCompletionManager {
    private final EnumMap<ObjectiveCategory, Integer> completedCounts = new EnumMap<>(ObjectiveCategory.class);

    public CategoryCompletionManager() {
        // Initialize counts to 0 for all categories
        for (ObjectiveCategory category : ObjectiveCategory.values()) {
            completedCounts.put(category, 0);
        }
    }

    // Increment completed count for a category
    public void incrementCompletedCount(ObjectiveCategory category) {
        completedCounts.put(category, completedCounts.getOrDefault(category, 0) + 1);
    }

    // Set completed count for a category (if you want to load/save it)
    public void setCompletedCount(ObjectiveCategory category, int count) {
        completedCounts.put(category, count);
    }

    // Get completed count for a category
    public int getCompletedCount(ObjectiveCategory category) {
        return completedCounts.getOrDefault(category, 0);
    }

    // Reset counts for all categories if needed
    public void reset() {
        for (ObjectiveCategory category : ObjectiveCategory.values()) {
            completedCounts.put(category, 0);
        }
    }

    public void decrementCompletedCount(ObjectiveCategory category) {
        int current = completedCounts.getOrDefault(category, 0);
        if (current > 0) {
            completedCounts.put(category, current - 1);
        }
    }

    public void recalculateFromObjectives(List<Objective> objectives, LocalDate date) {
        reset();
        for (Objective obj : objectives) {
            if (obj.isCompleted(date)) {
                incrementCompletedCount(obj.getCategory());
            }
        }
    }



}

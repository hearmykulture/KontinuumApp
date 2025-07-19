package com.kontinuum.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kontinuum.model.PenaltyService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Objective {
    private String description;
    private int xpReward;
    private ObjectiveCategory category;

    @SerializedName("completedByDate")
    @Expose
    private Map<String, Boolean> completedByDate = new HashMap<>();

    private transient Map<LocalDate, Boolean> completionMap = new HashMap<>();

    // Dependency hook (must be injected in app startup)
    private static PenaltyService penaltyService;

    public Objective() {}

    public Objective(String description, int xpReward, ObjectiveCategory category) {
        this.description = description;
        this.xpReward = xpReward;
        this.category = category;
    }

    public static void setPenaltyService(PenaltyService service) {
        penaltyService = service;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public ObjectiveCategory getCategory() {
        return category;
    }

    public void setCategory(ObjectiveCategory category) {
        this.category = category;
    }

    public boolean isCompleted(LocalDate date) {
        if (completionMap == null || completionMap.isEmpty()) {
            syncFromSerialized();
        }
        return completionMap.getOrDefault(date, false);
    }

    public void setCompleted(LocalDate date, boolean completed) {
        if (penaltyService != null && penaltyService.hasActivePenalties()) {
            System.out.println("❌ Cannot complete objective — active penalties exist.");
            return;
        }

        if (completionMap == null) {
            completionMap = new HashMap<>();
        }
        completionMap.put(date, completed);
        syncToSerialized();
    }

    public void reset() {
        completionMap.clear();
        completedByDate.clear();
    }

    private void syncFromSerialized() {
        completionMap = new HashMap<>();
        if (completedByDate != null) {
            for (Map.Entry<String, Boolean> entry : completedByDate.entrySet()) {
                try {
                    LocalDate key = LocalDate.parse(entry.getKey());
                    completionMap.put(key, entry.getValue());
                } catch (Exception e) {
                    // ignore parse errors
                }
            }
        }
    }

    private void syncToSerialized() {
        completedByDate = new HashMap<>();
        if (completionMap != null) {
            for (Map.Entry<LocalDate, Boolean> entry : completionMap.entrySet()) {
                completedByDate.put(entry.getKey().toString(), entry.getValue());
            }
        }
    }
}

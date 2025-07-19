package com.kontinuum.model;

import java.util.*;

public class PenaltyHistory {

    private final List<Penalty> completedPenalties = new ArrayList<>();
    private final List<Penalty> missedPenalties = new ArrayList<>();

    public void log(Penalty penalty) {
        if (penalty.isCompleted()) {
            completedPenalties.add(penalty);
        } else {
            missedPenalties.add(penalty);
        }
    }

    public List<Penalty> getAllCompleted() {
        return completedPenalties;
    }

    public List<Penalty> getAllMissed() {
        return missedPenalties;
    }

    public int getRepeatCountForType(Penalty.Type type) {
        long count = missedPenalties.stream()
                .filter(p -> p.getType() == type)
                .count();
        return (int) count;
    }
}

package com.kontinuum.model;

import java.util.HashMap;
import java.util.Map;

public class PenaltyTracker {

    private final Map<Penalty.Type, Integer> repeatMap = new HashMap<>();

    public void incrementMissed(Penalty.Type type) {
        repeatMap.put(type, repeatMap.getOrDefault(type, 0) + 1);
    }

    public void reset(Penalty.Type type) {
        repeatMap.put(type, 0);
    }

    public int getRepeatCount(Penalty.Type type) {
        return repeatMap.getOrDefault(type, 0);
    }
}

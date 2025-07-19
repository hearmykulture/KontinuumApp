package com.kontinuum.model;

import java.util.Map;

public class PenaltyRules {

    private static final Map<Penalty.Type, Integer> baseAmounts = Map.of(
            Penalty.Type.PUSHUPS, 100,
            Penalty.Type.JOG, 30,
            Penalty.Type.SQUATS, 50,
            Penalty.Type.PLANK, 2
    );

    public static int calculateAmount(Penalty.Type type, int repeatCount) {
        int base = baseAmounts.getOrDefault(type, 10);
        return base * repeatCount;
    }

    public static PenaltySeverity.Severity determineSeverity(int repeatCount) {
        return PenaltySeverity.fromRepeatCount(repeatCount);
    }
}

package com.kontinuum.model;

public class PenaltySeverity {

    public enum Severity {
        LIGHT,
        MODERATE,
        EXTREME
    }

    public static Severity fromRepeatCount(int repeatCount) {
        if (repeatCount <= 1) return Severity.LIGHT;
        if (repeatCount <= 3) return Severity.MODERATE;
        return Severity.EXTREME;
    }
}

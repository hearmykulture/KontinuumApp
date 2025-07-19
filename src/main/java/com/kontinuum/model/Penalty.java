package com.kontinuum.model;

import java.time.LocalDate;
import java.util.Objects;

public class Penalty {
    public enum Type {
        PUSHUPS, JOG, SQUATS, PLANK
        // Add more penalty types as needed
    }

    private final String id;
    private final LocalDate assignedDate;
    private final LocalDate dueDate;
    private final Type type;
    private final int amount;
    private final int repeatCount;
    private final PenaltySeverity.Severity severity;
    private boolean completed;

    public Penalty(String id, LocalDate assignedDate, LocalDate dueDate, Type type, int amount, int repeatCount, PenaltySeverity.Severity severity) {
        this.id = id;
        this.assignedDate = assignedDate;
        this.dueDate = dueDate;
        this.type = type;
        this.amount = amount;
        this.repeatCount = repeatCount;
        this.severity = severity;
        this.completed = false;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getId() {
        return id;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Type getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public PenaltySeverity.Severity getSeverity() {
        return severity;
    }

    public boolean isOverdue() {
        return !completed && LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return "Penalty{" +
                "id='" + id + '\'' +
                ", assignedDate=" + assignedDate +
                ", dueDate=" + dueDate +
                ", type=" + type +
                ", amount=" + amount +
                ", repeatCount=" + repeatCount +
                ", severity=" + severity +
                ", completed=" + completed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Penalty)) return false;
        Penalty penalty = (Penalty) o;
        return id.equals(penalty.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

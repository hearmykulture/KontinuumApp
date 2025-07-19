package com.kontinuum.model;

import java.time.LocalDate;

public class DailyProgress {
    private LocalDate date;
    private int completedTasks;
    private int totalTasks;

    public DailyProgress(LocalDate date, int completedTasks, int totalTasks) {
        this.date = date;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public double getCompletionRatio() {
        return totalTasks == 0 ? 0 : (double) completedTasks / totalTasks;
    }

    public int getCompletionPercentage() {
        return (int) Math.round(getCompletionRatio() * 100);
    }
}

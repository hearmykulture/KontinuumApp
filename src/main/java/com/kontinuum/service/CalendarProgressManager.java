package com.kontinuum.service;

import com.kontinuum.model.DailyProgress;
import com.kontinuum.model.Objective;

import java.time.LocalDate;
import java.util.*;

public class CalendarProgressManager {
    private final ObjectiveManager objectiveManager;
    private final List<ProgressUpdateListener> listeners = new ArrayList<>();

    public CalendarProgressManager(ObjectiveManager objectiveManager) {
        this.objectiveManager = objectiveManager;
    }

    public void addListener(ProgressUpdateListener listener) {
        listeners.add(listener);
    }

    private void notifyProgressUpdated(LocalDate date) {
        for (ProgressUpdateListener listener : listeners) {
            listener.onProgressUpdated(date);
        }
    }

    public void objectiveStateChanged(Objective objective, LocalDate date) {
        notifyProgressUpdated(date);
    }

    public DailyProgress getProgressForDate(LocalDate date) {
        List<Objective> allObjectives = objectiveManager.getObjectives();
        int total = allObjectives.size();
        int completed = (int) allObjectives.stream().filter(obj -> obj.isCompleted(date)).count();
        return new DailyProgress(date, completed, total);
    }

    public Map<LocalDate, DailyProgress> getWeekProgress(LocalDate startOfWeek) {
        Map<LocalDate, DailyProgress> map = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            map.put(date, getProgressForDate(date));
        }
        return map;
    }
}

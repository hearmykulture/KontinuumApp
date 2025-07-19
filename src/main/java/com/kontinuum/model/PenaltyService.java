package com.kontinuum.model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import com.kontinuum.service.ObjectiveManager;

public class PenaltyService {

    private final Map<String, Penalty> activePenalties = new HashMap<>();
    private final List<Penalty.Type> penaltyOrder = List.of(
            Penalty.Type.PUSHUPS, Penalty.Type.JOG,
            Penalty.Type.SQUATS, Penalty.Type.PLANK
    );

    private final PenaltyHistory history = new PenaltyHistory();
    private final PenaltyTracker tracker = new PenaltyTracker();
    private final MissedObjectiveTracker missedTracker = new MissedObjectiveTracker();

    private ObjectiveManager objectiveManager;

    private LocalDate lastEvaluatedDate = null;

    private static final String EVAL_DATE_FILE = "lastEvaluatedDate.txt";
    private static final String PENALTIES_FILE = "activePenalties.dat";

    public PenaltyService(ObjectiveManager objectiveManager) {
        this.objectiveManager = objectiveManager;
        loadLastEvaluatedDate();
        loadActivePenalties();
    }

    // Persist lastEvaluatedDate to file
    private void saveLastEvaluatedDate() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(EVAL_DATE_FILE))) {
            if (lastEvaluatedDate != null) {
                bw.write(lastEvaluatedDate.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLastEvaluatedDate() {
        File file = new File(EVAL_DATE_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                lastEvaluatedDate = LocalDate.parse(line);
            } catch (Exception e) {
                lastEvaluatedDate = null;
            }
        }
    }

    // Simple serialization of active penalties for persistence (you can improve with Gson or other)
    private void saveActivePenalties() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PENALTIES_FILE))) {
            oos.writeObject(activePenalties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadActivePenalties() {
        File file = new File(PENALTIES_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof Map) {
                    activePenalties.clear();
                    activePenalties.putAll((Map<String, Penalty>) obj);
                }
            } catch (Exception e) {
                // Could not load, start fresh
                activePenalties.clear();
            }
        }
    }

    public Penalty assignPenalty(String penaltyId, Penalty.Type type, LocalDate date) {
        int repeatCount = tracker.getRepeatCount(type);
        int amount = PenaltyRules.calculateAmount(type, repeatCount + 1);
        PenaltySeverity.Severity severity = PenaltyRules.determineSeverity(repeatCount + 1);

        Penalty penalty = new Penalty(penaltyId, date, date.plusDays(1), type, amount, repeatCount + 1, severity);
        activePenalties.put(penaltyId, penalty);
        tracker.incrementMissed(type);
        saveActivePenalties();

        return penalty;
    }

    public void completePenalty(String penaltyId) {
        Penalty penalty = activePenalties.get(penaltyId);
        if (penalty != null) {
            penalty.markCompleted();
            tracker.reset(penalty.getType());
            history.log(penalty);
            activePenalties.remove(penaltyId);
            saveActivePenalties();
        }
    }

    public Collection<Penalty> getActivePenalties() {
        return activePenalties.values();
    }

    // Return true if any active penalty is NOT completed and overdue
    public boolean hasActivePenalties() {
        return activePenalties.values().stream().anyMatch(p -> !p.isCompleted() && p.isOverdue());
    }

    public boolean hasBeenEvaluatedToday(LocalDate today) {
        return lastEvaluatedDate != null && lastEvaluatedDate.equals(today);
    }

    public void markEvaluatedToday(LocalDate today) {
        lastEvaluatedDate = today;
        saveLastEvaluatedDate();
    }

    public Penalty.Type pickPenaltyTypeForObjective(String objectiveId) {
        int index = Math.abs(objectiveId.hashCode()) % penaltyOrder.size();
        return penaltyOrder.get(index);
    }

    public PenaltyHistory getHistory() {
        return history;
    }

    /**
     * Evaluate penalties once per day based on the progress of the **previous day**.
     * Assign penalties for any missed objectives from yesterday.
     */
    public List<Penalty> evaluateDailyPenalty(DailyProgress progress, LocalDate evaluationDate) {
        if (hasBeenEvaluatedToday(evaluationDate)) return Collections.emptyList();

        List<Penalty> assigned = new ArrayList<>();

        int missed = progress.getTotalTasks() - progress.getCompletedTasks();
        missedTracker.evaluateDay(missed, evaluationDate.minusDays(1));
        int streak = missedTracker.getCurrentStreak();

        if (missed <= 0) {
            markEvaluatedToday(evaluationDate);
            return assigned;
        }

        for (int i = 0; i < missed; i++) {
            Penalty.Type type = penaltyOrder.get(i % penaltyOrder.size());
            int repeatCount = tracker.getRepeatCount(type) + streak;
            int amount = PenaltyRules.calculateAmount(type, repeatCount);
            PenaltySeverity.Severity severity = PenaltyRules.determineSeverity(repeatCount);

            Penalty penalty = new Penalty(
                    UUID.randomUUID().toString(),
                    evaluationDate.minusDays(1),
                    evaluationDate,
                    type,
                    amount,
                    repeatCount,
                    severity
            );

            activePenalties.put(penalty.getId(), penalty);
            tracker.incrementMissed(type);
            assigned.add(penalty);
        }

        markEvaluatedToday(evaluationDate);
        saveActivePenalties();
        return assigned;
    }

    /**
     * Call this on app start to evaluate penalties once per day, based on yesterday's objectives.
     */
    public List<Penalty> evaluateYesterdayPenalties() {
        LocalDate today = LocalDate.now();
        if (hasBeenEvaluatedToday(today)) return Collections.emptyList();

        LocalDate yesterday = today.minusDays(1);
        List<Objective> objectives = objectiveManager.getObjectives();
        int completed = 0;

        for (Objective obj : objectives) {
            if (obj.isCompleted(yesterday)) {
                completed++;
            }
        }

        DailyProgress progress = new DailyProgress(yesterday, completed, objectives.size());
        return evaluateDailyPenalty(progress, today);
    }

    public void setObjectiveManager(ObjectiveManager objectiveManager) {
        this.objectiveManager = objectiveManager;
    }

    public List<Penalty> evaluateTodayPenalties() {
        LocalDate today = LocalDate.now();
        if (hasBeenEvaluatedToday(today)) return Collections.emptyList();

        List<Objective> objectives = objectiveManager.getObjectives();
        int completed = 0;

        for (Objective obj : objectives) {
            if (obj.isCompleted(today)) {
                completed++;
            }
        }

        DailyProgress progress = new DailyProgress(today, completed, objectives.size());
        return evaluateDailyPenalty(progress, today);
    }

}

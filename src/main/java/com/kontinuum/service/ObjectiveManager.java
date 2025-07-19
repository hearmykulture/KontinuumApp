package com.kontinuum.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kontinuum.model.*;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ObjectiveManager {
    private List<Objective> objectives = new ArrayList<>();
    private static final String FILE_PATH = "objectives.json";
    private static final String RESET_DATE_FILE = "lastResetDate.txt";
    private LocalDate lastResetDate = LocalDate.now();
    private final PenaltyService penaltyService;

    public ObjectiveManager(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
        loadLastResetDate();
    }

    public void loadObjectives() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Objective>>() {}.getType();
            objectives = gson.fromJson(reader, type);

            // Reset objectives if day changed
            if (!lastResetDate.isEqual(LocalDate.now())) {
                resetObjectives();
                lastResetDate = LocalDate.now();
                saveLastResetDate();
                saveObjectives();

                // Evaluate penalties for yesterday's incomplete objectives
                penaltyService.evaluateYesterdayPenalties();
            }
        } catch (IOException e) {
            objectives = getDefaultObjectives();
            saveObjectives();
        }
    }

    public void saveObjectives() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            Gson gson = new Gson();
            gson.toJson(objectives, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetObjectives() {
        for (Objective obj : objectives) {
            obj.reset();
        }
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public List<Objective> getObjectivesByCategory(ObjectiveCategory category) {
        List<Objective> filtered = new ArrayList<>();
        for (Objective obj : objectives) {
            if (obj.getCategory() == category) {
                filtered.add(obj);
            }
        }
        return filtered;
    }

    public int calculateTotalXP(LocalDate date) {
        return objectives.stream()
                .filter(obj -> obj.isCompleted(date))
                .mapToInt(Objective::getXpReward)
                .sum();
    }

    public boolean canCompleteObjectives() {
        return penaltyService.getActivePenalties().isEmpty();
    }

    private List<Objective> getDefaultObjectives() {
        List<Objective> list = new ArrayList<>();
        list.add(new Objective("Make 1 beat", 50, ObjectiveCategory.PRODUCTION));
        list.add(new Objective("Drink 1 gallon of water", 25, ObjectiveCategory.HEALTH));
        list.add(new Objective("Write 8 bars", 40, ObjectiveCategory.RAPPING));
        list.add(new Objective("Workout", 30, ObjectiveCategory.HEALTH));
        list.add(new Objective("Study a plugin video", 20, ObjectiveCategory.PRODUCTION));
        return list;
    }

    private void loadLastResetDate() {
        File file = new File(RESET_DATE_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                lastResetDate = LocalDate.parse(line);
            } catch (Exception e) {
                lastResetDate = LocalDate.now();
            }
        }
    }

    private void saveLastResetDate() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RESET_DATE_FILE))) {
            bw.write(lastResetDate.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

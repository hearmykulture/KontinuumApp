package com.kontinuum.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kontinuum.model.Mission;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MissionDataStore {
    private static final String MISSIONS_FILE = "missions.json";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Register adapter here
            .setPrettyPrinting()
            .create();

    private static final Type missionListType = new TypeToken<List<Mission>>() {}.getType();

    public static List<Mission> loadMissions() {
        File file = new File(MISSIONS_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, missionListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveMissions(List<Mission> missions) {
        try (FileWriter writer = new FileWriter(MISSIONS_FILE)) {
            gson.toJson(missions, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

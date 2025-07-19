package com.kontinuum.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class MissionMetaDataStore {
    private static final String META_FILE = "mission_meta.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class MetaData {
        public String lastResetDate; // ISO-8601 string, e.g. "2025-07-19"
    }

    public static LocalDate loadLastResetDate() {
        File file = new File(META_FILE);
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            MetaData meta = gson.fromJson(reader, MetaData.class);
            if (meta != null && meta.lastResetDate != null) {
                return LocalDate.parse(meta.lastResetDate);
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveLastResetDate(LocalDate date) {
        MetaData meta = new MetaData();
        meta.lastResetDate = date.toString();
        try (FileWriter writer = new FileWriter(META_FILE)) {
            gson.toJson(meta, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

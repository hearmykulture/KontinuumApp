package com.kontinuum.service;

import com.kontinuum.model.Mission;
import com.kontinuum.model.MissionTier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MissionManager {
    private List<Mission> missions;
    private final Random random = new Random();
    private LocalDate lastResetDate = null;

    private static final int MAX_MISSIONS = 10;
    private static final int MAX_ACCEPTED = 5;

    public MissionManager() {
        this.missions = MissionDataStore.loadMissions();
        this.lastResetDate = loadLastResetDate();
    }

    public List<Mission> getAllMissions() {
        return missions;
    }

    public List<Mission> getAcceptedMissions() {
        return missions.stream()
                .filter(m -> m.isAccepted)
                .collect(Collectors.toList());
    }

    public List<Mission> getAvailableMissions() {
        return missions.stream()
                .filter(m -> !m.isAccepted && !m.isCompleted)
                .collect(Collectors.toList());
    }

    public void acceptMission(Mission mission) {
        if (getAcceptedMissions().size() < MAX_ACCEPTED && !mission.isAccepted && !mission.isCompleted) {
            mission.isAccepted = true;
            save();
        } else {
            // Optionally notify max accepted missions reached
        }
    }

    public void unacceptMission(Mission mission) {
        mission.isAccepted = false;
        save();
    }

    public void completeMission(Mission mission) {
        if (mission.isAccepted && !mission.isCompleted) {
            mission.isCompleted = true;
            mission.isAccepted = false;  // Mark as no longer accepted after completion
            mission.timesCompleted++;
            mission.lastCompletedDate = LocalDate.now();
            save();
        }
    }

    /**
     * Call this once per day (or at app start) to reset mission board if needed.
     * Generates new missions depending on player level and preserves accepted missions.
     */
    public void generateDailyMissions(int playerLevel) {
        LocalDate today = LocalDate.now();

        if (lastResetDate == null || !lastResetDate.equals(today)) {
            resetBoard(playerLevel);
            lastResetDate = today;
            saveLastResetDate(today);
        }
    }

    private void resetBoard(int playerLevel) {
        // Keep accepted missions, remove all others
        missions = missions.stream()
                .filter(m -> m.isAccepted)
                .collect(Collectors.toList());

        int toGenerate = MAX_MISSIONS - missions.size();

        for (int i = 0; i < toGenerate; i++) {
            missions.add(generateRandomMission(playerLevel));
        }

        save();
    }

    private Mission generateRandomMission(int playerLevel) {
        MissionTier tier = rollRarityByLevel(playerLevel);
        String category = pickRandomCategory();
        String title = generateTitle(category, tier);
        int xp = random.nextInt(tier.maxXp - tier.minXp + 1) + tier.minXp;

        return new Mission(
                generateId(),
                title,
                tier,
                xp,
                category,
                new ArrayList<>() // empty conditions for now
        );
    }

    /**
     * Rarity roll adjusted by player level:
     * Higher level -> higher chance for rare tiers, less common tiers
     */
    private MissionTier rollRarityByLevel(int playerLevel) {
        double roll = random.nextDouble();

        // Example adjustment: level 1-100 maps commonThreshold from 0.7 to 0.1
        double commonThreshold = Math.max(0.1, 0.7 - (playerLevel / 150.0));
        double uncommonThreshold = commonThreshold + 0.2;
        double rareThreshold = uncommonThreshold + 0.07;
        double veryRareThreshold = rareThreshold + 0.02;

        if (roll < commonThreshold) return MissionTier.COMMON;
        if (roll < uncommonThreshold) return MissionTier.UNCOMMON;
        if (roll < rareThreshold) return MissionTier.RARE;
        if (roll < veryRareThreshold) return MissionTier.VERY_RARE;
        return MissionTier.CHALLENGE;
    }

    private String pickRandomCategory() {
        String[] categories = {"Fitness", "Social", "Creative", "Focus", "Learning", "Health"};
        return categories[random.nextInt(categories.length)];
    }

    private String generateTitle(String category, MissionTier tier) {
        return switch (category) {
            case "Fitness" -> switch (tier.name()) {
                case "COMMON" -> "Do 20 pushups";
                case "UNCOMMON" -> "Run 1 mile";
                case "RARE" -> "Complete a full workout";
                case "VERY_RARE" -> "5AM Gym Session";
                case "CHALLENGE" -> "Train like a pro for 2 hours";
                default -> "Fitness Task";
            };
            case "Focus" -> switch (tier.name()) {
                case "COMMON" -> "Study for 30 minutes";
                case "UNCOMMON" -> "No distractions for 1 hour";
                case "RARE" -> "Deep focus work 2 hours";
                case "VERY_RARE" -> "Complete 5 focused tasks";
                case "CHALLENGE" -> "8-hour distraction-free day";
                default -> "Focus Task";
            };
            // Add more categories as you want here
            default -> tier.name() + " Task";
        };
    }

    private String generateId() {
        return "mission-" + System.currentTimeMillis() + "-" + random.nextInt(9999);
    }

    private void save() {
        MissionDataStore.saveMissions(missions);
    }

    private LocalDate loadLastResetDate() {
        return MissionMetaDataStore.loadLastResetDate();
    }

    private void saveLastResetDate(LocalDate date) {
        MissionMetaDataStore.saveLastResetDate(date);
    }

    public Duration getTimeUntilReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
        return Duration.between(now, midnight);
    }

    public LocalDate getLastResetDate() {
        return lastResetDate;
    }

}

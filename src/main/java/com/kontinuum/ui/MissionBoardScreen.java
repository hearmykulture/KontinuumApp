package com.kontinuum.ui;

import com.kontinuum.model.Mission;
import com.kontinuum.model.ObjectiveCategory;
import com.kontinuum.service.CategoryXpManager;
import com.kontinuum.service.MissionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class MissionBoardScreen extends BorderPane {

    private final MissionManager missionManager;
    private final CategoryXpManager xpManager;
    private final Consumer<Void> onXpUpdatedCallback;
    private final GridPane missionGrid = new GridPane();
    private final Label countdownLabel = new Label();

    public MissionBoardScreen(MissionManager missionManager, CategoryXpManager xpManager, Consumer<Void> onXpUpdatedCallback) {
        this.missionManager = missionManager;
        this.xpManager = xpManager;
        this.onXpUpdatedCallback = onXpUpdatedCallback;

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #fdf6e3;");

        // Header with countdown timer
        HBox headerBox = new HBox();
        Label header = new Label("Mission Board");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        countdownLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(header, spacer, countdownLabel);

        ScrollPane scrollPane = new ScrollPane(missionGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        setTop(headerBox);
        setCenter(scrollPane);

        refreshMissions();
        startCountdownTimer();
    }

    public void refreshMissions() {
        missionGrid.getChildren().clear();
        missionGrid.setHgap(20);
        missionGrid.setVgap(20);
        missionGrid.setPadding(new Insets(10));
        missionGrid.setAlignment(Pos.CENTER);

        List<Mission> missions = missionManager.getAllMissions();
        int columns = 5;
        int row = 0;
        int col = 0;

        for (Mission mission : missions) {
            VBox poster = createMissionPoster(mission);
            missionGrid.add(poster, col, row);
            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createMissionPoster(Mission mission) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefSize(200, 180);
        box.setStyle("-fx-border-color: #c29d52; -fx-border-width: 2px; -fx-background-color: #fff8dc;" +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        Label title = new Label(mission.title);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5c3b1e;");
        title.setWrapText(true);

        Label category = new Label("Type: " + mission.category);
        Label rarity = new Label("Rarity: " + mission.rarity.name);
        Label reward = new Label("XP: " + mission.xpReward);

        Button acceptBtn = new Button(mission.isAccepted ? "Accepted" : "Accept");
        acceptBtn.setDisable(mission.isAccepted || mission.isCompleted);
        acceptBtn.setOnAction(e -> {
            missionManager.acceptMission(mission);
            refreshMissions();
        });

        Button completeBtn = new Button("Complete");
        completeBtn.setDisable(!mission.isAccepted || mission.isCompleted);
        completeBtn.setOnAction(e -> {
            missionManager.completeMission(mission);
            awardXpForMission(mission);
            refreshMissions();
            onXpUpdatedCallback.accept(null);
        });

        box.getChildren().addAll(title, category, rarity, reward, acceptBtn, completeBtn);
        return box;
    }

    private void awardXpForMission(Mission mission) {
        ObjectiveCategory categoryEnum = mapMissionCategoryToObjectiveCategory(mission.category);
        if (categoryEnum != null) {
            xpManager.addXp(categoryEnum, mission.xpReward);
        } else {
            System.out.println("Unknown category for XP awarding: " + mission.category);
        }
    }

    private ObjectiveCategory mapMissionCategoryToObjectiveCategory(String missionCategory) {
        switch (missionCategory.toLowerCase()) {
            case "fitness":
            case "health":
                return ObjectiveCategory.HEALTH;
            case "learning":
            case "creative":
            case "focus":
                return ObjectiveCategory.PRODUCTION;
            case "social":
                return ObjectiveCategory.RAPPING;
            default:
                return null;
        }
    }

    private void startCountdownTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateCountdown(); // initial call to avoid delay
    }

    private void updateCountdown() {
        LocalDateTime nextResetTime = missionManager.getLastResetDate().plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(nextResetTime)) {
            countdownLabel.setText("Resets soon...");
        } else {
            java.time.Duration duration = java.time.Duration.between(now, nextResetTime);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();

            countdownLabel.setText(String.format("Resets in: %02dh %02dm %02ds", hours, minutes, seconds));
        }
    }
}

package com.kontinuum;

import com.kontinuum.model.Objective;
import com.kontinuum.model.ObjectiveCategory;
import com.kontinuum.model.Penalty;
import com.kontinuum.model.PenaltyService;
import com.kontinuum.service.CategoryXpManager;
import com.kontinuum.service.MissionManager;
import com.kontinuum.service.ObjectiveManager;
import com.kontinuum.service.CalendarProgressManager;
import com.kontinuum.ui.CalendarTopBar;
import com.kontinuum.ui.MissionBoardScreen;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class MainApp extends Application {

    private final CategoryXpManager xpManager = new CategoryXpManager();

    private final PenaltyService penaltyService;
    private final ObjectiveManager objectiveManager;

    {
        // Delayed dependency injection to avoid circular constructor dependency
        penaltyService = new PenaltyService(null); // temp null
        objectiveManager = new ObjectiveManager(penaltyService);
        penaltyService.setObjectiveManager(objectiveManager); // now inject properly
    }

    private final VBox objectivesList = new VBox(10);

    private final Label totalXpLabel = new Label();
    private final Label prodLabel = new Label();
    private final Label rapLabel = new Label();
    private final Label healthLabel = new Label();

    private VBox penaltiesBox = new VBox(5);
    private Button viewPenaltiesButton;

    private LocalDate selectedDate = LocalDate.now();
    private CalendarProgressManager calendarProgressManager;
    private MissionManager missionManager;

    @Override
    public void start(Stage stage) {
        Objective.setPenaltyService(penaltyService);

        objectiveManager.loadObjectives();

        System.out.println("[DEBUG] Starting penalty evaluation for yesterday...");
        penaltyService.evaluateYesterdayPenalties();
        System.out.println("[DEBUG] Penalties after yesterday evaluation: " + penaltyService.getActivePenalties().size());

        calendarProgressManager = new CalendarProgressManager(objectiveManager);

        missionManager = new MissionManager();
        int playerLevel = xpManager.getTotalLevelCapped();
        missionManager.generateDailyMissions(playerLevel);

        System.out.println("[DEBUG] Starting penalty evaluation for today...");
        penaltyService.evaluateTodayPenalties();
        System.out.println("[DEBUG] Penalties after today evaluation: " + penaltyService.getActivePenalties().size());

        CalendarTopBar calendarTopBar = new CalendarTopBar(calendarProgressManager);
        calendarTopBar.setOnDateSelectedListener(date -> {
            selectedDate = date;
            updateObjectives();
            updateXpLabels();
        });

        updateObjectives();
        updateXpLabels();
        updatePenaltiesSummary();

        VBox statsBox = createStatsPanel();
        VBox controlsBox = createXpButtons();
        VBox penaltiesPanel = createPenaltiesPanel();

        VBox rightPanel = new VBox(20, statsBox, controlsBox, penaltiesPanel);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #cccccc; -fx-border-width: 1px;");

        ScrollPane scrollPane = new ScrollPane(objectivesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white;");

        BorderPane progressScreen = new BorderPane();
        progressScreen.setTop(calendarTopBar);
        progressScreen.setCenter(scrollPane);
        progressScreen.setRight(rightPanel);
        progressScreen.setPadding(new Insets(20));

        MissionBoardScreen missionBoardScreen = new MissionBoardScreen(
                missionManager,
                xpManager,
                (v) -> updateXpLabels()
        );

        TabPane tabPane = new TabPane();
        Tab progressTab = new Tab("Progress", progressScreen);
        Tab missionsTab = new Tab("Missions", missionBoardScreen);

        progressTab.setClosable(false);
        missionsTab.setClosable(false);

        tabPane.getTabs().addAll(progressTab, missionsTab);

        Scene scene = new Scene(tabPane, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Kontinuum Tracker");
        stage.show();

        // Run test penalty assignment to verify logic quickly
        testPenaltyAssignment();
    }

    private void testPenaltyAssignment() {
        // Simulate a test date (e.g., 20th of current month)
        LocalDate testDate = LocalDate.now().withDayOfMonth(20);

        // Assume total 5 objectives, 3 completed, so 2 incomplete
        int totalObjectives = 5;
        int completedObjectives = 3;

        // Create DailyProgress object
        var progress = new com.kontinuum.model.DailyProgress(testDate, completedObjectives, totalObjectives);

        System.out.println("[DEBUG TEST] Running penalty evaluation for test date: " + testDate);
        List<Penalty> penalties = penaltyService.evaluateDailyPenalty(progress, testDate);

        if (penalties.isEmpty()) {
            System.out.println("[DEBUG TEST] No penalties assigned.");
        } else {
            for (Penalty p : penalties) {
                System.out.println("[DEBUG TEST] Penalty assigned: " + p.getType() + " x" + p.getAmount() + " (Severity: " + p.getSeverity() + ")");
            }
        }
        System.out.println("[DEBUG TEST] End of test penalty assignment\n");
    }

    private VBox createStatsPanel() {
        Label statsHeader = new Label("XP STATS");
        statsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        return new VBox(8, statsHeader, totalXpLabel, prodLabel, rapLabel, healthLabel);
    }

    private VBox createXpButtons() {
        Label buttonsHeader = new Label("Manual XP Add");
        buttonsHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button addProdXpButton = new Button("➕ 30 XP to Production");
        addProdXpButton.setOnAction(e -> {
            xpManager.addXp(ObjectiveCategory.PRODUCTION, 30);
            updateXpLabels();
        });

        Button addRapXpButton = new Button("➕ 30 XP to Rapping");
        addRapXpButton.setOnAction(e -> {
            xpManager.addXp(ObjectiveCategory.RAPPING, 30);
            updateXpLabels();
        });

        Button addHealthXpButton = new Button("➕ 30 XP to Health");
        addHealthXpButton.setOnAction(e -> {
            xpManager.addXp(ObjectiveCategory.HEALTH, 30);
            updateXpLabels();
        });

        return new VBox(10, buttonsHeader, addProdXpButton, addRapXpButton, addHealthXpButton);
    }

    private VBox createPenaltiesPanel() {
        Label penaltiesHeader = new Label("Active Penalties");
        penaltiesHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        viewPenaltiesButton = new Button("View Active Penalties");
        viewPenaltiesButton.setOnAction(e -> showActivePenaltiesDialog());

        penaltiesBox.getChildren().clear();
        updatePenaltiesSummary();

        VBox box = new VBox(10, penaltiesHeader, penaltiesBox, viewPenaltiesButton);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void updatePenaltiesSummary() {
        penaltiesBox.getChildren().clear();
        int activeCount = penaltyService.getActivePenalties().size();
        Label countLabel = new Label("You have " + activeCount + " active penalty" + (activeCount != 1 ? "ies" : ""));
        penaltiesBox.getChildren().add(countLabel);
    }

    private void showActivePenaltiesDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Active Penalties");

        VBox penaltyList = new VBox(10);
        penaltyList.setPadding(new Insets(10));

        for (Penalty penalty : penaltyService.getActivePenalties()) {
            HBox penaltyItem = new HBox(10);
            penaltyItem.setAlignment(Pos.CENTER_LEFT);

            Label label = new Label(
                    penalty.getType() + ": " + penalty.getAmount() + " (Due: " + penalty.getDueDate() +
                            ", Severity: " + penalty.getSeverity() + (penalty.isOverdue() ? ", OVERDUE" : "") + ")"
            );

            Button completeButton = new Button("Complete");
            completeButton.setOnAction(e -> {
                penaltyService.completePenalty(penalty.getId());
                updatePenaltiesSummary();
                dialog.close();
                showActivePenaltiesDialog();
            });

            penaltyItem.getChildren().addAll(label, completeButton);
            penaltyList.getChildren().add(penaltyItem);
        }

        if (penaltyList.getChildren().isEmpty()) {
            penaltyList.getChildren().add(new Label("No active penalties!"));
        }

        ScrollPane scrollPane = new ScrollPane(penaltyList);
        scrollPane.setPrefSize(400, 300);

        VBox root = new VBox(scrollPane);
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    private void updateObjectives() {
        objectivesList.getChildren().clear();

        for (Objective obj : objectiveManager.getObjectives()) {
            CheckBox checkbox = new CheckBox(obj.getDescription());
            checkbox.setSelected(obj.isCompleted(selectedDate));

            checkbox.setOnAction(e -> {
                boolean nowCompleted = checkbox.isSelected();

                if (penaltyService.hasActivePenalties()) {
                    checkbox.setSelected(false);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Penalties Active");
                    alert.setHeaderText("Can't complete objectives");
                    alert.setContentText("You must complete all penalties before checking off objectives.");
                    alert.showAndWait();
                    return;
                }

                obj.setCompleted(selectedDate, nowCompleted);

                if (nowCompleted) {
                    xpManager.addXp(obj.getCategory(), obj.getXpReward());
                } else {
                    xpManager.removeXp(obj.getCategory(), obj.getXpReward());
                }

                updateXpLabels();
                objectiveManager.saveObjectives();
                calendarProgressManager.objectiveStateChanged(obj, selectedDate);
            });

            objectivesList.getChildren().add(checkbox);
        }
    }

    private void updateXpLabels() {
        int totalXp = xpManager.getTotalXp();

        totalXpLabel.setText("TOTAL XP: " + totalXp +
                " | LEVEL: " + xpManager.getTotalLevelCapped());

        prodLabel.setText("PRODUCTION → XP: " + xpManager.getTracker(ObjectiveCategory.PRODUCTION).getXp()
                + " | LVL: " + xpManager.getTracker(ObjectiveCategory.PRODUCTION).getLevel()
                + " | Next: " + xpManager.getTracker(ObjectiveCategory.PRODUCTION).getXpToNextLevel());

        rapLabel.setText("RAPPING → XP: " + xpManager.getTracker(ObjectiveCategory.RAPPING).getXp()
                + " | LVL: " + xpManager.getTracker(ObjectiveCategory.RAPPING).getLevel()
                + " | Next: " + xpManager.getTracker(ObjectiveCategory.RAPPING).getXpToNextLevel());

        healthLabel.setText("HEALTH → XP: " + xpManager.getTracker(ObjectiveCategory.HEALTH).getXp()
                + " | LVL: " + xpManager.getTracker(ObjectiveCategory.HEALTH).getLevel()
                + " | Next: " + xpManager.getTracker(ObjectiveCategory.HEALTH).getXpToNextLevel());
    }

    public static void main(String[] args) {
        launch();
    }
}

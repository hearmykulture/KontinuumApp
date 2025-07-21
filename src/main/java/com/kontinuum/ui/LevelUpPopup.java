package com.kontinuum.ui;

import com.kontinuum.model.ObjectiveCategory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LevelUpPopup {

    private final Stage stage;

    public LevelUpPopup(ObjectiveCategory category, int newLevel, int newTotalXp, int completedObjectivesCount) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Level Up!");

        Label congratsLabel = new Label("🎉 Level Up! 🎉");
        congratsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label categoryLabel = new Label("Category: " + category.name());
        categoryLabel.setStyle("-fx-font-size: 18px;");

        Label levelLabel = new Label("New Level: " + newLevel);
        levelLabel.setStyle("-fx-font-size: 18px;");

        Label xpLabel = new Label("Total XP: " + newTotalXp);
        xpLabel.setStyle("-fx-font-size: 16px;");

        Label completedLabel = new Label("Objectives completed this level: " + completedObjectivesCount);
        completedLabel.setStyle("-fx-font-size: 16px;");

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        VBox layout = new VBox(15, congratsLabel, categoryLabel, levelLabel, xpLabel, completedLabel, closeButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 350, 300);
        stage.setScene(scene);
    }

    public void show() {
        stage.showAndWait();
    }
}

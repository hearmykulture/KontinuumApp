package com.kontinuum.ui.calendar;

import com.kontinuum.model.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarTopBar extends VBox {
    private final HBox dateRow = new HBox(10);
    private final Label fullDateLabel = new Label();
    private LocalDate currentStartDate = LocalDate.now().minusDays(3);
    private DateCircle selectedCircle = null;

    public CalendarTopBar() {
        setSpacing(10);
        setPadding(new Insets(10));

        // Top section with full date label (no arrows here)
        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);

        fullDateLabel.setFont(new Font(16));
        fullDateLabel.setText(currentStartDate.plusDays(3).format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        topRow.getChildren().add(fullDateLabel);

        // Arrows + dates row
        HBox dateRowWithArrows = new HBox(10);
        dateRowWithArrows.setAlignment(Pos.CENTER);

        DateNavigator navigator = new DateNavigator();
        navigator.leftButton.setOnAction(e -> shiftDates(-1));
        navigator.rightButton.setOnAction(e -> shiftDates(1));

        // Wrap arrow buttons in VBoxes to center vertically and match circle height
        VBox leftArrowContainer = new VBox(navigator.leftButton);
        leftArrowContainer.setAlignment(Pos.CENTER);
        leftArrowContainer.setPrefHeight(60);  // Match circle height
        leftArrowContainer.setPadding(new Insets(6, 0, 0, 0)); // Add top padding to lower arrow

        VBox rightArrowContainer = new VBox(navigator.rightButton);
        rightArrowContainer.setAlignment(Pos.CENTER);
        rightArrowContainer.setPrefHeight(60); // Match circle height
        rightArrowContainer.setPadding(new Insets(6, 0, 0, 0)); // Add top padding to lower arrow

        // Add left arrow, dateRow, right arrow
        dateRowWithArrows.getChildren().addAll(
                leftArrowContainer,
                dateRow,
                rightArrowContainer
        );

        // Date circles container
        dateRow.setAlignment(Pos.CENTER);
        refreshDates();

        getChildren().addAll(topRow, dateRowWithArrows);
    }

    private void refreshDates() {
        dateRow.getChildren().clear();

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentStartDate.plusDays(i);
            String dayAbbrev = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int dayOfMonth = date.getDayOfMonth();

            List<Task> tasksForDate = generateMockTasksForDate(date);

            boolean isSelected = (selectedCircle == null && i == 3)
                    || (selectedCircle != null && selectedCircle.getTasks() == tasksForDate);

            DateCircle circle = new DateCircle(dayAbbrev, dayOfMonth, tasksForDate, isSelected);

            circle.setOnMouseClicked(e -> {
                if (selectedCircle != null) {
                    selectedCircle.setSelected(false);
                }
                circle.setSelected(true);
                selectedCircle = circle;
                fullDateLabel.setText(date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            });

            if (isSelected) {
                selectedCircle = circle;
            }

            dateRow.getChildren().add(circle);
        }
    }

    private void shiftDates(int direction) {
        currentStartDate = currentStartDate.plusDays(direction);
        selectedCircle = null;
        refreshDates();
    }

    private List<Task> generateMockTasksForDate(LocalDate date) {
        List<Task> tasks = new ArrayList<>();
        int taskCount = 3 + (int)(Math.random() * 5);
        for (int i = 1; i <= taskCount; i++) {
            boolean completed = Math.random() > 0.5;
            tasks.add(new Task("Task " + i, completed));
        }
        return tasks;
    }
}

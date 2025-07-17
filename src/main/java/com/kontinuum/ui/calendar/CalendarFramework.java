package com.kontinuum.ui.calendar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.IntStream;

public class CalendarFramework extends VBox {

    private final Label yearLabel = new Label();
    private final Button yearUpButton = new Button("▲");
    private final Button yearDownButton = new Button("▼");
    private final Popup yearDropdown = new Popup();
    private final ListView<Integer> yearListView = new ListView<>();

    private final GridPane monthsGrid = new GridPane();
    private final Label selectedMonthLabel = new Label();
    private final GridPane daysGrid = new GridPane();

    private int selectedYear;
    private int selectedMonth; // 1-based (January=1)

    public CalendarFramework() {
        setSpacing(15);
        setPadding(new Insets(15));
        setPrefWidth(400);
        setStyle("-fx-background-color: #f0f0f0;");

        LocalDate today = LocalDate.now();
        selectedYear = today.getYear();
        selectedMonth = today.getMonthValue();

        createYearSelector();
        createMonthsGrid();
        createSelectedMonthLabel();
        createDaysGrid();

        refreshUI();
    }

    private void createYearSelector() {
        HBox yearBox = new HBox(5);
        yearBox.setAlignment(Pos.CENTER_LEFT);

        yearLabel.setFont(Font.font(20));
        yearLabel.setPrefWidth(100);
        yearLabel.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 5; -fx-background-color: white;");
        yearLabel.setAlignment(Pos.CENTER);

        yearUpButton.setFocusTraversable(false);
        yearDownButton.setFocusTraversable(false);

        yearUpButton.setOnAction(e -> {
            selectedYear++;
            refreshUI();
        });
        yearDownButton.setOnAction(e -> {
            selectedYear--;
            refreshUI();
        });

        // Setup year dropdown list
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int y = selectedYear - 10; y <= selectedYear + 10; y++) {
            years.add(y);
        }
        yearListView.setItems(years);
        yearListView.setPrefSize(100, 150);

        yearListView.setOnMouseClicked(e -> {
            Integer y = yearListView.getSelectionModel().getSelectedItem();
            if (y != null) {
                selectedYear = y;
                yearDropdown.hide();
                refreshUI();
            }
        });

        yearDropdown.getContent().add(yearListView);
        yearDropdown.setAutoHide(true);

        yearLabel.setOnMouseClicked(e -> {
            if (yearDropdown.isShowing()) {
                yearDropdown.hide();
            } else {
                // Position popup under yearLabel
                Point2D point = yearLabel.localToScreen(0, yearLabel.getHeight());
                yearDropdown.show(yearLabel, point.getX(), point.getY());
            }
        });

        yearBox.getChildren().addAll(yearLabel, yearUpButton, yearDownButton);
        getChildren().add(yearBox);
    }

    private void createMonthsGrid() {
        monthsGrid.setHgap(10);
        monthsGrid.setVgap(10);
        monthsGrid.setAlignment(Pos.CENTER_LEFT);

        String[] monthAbbrs = IntStream.rangeClosed(1, 12)
                .mapToObj(m -> LocalDate.of(2000, m, 1).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase())
                .toArray(String[]::new);

        // 6 columns, 2 rows
        for (int i = 0; i < 12; i++) {
            Label monthLabel = new Label(monthAbbrs[i]);
            monthLabel.setPrefWidth(40);
            monthLabel.setPrefHeight(30);
            monthLabel.setFont(Font.font(14));
            monthLabel.setAlignment(Pos.CENTER);
            monthLabel.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: white;");
            final int monthIndex = i + 1;
            monthLabel.setOnMouseClicked(e -> {
                selectedMonth = monthIndex;
                refreshUI();
            });

            monthsGrid.add(monthLabel, i % 6, i / 6);
        }

        getChildren().add(monthsGrid);
    }

    private void createSelectedMonthLabel() {
        selectedMonthLabel.setFont(Font.font(18));
        selectedMonthLabel.setPadding(new Insets(5, 0, 5, 0));
        getChildren().add(selectedMonthLabel);
    }

    private void createDaysGrid() {
        daysGrid.setHgap(5);
        daysGrid.setVgap(5);
        daysGrid.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(daysGrid);
    }

    private void refreshUI() {
        yearLabel.setText(String.valueOf(selectedYear));
        selectedMonthLabel.setText(LocalDate.of(selectedYear, selectedMonth, 1)
                .getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        updateMonthHighlight();
        updateDaysGrid();
        updateYearDropdownItems();
    }

    private void updateMonthHighlight() {
        // Reset all month label styles
        monthsGrid.getChildren().forEach(node -> {
            if (node instanceof Label) {
                node.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: white;");
            }
        });

        // Highlight selected month
        for (var node : monthsGrid.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                int col = GridPane.getColumnIndex(label);
                int row = GridPane.getRowIndex(label);
                int monthNumber = row * 6 + col + 1;
                if (monthNumber == selectedMonth) {
                    label.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: lightblue;");
                }
            }
        }
    }

    private void updateDaysGrid() {
        daysGrid.getChildren().clear();

        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        int totalDays = yearMonth.lengthOfMonth();

        // Optional: add day-of-week headers (Sun, Mon, Tue...)
        String[] weekDays = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> LocalDate.of(2025, 7, i).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .toArray(String[]::new);

        for (int i = 0; i < 7; i++) {
            Label dayOfWeek = new Label(weekDays[i]);
            dayOfWeek.setFont(Font.font(12));
            dayOfWeek.setPrefWidth(30);
            dayOfWeek.setAlignment(Pos.CENTER);
            daysGrid.add(dayOfWeek, i, 0);
        }

        // Start the first day on the correct weekday column
        LocalDate firstOfMonth = LocalDate.of(selectedYear, selectedMonth, 1);
        int startDay = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday=0, Monday=1...

        int dayCounter = 1;
        for (int row = 1; dayCounter <= totalDays; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < startDay) {
                    // empty cell before first day
                    daysGrid.add(new Label(""), col, row);
                } else if (dayCounter <= totalDays) {
                    Label dayLabel = new Label(String.valueOf(dayCounter));
                    dayLabel.setPrefWidth(30);
                    dayLabel.setAlignment(Pos.CENTER);
                    dayLabel.setFont(Font.font(12));
                    dayLabel.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: white;");
                    daysGrid.add(dayLabel, col, row);
                    dayCounter++;
                }
            }
        }
    }

    private void updateYearDropdownItems() {
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int y = selectedYear - 10; y <= selectedYear + 10; y++) {
            years.add(y);
        }
        yearListView.setItems(years);
        yearListView.getSelectionModel().select(Integer.valueOf(selectedYear));
    }

    // For testing/demo usage
    public static void showTestUI(Stage stage) {
        CalendarFramework calendarFramework = new CalendarFramework();
        Scene scene = new Scene(calendarFramework);
        stage.setScene(scene);
        stage.setTitle("Calendar Framework");
        stage.show();
    }
}

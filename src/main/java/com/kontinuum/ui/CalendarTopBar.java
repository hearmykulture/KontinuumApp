package com.kontinuum.ui;

import com.kontinuum.model.DailyProgress;
import com.kontinuum.service.CalendarProgressManager;
import com.kontinuum.service.ProgressUpdateListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CalendarTopBar extends VBox implements ProgressUpdateListener {
    private final CalendarProgressManager progressManager;
    private LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    private LocalDate selectedDate = LocalDate.now();
    private final Label fullDateLabel = new Label();

    private final Map<LocalDate, Double> previousRatios = new HashMap<>();
    private final Map<LocalDate, Circle> dateToProgressCircle = new HashMap<>();
    private Circle selectedProgressCircle;

    private HBox topRow;
    private HBox daysBox;
    private VBox leftCircleBox;
    private StackPane bigProgressStack;

    public CalendarTopBar(CalendarProgressManager progressManager) {
        this.progressManager = progressManager;
        this.progressManager.addListener(this);

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setBackground(new Background(new BackgroundFill(Color.web("#f7f7f7"), CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(10));

        buildInitialUI();
        initializePreviousRatios();
        updateSmallDateCircles();
        updateBigCircle();
    }

    private void buildInitialUI() {
        topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button prev = new Button("<");
        prev.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateSmallDateCircles();
            updateBigCircle();
        });

        Button next = new Button(">");
        next.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateSmallDateCircles();
            updateBigCircle();
        });

        daysBox = new HBox(5);
        daysBox.setAlignment(Pos.CENTER);

        fullDateLabel.setFont(Font.font(14));

        leftCircleBox = new VBox(5);
        leftCircleBox.setAlignment(Pos.CENTER);

        // Create big circle once and keep reference to progressCircle inside it
        bigProgressStack = createProgressCircle(30, 0.0, "0%");
        bigProgressStack.setMaxSize(68, 68);
        bigProgressStack.setPrefSize(68, 68);

        Node bigShape = bigProgressStack.getChildren().get(1);
        if (bigShape instanceof Circle) {
            selectedProgressCircle = (Circle) bigShape;
            double radius = selectedProgressCircle.getRadius();
            double circumference = 2 * Math.PI * radius;
            selectedProgressCircle.getStrokeDashArray().setAll(circumference, circumference);
            selectedProgressCircle.setStrokeDashOffset(circumference);
            selectedProgressCircle.setStroke(Color.LIMEGREEN);
            selectedProgressCircle.setStrokeWidth(6);
            selectedProgressCircle.setFill(Color.TRANSPARENT);
            selectedProgressCircle.setRotate(-90);
            selectedProgressCircle.setStrokeLineCap(StrokeLineCap.ROUND);
        }

        leftCircleBox.getChildren().addAll(fullDateLabel, bigProgressStack);

        topRow.getChildren().addAll(leftCircleBox, prev, daysBox, next);
        getChildren().add(topRow);
    }

    private void initializePreviousRatios() {
        Map<LocalDate, DailyProgress> weekProgress = progressManager.getWeekProgress(currentWeekStart);
        for (LocalDate date : weekProgress.keySet()) {
            DailyProgress dp = weekProgress.get(date);
            previousRatios.putIfAbsent(date, dp.getCompletionRatio());
        }
        if (!previousRatios.containsKey(selectedDate)) {
            DailyProgress dp = progressManager.getProgressForDate(selectedDate);
            previousRatios.put(selectedDate, dp.getCompletionRatio());
        }
    }

    private void updateSmallDateCircles() {
        daysBox.getChildren().clear();
        dateToProgressCircle.clear();

        Map<LocalDate, DailyProgress> weekProgress = progressManager.getWeekProgress(currentWeekStart);

        for (Map.Entry<LocalDate, DailyProgress> entry : weekProgress.entrySet()) {
            LocalDate date = entry.getKey();
            DailyProgress progress = entry.getValue();

            double newRatio = progress.getCompletionRatio();
            previousRatios.put(date, newRatio);  // update immediately, no animation

            VBox dayBox = new VBox(3);
            dayBox.setAlignment(Pos.CENTER);
            dayBox.setPrefWidth(50);

            Label dayLabel = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            dayLabel.setStyle("-fx-font-size: 12px;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);

            StackPane circleStack = createProgressCircle(20, newRatio, String.valueOf(date.getDayOfMonth()));
            circleStack.setMaxSize(44, 44);
            circleStack.setPrefSize(44, 44);

            Node shape = circleStack.getChildren().get(1);
            if (!(shape instanceof Circle)) continue;
            Circle arc = (Circle) shape;

            if (date.equals(selectedDate)) {
                circleStack.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2px; -fx-border-radius: 50%;");
            }

            dateToProgressCircle.put(date, arc);

            circleStack.setOnMouseClicked(e -> {
                selectedDate = date;
                initializePreviousRatios();
                updateSmallDateCircles();
                updateBigCircle();
                if (dateSelectedListener != null) {
                    dateSelectedListener.onDateSelected(selectedDate);
                }
            });

            dayBox.getChildren().addAll(dayLabel, circleStack);
            daysBox.getChildren().add(dayBox);
        }
    }

    private void updateBigCircle() {
        DailyProgress selectedProgress = progressManager.getProgressForDate(selectedDate);
        if (selectedProgress == null || selectedProgressCircle == null) return;

        double currentRatio = selectedProgress.getCompletionRatio();

        fullDateLabel.setText(selectedDate.getMonth() + " " + selectedDate.getDayOfMonth() + ", " + selectedDate.getYear());

        double radius = selectedProgressCircle.getRadius();
        double circumference = 2 * Math.PI * radius;

        double offset = circumference * (1 - currentRatio);

        selectedProgressCircle.setStrokeDashOffset(offset);
        selectedProgressCircle.setStrokeLineCap(currentRatio <= 0.001 ? StrokeLineCap.BUTT : StrokeLineCap.ROUND);

        previousRatios.put(selectedDate, currentRatio);

        if (bigProgressStack.getChildren().size() > 2) {
            Node textNode = bigProgressStack.getChildren().get(2);
            if (textNode instanceof Text) {
                ((Text) textNode).setText(selectedProgress.getCompletionPercentage() + "%");
            }
        }
    }

    @Override
    public void onProgressUpdated(LocalDate date) {
        if (isInCurrentWeek(date)) {
            updateSmallDateCircles();
            if (date.equals(selectedDate)) {
                updateBigCircle();
            }
        } else if (date.equals(selectedDate)) {
            updateBigCircle();
        }
    }

    private boolean isInCurrentWeek(LocalDate date) {
        return !date.isBefore(currentWeekStart) && date.isBefore(currentWeekStart.plusDays(7));
    }

    private StackPane createProgressCircle(double radius, double progressRatio, String centerText) {
        double strokeWidth = 6;
        double circumference = 2 * Math.PI * radius;

        Circle background = new Circle(radius);
        background.setFill(Color.TRANSPARENT);
        background.setStroke(Color.LIGHTGRAY);
        background.setStrokeWidth(strokeWidth);

        Circle progressCircle = new Circle(radius);
        progressCircle.setFill(Color.TRANSPARENT);
        progressCircle.setStroke(Color.LIMEGREEN);
        progressCircle.setStrokeWidth(strokeWidth);
        progressCircle.setRotate(-90); // Start at top
        progressCircle.setStrokeLineCap(progressRatio <= 0.001 ? StrokeLineCap.BUTT : StrokeLineCap.ROUND);
        progressCircle.getStrokeDashArray().setAll(circumference, circumference);
        progressCircle.setStrokeDashOffset(circumference * (1 - progressRatio));

        Text text = new Text(centerText);
        text.setFill(Color.BLACK);
        text.setFont(Font.font(12));

        StackPane stack = new StackPane();
        stack.getChildren().addAll(background, progressCircle, text);
        stack.setPrefSize(radius * 2 + strokeWidth, radius * 2 + strokeWidth);

        return stack;
    }

    public interface DateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    private DateSelectedListener dateSelectedListener;

    public void setOnDateSelectedListener(DateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }
}

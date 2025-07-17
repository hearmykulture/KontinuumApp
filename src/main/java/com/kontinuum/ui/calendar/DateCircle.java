package com.kontinuum.ui.calendar;

import com.kontinuum.model.Task;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class DateCircle extends StackPane {

    private final Canvas canvas;
    private final Label dayLabel;
    private final Label dateLabel;
    private final StackPane circle;
    private final Circle selectionOutline;

    private boolean selected = false;
    private List<Task> tasks = new ArrayList<>();

    public DateCircle(String dayAbbreviation, int dayOfMonth, List<Task> tasks, boolean isSelected) {
        this.tasks = tasks;
        this.selected = isSelected;

        this.canvas = new Canvas(60, 60);
        this.dayLabel = new Label(dayAbbreviation);
        this.dateLabel = new Label(String.valueOf(dayOfMonth));
        this.selectionOutline = new Circle(30);

        dayLabel.setFont(new Font(10));
        dateLabel.setFont(new Font(16));
        dateLabel.setTextFill(Color.BLACK);

        selectionOutline.setFill(Color.TRANSPARENT);
        selectionOutline.setStrokeWidth(2);
        selectionOutline.setStroke(Color.GRAY);
        selectionOutline.setVisible(isSelected);

        this.circle = new StackPane(canvas, dateLabel);
        circle.setPrefSize(60, 60);

        drawProgressCircle();

        setAlignment(Pos.CENTER);

        // Use VBox to stack dayLabel on top of the circle
        VBox vbox = new VBox(4, dayLabel, new StackPane(selectionOutline, circle));
        vbox.setAlignment(Pos.CENTER);

        getChildren().clear();
        getChildren().add(vbox);
    }

    private void drawProgressCircle() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double progress = calculateProgress();

        // Background circle
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(5);
        gc.strokeOval(5, 5, 50, 50);

        // Progress circle
        gc.setStroke(Color.LIMEGREEN);
        gc.setLineWidth(5);
        gc.strokeArc(5, 5, 50, 50, 90, -progress * 360, javafx.scene.shape.ArcType.OPEN);
    }

    private double calculateProgress() {
        if (tasks.isEmpty()) return 0.0;
        long completed = tasks.stream().filter(Task::isCompleted).count();
        return completed / (double) tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        drawProgressCircle();
    }

    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
        selectionOutline.setVisible(isSelected);
    }

    public boolean isSelected() {
        return selected;
    }

    public int getTaskCount() {
        return tasks.size();
    }

    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }

    public List<Task> getTasks() {
        return tasks;
    }
}

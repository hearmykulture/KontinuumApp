package com.kontinuum;

import com.kontinuum.ui.calendar.CalendarFramework;
import com.kontinuum.ui.calendar.CalendarTopBar;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        CalendarTopBar progressDatesBar = new CalendarTopBar();
        CalendarFramework calendarPanel = new CalendarFramework();

        VBox root = new VBox(10); // 10px spacing between components
        root.getChildren().addAll(progressDatesBar, calendarPanel);

        Scene scene = new Scene(root, 600, 600); // Adjust size as needed
        primaryStage.setTitle("Kontinuum Calendar Module");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

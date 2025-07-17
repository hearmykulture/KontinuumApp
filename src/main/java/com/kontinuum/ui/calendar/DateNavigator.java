package com.kontinuum.ui.calendar;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class DateNavigator extends HBox {
    public final Button leftButton = new Button("<");
    public final Button rightButton = new Button(">");

    public DateNavigator() {
        leftButton.setStyle("-fx-font-size: 18;");
        rightButton.setStyle("-fx-font-size: 18;");
        setSpacing(10);
        getChildren().addAll(leftButton, rightButton);
    }
}

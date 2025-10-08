package team13.tetris.view.scene;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import team13.tetris.model.data.Settings;
import team13.tetris.view.SceneManager;

public class SettingsScene {
    private final SceneManager manager;

    public SettingsScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene getScene() {
        Settings settings = manager.getSettings();

        Label title = new Label("Settings");
        title.getStyleClass().add("label-title");

        Button smallBtn = new Button("Small");
        Button mediumBtn = new Button("Medium");
        Button largeBtn = new Button("Large");

        smallBtn.setOnAction(e -> {
            manager.setWindowSize(400, 500);
            settings.setWindowSize("SMALL");
        });
        mediumBtn.setOnAction(e -> {
            manager.setWindowSize(600, 700);
            settings.setWindowSize("MEDIUM");
        });
        largeBtn.setOnAction(e -> {
            manager.setWindowSize(800, 900);
            settings.setWindowSize("LARGE");
        });

        Button keyBtn = new Button("Key Settings");
        keyBtn.setOnAction(e -> manager.showKeySettings());

        ToggleButton colorBlindBtn = new ToggleButton();
        boolean isColorBlind = settings.isColorBlindMode();
        colorBlindBtn.setSelected(isColorBlind);
        colorBlindBtn.setText(isColorBlind ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");
        colorBlindBtn.setOnAction(e -> {
            boolean newState = colorBlindBtn.isSelected();
            colorBlindBtn.setText(newState ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");
            manager.setColorBlindMode(newState);
            settings.setColorBlindMode(newState);
        });

        Button resetBtn = new Button("Reset Scoreboard");
        resetBtn.setOnAction(e -> {
            manager.getScoreBoard().resetScores();
        });

        Button defaultBtn = new Button("Restore Defaults");
        defaultBtn.setOnAction(e -> {
            manager.setColorBlindMode(false);
            manager.setWindowSize(600, 700);
            settings.setColorBlindMode(false);
            settings.setWindowSize("MEDIUM");
            colorBlindBtn.setSelected(false);
            colorBlindBtn.setText("Color Blind Mode: OFF");
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.showMainMenu());

        VBox layout = new VBox(15, title, smallBtn, mediumBtn, largeBtn, keyBtn, colorBlindBtn, resetBtn, defaultBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);
        return scene;
    }
}

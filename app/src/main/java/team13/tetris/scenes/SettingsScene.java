package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;

public class SettingsScene {
    private final SceneManager manager;
    private final Settings settings;
    private final ScoreBoard scoreBoard;

    public SettingsScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
        this.scoreBoard = new ScoreBoard();
    }

    public Scene getScene() {
        Label title = new Label("Settings");
        title.getStyleClass().add("label-title");

        Button smallBtn = new Button("Small");
        Button mediumBtn = new Button("Medium");
        Button largeBtn = new Button("Large");

        smallBtn.setOnAction(
                e -> {
                    manager.setWindowSize(400, 500);
                    settings.setWindowSize("SMALL");
                });
        mediumBtn.setOnAction(
                e -> {
                    manager.setWindowSize(600, 700);
                    settings.setWindowSize("MEDIUM");
                });
        largeBtn.setOnAction(
                e -> {
                    manager.setWindowSize(800, 900);
                    settings.setWindowSize("LARGE");
                });

        Button keyBtn = new Button("Key Settings");
        keyBtn.setOnAction(e -> manager.showKeySettings(settings));

        Button colorBlindBtn = new Button();

        boolean isColorBlind = settings.isColorBlindMode();
        colorBlindBtn.setText(isColorBlind ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");

        colorBlindBtn.setOnAction(
                e -> {
                    boolean currentState = settings.isColorBlindMode();
                    boolean newState = !currentState;
                    colorBlindBtn.setText(
                            newState ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");

                    manager.setColorBlindMode(newState);
                    settings.setColorBlindMode(newState);
                });

        Button resetBtn = new Button("Reset Scoreboard");
        resetBtn.setOnAction(
                e -> {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Reset Scoreboard");
                    confirmAlert.setHeaderText("Are you sure you want to reset all scores?");
                    confirmAlert.setContentText("This action cannot be undone.");

                    confirmAlert
                            .showAndWait()
                            .ifPresent(
                                    response -> {
                                        if (response == ButtonType.OK) {
                                            scoreBoard.resetScores();

                                            Alert successAlert =
                                                    new Alert(Alert.AlertType.INFORMATION);
                                            successAlert.setTitle("Scoreboard Reset");
                                            successAlert.setHeaderText(null);
                                            successAlert.setContentText(
                                                    "Scoreboard has been reset successfully!");
                                            successAlert.showAndWait();

                                            System.out.println("Scoreboard has been reset.");
                                        }
                                    });
                });

        Button defaultBtn = new Button("Restore Defaults");
        defaultBtn.setOnAction(
                e -> {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Restore Defaults");
                    confirmAlert.setHeaderText(
                            "Are you sure you want to restore default settings?");
                    confirmAlert.setContentText("This action cannot be undone.");

                    confirmAlert
                            .showAndWait()
                            .ifPresent(
                                    response -> {
                                        if (response == ButtonType.OK) {
                                            manager.setColorBlindMode(false);
                                            manager.setWindowSize(600, 700);
                                            settings.setColorBlindMode(false);
                                            settings.setWindowSize("MEDIUM");
                                            colorBlindBtn.setText("Color Blind Mode: OFF");
                                            settings.restoreDefaultKeys();

                                            Alert successAlert =
                                                    new Alert(Alert.AlertType.INFORMATION);
                                            successAlert.setTitle(
                                                    "Settings initialization completed");
                                            successAlert.setHeaderText(null);
                                            successAlert.setContentText(
                                                    "Settings have been restored to default successfully!");
                                            successAlert.showAndWait();
                                        }
                                    });
                });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout =
                new VBox(
                        15,
                        title,
                        smallBtn,
                        mediumBtn,
                        largeBtn,
                        keyBtn,
                        colorBlindBtn,
                        resetBtn,
                        defaultBtn,
                        backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}

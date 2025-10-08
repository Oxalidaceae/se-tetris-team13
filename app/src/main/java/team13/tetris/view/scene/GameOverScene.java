package team13.tetris.view.scene;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import team13.tetris.view.SceneManager;

public class GameOverScene {
    private final SceneManager manager;
    private final int finalScore;

    public GameOverScene(SceneManager manager, int finalScore) {
        this.manager = manager;
        this.finalScore = finalScore;
    }

    public Scene getScene() {
        Label title = new Label("Game Over!");
        Label scoreLabel = new Label("Your Score: " + finalScore);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Button saveBtn = new Button("Save Score");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = "Player";
            }
            manager.getScoreBoard().addScore(name, finalScore);
            saveBtn.setDisable(true);
            manager.showScoreboard();
        });

        Button mainMenuBtn = new Button("Main Menu");
        mainMenuBtn.setOnAction(e -> manager.showMainMenu());

        VBox layout = new VBox(15, title, scoreLabel, nameField, saveBtn, mainMenuBtn);
        layout.setStyle("-fx-alignment: center;");
        return new Scene(layout, 600, 700);
    }
}

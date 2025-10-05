package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class GameOverScene {
    private final SceneManager manager;
    private final Settings settings;
    private final int finalScore;

    public GameOverScene(SceneManager manager, Settings settings, int finalScore) {
        this.manager = manager;
        this.settings = settings;
        this.finalScore = finalScore;
    }

    public Scene getScene() {
        Label title = new Label("Game Over!");
        Label scoreLabel = new Label("Your Score: " + finalScore);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Button saveBtn = new Button("Save Score");
        //saveBtn.setOnAction(e -> {
        //    String name = nameField.getText().trim();
        //    if (!name.isEmpty()) {
        //        ScoreRepository.addScore(name, finalScore);
        //        manager.showScoreboard(settings);
        //    }
        //});

        VBox layout = new VBox(15, title, scoreLabel, nameField, saveBtn);
        layout.setStyle("-fx-alignment: center;");
        Scene scene = new Scene(layout, 600, 700);
        
        return scene;
    }
}

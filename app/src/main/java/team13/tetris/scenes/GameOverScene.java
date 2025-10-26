package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class GameOverScene {
    private final SceneManager manager;
    private final Settings settings;
    private final int finalScore;
    private final ScoreBoard.ScoreEntry.Mode difficulty;

    private final ScoreBoard scoreBoard;

    public GameOverScene(SceneManager manager, Settings settings, int finalScore, ScoreBoard.ScoreEntry.Mode difficulty) {
        this.manager = manager;
        this.settings = settings;
        this.finalScore = finalScore;
        this.difficulty = difficulty;
        this.scoreBoard = new ScoreBoard();
    }

    public Scene getScene() {
        Label title = new Label("Game Over!");
        Label scoreLabel = new Label("Your Score: " + finalScore);
        Label difficultyLabel = new Label("Difficulty: " + difficulty.name());

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Label statusLabel = new Label();

        Button saveBtn = new Button("Save Score");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                statusLabel.setText("Please enter your name before saving!");
                return;
            }

            // 점수 저장
            scoreBoard.addScore(name, finalScore, difficulty);
            statusLabel.setText("✅ Score saved successfully!");

            // 저장 후 scoreboard로 이동
             manager.showScoreboard(settings, name, finalScore, difficulty);
        });

        Button backToMenuBtn = new Button("Back to Menu");
        backToMenuBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout = new VBox(15, title, scoreLabel, difficultyLabel, nameField, statusLabel, saveBtn, backToMenuBtn);
        layout.setStyle("-fx-alignment: center;");
        Scene scene = new Scene(layout, 600, 700);

        manager.enableArrowAsTab(scene);

        return scene;
    }
}

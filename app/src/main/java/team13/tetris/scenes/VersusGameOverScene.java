package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class VersusGameOverScene {
    private final SceneManager manager;
    private final Settings settings;
    private final String winner;
    private final int winnerScore;
    private final int loserScore;
    private final boolean timerMode;
    private final boolean itemMode;

    public VersusGameOverScene(
            SceneManager manager,
            Settings settings,
            String winner,
            int winnerScore,
            int loserScore,
            boolean timerMode,
            boolean itemMode) {
        this.manager = manager;
        this.settings = settings;
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
        this.timerMode = timerMode;
        this.itemMode = itemMode;
    }

    public Scene getScene() {
        Label titleLabel = new Label("Game Over");
        titleLabel.getStyleClass().add("label-title");

        Label winnerLabel = new Label(winner + " Wins!");
        winnerLabel.getStyleClass().add("label-title");
        winnerLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 32px;"); // 금색

        Label scoreLabel = new Label(
            winner + " Score: " + winnerScore + "\n" +
            (winner.equals("Player 1") ? "Player 2" : "Player 1") + " Score: " + loserScore
        );
        scoreLabel.getStyleClass().add("label");
        scoreLabel.setStyle("-fx-font-size: 20px;");

        Button retryBtn = new Button("Play Again");
        Button mainMenuBtn = new Button("Main Menu");

        retryBtn.setOnAction(e -> {
            manager.show2PGame(settings, timerMode, itemMode);
        });

        mainMenuBtn.setOnAction(e -> {
            manager.showMainMenu(settings);
        });

        VBox layout = new VBox(20, titleLabel, winnerLabel, scoreLabel, retryBtn, mainMenuBtn);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}

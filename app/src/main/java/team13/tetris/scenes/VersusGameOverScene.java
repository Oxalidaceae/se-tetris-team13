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
    private final String currentPlayer; // 현재 플레이어 ("Player 1" 또는 "Player 2")
    private final boolean isNetworkMode; // 네트워크 모드 여부
    private final Runnable onPlayAgain; // 네트워크 모드에서 Play Again 시 실행할 콜백

    public VersusGameOverScene(
            SceneManager manager,
            Settings settings,
            String winner,
            int winnerScore,
            int loserScore,
            boolean timerMode,
            boolean itemMode) {
        this(manager, settings, winner, winnerScore, loserScore, timerMode, itemMode, "Player 1", false, null);
    }

    // public VersusGameOverScene(
    //         SceneManager manager,
    //         Settings settings,
    //         String winner,
    //         int winnerScore,
    //         int loserScore,
    //         boolean timerMode,
    //         boolean itemMode,
    //         String currentPlayer,
    //         boolean isNetworkMode) {
    //     this(manager, settings, winner, winnerScore, loserScore, timerMode, itemMode, currentPlayer, isNetworkMode, null);
    // }

    public VersusGameOverScene(
            SceneManager manager,
            Settings settings,
            String winner,
            int winnerScore,
            int loserScore,
            boolean timerMode,
            boolean itemMode,
            String currentPlayer,
            boolean isNetworkMode,
            Runnable onPlayAgain) {
        this.manager = manager;
        this.settings = settings;
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
        this.timerMode = timerMode;
        this.itemMode = itemMode;
        this.currentPlayer = currentPlayer != null ? currentPlayer : "Player 1";
        this.isNetworkMode = isNetworkMode;
        this.onPlayAgain = onPlayAgain;
    }

    public Scene getScene() {
        Label titleLabel = new Label("Game Over");
        titleLabel.getStyleClass().add("label-title");

        // 게임 모드에 따라 다른 결과 메시지 생성
        Label resultLabel;
        Label scoreLabel;
        
        if (isNetworkMode) {
            // 네트워크 모드: 플레이어 관점에서 표시
            if (winner.equals("Draw")) {
                resultLabel = new Label("Draw!");
                resultLabel.setStyle("-fx-text-fill: #FFA500; -fx-font-size: 32px;"); // 주황색
            } else if (winner.equals(currentPlayer) || winner.contains("You")) {
                resultLabel = new Label("You Win!");
                resultLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 32px;"); // 금색
            } else {
                resultLabel = new Label("You Lose!");
                resultLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 32px;"); // 빨간색
            }
            
            // 네트워크 모드 점수 표시
            scoreLabel = new Label(
                "Your Score: " + (winner.equals(currentPlayer) || winner.contains("You") ? winnerScore : loserScore) + "\n" +
                "Opponent Score: " + (winner.equals(currentPlayer) || winner.contains("You") ? loserScore : winnerScore)
            );
        } else {
            // 로컬 모드: 기존 방식 (Player # Wins!)
            resultLabel = new Label(winner + " Wins!");
            resultLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 32px;"); // 금색
            
            // 로컬 모드 점수 표시 (항상 Player 1, Player 2 순서)
            int player1Score, player2Score;
            if (winner.equals("Player 1")) {
                player1Score = winnerScore;
                player2Score = loserScore;
            } else {
                player1Score = loserScore;
                player2Score = winnerScore;
            }
            
            scoreLabel = new Label(
                "Player 1 Score: " + player1Score + "\n" +
                "Player 2 Score: " + player2Score
            );
        }
        
        resultLabel.getStyleClass().add("label-title");
        scoreLabel.getStyleClass().add("label");
        scoreLabel.setStyle("-fx-font-size: 20px;");

        Button retryBtn = new Button("Play Again");
        Button mainMenuBtn = new Button("Main Menu");

        if (isNetworkMode && onPlayAgain != null) {
            // 네트워크 모드: Play Again은 로비로 복귀
            retryBtn.setOnAction(e -> onPlayAgain.run());
        } else {
            // 로컬 모드: 기존 동작 유지
            retryBtn.setOnAction(e -> {
                manager.show2PGame(settings, timerMode, itemMode);
            });
        }

        mainMenuBtn.setOnAction(e -> {
            manager.showMainMenu(settings);
        });

        // 네트워크 모드에서는 Main Menu 버튼 숨김
        VBox layout;
        if (isNetworkMode) {
            layout = new VBox(20, titleLabel, resultLabel, scoreLabel, retryBtn);
        } else {
            layout = new VBox(20, titleLabel, resultLabel, scoreLabel, retryBtn, mainMenuBtn);
        }
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}

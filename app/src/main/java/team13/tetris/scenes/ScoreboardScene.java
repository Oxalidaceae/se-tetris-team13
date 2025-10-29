package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

// 점수판 화면
public class ScoreboardScene {
    private final SceneManager manager;
    private final Settings settings;
    private final ScoreBoard scoreBoard;
    private final String highlightName;
    private final Integer highlightScore;
    private final ScoreBoard.ScoreEntry.Mode highlightMode;

    public ScoreboardScene(SceneManager manager, Settings settings) {
        this(manager, settings, null, null, null);
    }

    public ScoreboardScene(SceneManager manager, Settings settings, String highlightName, Integer highlightScore, ScoreBoard.ScoreEntry.Mode highlightMode) {
        this.manager = manager;
        this.settings = settings;
        this.scoreBoard = new ScoreBoard();
        this.highlightName = highlightName;
        this.highlightScore = highlightScore;
        this.highlightMode = highlightMode;
    }

    public Scene getScene() {
        Label title = new Label("Scoreboard");
        title.getStyleClass().add("label-title");

        ListView<String> scoreList = new ListView<>();
        scoreBoard.getGameScores().forEach(entry -> scoreList.getItems().add(String.format("[%s] %s : %d", entry.getMode().name(), entry.getName(), entry.getScore())));
        scoreList.setMaxHeight(300);

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout;

        if (highlightName != null && highlightScore != null && highlightMode != null) {
            Button exitBtn = new Button("Exit");
            exitBtn.setOnAction(e -> manager.showExitScene(settings, () -> manager.showScoreboard(settings, highlightName, highlightScore, highlightMode)));
            layout = new VBox(15, title, scoreList, backBtn, exitBtn);
        } else {
            layout = new VBox(15, title, scoreList, backBtn);
        }

        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        setupListNavigation(scoreList, backBtn);
        applyHighlight(scoreList);

        return scene;
    }

    // 리스트 키보드 네비게이션 설정
    private void setupListNavigation(ListView<String> scoreList, Button backBtn) {
        scoreList.setOnKeyPressed(e -> {
            int selected = scoreList.getSelectionModel().getSelectedIndex();
            int lastIndex = scoreList.getItems().size() - 1;

            if (e.getCode() == javafx.scene.input.KeyCode.DOWN && selected == lastIndex) {
                backBtn.requestFocus();
                e.consume();
            }

            if (e.getCode() == javafx.scene.input.KeyCode.UP && selected == -1) {
                scoreList.getSelectionModel().selectLast();
                e.consume();
            }
        });
    }

    // 새로 저장된 점수 강조 표시
    private void applyHighlight(ListView<String> scoreList) {
        if (highlightName != null && highlightScore != null && highlightMode != null) {
            String formatted = String.format("[%s] %s : %d", highlightMode.name(), highlightName, highlightScore);
            for (int i = 0; i < scoreList.getItems().size(); i++) {
                if (scoreList.getItems().get(i).equals(formatted)) {
                    scoreList.getSelectionModel().select(i);
                    scoreList.scrollTo(i);
                    break;
                }
            }
        }
    }
}

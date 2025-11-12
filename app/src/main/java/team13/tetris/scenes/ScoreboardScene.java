package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

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

    // 하이라이트용 생성자
    public ScoreboardScene(SceneManager manager, Settings settings,
                           String highlightName, Integer highlightScore, ScoreBoard.ScoreEntry.Mode highlightMode) {
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
        scoreBoard.getGameScores().forEach(entry ->
            scoreList.getItems().add(String.format("[%s] %s : %d", 
                entry.getMode().name(), entry.getName(), entry.getScore()))
        );

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout;
        
        if (highlightName != null && highlightScore != null && highlightMode != null) {
            Button exitBtn = new Button("Exit");
            exitBtn.setOnAction(e -> manager.showConfirmScene(settings,"Exit Game?", () -> manager.exitWithSave(settings), () -> manager.showScoreboard(settings, highlightName, highlightScore, highlightMode)));
            layout = new VBox(15, title, scoreList, backBtn, exitBtn);
        } 
        else {
            layout = new VBox(15, title, scoreList, backBtn);
        }

        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        setupListNavigation(scoreList, backBtn);
        applyHighlight(scoreList);

        return scene;
    }

    private void setupListNavigation(ListView<String> scoreList, Button backBtn) {
        scoreList.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            int selected = scoreList.getSelectionModel().getSelectedIndex();
            int lastIndex = scoreList.getItems().size() - 1;

            // 마지막 항목에서 아래 방향키 → Back 버튼 이동
            if (e.getCode() == javafx.scene.input.KeyCode.DOWN && selected == lastIndex ) {
                scoreList.getSelectionModel().clearSelection();
                backBtn.requestFocus();
                e.consume();
                
            }

            // 첫번째 항목에서 위 방향키 → Back 버튼 이동
            if (e.getCode() == javafx.scene.input.KeyCode.UP && (selected == -1 || selected == 0)) {
                scoreList.getSelectionModel().clearSelection();
                backBtn.requestFocus();
                e.consume();
            }
        });

        // Back 버튼에서 위 방향키 → 리스트의 마지막 항목으로 이동
        backBtn.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.UP) {
                scoreList.requestFocus();
                scoreList.getSelectionModel().select(scoreList.getItems().size() - 1);
                scoreList.scrollTo(scoreList.getItems().size() - 1);
                e.consume();
            }
        });
    }

    private void applyHighlight(ListView<String> scoreList) {
        if (highlightName != null && highlightScore != null && highlightMode != null) {
            String formatted = String.format("[%s] %s : %d",
                    highlightMode.name(), highlightName, highlightScore);
                    
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

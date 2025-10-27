package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
        // 타이틀
        Label title = new Label("Scoreboard");
        title.getStyleClass().add("label-title");

        // 점수 리스트
        ListView<String> scoreList = new ListView<>();
        scoreBoard.getNormalGameScores().forEach(entry ->
            scoreList.getItems().add(String.format("[%s] %s : %d", 
                entry.getMode().name(), entry.getName(), entry.getScore()))
        );
        scoreList.setMaxHeight(300);

        // 뒤로가기 버튼
        Button backBtn = new Button("Back to Main Menu");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        // 레이아웃
        VBox layout;
        
        // 하이라이트 생성자가 호출된 경우에만 추가 버튼 표시
        if (highlightName != null && highlightScore != null && highlightMode != null) {
            Button exitBtn = new Button("Exit");
            exitBtn.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Exit Confirmation");
                confirmAlert.setHeaderText("Are you sure you want to exit?");
                confirmAlert.setContentText("");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if(response == ButtonType.OK)
                        manager.exitWithSave(settings);
                });
            });
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

    // 리스트와 버튼 간 키보드 내비게이션 설정
    private void setupListNavigation(ListView<String> scoreList, Button backBtn) {
        scoreList.setOnKeyPressed(e -> {
            int selected = scoreList.getSelectionModel().getSelectedIndex();
            int lastIndex = scoreList.getItems().size() - 1;

            // ↓ 키: 마지막 항목 → Back 버튼 이동
            if (e.getCode() == javafx.scene.input.KeyCode.DOWN && selected == lastIndex) {
                backBtn.requestFocus();
                e.consume();
            }

            // ↑ 키: 아무 선택 없음 → 마지막 항목 선택
            if (e.getCode() == javafx.scene.input.KeyCode.UP && selected == -1) {
                scoreList.getSelectionModel().selectLast();
                e.consume();
            }
        });
    }

    // 하이라이트 적용
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

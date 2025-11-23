package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;

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
    public ScoreboardScene(
            SceneManager manager,
            Settings settings,
            String highlightName,
            Integer highlightScore,
            ScoreBoard.ScoreEntry.Mode highlightMode) {
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
        scoreBoard
                .getGameScores()
                .forEach(
                        entry ->
                                scoreList
                                        .getItems()
                                        .add(
                                                String.format(
                                                        "[%s] %s : %d",
                                                        entry.getMode().name(),
                                                        entry.getName(),
                                                        entry.getScore())));

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout;

        Button exitBtn = null;
        if (highlightName != null && highlightScore != null && highlightMode != null) {
            exitBtn = new Button("Exit");
            exitBtn.setOnAction(
                    e ->
                            manager.showConfirmScene(
                                    settings,
                                    "Exit Game?",
                                    () -> manager.exitWithSave(settings),
                                    () ->
                                            manager.showScoreboard(
                                                    settings,
                                                    highlightName,
                                                    highlightScore,
                                                    highlightMode)));
            layout = new VBox(15, title, scoreList, backBtn, exitBtn);
        } else {
            layout = new VBox(15, title, scoreList, backBtn);
        }

        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        setupListNavigation(scoreList, backBtn, exitBtn);
        applyHighlight(scoreList);

        // 초기 포커스 설정: 스코어보드가 비어있으면 Back 버튼, 아니면 첫 번째 엔트리
        if (scoreList.getItems().isEmpty()) {
            backBtn.requestFocus();
        } else {
            scoreList.requestFocus();
            scoreList.getSelectionModel().select(0);
        }

        return scene;
    }

    private void setupListNavigation(ListView<String> scoreList, Button backBtn, Button exitBtn) {
        scoreList.addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                e -> {
                    int selected = scoreList.getSelectionModel().getSelectedIndex();
                    int lastIndex = scoreList.getItems().size() - 1;

                    // 아래 방향키로 Back 버튼 이동: 마지막 항목이 선택된 경우
                    if (e.getCode() == javafx.scene.input.KeyCode.DOWN
                            && (selected >= 0 && selected == lastIndex)) {
                        scoreList.getSelectionModel().clearSelection();
                        backBtn.requestFocus();
                        e.consume();
                    }

                    // 첫번째 항목에서 위 방향키 → Exit 버튼 (하이라이트 모드) 또는 Back 버튼 이동
                    if (e.getCode() == javafx.scene.input.KeyCode.UP
                            && (selected == -1 || selected == 0)) {
                        scoreList.getSelectionModel().clearSelection();
                        if (exitBtn != null) {
                            exitBtn.requestFocus();
                        } else {
                            backBtn.requestFocus();
                        }
                        e.consume();
                    }
                });

        // Back 버튼에 이벤트 필터 추가하여 키 네비게이션 제어
        backBtn.addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                e -> {
                    if (e.getCode() == javafx.scene.input.KeyCode.UP) {
                        if (!scoreList.getItems().isEmpty()) {
                            // 위 방향키 → 리스트의 마지막 항목으로 이동
                            scoreList.requestFocus();
                            scoreList.getSelectionModel().select(scoreList.getItems().size() - 1);
                            scoreList.scrollTo(scoreList.getItems().size() - 1);
                        }
                        e.consume(); // 리스트가 비어있어도 이벤트 소비하여 포커스 유지
                    } else if (e.getCode() == javafx.scene.input.KeyCode.DOWN) {
                        if (exitBtn != null) {
                            // Exit 버튼이 있으면 Exit 버튼으로 이동
                            exitBtn.requestFocus();
                            e.consume();
                        } else if (!scoreList.getItems().isEmpty()) {
                            // Exit 버튼이 없고 리스트에 항목이 있으면 첫 번째 항목으로 이동
                            scoreList.requestFocus();
                            scoreList.getSelectionModel().select(0);
                            scoreList.scrollTo(0);
                            e.consume();
                        }
                    }
                });

        // Exit 버튼에서 키 네비게이션 (Exit 버튼이 있을 때만)
        if (exitBtn != null) {
            exitBtn.addEventFilter(
                    javafx.scene.input.KeyEvent.KEY_PRESSED,
                    e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.UP) {
                            // 위 방향키 → Back 버튼으로 이동
                            backBtn.requestFocus();
                            e.consume();
                        } else if (e.getCode() == javafx.scene.input.KeyCode.DOWN) {
                            // 아래 방향키 → 리스트의 첫 번째 항목으로 이동 (하이라이트 모드에서는 항상 엔트리 존재)
                            scoreList.requestFocus();
                            scoreList.getSelectionModel().select(0);
                            scoreList.scrollTo(0);
                            e.consume();
                        }
                    });
        }
    }

    private void applyHighlight(ListView<String> scoreList) {
        if (highlightName != null && highlightScore != null && highlightMode != null) {
            String formatted =
                    String.format(
                            "[%s] %s : %d", highlightMode.name(), highlightName, highlightScore);

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

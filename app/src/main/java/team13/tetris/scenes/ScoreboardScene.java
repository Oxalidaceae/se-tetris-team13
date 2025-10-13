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

    public ScoreboardScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
        this.scoreBoard = new ScoreBoard();
    }
    
    public Scene getScene() {
        // 타이틀
        Label title = new Label("Scoreboard");
        title.getStyleClass().add("label-title");

        // 점수 리스트
        ListView<String> scoreList = new ListView<>();
        scoreBoard.getScores().forEach(entry ->
            scoreList.getItems().add(String.format("%s : %d", entry.getName(), entry.getScore()))
        );
        scoreList.setMaxHeight(250);

        // 뒤로가기 버튼
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

         // 레이아웃
        VBox layout = new VBox(15, title, scoreList, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        manager.enableArrowAsTab(scene);
        return scene;
    }
}

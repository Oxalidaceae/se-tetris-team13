package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainMenuScene {
    private final SceneManager manager;
    private final Settings settings;

    public MainMenuScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {

        Label title = new Label("TETRIS");
        title.getStyleClass().add("label-title");

        Button startBtn = new Button("Start Game");
        Button optionBtn = new Button("Options");
        Button scoreBtn = new Button("Scoreboard");
        Button exitBtn = new Button("Exit");

        // 게임오버시 화면 전환 테스트용 버튼
        Button testGameOverBtn = new Button("Test Game Over");
        testGameOverBtn.setOnAction(e ->
            manager.changeScene(new GameOverScene(manager, settings, 12345, team13.tetris.data.ScoreBoard.ScoreEntry.Mode.NORMAL).getScene())
        );

        startBtn.setOnAction(e -> manager.showDifficultySelection(settings));
        optionBtn.setOnAction(e -> manager.showSettings(settings));
        scoreBtn.setOnAction(e -> manager.showScoreboard(settings));
        exitBtn.setOnAction(e -> manager.exitWithSave(settings));

        VBox layout = new VBox(10, title, startBtn, optionBtn, scoreBtn, exitBtn, testGameOverBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        manager.enableArrowAsTab(scene);
        return scene;
    }
    
}

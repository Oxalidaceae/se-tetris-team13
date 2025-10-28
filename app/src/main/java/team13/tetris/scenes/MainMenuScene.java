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
        Button itemModeBtn = new Button("Start Item Mode");
        Button optionBtn = new Button("Options");
        Button scoreBtn = new Button("Scoreboard");
        Button exitBtn = new Button("Exit");

        startBtn.setOnAction(e -> manager.showDifficultySelection(settings));
        itemModeBtn.setOnAction(e -> manager.showGame(settings, team13.tetris.data.ScoreBoard.ScoreEntry.Mode.ITEM));
        optionBtn.setOnAction(e -> manager.showSettings(settings));
        scoreBtn.setOnAction(e -> manager.showScoreboard(settings));
        exitBtn.setOnAction(e -> manager.exitWithSave(settings));

        VBox layout = new VBox(10, title, startBtn, itemModeBtn, optionBtn, scoreBtn, exitBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        manager.enableArrowAsTab(scene);
        return scene;
    }
    
}

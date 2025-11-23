package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

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

        startBtn.setOnAction(e -> manager.showGameModeSelection(settings));
        optionBtn.setOnAction(e -> manager.showSettings(settings));
        scoreBtn.setOnAction(e -> manager.showScoreboard(settings));
        exitBtn.setOnAction(
                e ->
                        manager.showConfirmScene(
                                settings,
                                "Exit Game?",
                                () -> manager.exitWithSave(settings),
                                () -> manager.showMainMenu(settings)));

        VBox layout = new VBox(10, title, startBtn, optionBtn, scoreBtn, exitBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}

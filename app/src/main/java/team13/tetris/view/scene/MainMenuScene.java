package team13.tetris.view.scene;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.view.SceneManager;

public class MainMenuScene {
    private final SceneManager manager;

    public MainMenuScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene getScene() {
        Label title = new Label("TETRIS");
        title.getStyleClass().add("label-title");

        Button startBtn = new Button("Start Game");
        Button optionBtn = new Button("Options");
        Button scoreBtn = new Button("Scoreboard");
        Button exitBtn = new Button("Exit");
        Button testGameOverBtn = new Button("Test Game Over");

        startBtn.setOnAction(e -> manager.showGame());
        optionBtn.setOnAction(e -> manager.showSettings());
        scoreBtn.setOnAction(e -> manager.showScoreboard());
        exitBtn.setOnAction(e -> manager.exitWithSave());
        testGameOverBtn.setOnAction(e -> manager.showGameOver(12345));

        VBox layout = new VBox(10, title, startBtn, optionBtn, scoreBtn, exitBtn, testGameOverBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);
        return scene;
    }
}

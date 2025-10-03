package org.example.scenes;

import org.example.SceneManager;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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

        VBox layout = new VBox(10, title, startBtn, optionBtn, scoreBtn, exitBtn);
        layout.setStyle("-fx-alignment: center;");

        // 예: 옵션 버튼 클릭 → SettingsScene으로 이동
        startBtn.setOnAction(e -> manager.changeScene(new GameScene(manager).getScene()));
        optionBtn.setOnAction(e -> manager.changeScene(new SettingsScene(manager).getScene()));
        scoreBtn.setOnAction(e -> manager.changeScene(new ScoreboardScene(manager).getScene()));
        exitBtn.setOnAction(e -> Platform.exit()); //e -> System.exit(0)?

        Scene scene = new Scene(layout, 400, 400);
        scene.getStylesheets().add(
            getClass().getResource("/application.css").toExternalForm()
        );

        return scene;
    }
    
}

package org.example.scenes;

import org.example.SceneManager;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class ScoreboardScene {
private final SceneManager manager;

    public ScoreboardScene(SceneManager manager) {
        this.manager = manager;
    }
    
    public Scene getScene() {
        // 타이틀
        Label title = new Label("Scoreboard");
        title.getStyleClass().add("label-title");

        // 점수 리스트 (임시 더미 데이터)
        ListView<String> scoreList = new ListView<>();
        scoreList.getItems().addAll(
                "AAA - 1000",
                "BBB - 800",
                "CCC - 600",
                "DDD - 400",
                "EEE - 200"
        );
        scoreList.setMaxHeight(200);

        // 뒤로가기 버튼
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.changeScene(new MainMenuScene(manager).getScene()));

         // 레이아웃
        VBox layout = new VBox(15, title, scoreList, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 400);

        scene.getStylesheets().add(
            getClass().getResource("/application.css").toExternalForm()
        );

        return scene;
    }
}

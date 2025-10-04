package org.example.scenes;

import org.example.SceneManager;
import org.example.config.Settings;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class ScoreboardScene {
private final SceneManager manager;
private final Settings settings;

    public ScoreboardScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }
    
    public Scene getScene() {
        // 타이틀
        Label title = new Label("Scoreboard");
        title.getStyleClass().add("label-title");

        // 점수 리스트 (임시 더미 데이터)
        ListView<String> scoreList = new ListView<>();
        scoreList.getItems().addAll(
                "AAA  score",
                "BBB  score",
                "CCC  score",
                "DDD  score",
                "EEE  score"
        );
        scoreList.setMaxHeight(200);

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

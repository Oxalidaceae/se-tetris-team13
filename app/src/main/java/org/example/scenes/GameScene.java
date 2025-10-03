package org.example.scenes;

import org.example.SceneManager;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameScene {
    private final SceneManager manager;
    private Label scoreLabel;
    private Label boardLabel;

    public GameScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene getScene() {
        // 상단 점수 표시
        scoreLabel = new Label("Score: 0");
        scoreLabel.getStyleClass().add("label");

        // 게임 보드 영역 (임시 Placeholder)
        boardLabel = new Label("[Game Board Here]");
        boardLabel.setMinSize(200, 400);
        boardLabel.setStyle(
                "-fx-border-color: white; -fx-border-width: 2; -fx-alignment: center;"
        );

        // 오른쪽 패널 (다음 블럭 표시 + 뒤로가기 버튼)
        Label nextBlockLabel = new Label("Next Block");
        nextBlockLabel.getStyleClass().add("label");

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.changeScene(new MainMenuScene(manager).getScene()));

        VBox rightPanel = new VBox(15, nextBlockLabel, backBtn);
        rightPanel.setStyle("-fx-alignment: top-center;");

        // 전체 레이아웃
        BorderPane layout = new BorderPane();
        layout.setTop(new HBox(scoreLabel));
        layout.setCenter(boardLabel);
        layout.setRight(rightPanel);

        Scene scene = new Scene(layout, 600, 500);

        // CSS 연결
        scene.getStylesheets().add(
            getClass().getResource("/application.css").toExternalForm()
        );

        return scene;
    }

    // 나중에 1번, 4번 모듈이 호출할 메서드
    public void updateBoard(Object board) {
        // TODO: 1번 담당이 만든 Board 데이터를 받아서 화면 갱신
        boardLabel.setText("[Updated Board]");
    }
    public void updateScore(int score) { 
        scoreLabel.setText("Score: " + score);
    }
}

package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class LocalMultiModeSelectionScene {
    private final SceneManager manager;
    private final Settings settings;

    public LocalMultiModeSelectionScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Local Multi Mode");
        title.getStyleClass().add("label-title");

        Button normalBtn = new Button("Normal");
        Button itemBtn = new Button("Item Mode");
        Button timerBtn = new Button("Timer Mode");
        Button backBtn = new Button("Back");

        // 일반 모드 (대전 모드)
        normalBtn.setOnAction(e -> manager.show2PGame(settings, false, false));

        // 아이템 모드 (대전 + 아이템)
        itemBtn.setOnAction(e -> manager.show2PGame(settings, false, true));

        // 타이머 모드 (2분 시간제한 대전)
        timerBtn.setOnAction(e -> manager.show2PGame(settings, true, false));

        backBtn.setOnAction(e -> manager.showMultiModeSelection(settings));

        VBox layout = new VBox(15, title, normalBtn, itemBtn, timerBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}

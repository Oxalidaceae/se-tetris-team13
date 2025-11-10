package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class P2PModeSelectionScene {
    private final SceneManager manager;
    private final Settings settings;

    public P2PModeSelectionScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Online Multi Mode");
        title.getStyleClass().add("label-title");

        Button normalBtn = new Button("Normal");
        Button itemBtn = new Button("Item Mode");
        Button timerBtn = new Button("Timer Mode");
        Button backBtn = new Button("Back");

        // 모두 미구현
        normalBtn.setOnAction(e -> manager.showNotImplemented());
        itemBtn.setOnAction(e -> manager.showNotImplemented());
        timerBtn.setOnAction(e -> manager.showNotImplemented());
        
        backBtn.setOnAction(e -> manager.showMultiModeSelection(settings));

        VBox layout = new VBox(15, title, normalBtn, itemBtn, timerBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}

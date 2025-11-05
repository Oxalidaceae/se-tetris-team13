package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class ExitScene {
    private final SceneManager manager;
    private final Settings settings;
    private final Runnable onCancel; // 취소 시 실행할 동작

    public ExitScene(SceneManager manager, Settings settings, Runnable onCancel) {
        this.manager = manager;
        this.settings = settings;
        this.onCancel = onCancel;
    }

    public Scene getScene() {
        Label title = new Label("Exit Game?");
        title.getStyleClass().add("label-title");

        Button confirmBtn = new Button("Confirm");
        Button cancelBtn = new Button("Cancel");

        confirmBtn.setOnAction(e -> manager.exitWithSave(settings));
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) {
                onCancel.run();
            }
        });

        HBox buttonBox = new HBox(20, confirmBtn, cancelBtn);
        buttonBox.setStyle("-fx-alignment: center;");
        
        VBox layout = new VBox(20, title, buttonBox);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        return scene;
    }
}

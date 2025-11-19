package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class ConfirmScene {
    // 사용하지 않는 필드이지만 향후 확장을 위해 보존
    @SuppressWarnings("unused")
    private final SceneManager manager;
    @SuppressWarnings("unused")
    private final Settings settings;
    private final String title;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmScene(SceneManager manager, Settings settings, String title, Runnable onConfirm, Runnable onCancel) {
        this.manager = manager;
        this.settings = settings;
        this.title = title;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public Scene getScene() {
        Label title = new Label(this.title);
        title.getStyleClass().add("label-title");

        Button confirmBtn = new Button("Confirm");
        Button cancelBtn = new Button("Cancel");

        // confirmBtn.setOnAction(e -> manager.exitWithSave(settings));
        confirmBtn.setOnAction(e -> {
            if (onConfirm != null) onConfirm.run();
        });
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        HBox buttonBox = new HBox(20, confirmBtn, cancelBtn);
        buttonBox.setStyle("-fx-alignment: center;");
        
        VBox layout = new VBox(20, title, buttonBox);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);

        return scene;
    }
}

package team13.tetris.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import team13.tetris.config.Settings;

public class KeyInputHandler {

    private final Settings settings;
    private KeyInputCallback callback;

    public KeyInputHandler(Settings settings) {
        this.settings = settings;
    }

    public void attachToScene(Scene scene, KeyInputCallback callback) {
        this.callback = callback;
        scene.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        if (callback == null) return;

        KeyCode keyCode = event.getCode();

        if (isLeftClicked(keyCode)) {
            callback.onLeftPressed();
        } else if (isRightClicked(keyCode)) {
            callback.onRightPressed();
        } else if (isRotateClicked(keyCode)) {
            callback.onRotatePressed();
        } else if (isDropClicked(keyCode)) {
            callback.onDropPressed();
        } else if (isHardDropClicked(keyCode)) {
            callback.onHardDropPressed();
        } else if (isPauseClicked(keyCode)) {
            callback.onPausePressed();
        }
    }

    private boolean isKeyMatch(KeyCode userPressed, String configuredKey) {
        if (configuredKey == null || configuredKey.trim().isEmpty()) return false;

        String userKey = userPressed.toString();
        String settingKey = configuredKey.trim().toUpperCase();

        return userKey.equals(settingKey);
    }

    public boolean isLeftClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyLeft());
    }

    public boolean isRightClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyRight());
    }

    public boolean isDropClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyDown());
    }

    public boolean isRotateClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyRotate());
    }

    public boolean isHardDropClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyDrop());
    }

    public boolean isPauseClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getPause());
    }

    public interface KeyInputCallback {
        void onLeftPressed();
        void onRightPressed();
        void onRotatePressed();
        void onDropPressed();
        void onHardDropPressed();
        void onPausePressed();
    }
}

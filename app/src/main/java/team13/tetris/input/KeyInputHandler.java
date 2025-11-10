package team13.tetris.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import team13.tetris.config.Settings;
import java.util.Set;
import java.util.HashSet;

public class KeyInputHandler {
    private final Settings settings;
    private KeyInputCallback callback;
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public KeyInputHandler(Settings settings) {
        this.settings = settings;
    }

    public void attachToScene(Scene scene, KeyInputCallback callback) {
        this.callback = callback;
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);
    }

    private void handleKeyPress(KeyEvent event) {
        if (callback == null) return;

        KeyCode keyCode = event.getCode();
        
        // 키 상태 추가 (키 반복 허용)
        pressedKeys.add(keyCode);

        // else if 제거하여 동시 입력 지원
        if (isLeftClicked(keyCode)) {
            callback.onLeftPressed();
        }
        if (isRightClicked(keyCode)) {
            callback.onRightPressed();
        }
        if (isRotateClicked(keyCode)) {
            callback.onRotatePressed();
        }
        if (isDropClicked(keyCode)) {
            callback.onDropPressed();
        }
        if (isHardDropClicked(keyCode)) {
            callback.onHardDropPressed();
        }
        if (isPauseClicked(keyCode)) {
            callback.onPausePressed();
        }
    }
    
    private void handleKeyRelease(KeyEvent event) {
        // 키를 뗄 때 상태에서 제거
        pressedKeys.remove(event.getCode());
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

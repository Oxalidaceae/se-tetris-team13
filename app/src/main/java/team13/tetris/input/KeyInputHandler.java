// 경로: src/main/team13/tetris/input/KeyInputHandler.java
package team13.tetris.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import team13.tetris.config.Settings;

/**
 * 키보드 입력을 처리하는 핸들러 클래스
 * Settings에 정의된 키 매핑을 기반으로 사용자 입력을 해석
 */
public class KeyInputHandler {

    private final Settings settings;
    private KeyInputCallback callback;

    public KeyInputHandler(Settings settings) {
        this.settings = settings;
    }

    /**
     * Scene에 키 이벤트 핸들러를 등록
     * 
     * @param scene    키 입력을 받을 Scene
     * @param callback 키 입력 시 호출될 콜백 인터페이스
     */
    public void attachToScene(Scene scene, KeyInputCallback callback) {
        this.callback = callback;
        scene.setOnKeyPressed(this::handleKeyPress);
    }

    // 키 입력 이벤트를 처리하고 적절한 콜백 메서드를 호출
    private void handleKeyPress(KeyEvent event) {
        if (callback == null) {
            return;
        }

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

    // 키 매칭 헬퍼 메서드
    private boolean isKeyMatch(KeyCode userPressed, String configuredKey) {
        if (configuredKey == null || configuredKey.trim().isEmpty()) {
            return false;
        }

        String userKey = userPressed.toString();
        String settingKey = configuredKey.trim().toUpperCase();

        // KeyCode.toString() 결과는 대문자로 반환되므로 대문자로 비교
        return userKey.equals(settingKey);
    }

    // 왼쪽 이동
    public boolean isLeftClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyLeft());
    }

    // 오른쪽 이동
    public boolean isRightClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyRight());
    }

    // 아래쪽 이동/소프트 드롭
    public boolean isDropClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyDown());
    }

    // 회전
    public boolean isRotateClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyRotate());
    }

    // 하드 드롭
    public boolean isHardDropClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getKeyDrop());
    }

    // 일시정지
    public boolean isPauseClicked(KeyCode userPressedKey) {
        return isKeyMatch(userPressedKey, settings.getPause());
    }

    // 키 입력 이벤트를 처리하기 위한 콜백 인터페이스
    public interface KeyInputCallback {
        void onLeftPressed();

        void onRightPressed();

        void onRotatePressed();

        void onDropPressed();

        void onHardDropPressed();

        void onPausePressed();
    }
}

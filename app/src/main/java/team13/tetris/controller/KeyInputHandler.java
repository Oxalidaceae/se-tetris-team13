// 경로: src/main/team13/tetris/input/KeyInputHandler.java
package team13.tetris.controller;

import javafx.scene.input.KeyCode;
import team13.tetris.model.data.Settings;

public class KeyInputHandler {

  private final Settings settings;

  public KeyInputHandler(Settings settings) {
    this.settings = settings;
  }

  // 키 매칭 헬퍼 메서드
  private boolean isKeyMatch(KeyCode userPressed, String configuredKey) {
    if (configuredKey == null || configuredKey.trim().isEmpty()) {
      return false;
    }

    String userKey = userPressed.toString();
    String settingKey = configuredKey.trim();

    // 대소문자 차이로 인한 문제를 방지하기 위해 대소문자를 무시하고 비교
    return userKey.equalsIgnoreCase(settingKey);
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

  // 게임 종료
  public boolean isEscClicked(KeyCode userPressedKey) {
    return isKeyMatch(userPressedKey, settings.getExit());
  }
}
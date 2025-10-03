// 경로: src/main/team13/tetris/input/KeyInputHandler.java
package team13.tetris.input;

import javafx.scene.input.KeyCode;
import team13.tetris.config.PlayerCommand;
import team13.tetris.config.KeySettings;

public class KeyInputHandler {

  private final KeySettings keySettings;

  public KeyInputHandler(KeySettings keySettings) {
    this.keySettings = keySettings;
  }

  // default: '왼쪽 방향키'
  public boolean isLeftClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.MOVE_LEFT);
  }

  // default: '오른쪽 방향키'
  public boolean isRightClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.MOVE_RIGHT);
  }

  // default: '아래쪽 방향키'
  public boolean isDownClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.SOFT_DROP);
  }

  // default: '위쪽 방향키'
  public boolean isUpClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.MOVE_UP);
  }

  // default: 'Z' 키
  public boolean isRotateClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.ROTATE);
  }

  // default: 'X' 키
  public boolean isHardDropClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.HARD_DROP);
  }

  // default: 'P' 키
  public boolean isPauseClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.PAUSE);
  }

  // default: 'ESC' 키
  public boolean isEscClicked(KeyCode userPressedKey) {
    return userPressedKey == keySettings.getKeyForAction(PlayerCommand.EXIT);
  }
}
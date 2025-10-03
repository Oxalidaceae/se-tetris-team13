package team13.tetris.config;

import javafx.scene.input.KeyCode;
import java.util.HashMap;
import java.util.Map;

// 게임의 키 설정을 관리하는 클래스
// PlayerCommand와 KeyCode를 Map으로 연결하여 저장

public class KeySettings {

  private final Map<PlayerCommand, KeyCode> keyMap;

  public KeySettings() {
    this.keyMap = new HashMap<>();
    setDefaultKeys();
  }

  // 게임의 기본 키 설정을 초기화
  public final void setDefaultKeys() {
    keyMap.put(PlayerCommand.MOVE_LEFT, KeyCode.LEFT);
    keyMap.put(PlayerCommand.MOVE_RIGHT, KeyCode.RIGHT);
    keyMap.put(PlayerCommand.SOFT_DROP, KeyCode.DOWN);
    keyMap.put(PlayerCommand.MOVE_UP, KeyCode.UP);
    keyMap.put(PlayerCommand.ROTATE, KeyCode.Z);
    keyMap.put(PlayerCommand.HARD_DROP, KeyCode.X);
    keyMap.put(PlayerCommand.PAUSE, KeyCode.P);
    keyMap.put(PlayerCommand.EXIT, KeyCode.ESCAPE);
  }

  // 특정 명령에 설정된 키코드를 반환
  public KeyCode getKeyForAction(PlayerCommand command) {
    return keyMap.get(command);
  }

  // 특정 명령의 키를 새로 설정합니다. (설정 메뉴 구현 시 사용)
  public void setKeyForAction(PlayerCommand command, KeyCode newKey) {
    keyMap.put(command, newKey);
  }
}
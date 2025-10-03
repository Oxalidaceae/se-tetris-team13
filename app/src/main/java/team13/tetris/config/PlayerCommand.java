package team13.tetris.config;

// 사용자가 게임에 내릴 수 있는 모든 명령(Command)을 정의하는 열거형
// KeySettings와 KeyInputHandler에서 키를 구분하는 기준으로 사용
public enum PlayerCommand {
  MOVE_LEFT,
  MOVE_RIGHT,
  SOFT_DROP,
  MOVE_UP,
  ROTATE,
  HARD_DROP,
  PAUSE,
  EXIT,
}
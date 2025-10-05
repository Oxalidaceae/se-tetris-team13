package team13.tetris.input;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javafx.scene.input.KeyCode;
import team13.tetris.config.Settings;

@DisplayName("KeyInputHandler 테스트")
class KeyInputHandlerTest {

  private Settings settings;
  private KeyInputHandler keyInputHandler;

  @BeforeEach
  void setUp() {
    settings = new Settings();
    keyInputHandler = new KeyInputHandler(settings);
  }

  @Test
  @DisplayName("기본 WASD 키 매칭 테스트")
  void testWASDKeyMatching() {
    // given - 기본 설정값 사용
    settings.setKeyLeft("A");
    settings.setKeyRight("D");
    settings.setKeyDown("S");
    settings.setKeyRotate("W");

    // when & then
    assertTrue(keyInputHandler.isLeftClicked(KeyCode.A));
    assertFalse(keyInputHandler.isLeftClicked(KeyCode.D));

    assertTrue(keyInputHandler.isRightClicked(KeyCode.D));
    assertFalse(keyInputHandler.isRightClicked(KeyCode.A));

    assertTrue(keyInputHandler.isDropClicked(KeyCode.S));
    assertTrue(keyInputHandler.isRotateClicked(KeyCode.W));
  }

  @Test
  @DisplayName("특수 키 매칭 테스트")
  void testSpecialKeyMatching() {
    // given
    settings.setKeyDrop("SPACE");
    settings.setExit("ESCAPE");
    settings.setPause("P");

    // when & then
    assertTrue(keyInputHandler.isHardDropClicked(KeyCode.SPACE));
    assertFalse(keyInputHandler.isHardDropClicked(KeyCode.ENTER));

    assertTrue(keyInputHandler.isEscClicked(KeyCode.ESCAPE));
    assertFalse(keyInputHandler.isEscClicked(KeyCode.SPACE));

    assertTrue(keyInputHandler.isPauseClicked(KeyCode.P));
    assertFalse(keyInputHandler.isPauseClicked(KeyCode.Q));
  }

  @Test
  @DisplayName("숫자 키 매칭 테스트")
  void testDigitKeyMatching() {
    // given
    settings.setKeyLeft("DIGIT1");
    settings.setKeyRight("NUMPAD5");

    // when & then
    assertTrue(keyInputHandler.isLeftClicked(KeyCode.DIGIT1));
    assertFalse(keyInputHandler.isLeftClicked(KeyCode.DIGIT2));

    assertTrue(keyInputHandler.isRightClicked(KeyCode.NUMPAD5));
    assertFalse(keyInputHandler.isRightClicked(KeyCode.NUMPAD1));
  }

  @Test
  @DisplayName("방향키 매칭 테스트")
  void testArrowKeyMatching() {
    // given
    settings.setKeyLeft("LEFT");
    settings.setKeyRight("RIGHT");
    settings.setKeyDown("DOWN");
    settings.setKeyRotate("UP");

    // when & then
    assertTrue(keyInputHandler.isLeftClicked(KeyCode.LEFT));
    assertTrue(keyInputHandler.isRightClicked(KeyCode.RIGHT));
    assertTrue(keyInputHandler.isDropClicked(KeyCode.DOWN));
    assertTrue(keyInputHandler.isRotateClicked(KeyCode.UP));
  }

  @Test
  @DisplayName("펑션 키 매칭 테스트")
  void testFunctionKeyMatching() {
    // given
    settings.setKeyRotate("F1");
    settings.setPause("F2");

    // when & then
    assertTrue(keyInputHandler.isRotateClicked(KeyCode.F1));
    assertTrue(keyInputHandler.isPauseClicked(KeyCode.F2));
    assertFalse(keyInputHandler.isRotateClicked(KeyCode.F2));
  }

  @Test
  @DisplayName("잘못된 키 매칭 테스트")
  void testWrongKeyMatching() {
    // given
    settings.setKeyLeft("A");
    settings.setKeyRight("D");

    // when & then - 다른 키를 누르면 false
    assertFalse(keyInputHandler.isLeftClicked(KeyCode.B));
    assertFalse(keyInputHandler.isLeftClicked(KeyCode.SPACE));
    assertFalse(keyInputHandler.isRightClicked(KeyCode.F));
    assertFalse(keyInputHandler.isRightClicked(KeyCode.ENTER));
  }

  @Test
  @DisplayName("키 중복 설정 테스트")
  void testDuplicateKeySettings() {
    // given - 같은 키를 여러 액션에 설정
    settings.setKeyLeft("A");
    settings.setKeyRight("A");

    // when & then - 둘 다 true가 되어야 함
    assertTrue(keyInputHandler.isLeftClicked(KeyCode.A));
    assertTrue(keyInputHandler.isRightClicked(KeyCode.A));
  }

  @Test
  @DisplayName("현재 settings.json과 동일한 설정 테스트")
  void testCurrentJsonSettings() {
    // given - 현재 settings.json과 동일한 설정
    settings.setKeyLeft("E");
    settings.setKeyRight("D");
    settings.setKeyDown("S");
    settings.setKeyRotate("W");
    settings.setKeyDrop("X");
    settings.setPause("P");
    settings.setExit("ESC");

    // when & then
    assertTrue(keyInputHandler.isLeftClicked(KeyCode.E));
    assertTrue(keyInputHandler.isRightClicked(KeyCode.D));
    assertTrue(keyInputHandler.isDropClicked(KeyCode.S));
    assertTrue(keyInputHandler.isRotateClicked(KeyCode.W));
    assertTrue(keyInputHandler.isHardDropClicked(KeyCode.X));
    assertTrue(keyInputHandler.isPauseClicked(KeyCode.P));

    // ESC 주의: settings.json에서는 "ESC"이지만 KeyCode는 ESCAPE
    assertFalse(keyInputHandler.isEscClicked(KeyCode.ESCAPE)); // 매칭되지 않음
    // 올바른 설정이라면:
    settings.setExit("ESCAPE");
    assertTrue(keyInputHandler.isEscClicked(KeyCode.ESCAPE));
  }
}
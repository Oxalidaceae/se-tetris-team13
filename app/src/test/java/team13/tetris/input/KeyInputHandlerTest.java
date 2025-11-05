package team13.tetris.input;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javafx.scene.input.KeyCode;
import team13.tetris.config.Settings;

// KeyInputHandler 테스트: Tests keyboard input mapping, key matching logic
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
    @DisplayName("기본 설정값으로 키 매칭 테스트")
    void testDefaultKeyMatching() {
        // given - Settings의 기본값 사용 (LEFT, RIGHT, DOWN, Z, X, P, ESCAPE)
        Settings defaultSettings = new Settings();
        defaultSettings.restoreDefaultKeys();
        KeyInputHandler handler = new KeyInputHandler(defaultSettings);

        // when & then - 방향키와 Z, X, P, ESCAPE가 기본 키
        assertTrue(handler.isLeftClicked(KeyCode.LEFT), "LEFT 키가 왼쪽 이동과 매칭되어야 함");
        assertTrue(handler.isRightClicked(KeyCode.RIGHT), "RIGHT 키가 오른쪽 이동과 매칭되어야 함");
        assertTrue(handler.isDropClicked(KeyCode.DOWN), "DOWN 키가 소프트 드롭과 매칭되어야 함");
        assertTrue(handler.isRotateClicked(KeyCode.Z), "Z 키가 회전과 매칭되어야 함");
        assertTrue(handler.isHardDropClicked(KeyCode.X), "X 키가 하드 드롭과 매칭되어야 함");
        assertTrue(handler.isPauseClicked(KeyCode.P), "P 키가 일시정지와 매칭되어야 함");

        // WASD는 기본 키가 아님
        assertFalse(handler.isLeftClicked(KeyCode.A), "A 키는 기본 키가 아님");
        assertFalse(handler.isRightClicked(KeyCode.D), "D 키는 기본 키가 아님");
        assertFalse(handler.isDropClicked(KeyCode.S), "S 키는 기본 키가 아님");
        assertFalse(handler.isRotateClicked(KeyCode.W), "W 키는 기본 키가 아님");
    }

    @Test
    @DisplayName("WASD 키로 변경 후 매칭 테스트")
    void testWASDKeyMatching() {
        // given - WASD 설정
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");

        // when & then
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "A 키가 왼쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.D), "D 키는 왼쪽 이동과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isRightClicked(KeyCode.D), "D 키가 오른쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.A), "A 키는 오른쪽 이동과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isDropClicked(KeyCode.S), "S 키가 소프트 드롭과 매칭되어야 함");
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.W), "W 키가 회전과 매칭되어야 함");
    }

    @Test
    @DisplayName("특수 키 매칭 테스트")
    void testSpecialKeyMatching() {
        // given
        settings.setKeyDrop("SPACE");
        settings.setPause("P");

        // when & then
        assertTrue(keyInputHandler.isHardDropClicked(KeyCode.SPACE),
                "SPACE 키가 하드 드롭과 매칭되어야 함");
        assertFalse(keyInputHandler.isHardDropClicked(KeyCode.ENTER),
                "ENTER 키는 하드 드롭과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isPauseClicked(KeyCode.P),
                "P 키가 일시정지와 매칭되어야 함");
        assertFalse(keyInputHandler.isPauseClicked(KeyCode.Q),
                "Q 키는 일시정지와 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("숫자 키 매칭 테스트")
    void testDigitKeyMatching() {
        // given
        settings.setKeyLeft("DIGIT1");
        settings.setKeyRight("NUMPAD5");

        // when & then
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.DIGIT1),
                "DIGIT1 키가 왼쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.DIGIT2),
                "DIGIT2 키는 왼쪽 이동과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isRightClicked(KeyCode.NUMPAD5),
                "NUMPAD5 키가 오른쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.NUMPAD1),
                "NUMPAD1 키는 오른쪽 이동과 매칭되지 않아야 함");
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
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.LEFT), "LEFT 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.RIGHT), "RIGHT 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isDropClicked(KeyCode.DOWN), "DOWN 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.UP), "UP 키가 매칭되어야 함");
    }

    @Test
    @DisplayName("펑션 키 매칭 테스트")
    void testFunctionKeyMatching() {
        // given
        settings.setKeyRotate("F1");
        settings.setPause("F2");

        // when & then
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.F1), "F1 키가 회전과 매칭되어야 함");
        assertTrue(keyInputHandler.isPauseClicked(KeyCode.F2), "F2 키가 일시정지와 매칭되어야 함");
        assertFalse(keyInputHandler.isRotateClicked(KeyCode.F2),
                "F2 키는 회전과 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("잘못된 키 매칭 테스트")
    void testWrongKeyMatching() {
        // given
        settings.setKeyLeft("A");
        settings.setKeyRight("D");

        // when & then - 다른 키를 누르면 false
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.B), "B 키는 매칭되지 않아야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.SPACE), "SPACE 키는 매칭되지 않아야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.F), "F 키는 매칭되지 않아야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.ENTER), "ENTER 키는 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("키 중복 설정 테스트")
    void testDuplicateKeySettings() {
        // given - 같은 키를 여러 액션에 설정
        settings.setKeyLeft("A");
        settings.setKeyRight("A");

        // when & then - 둘 다 true가 되어야 함
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "A 키가 왼쪽 이동과 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.A), "A 키가 오른쪽 이동과도 매칭되어야 함");
    }

    @Test
    @DisplayName("대소문자 구분 없이 매칭되는지 테스트")
    void testCaseInsensitiveMatching() {
        // given - 소문자로 설정
        settings.setKeyLeft("a");
        settings.setKeyRight("d");
        settings.setKeyDown("s");

        // when & then - KeyCode는 대문자이므로 isKeyMatch에서 대문자로 변환하여 매칭
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "소문자 'a' 설정이 KeyCode.A와 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.D), "소문자 'd' 설정이 KeyCode.D와 매칭되어야 함");
        assertTrue(keyInputHandler.isDropClicked(KeyCode.S), "소문자 's' 설정이 KeyCode.S와 매칭되어야 함");
    }

    @Test
    @DisplayName("null 또는 빈 문자열 설정 테스트")
    void testNullOrEmptyKeySettings() {
        // given - null 또는 빈 문자열 설정
        settings.setKeyLeft(null);
        settings.setKeyRight("");
        settings.setKeyDown("  "); // 공백만

        // when & then - 모두 false를 반환해야 함
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.A), "null 설정은 어떤 키와도 매칭되지 않아야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.D), "빈 문자열 설정은 어떤 키와도 매칭되지 않아야 함");
        assertFalse(keyInputHandler.isDropClicked(KeyCode.S), "공백만 있는 설정은 어떤 키와도 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("공백이 포함된 키 설정 테스트")
    void testKeySettingsWithWhitespace() {
        // given - 앞뒤 공백이 포함된 설정
        settings.setKeyLeft(" LEFT ");
        settings.setKeyRight("  RIGHT  ");
        settings.setKeyDown("\tDOWN\t");

        // when & then - trim()으로 공백이 제거되어 매칭되어야 함
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.LEFT), "공백이 포함된 LEFT 설정이 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.RIGHT), "공백이 포함된 RIGHT 설정이 매칭되어야 함");
        assertTrue(keyInputHandler.isDropClicked(KeyCode.DOWN), "탭이 포함된 DOWN 설정이 매칭되어야 함");
    }

    @Test
    @DisplayName("모든 키 액션 메서드가 존재하는지 테스트")
    void testAllKeyActionMethodsExist() {
        // given
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");
        settings.setKeyDrop("X");
        settings.setPause("P");

        // when & then - 모든 메서드가 정상 동작해야 함
        assertDoesNotThrow(() -> keyInputHandler.isLeftClicked(KeyCode.A));
        assertDoesNotThrow(() -> keyInputHandler.isRightClicked(KeyCode.D));
        assertDoesNotThrow(() -> keyInputHandler.isDropClicked(KeyCode.S));
        assertDoesNotThrow(() -> keyInputHandler.isRotateClicked(KeyCode.W));
        assertDoesNotThrow(() -> keyInputHandler.isHardDropClicked(KeyCode.X));
        assertDoesNotThrow(() -> keyInputHandler.isPauseClicked(KeyCode.P));
    }

    @Test
    @DisplayName("Settings 객체 변경 시 즉시 반영되는지 테스트")
    void testSettingsChangeReflection() {
        // given - 초기 설정
        settings.setKeyLeft("A");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "초기 설정이 매칭되어야 함");

        // when - 설정 변경
        settings.setKeyLeft("B");

        // then - 즉시 반영
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.A), "변경 후 이전 키는 매칭되지 않아야 함");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.B), "변경 후 새 키가 매칭되어야 함");
    }

    @Test
    @DisplayName("KeyInputHandler가 Settings를 올바르게 참조하는지 테스트")
    void testSettingsReference() {
        // given
        Settings newSettings = new Settings();
        newSettings.setKeyLeft("Q");
        KeyInputHandler newHandler = new KeyInputHandler(newSettings);

        // when & then
        assertTrue(newHandler.isLeftClicked(KeyCode.Q), "새 Settings 객체의 설정이 적용되어야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.Q), "기존 핸들러는 영향받지 않아야 함");
    }
}

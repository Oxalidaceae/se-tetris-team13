package team13.tetris.input;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        // given - Settings의 기본값 사용 (A, D, S, W, SPACE, ESCAPE)
        Settings defaultSettings = new Settings();
        defaultSettings.restoreDefaultKeys();
        KeyInputHandler handler = new KeyInputHandler(defaultSettings);

        // when & then - WASD와 SPACE, ESCAPE가 기본 키
        assertTrue(handler.isLeftClicked(KeyCode.A), "A 키가 왼쪽 이동과 매칭되어야 함");
        assertTrue(handler.isRightClicked(KeyCode.D), "D 키가 오른쪽 이동과 매칭되어야 함");
        assertTrue(handler.isDropClicked(KeyCode.S), "S 키가 소프트 드롭과 매칭되어야 함");
        assertTrue(handler.isRotateClicked(KeyCode.W), "W 키가 회전과 매칭되어야 함");
        assertTrue(handler.isHardDropClicked(KeyCode.SPACE), "SPACE 키가 하드 드롭과 매칭되어야 함");
        assertTrue(handler.isPauseClicked(KeyCode.ESCAPE), "ESCAPE 키가 일시정지와 매칭되어야 함");

        // 방향키는 기본 키가 아님
        assertFalse(handler.isLeftClicked(KeyCode.LEFT), "LEFT 키는 기본 키가 아님");
        assertFalse(handler.isRightClicked(KeyCode.RIGHT), "RIGHT 키는 기본 키가 아님");
        assertFalse(handler.isDropClicked(KeyCode.DOWN), "DOWN 키는 기본 키가 아님");
        assertFalse(handler.isRotateClicked(KeyCode.UP), "UP 키는 기본 키가 아님");
    }

    @Test
    @DisplayName("방향키로 변경 후 매칭 테스트")
    void testChangeToArrowKeys() {
        // given - 방향키 설정
        settings.setKeyLeft("LEFT");
        settings.setKeyRight("RIGHT");
        settings.setKeyDown("DOWN");
        settings.setKeyRotate("UP");

        // when & then
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.LEFT), "LEFT 키가 왼쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.RIGHT), "RIGHT 키는 왼쪽 이동과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isRightClicked(KeyCode.RIGHT), "RIGHT 키가 오른쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.LEFT), "LEFT 키는 오른쪽 이동과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isDropClicked(KeyCode.DOWN), "DOWN 키가 소프트 드롭과 매칭되어야 함");
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.UP), "UP 키가 회전과 매칭되어야 함");
    }

    @Test
    @DisplayName("특수 키 매칭 테스트")
    void testSpecialKeyMatching() {
        // given
        settings.setKeyDrop("SPACE");
        settings.setPause("P");

        // when & then
        assertTrue(keyInputHandler.isHardDropClicked(KeyCode.SPACE), "SPACE 키가 하드 드롭과 매칭되어야 함");
        assertFalse(keyInputHandler.isHardDropClicked(KeyCode.ENTER), "ENTER 키는 하드 드롭과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isPauseClicked(KeyCode.P), "P 키가 일시정지와 매칭되어야 함");
        assertFalse(keyInputHandler.isPauseClicked(KeyCode.Q), "Q 키는 일시정지와 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("숫자 키 매칭 테스트")
    void testDigitKeyMatching() {
        // given
        settings.setKeyLeft("DIGIT1");
        settings.setKeyRight("NUMPAD5");

        // when & then
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.DIGIT1), "DIGIT1 키가 왼쪽 이동과 매칭되어야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.DIGIT2), "DIGIT2 키는 왼쪽 이동과 매칭되지 않아야 함");

        assertTrue(keyInputHandler.isRightClicked(KeyCode.NUMPAD5), "NUMPAD5 키가 오른쪽 이동과 매칭되어야 함");
        assertFalse(
                keyInputHandler.isRightClicked(KeyCode.NUMPAD1), "NUMPAD1 키는 오른쪽 이동과 매칭되지 않아야 함");
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
        assertFalse(keyInputHandler.isRotateClicked(KeyCode.F2), "F2 키는 회전과 매칭되지 않아야 함");
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

    @Test
    @DisplayName("특수 키 코드들과의 매칭 테스트")
    void testSpecialKeyCodes() {
        // given - 특수 키 코드들 설정
        settings.setKeyLeft("CONTROL");
        settings.setKeyRight("SHIFT");
        settings.setKeyDown("ALT");
        settings.setKeyRotate("TAB");
        settings.setKeyDrop("ENTER");
        settings.setPause("ESCAPE");

        // when & then
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.CONTROL), "CONTROL 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.SHIFT), "SHIFT 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isDropClicked(KeyCode.ALT), "ALT 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.TAB), "TAB 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isHardDropClicked(KeyCode.ENTER), "ENTER 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isPauseClicked(KeyCode.ESCAPE), "ESCAPE 키가 매칭되어야 함");
    }

    @Test
    @DisplayName("숫자 키와 넘패드 키 매칭 테스트")
    void testNumericKeys() {
        // given
        String[] digitKeys = {
            "DIGIT0", "DIGIT1", "DIGIT2", "DIGIT3", "DIGIT4", "DIGIT5", "DIGIT6", "DIGIT7",
            "DIGIT8", "DIGIT9"
        };
        String[] numpadKeys = {
            "NUMPAD0", "NUMPAD1", "NUMPAD2", "NUMPAD3", "NUMPAD4", "NUMPAD5", "NUMPAD6", "NUMPAD7",
            "NUMPAD8", "NUMPAD9"
        };
        KeyCode[] digitKeyCodes = {
            KeyCode.DIGIT0,
            KeyCode.DIGIT1,
            KeyCode.DIGIT2,
            KeyCode.DIGIT3,
            KeyCode.DIGIT4,
            KeyCode.DIGIT5,
            KeyCode.DIGIT6,
            KeyCode.DIGIT7,
            KeyCode.DIGIT8,
            KeyCode.DIGIT9
        };
        KeyCode[] numpadKeyCodes = {
            KeyCode.NUMPAD0,
            KeyCode.NUMPAD1,
            KeyCode.NUMPAD2,
            KeyCode.NUMPAD3,
            KeyCode.NUMPAD4,
            KeyCode.NUMPAD5,
            KeyCode.NUMPAD6,
            KeyCode.NUMPAD7,
            KeyCode.NUMPAD8,
            KeyCode.NUMPAD9
        };

        for (int i = 0; i < digitKeys.length; i++) {
            settings.setKeyLeft(digitKeys[i]);
            assertTrue(
                    keyInputHandler.isLeftClicked(digitKeyCodes[i]), digitKeys[i] + " 키가 매칭되어야 함");

            settings.setKeyRight(numpadKeys[i]);
            assertTrue(
                    keyInputHandler.isRightClicked(numpadKeyCodes[i]),
                    numpadKeys[i] + " 키가 매칭되어야 함");
        }
    }

    @Test
    @DisplayName("모든 F키 매칭 테스트")
    void testFunctionKeys() {
        String[] fKeys = {
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"
        };
        KeyCode[] fKeyCodes = {
            KeyCode.F1,
            KeyCode.F2,
            KeyCode.F3,
            KeyCode.F4,
            KeyCode.F5,
            KeyCode.F6,
            KeyCode.F7,
            KeyCode.F8,
            KeyCode.F9,
            KeyCode.F10,
            KeyCode.F11,
            KeyCode.F12
        };

        for (int i = 0; i < fKeys.length; i++) {
            settings.setKeyRotate(fKeys[i]);
            assertTrue(keyInputHandler.isRotateClicked(fKeyCodes[i]), fKeys[i] + " 키가 회전과 매칭되어야 함");
        }
    }

    @Test
    @DisplayName("잘못된 키 이름 설정 테스트")
    void testInvalidKeyNames() {
        String[] invalidKeys = {"INVALID_KEY", "KEY_NOT_EXISTS", "WRONG_NAME", "123", "!@#", "가나다"};

        for (String invalidKey : invalidKeys) {
            settings.setKeyLeft(invalidKey);

            // 모든 KeyCode에 대해 false를 반환해야 함
            assertFalse(
                    keyInputHandler.isLeftClicked(KeyCode.A),
                    invalidKey + " 설정 시 어떤 키와도 매칭되지 않아야 함");
            assertFalse(
                    keyInputHandler.isLeftClicked(KeyCode.SPACE),
                    invalidKey + " 설정 시 SPACE와도 매칭되지 않아야 함");
            assertFalse(
                    keyInputHandler.isLeftClicked(KeyCode.ENTER),
                    invalidKey + " 설정 시 ENTER와도 매칭되지 않아야 함");
        }
    }

    @Test
    @DisplayName("키 매칭 성능 테스트")
    void testKeyMatchingPerformance() {
        // given - 여러 키 설정
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");
        settings.setKeyDrop("SPACE");
        settings.setPause("ESCAPE");

        // when & then - 대량의 키 매칭 테스트가 빠르게 실행되어야 함
        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) { // 반복 횟수를 줄임
            keyInputHandler.isLeftClicked(KeyCode.A);
            keyInputHandler.isRightClicked(KeyCode.D);
            keyInputHandler.isDropClicked(KeyCode.S);
            keyInputHandler.isRotateClicked(KeyCode.W);
            keyInputHandler.isHardDropClicked(KeyCode.SPACE);
            keyInputHandler.isPauseClicked(KeyCode.ESCAPE);

            keyInputHandler.isLeftClicked(KeyCode.B);
            keyInputHandler.isRightClicked(KeyCode.F);
            keyInputHandler.isDropClicked(KeyCode.X);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // 1000번의 매칭이 1초 이내에 완료되어야 함
        assertTrue(duration < 1_000_000_000L, "키 매칭이 충분히 빨라야 함");
    }

    @Test
    @DisplayName("동시 키 매칭 테스트")
    void testConcurrentKeyMatching() {
        // given
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");

        // when & then - 여러 키를 동시에 확인
        assertTrue(
                keyInputHandler.isLeftClicked(KeyCode.A)
                        && keyInputHandler.isRightClicked(KeyCode.D),
                "여러 키가 동시에 매칭될 수 있어야 함");

        assertTrue(
                keyInputHandler.isDropClicked(KeyCode.S)
                        && keyInputHandler.isRotateClicked(KeyCode.W),
                "다른 키들도 동시에 매칭될 수 있어야 함");

        assertFalse(
                keyInputHandler.isLeftClicked(KeyCode.B)
                        || keyInputHandler.isRightClicked(KeyCode.F),
                "잘못된 키들은 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("키 설정 변경 후 이전 키 무효화 테스트")
    void testKeySettingInvalidation() {
        // given - 초기 키 설정
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "초기 A 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.D), "초기 D 키가 매칭되어야 함");

        // when - 키 설정 변경
        settings.setKeyLeft("Q");
        settings.setKeyRight("E");

        // then - 이전 키는 무효화되고 새 키만 유효
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.A), "이전 A 키는 무효화되어야 함");
        assertFalse(keyInputHandler.isRightClicked(KeyCode.D), "이전 D 키는 무효화되어야 함");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.Q), "새 Q 키가 매칭되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.E), "새 E 키가 매칭되어야 함");
    }

    @Test
    @DisplayName("모든 알파벳 키 매칭 테스트")
    void testAllAlphabetKeys() {
        String[] alphabet = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        };
        KeyCode[] alphabetKeyCodes = {
            KeyCode.A, KeyCode.B, KeyCode.C, KeyCode.D, KeyCode.E, KeyCode.F, KeyCode.G, KeyCode.H,
            KeyCode.I, KeyCode.J, KeyCode.K, KeyCode.L, KeyCode.M, KeyCode.N, KeyCode.O, KeyCode.P,
            KeyCode.Q, KeyCode.R, KeyCode.S, KeyCode.T, KeyCode.U, KeyCode.V, KeyCode.W, KeyCode.X,
            KeyCode.Y, KeyCode.Z
        };

        for (int i = 0; i < alphabet.length; i++) {
            settings.setKeyLeft(alphabet[i]);
            assertTrue(
                    keyInputHandler.isLeftClicked(alphabetKeyCodes[i]),
                    alphabet[i] + " 키가 매칭되어야 함");
        }
    }
}

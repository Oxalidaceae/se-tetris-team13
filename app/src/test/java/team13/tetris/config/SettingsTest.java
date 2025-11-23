package team13.tetris.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Settings class 테스트: Tests user configuration management including keys, window size, color, and
// blind mode
@DisplayName("Settings 테스트")
public class SettingsTest {

    private Settings settings;

    @BeforeEach
    void setUp() {
        settings = new Settings();
    }

    @Test
    @DisplayName("Settings 인스턴스가 정상적으로 생성되는지 확인")
    void testSettingsCreation() {
        assertNotNull(settings, "Settings instance should not be null");
    }

    @Test
    @DisplayName("색맹 모드 기본값이 false인지 확인")
    void testColorBlindModeDefaultValue() {
        assertFalse(settings.isColorBlindMode(), "Color blind mode should be false by default");
    }

    @Test
    @DisplayName("색맹 모드를 설정하고 가져올 수 있는지 확인")
    void testColorBlindModeGetterSetter() {
        settings.setColorBlindMode(true);
        assertTrue(settings.isColorBlindMode(), "Color blind mode should be true after setting");

        settings.setColorBlindMode(false);
        assertFalse(settings.isColorBlindMode(), "Color blind mode should be false after setting");
    }

    @Test
    @DisplayName("창 크기 기본값이 MEDIUM인지 확인")
    void testWindowSizeDefaultValue() {
        assertEquals("MEDIUM", settings.getWindowSize(), "Window size should be MEDIUM by default");
    }

    @Test
    @DisplayName("창 크기를 SMALL로 설정할 수 있는지 확인")
    void testWindowSizeSmall() {
        settings.setWindowSize("SMALL");
        assertEquals(
                "SMALL", settings.getWindowSize(), "Window size should be SMALL after setting");
    }

    @Test
    @DisplayName("창 크기를 MEDIUM으로 설정할 수 있는지 확인")
    void testWindowSizeMedium() {
        settings.setWindowSize("MEDIUM");
        assertEquals(
                "MEDIUM", settings.getWindowSize(), "Window size should be MEDIUM after setting");
    }

    @Test
    @DisplayName("창 크기를 LARGE로 설정할 수 있는지 확인")
    void testWindowSizeLarge() {
        settings.setWindowSize("LARGE");
        assertEquals(
                "LARGE", settings.getWindowSize(), "Window size should be LARGE after setting");
    }

    @Test
    @DisplayName("왼쪽 이동 키 기본값이 A인지 확인")
    void testKeyLeftDefaultValue() {
        assertEquals("A", settings.getKeyLeft(), "Left key should be A by default");
    }

    @Test
    @DisplayName("왼쪽 이동 키를 설정하고 가져올 수 있는지 확인")
    void testKeyLeftGetterSetter() {
        settings.setKeyLeft("A");
        assertEquals("A", settings.getKeyLeft(), "Left key should be A after setting");
    }

    @Test
    @DisplayName("오른쪽 이동 키 기본값이 D인지 확인")
    void testKeyRightDefaultValue() {
        assertEquals("D", settings.getKeyRight(), "Right key should be D by default");
    }

    @Test
    @DisplayName("오른쪽 이동 키를 설정하고 가져올 수 있는지 확인")
    void testKeyRightGetterSetter() {
        settings.setKeyRight("D");
        assertEquals("D", settings.getKeyRight(), "Right key should be D after setting");
    }

    @Test
    @DisplayName("아래 이동 키 기본값이 S인지 확인")
    void testKeyDownDefaultValue() {
        assertEquals("S", settings.getKeyDown(), "Down key should be S by default");
    }

    @Test
    @DisplayName("아래 이동 키를 설정하고 가져올 수 있는지 확인")
    void testKeyDownGetterSetter() {
        settings.setKeyDown("S");
        assertEquals("S", settings.getKeyDown(), "Down key should be S after setting");
    }

    @Test
    @DisplayName("회전 키 기본값이 W인지 확인")
    void testKeyRotateDefaultValue() {
        assertEquals("W", settings.getKeyRotate(), "Rotate key should be W by default");
    }

    @Test
    @DisplayName("회전 키를 설정하고 가져올 수 있는지 확인")
    void testKeyRotateGetterSetter() {
        settings.setKeyRotate("W");
        assertEquals("W", settings.getKeyRotate(), "Rotate key should be W after setting");
    }

    @Test
    @DisplayName("하드 드롭 키 기본값이 SPACE인지 확인")
    void testKeyDropDefaultValue() {
        assertEquals("SPACE", settings.getKeyDrop(), "Drop key should be SPACE by default");
    }

    @Test
    @DisplayName("하드 드롭 키를 설정하고 가져올 수 있는지 확인")
    void testKeyDropGetterSetter() {
        settings.setKeyDrop("SPACE");
        assertEquals("SPACE", settings.getKeyDrop(), "Drop key should be SPACE after setting");
    }

    @Test
    @DisplayName("일시정지 키 기본값이 ESCAPE인지 확인")
    void testPauseDefaultValue() {
        assertEquals("ESCAPE", settings.getPause(), "Pause key should be ESCAPE by default");
    }

    @Test
    @DisplayName("일시정지 키를 설정하고 가져올 수 있는지 확인")
    void testPauseGetterSetter() {
        settings.setPause("ESC");
        assertEquals("ESC", settings.getPause(), "Pause key should be ESC after setting");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - A")
    void testIsKeyAlreadyUsedLeft() {
        assertTrue(settings.isKeyAlreadyUsed("A"), "A key should be already used");
        assertTrue(
                settings.isKeyAlreadyUsed("a"), "a key should be already used (case insensitive)");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - D")
    void testIsKeyAlreadyUsedRight() {
        assertTrue(settings.isKeyAlreadyUsed("D"), "D key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - S")
    void testIsKeyAlreadyUsedDown() {
        assertTrue(settings.isKeyAlreadyUsed("S"), "S key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - W")
    void testIsKeyAlreadyUsedZ() {
        assertTrue(settings.isKeyAlreadyUsed("W"), "W key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - SPACE")
    void testIsKeyAlreadyUsedX() {
        assertTrue(settings.isKeyAlreadyUsed("SPACE"), "SPACE key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - ESCAPE")
    void testIsKeyAlreadyUsedP() {
        assertTrue(settings.isKeyAlreadyUsed("ESCAPE"), "ESCAPE key should be already used");
    }

    @Test
    @DisplayName("사용되지 않은 키는 false를 반환하는지 확인")
    void testIsKeyAlreadyUsedUnused() {
        assertFalse(settings.isKeyAlreadyUsed("B"), "B key should not be already used");
        assertFalse(settings.isKeyAlreadyUsed("C"), "C key should not be already used");
        assertFalse(settings.isKeyAlreadyUsed("F1"), "F1 key should not be already used");
    }

    @Test
    @DisplayName("사용자 정의 키가 이미 사용 중인지 확인")
    void testIsKeyAlreadyUsedCustomKey() {
        settings.setKeyLeft("LEFT");
        assertTrue(
                settings.isKeyAlreadyUsed("LEFT"),
                "LEFT key should be already used after setting as left key");
        assertFalse(
                settings.isKeyAlreadyUsed("A"), "A key should not be used anymore after changing");
    }

    @Test
    @DisplayName("키 중복 감지 - 대소문자 구분 없이")
    void testIsKeyAlreadyUsedCaseInsensitive() {
        settings.setKeyLeft("a");
        assertTrue(settings.isKeyAlreadyUsed("A"), "Should detect 'A' as used (case insensitive)");
        assertTrue(settings.isKeyAlreadyUsed("a"), "Should detect 'a' as used (case insensitive)");
    }

    @Test
    @DisplayName("기본 키 복원 기능 확인")
    void testRestoreDefaultKeys() {
        // 키 설정 변경
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");
        settings.setKeyDrop("SPACE");
        settings.setPause("P");

        // 변경 확인
        assertEquals("A", settings.getKeyLeft());
        assertEquals("D", settings.getKeyRight());

        // 기본값 복원
        settings.restoreDefaultKeys();

        // 기본값으로 복원되었는지 확인
        assertEquals("A", settings.getKeyLeft(), "Left key should be restored to A");
        assertEquals("D", settings.getKeyRight(), "Right key should be restored to D");
        assertEquals("S", settings.getKeyDown(), "Down key should be restored to S");
        assertEquals("W", settings.getKeyRotate(), "Rotate key should be restored to W");
        assertEquals("SPACE", settings.getKeyDrop(), "Drop key should be restored to SPACE");
        assertEquals("ESCAPE", settings.getPause(), "Pause key should be restored to ESCAPE");
    }

    @Test
    @DisplayName("모든 키를 WASD로 변경할 수 있는지 확인")
    void testChangeAllKeysToWASD() {
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");

        assertEquals("A", settings.getKeyLeft());
        assertEquals("D", settings.getKeyRight());
        assertEquals("S", settings.getKeyDown());
        assertEquals("W", settings.getKeyRotate());
    }

    @Test
    @DisplayName("여러 키를 동일한 값으로 설정할 수 있는지 확인")
    void testDuplicateKeyAssignment() {
        // 중복 키 설정 허용 (검증은 UI에서 처리)
        settings.setKeyLeft("A");
        settings.setKeyRight("A");

        assertEquals("A", settings.getKeyLeft());
        assertEquals("A", settings.getKeyRight());
        assertTrue(settings.isKeyAlreadyUsed("A"));
    }

    @Test
    @DisplayName("모든 기본값이 올바르게 설정되는지 확인")
    void testAllDefaultValues() {
        Settings newSettings = new Settings();

        assertFalse(newSettings.isColorBlindMode(), "Color blind mode default");
        assertEquals("MEDIUM", newSettings.getWindowSize(), "Window size default");
        assertEquals("A", newSettings.getKeyLeft(), "Left key default");
        assertEquals("D", newSettings.getKeyRight(), "Right key default");
        assertEquals("S", newSettings.getKeyDown(), "Down key default");
        assertEquals("W", newSettings.getKeyRotate(), "Rotate key default");
        assertEquals("SPACE", newSettings.getKeyDrop(), "Drop key default");
        assertEquals("ESCAPE", newSettings.getPause(), "Pause key default");
    }

    @Test
    @DisplayName("여러 Settings 인스턴스가 독립적인지 확인")
    void testMultipleSettingsInstancesAreIndependent() {
        Settings settings1 = new Settings();
        Settings settings2 = new Settings();

        settings1.setKeyLeft("A");
        settings2.setKeyLeft("B");

        assertEquals("A", settings1.getKeyLeft(), "First instance should have A");
        assertEquals("B", settings2.getKeyLeft(), "Second instance should have B");
        assertNotEquals(
                settings1.getKeyLeft(), settings2.getKeyLeft(), "Instances should be independent");
    }

    @Test
    @DisplayName("null 키 설정 처리 확인")
    void testNullKeyHandling() {
        assertDoesNotThrow(
                () -> {
                    settings.setKeyLeft(null);
                    settings.isKeyAlreadyUsed(null);
                },
                "Should handle null keys without throwing exception");
    }

    @Test
    @DisplayName("빈 문자열 키 설정 처리 확인")
    void testEmptyStringKeyHandling() {
        assertDoesNotThrow(
                () -> {
                    settings.setKeyLeft("");
                    settings.isKeyAlreadyUsed("");
                },
                "Should handle empty string keys without throwing exception");
    }

    @Test
    @DisplayName("특수 키 코드 설정 확인")
    void testSpecialKeyCodes() {
        settings.setKeyLeft("F1");
        settings.setKeyRight("SHIFT");
        settings.setKeyDown("CTRL");
        settings.setKeyRotate("ALT");

        assertEquals("F1", settings.getKeyLeft());
        assertEquals("SHIFT", settings.getKeyRight());
        assertEquals("CTRL", settings.getKeyDown());
        assertEquals("ALT", settings.getKeyRotate());

        assertTrue(settings.isKeyAlreadyUsed("F1"));
        assertTrue(settings.isKeyAlreadyUsed("SHIFT"));
    }

    @Test
    @DisplayName("창 크기 변경이 다른 설정에 영향을 주지 않는지 확인")
    void testWindowSizeChangeDoesNotAffectOtherSettings() {
        settings.setKeyLeft("A");
        settings.setColorBlindMode(true);

        settings.setWindowSize("LARGE");

        assertEquals("A", settings.getKeyLeft(), "Key settings should not change");
        assertTrue(settings.isColorBlindMode(), "Color blind mode should not change");
    }

    @Test
    @DisplayName("색맹 모드 변경이 다른 설정에 영향을 주지 않는지 확인")
    void testColorBlindModeChangeDoesNotAffectOtherSettings() {
        settings.setKeyLeft("A");
        settings.setWindowSize("LARGE");

        settings.setColorBlindMode(true);

        assertEquals("A", settings.getKeyLeft(), "Key settings should not change");
        assertEquals("LARGE", settings.getWindowSize(), "Window size should not change");
    }

    @Test
    @DisplayName("잘못된 창 크기 값 설정 테스트")
    void testInvalidWindowSizeValues() {
        String[] invalidSizes = {"small", "medium", "large", "EXTRA_LARGE", "TINY", null, "", "XL"};

        for (String invalidSize : invalidSizes) {
            String originalSize = settings.getWindowSize();
            assertDoesNotThrow(
                    () -> {
                        settings.setWindowSize(invalidSize);
                    },
                    "잘못된 창 크기 설정도 예외 없이 처리되어야 함: " + invalidSize);

            // 일부는 유효할 수도 있으므로 원래 값이 보존되는지 확인하지 않음
        }
    }

    @Test
    @DisplayName("특수 문자가 포함된 키 설정 테스트")
    void testSpecialCharacterKeys() {
        String[] specialKeys = {
            "SPACE",
            "ENTER",
            "SHIFT",
            "CTRL",
            "ALT",
            "TAB",
            "ESC",
            "F1",
            "NUMPAD_1",
            "UP",
            "DOWN",
            "LEFT",
            "RIGHT"
        };

        for (String key : specialKeys) {
            assertDoesNotThrow(
                    () -> {
                        settings.setKeyLeft(key);
                        settings.setKeyRight(key);
                        settings.setKeyDown(key);
                        settings.setKeyRotate(key);
                        settings.setKeyDrop(key);
                    },
                    "특수 키 설정도 안전해야 함: " + key);
        }
    }

    @Test
    @DisplayName("빈 문자열이나 null 키 설정 테스트")
    void testNullAndEmptyKeys() {
        String[] invalidKeys = {null, "", " ", "  "};

        for (String invalidKey : invalidKeys) {
            assertDoesNotThrow(
                    () -> {
                        settings.setKeyLeft(invalidKey);
                        settings.setKeyRight(invalidKey);
                        settings.setKeyDown(invalidKey);
                        settings.setKeyRotate(invalidKey);
                        settings.setKeyDrop(invalidKey);
                    },
                    "잘못된 키 값 설정도 안전해야 함: " + invalidKey);
        }
    }

    @Test
    @DisplayName("모든 키를 동일한 값으로 설정")
    void testAllKeysSetToSameValue() {
        String sameKey = "SPACE";

        assertDoesNotThrow(
                () -> {
                    settings.setKeyLeft(sameKey);
                    settings.setKeyRight(sameKey);
                    settings.setKeyDown(sameKey);
                    settings.setKeyRotate(sameKey);
                    settings.setKeyDrop(sameKey);

                    assertEquals(sameKey, settings.getKeyLeft(), "모든 키가 같은 값으로 설정되어야 함");
                    assertEquals(sameKey, settings.getKeyRight(), "모든 키가 같은 값으로 설정되어야 함");
                    assertEquals(sameKey, settings.getKeyDown(), "모든 키가 같은 값으로 설정되어야 함");
                    assertEquals(sameKey, settings.getKeyRotate(), "모든 키가 같은 값으로 설정되어야 함");
                    assertEquals(sameKey, settings.getKeyDrop(), "모든 키가 같은 값으로 설정되어야 함");
                },
                "모든 키를 같은 값으로 설정하는 것은 허용되어야 함");
    }

    @Test
    @DisplayName("설정 값 대소문자 구분 테스트")
    void testCaseSensitivity() {
        settings.setKeyLeft("a");
        assertEquals("a", settings.getKeyLeft(), "소문자 키가 올바르게 저장되어야 함");

        settings.setKeyLeft("A");
        assertEquals("A", settings.getKeyLeft(), "대문자 키가 올바르게 저장되어야 함");

        settings.setWindowSize("small");
        // 창 크기는 대소문자 구분 여부를 확인
        assertNotNull(settings.getWindowSize(), "창 크기가 설정되어야 함");
    }

    @Test
    @DisplayName("연속적인 설정 변경 테스트")
    void testConsecutiveSettingChanges() {
        for (int i = 0; i < 100; i++) {
            boolean colorBlind = i % 2 == 0;
            String windowSize = (i % 3 == 0) ? "SMALL" : (i % 3 == 1) ? "MEDIUM" : "LARGE";
            String key = "KEY_" + i;

            assertDoesNotThrow(
                    () -> {
                        settings.setColorBlindMode(colorBlind);
                        settings.setWindowSize(windowSize);
                        settings.setKeyLeft(key);
                        settings.setKeyRight(key);
                        settings.setKeyDown(key);
                        settings.setKeyRotate(key);
                        settings.setKeyDrop(key);

                        assertEquals(
                                colorBlind, settings.isColorBlindMode(), "색맹 모드가 올바르게 설정되어야 함");
                    },
                    "연속적인 설정 변경도 안전해야 함: " + i);
        }
    }

    @Test
    @DisplayName("유니코드 문자가 포함된 키 설정 테스트")
    void testUnicodeKeys() {
        String[] unicodeKeys = {"가", "나", "다", "α", "β", "γ", "ñ", "ü", "🎮", "⌨️"};

        for (String unicodeKey : unicodeKeys) {
            assertDoesNotThrow(
                    () -> {
                        settings.setKeyLeft(unicodeKey);
                        assertEquals(unicodeKey, settings.getKeyLeft(), "유니코드 키가 올바르게 저장되어야 함");
                    },
                    "유니코드 키 설정도 안전해야 함: " + unicodeKey);
        }
    }

    @Test
    @DisplayName("매우 긴 키 이름 설정 테스트")
    void testVeryLongKeyNames() {
        String longKey = "VERY_LONG_KEY_NAME_".repeat(10);

        assertDoesNotThrow(
                () -> {
                    settings.setKeyLeft(longKey);
                    assertEquals(longKey, settings.getKeyLeft(), "긴 키 이름도 올바르게 저장되어야 함");
                },
                "긴 키 이름 설정도 안전해야 함");
    }

    @Test
    @DisplayName("여러 Settings 인스턴스 독립성 테스트")
    void testMultipleSettingsInstances() {
        Settings settings1 = new Settings();
        Settings settings2 = new Settings();

        settings1.setColorBlindMode(true);
        settings1.setKeyLeft("Q");
        settings1.setWindowSize("SMALL");

        settings2.setColorBlindMode(false);
        settings2.setKeyLeft("E");
        settings2.setWindowSize("LARGE");

        // 인스턴스들이 독립적이어야 함
        assertTrue(settings1.isColorBlindMode(), "첫 번째 인스턴스의 색맹 모드");
        assertFalse(settings2.isColorBlindMode(), "두 번째 인스턴스의 색맹 모드");
        assertEquals("Q", settings1.getKeyLeft(), "첫 번째 인스턴스의 왼쪽 키");
        assertEquals("E", settings2.getKeyLeft(), "두 번째 인스턴스의 왼쪽 키");
        assertEquals("SMALL", settings1.getWindowSize(), "첫 번째 인스턴스의 창 크기");
        assertEquals("LARGE", settings2.getWindowSize(), "두 번째 인스턴스의 창 크기");
    }

    @Test
    @DisplayName("설정 값 불변성 테스트")
    void testSettingsImmutability() {
        String originalKey = settings.getKeyLeft();
        boolean originalColorBlind = settings.isColorBlindMode();
        String originalWindowSize = settings.getWindowSize();

        // 반환된 값을 수정해도 원본에 영향을 주지 않는지 확인
        // (String은 불변이므로 이 테스트는 주로 설계 확인용)
        String keyLeft = settings.getKeyLeft();
        keyLeft = "MODIFIED"; // 이것은 settings에 영향을 주지 않아야 함

        assertEquals(originalKey, settings.getKeyLeft(), "원본 키 값이 보존되어야 함");
        assertEquals(originalColorBlind, settings.isColorBlindMode(), "원본 색맹 모드가 보존되어야 함");
        assertEquals(originalWindowSize, settings.getWindowSize(), "원본 창 크기가 보존되어야 함");
    }
}

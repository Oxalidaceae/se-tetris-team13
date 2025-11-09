package team13.tetris.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// Settings class 테스트: Tests user configuration management including keys, window size, color, and blind mode
@DisplayName("Settings 테스트")
public class SettingsTest {

    private Settings settings;

    @BeforeEach
    void setUp() { settings = new Settings(); }

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
        assertEquals("SMALL", settings.getWindowSize(), "Window size should be SMALL after setting");
    }

    @Test
    @DisplayName("창 크기를 MEDIUM으로 설정할 수 있는지 확인")
    void testWindowSizeMedium() {
        settings.setWindowSize("MEDIUM");
        assertEquals("MEDIUM", settings.getWindowSize(), "Window size should be MEDIUM after setting");
    }

    @Test
    @DisplayName("창 크기를 LARGE로 설정할 수 있는지 확인")
    void testWindowSizeLarge() {
        settings.setWindowSize("LARGE");
        assertEquals("LARGE", settings.getWindowSize(), "Window size should be LARGE after setting");
    }

    @Test
    @DisplayName("왼쪽 이동 키 기본값이 LEFT인지 확인")
    void testKeyLeftDefaultValue() {
        assertEquals("LEFT", settings.getKeyLeft(), "Left key should be LEFT by default");
    }

    @Test
    @DisplayName("왼쪽 이동 키를 설정하고 가져올 수 있는지 확인")
    void testKeyLeftGetterSetter() {
        settings.setKeyLeft("A");
        assertEquals("A", settings.getKeyLeft(), "Left key should be A after setting");
    }

    @Test
    @DisplayName("오른쪽 이동 키 기본값이 RIGHT인지 확인")
    void testKeyRightDefaultValue() {
        assertEquals("RIGHT", settings.getKeyRight(), "Right key should be RIGHT by default");
    }

    @Test
    @DisplayName("오른쪽 이동 키를 설정하고 가져올 수 있는지 확인")
    void testKeyRightGetterSetter() {
        settings.setKeyRight("D");
        assertEquals("D", settings.getKeyRight(), "Right key should be D after setting");
    }

    @Test
    @DisplayName("아래 이동 키 기본값이 DOWN인지 확인")
    void testKeyDownDefaultValue() {
        assertEquals("DOWN", settings.getKeyDown(), "Down key should be DOWN by default");
    }

    @Test
    @DisplayName("아래 이동 키를 설정하고 가져올 수 있는지 확인")
    void testKeyDownGetterSetter() {
        settings.setKeyDown("S");
        assertEquals("S", settings.getKeyDown(), "Down key should be S after setting");
    }

    @Test
    @DisplayName("회전 키 기본값이 Z인지 확인")
    void testKeyRotateDefaultValue() {
        assertEquals("Z", settings.getKeyRotate(), "Rotate key should be Z by default");
    }

    @Test
    @DisplayName("회전 키를 설정하고 가져올 수 있는지 확인")
    void testKeyRotateGetterSetter() {
        settings.setKeyRotate("W");
        assertEquals("W", settings.getKeyRotate(), "Rotate key should be W after setting");
    }

    @Test
    @DisplayName("하드 드롭 키 기본값이 X인지 확인")
    void testKeyDropDefaultValue() {
        assertEquals("X", settings.getKeyDrop(), "Drop key should be X by default");
    }

    @Test
    @DisplayName("하드 드롭 키를 설정하고 가져올 수 있는지 확인")
    void testKeyDropGetterSetter() {
        settings.setKeyDrop("SPACE");
        assertEquals("SPACE", settings.getKeyDrop(), "Drop key should be SPACE after setting");
    }

    @Test
    @DisplayName("일시정지 키 기본값이 P인지 확인")
    void testPauseDefaultValue() {
        assertEquals("P", settings.getPause(), "Pause key should be P by default");
    }

    @Test
    @DisplayName("일시정지 키를 설정하고 가져올 수 있는지 확인")
    void testPauseGetterSetter() {
        settings.setPause("ESC");
        assertEquals("ESC", settings.getPause(), "Pause key should be ESC after setting");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - LEFT")
    void testIsKeyAlreadyUsedLeft() {
        assertTrue(settings.isKeyAlreadyUsed("LEFT"), "LEFT key should be already used");
        assertTrue(settings.isKeyAlreadyUsed("left"), "left key should be already used (case insensitive)");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - RIGHT")
    void testIsKeyAlreadyUsedRight() {
        assertTrue(settings.isKeyAlreadyUsed("RIGHT"), "RIGHT key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - DOWN")
    void testIsKeyAlreadyUsedDown() {
        assertTrue(settings.isKeyAlreadyUsed("DOWN"), "DOWN key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - Z")
    void testIsKeyAlreadyUsedZ() {
        assertTrue(settings.isKeyAlreadyUsed("Z"), "Z key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - X")
    void testIsKeyAlreadyUsedX() {
        assertTrue(settings.isKeyAlreadyUsed("X"), "X key should be already used");
    }

    @Test
    @DisplayName("기본 키가 이미 사용 중인지 확인 - P")
    void testIsKeyAlreadyUsedP() {
        assertTrue(settings.isKeyAlreadyUsed("P"), "P key should be already used");
    }

    @Test
    @DisplayName("사용되지 않은 키는 false를 반환하는지 확인")
    void testIsKeyAlreadyUsedUnused() {
        assertFalse(settings.isKeyAlreadyUsed("A"), "A key should not be already used");
        assertFalse(settings.isKeyAlreadyUsed("B"), "B key should not be already used");
        assertFalse(settings.isKeyAlreadyUsed("F1"), "F1 key should not be already used");
    }

    @Test
    @DisplayName("사용자 정의 키가 이미 사용 중인지 확인")
    void testIsKeyAlreadyUsedCustomKey() {
        settings.setKeyLeft("A");
        assertTrue(settings.isKeyAlreadyUsed("A"), "A key should be already used after setting as left key");
        assertFalse(settings.isKeyAlreadyUsed("LEFT"), "LEFT key should not be used anymore after changing");
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
        assertEquals("LEFT", settings.getKeyLeft(), "Left key should be restored to LEFT");
        assertEquals("RIGHT", settings.getKeyRight(), "Right key should be restored to RIGHT");
        assertEquals("DOWN", settings.getKeyDown(), "Down key should be restored to DOWN");
        assertEquals("Z", settings.getKeyRotate(), "Rotate key should be restored to Z");
        assertEquals("X", settings.getKeyDrop(), "Drop key should be restored to X");
        assertEquals("P", settings.getPause(), "Pause key should be restored to P");
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
        assertEquals("LEFT", newSettings.getKeyLeft(), "Left key default");
        assertEquals("RIGHT", newSettings.getKeyRight(), "Right key default");
        assertEquals("DOWN", newSettings.getKeyDown(), "Down key default");
        assertEquals("Z", newSettings.getKeyRotate(), "Rotate key default");
        assertEquals("X", newSettings.getKeyDrop(), "Drop key default");
        assertEquals("P", newSettings.getPause(), "Pause key default");
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
        assertNotEquals(settings1.getKeyLeft(), settings2.getKeyLeft(), "Instances should be independent");
    }

    @Test
    @DisplayName("null 키 설정 처리 확인")
    void testNullKeyHandling() {
        assertDoesNotThrow(() -> {
            settings.setKeyLeft(null);
            settings.isKeyAlreadyUsed(null);
        }, "Should handle null keys without throwing exception");
    }

    @Test
    @DisplayName("빈 문자열 키 설정 처리 확인")
    void testEmptyStringKeyHandling() {
        assertDoesNotThrow(() -> {
            settings.setKeyLeft("");
            settings.isKeyAlreadyUsed("");
        }, "Should handle empty string keys without throwing exception");
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
}

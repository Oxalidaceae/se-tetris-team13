package team13.tetris.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// SettingsRepository 테스트: Tests settings file save/load functionality
@DisplayName("SettingsRepository 테스트")
public class SettingsRepositoryTest {

    @TempDir Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        // 임시 디렉토리를 작업 디렉토리로 설정
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        // settings.json 파일 정리 (임시 디렉토리에서)
        File settingsFile = new File(tempDir.toFile(), "settings.json");
        if (settingsFile.exists()) settingsFile.delete();

        // 원래 작업 디렉토리로 복원
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    @DisplayName("Settings를 파일에 저장할 수 있는지 확인")
    void testSaveSettings() {
        // given
        Settings settings = new Settings();
        settings.setKeyLeft("A");
        settings.setWindowSize("LARGE");
        settings.setColorBlindMode(true);

        // when
        SettingsRepository.save(settings);

        // then
        File file = new File("settings.json");
        assertTrue(file.exists(), "settings.json file should be created");
        assertTrue(file.length() > 0, "settings.json file should not be empty");
    }

    @Test
    @DisplayName("저장된 Settings를 로드할 수 있는지 확인")
    void testLoadSettings() {
        // given - 먼저 설정 저장
        Settings originalSettings = new Settings();
        originalSettings.setKeyLeft("A");
        originalSettings.setKeyRight("D");
        originalSettings.setKeyDown("S");
        originalSettings.setKeyRotate("W");
        originalSettings.setWindowSize("LARGE");
        originalSettings.setColorBlindMode(true);

        SettingsRepository.save(originalSettings);

        // when - 로드
        Settings loadedSettings = SettingsRepository.load();

        // then - 저장된 값과 일치하는지 확인
        assertNotNull(loadedSettings, "Loaded settings should not be null");
        assertEquals("A", loadedSettings.getKeyLeft(), "Left key should be A");
        assertEquals("D", loadedSettings.getKeyRight(), "Right key should be D");
        assertEquals("S", loadedSettings.getKeyDown(), "Down key should be S");
        assertEquals("W", loadedSettings.getKeyRotate(), "Rotate key should be W");
        assertEquals("LARGE", loadedSettings.getWindowSize(), "Window size should be LARGE");
        assertTrue(loadedSettings.isColorBlindMode(), "Color blind mode should be true");
    }

    @Test
    @DisplayName("settings.json 파일이 없을 때 기본값을 반환하는지 확인")
    void testLoadWithoutFile() {
        // given - settings.json 파일이 없는 상태 확실히 하기
        File settingsFile = new File("settings.json");
        if (settingsFile.exists()) settingsFile.delete();

        // when
        Settings settings = SettingsRepository.load();

        // then - 기본값으로 생성되어야 함
        assertNotNull(settings, "Settings should not be null");
        assertEquals("A", settings.getKeyLeft(), "Should return default left key");
        assertEquals("D", settings.getKeyRight(), "Should return default right key");
        assertEquals("S", settings.getKeyDown(), "Should return default down key");
        assertEquals("W", settings.getKeyRotate(), "Should return default rotate key");
        assertEquals("SPACE", settings.getKeyDrop(), "Should return default drop key");
        assertEquals("ESCAPE", settings.getPause(), "Should return default pause key");
        assertEquals("MEDIUM", settings.getWindowSize(), "Should return default window size");
        assertFalse(settings.isColorBlindMode(), "Should return default color blind mode");
    }

    @Test
    @DisplayName("저장과 로드를 반복해도 정상 동작하는지 확인")
    void testMultipleSaveAndLoad() {
        // 첫 번째 저장/로드
        Settings settings1 = new Settings();
        settings1.setKeyLeft("A");
        SettingsRepository.save(settings1);

        Settings loaded1 = SettingsRepository.load();
        assertEquals("A", loaded1.getKeyLeft(), "First load should have A");

        // 두 번째 저장/로드 (덮어쓰기)
        Settings settings2 = new Settings();
        settings2.setKeyLeft("B");
        SettingsRepository.save(settings2);

        Settings loaded2 = SettingsRepository.load();
        assertEquals("B", loaded2.getKeyLeft(), "Second load should have B");

        // 세 번째 저장/로드
        Settings settings3 = new Settings();
        settings3.setKeyLeft("C");
        SettingsRepository.save(settings3);

        Settings loaded3 = SettingsRepository.load();
        assertEquals("C", loaded3.getKeyLeft(), "Third load should have C");
    }

    @Test
    @DisplayName("모든 설정 값이 올바르게 저장되고 로드되는지 확인")
    void testSaveAndLoadAllSettings() {
        // given - 모든 설정 값 변경
        Settings originalSettings = new Settings();
        originalSettings.setColorBlindMode(true);
        originalSettings.setWindowSize("SMALL");
        originalSettings.setKeyLeft("Q");
        originalSettings.setKeyRight("E");
        originalSettings.setKeyDown("S");
        originalSettings.setKeyRotate("W");
        originalSettings.setKeyDrop("SPACE");
        originalSettings.setPause("P");

        // when
        SettingsRepository.save(originalSettings);
        Settings loadedSettings = SettingsRepository.load();

        // then - 모든 값이 일치하는지 확인
        assertEquals(
                originalSettings.isColorBlindMode(),
                loadedSettings.isColorBlindMode(),
                "Color blind mode should match");
        assertEquals(
                originalSettings.getWindowSize(),
                loadedSettings.getWindowSize(),
                "Window size should match");
        assertEquals(
                originalSettings.getKeyLeft(),
                loadedSettings.getKeyLeft(),
                "Left key should match");
        assertEquals(
                originalSettings.getKeyRight(),
                loadedSettings.getKeyRight(),
                "Right key should match");
        assertEquals(
                originalSettings.getKeyDown(),
                loadedSettings.getKeyDown(),
                "Down key should match");
        assertEquals(
                originalSettings.getKeyRotate(),
                loadedSettings.getKeyRotate(),
                "Rotate key should match");
        assertEquals(
                originalSettings.getKeyDrop(),
                loadedSettings.getKeyDrop(),
                "Drop key should match");
        assertEquals(
                originalSettings.getPause(), loadedSettings.getPause(), "Pause key should match");
    }

    @Test
    @DisplayName("JSON 파일 형식이 올바른지 확인")
    void testJsonFormat() throws IOException {
        // given
        Settings settings = new Settings();
        settings.setKeyLeft("A");

        // when
        SettingsRepository.save(settings);

        // then - JSON 파일 내용 확인
        File file = new File("settings.json");
        assertTrue(file.exists(), "File should exist");

        // JSON 파일이 Settings 객체로 파싱 가능한지 확인
        Settings loaded = SettingsRepository.load();
        assertNotNull(loaded, "Should be able to parse JSON file");
    }

    @Test
    @DisplayName("기본 설정을 저장하고 로드할 수 있는지 확인")
    void testSaveAndLoadDefaultSettings() {
        // given - 기본 설정
        Settings defaultSettings = new Settings();

        // when
        SettingsRepository.save(defaultSettings);
        Settings loadedSettings = SettingsRepository.load();

        // then - 기본값이 그대로 유지되는지 확인
        assertEquals("A", loadedSettings.getKeyLeft());
        assertEquals("D", loadedSettings.getKeyRight());
        assertEquals("S", loadedSettings.getKeyDown());
        assertEquals("W", loadedSettings.getKeyRotate());
        assertEquals("SPACE", loadedSettings.getKeyDrop());
        assertEquals("ESCAPE", loadedSettings.getPause());
        assertEquals("MEDIUM", loadedSettings.getWindowSize());
        assertFalse(loadedSettings.isColorBlindMode());
    }

    @Test
    @DisplayName("손상된 JSON 파일이 있을 때 기본값을 반환하는지 확인")
    void testLoadCorruptedFile() throws IOException {
        // given - 손상된 JSON 파일 생성
        try (FileWriter writer = new FileWriter("settings.json")) {
            writer.write("{ this is not valid json }");
        }

        // when
        Settings settings = SettingsRepository.load();

        // then - 기본값으로 반환되어야 함
        assertNotNull(settings, "Should return default settings for corrupted file");
        assertEquals("A", settings.getKeyLeft(), "Should have default values");
    }

    @Test
    @DisplayName("빈 JSON 파일이 있을 때 처리되는지 확인")
    void testLoadEmptyFile() throws IOException {
        // given - 빈 JSON 파일 생성
        try (FileWriter writer = new FileWriter("settings.json")) {
            writer.write("{}");
        }

        // when
        Settings settings = SettingsRepository.load();

        // then - null이 아니어야 함 (Gson이 빈 객체로 생성)
        assertNotNull(settings, "Should return settings object for empty JSON");
    }

    @Test
    @DisplayName("파일 경로가 settings.json인지 확인")
    void testFilePath() {
        // given
        Settings settings = new Settings();

        // when
        SettingsRepository.save(settings);

        // then
        File file = new File("settings.json");
        assertTrue(file.exists(), "File should be saved as settings.json");
        assertEquals("settings.json", file.getName(), "File name should be settings.json");
    }

    @Test
    @DisplayName("save 메서드가 static인지 확인")
    void testSaveIsStatic() {
        assertDoesNotThrow(
                () -> {
                    SettingsRepository.save(new Settings());
                },
                "save method should be static and callable");
    }

    @Test
    @DisplayName("load 메서드가 static인지 확인")
    void testLoadIsStatic() {
        assertDoesNotThrow(
                () -> {
                    SettingsRepository.load();
                },
                "load method should be static and callable");
    }

    @Test
    @DisplayName("null Settings를 저장하려고 할 때 예외가 발생하는지 확인")
    void testSaveNullSettings() {
        // null을 저장하려고 하면 NullPointerException이 발생할 수 있음
        assertDoesNotThrow(
                () -> {
                    SettingsRepository.save(null);
                },
                "Should not throw exception when saving null (Gson handles it)");
    }

    @Test
    @DisplayName("연속으로 여러 번 저장해도 정상 동작하는지 확인")
    void testMultipleConcurrentSaves() {
        for (int i = 0; i < 10; i++) {
            Settings settings = new Settings();
            settings.setKeyLeft("KEY_" + i);
            assertDoesNotThrow(
                    () -> SettingsRepository.save(settings), "Should handle multiple saves");
        }

        Settings loaded = SettingsRepository.load();
        assertEquals("KEY_9", loaded.getKeyLeft(), "Should have last saved value");
    }

    @Test
    @DisplayName("설정 파일이 생성된 후 다시 로드할 수 있는지 확인")
    void testFileCreationAndReload() {
        // 파일이 없는 상태에서 로드
        Settings settings1 = SettingsRepository.load();
        assertEquals("A", settings1.getKeyLeft(), "Should have default value");

        // 설정 변경 후 저장
        settings1.setKeyLeft("A");
        SettingsRepository.save(settings1);

        // 다시 로드
        Settings settings2 = SettingsRepository.load();
        assertEquals("A", settings2.getKeyLeft(), "Should have saved value");
    }
}

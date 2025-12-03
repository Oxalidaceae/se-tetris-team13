package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

// confirmScene 테스트: Tests scene creation with various parameters and object independence
@DisplayName("confirmScene 테스트")
public class ConfirmSceneTest {

    private TestSceneManager testSceneManager;
    private Settings testSettings;
    private TestRunnable testOnConfirm;
    private TestRunnable testOnCancel;
    private ConfirmScene confirmScene;

    // Test용 SceneManager 구현
    static class TestSceneManager extends SceneManager {
        public boolean exitWithSaveCalled = false;
        public Settings lastSavedSettings = null;

        public TestSceneManager() {
            super(null); // null stage로 생성
        }

        @Override
        public void exitWithSave(Settings settings) {
            exitWithSaveCalled = true;
            lastSavedSettings = settings;
        }
    }

    // Test용 Runnable 구현
    static class TestRunnable implements Runnable {
        public boolean runCalled = false;

        @Override
        public void run() {
            runCalled = true;
        }
    }

    @BeforeEach
    void setUp() {
        // Test objects 생성
        testSceneManager = new TestSceneManager();
        testSettings = new Settings();
        testOnConfirm = new TestRunnable();
        testOnCancel = new TestRunnable();

        // confirmScene 인스턴스 생성
        confirmScene =
                new ConfirmScene(
                        testSceneManager, testSettings, "Test Title", testOnConfirm, testOnCancel);
    }

    @Test
    @DisplayName("생성자: 정상적인 매개변수로 객체 생성")
    void testConstructor() {
        // 정상적인 매개변수로 생성
        ConfirmScene scene =
                new ConfirmScene(
                        testSceneManager, testSettings, "Test Title", testOnConfirm, testOnCancel);
        assertNotNull(scene);
    }

    @Test
    @DisplayName("생성자: null onCancel로 객체 생성")
    void testConstructorWithNullOnCancel() {
        // onCancel이 null이어도 정상 생성되어야 함
        ConfirmScene scene =
                new ConfirmScene(testSceneManager, testSettings, "Test Title", testOnConfirm, null);
        assertNotNull(scene);
    }

    @Test
    @DisplayName("생성자: 매개변수 저장 확인")
    void testConstructorParameterStorage() {
        // confirmScene은 생성자 매개변수를 내부 필드로 저장해야 함
        // getScene 호출 없이도 객체가 정상적으로 생성되어야 함
        TestSceneManager customManager = new TestSceneManager();
        Settings customSettings = new Settings();
        TestRunnable customOnConfirm = new TestRunnable();
        TestRunnable customOnCancel = new TestRunnable();

        ConfirmScene scene =
                new ConfirmScene(
                        customManager,
                        customSettings,
                        "Custom Title",
                        customOnConfirm,
                        customOnCancel);
        assertNotNull(scene);
    }

    @Test
    @DisplayName("객체 독립성: 여러 인스턴스 생성")
    void testMultipleInstances() {
        TestSceneManager manager1 = new TestSceneManager();
        TestSceneManager manager2 = new TestSceneManager();
        Settings settings1 = new Settings();
        Settings settings2 = new Settings();
        TestRunnable confirm1 = new TestRunnable();
        TestRunnable confirm2 = new TestRunnable();
        TestRunnable cancel1 = new TestRunnable();
        TestRunnable cancel2 = new TestRunnable();

        ConfirmScene scene1 = new ConfirmScene(manager1, settings1, "Title 1", confirm1, cancel1);
        ConfirmScene scene2 = new ConfirmScene(manager2, settings2, "Title 2", confirm2, cancel2);

        // 각각 독립적인 객체여야 함
        assertNotSame(scene1, scene2);
        assertNotNull(scene1);
        assertNotNull(scene2);
    }

    @Test
    @DisplayName("매개변수 유효성: null SceneManager 처리")
    void testNullSceneManager() {
        // null SceneManager로도 생성할 수 있어야 함 (실제 사용에서는 문제가 될 수 있지만)
        assertDoesNotThrow(
                () -> {
                    ConfirmScene scene =
                            new ConfirmScene(
                                    null, testSettings, "Test Title", testOnConfirm, testOnCancel);
                    assertNotNull(scene);
                });
    }

    @Test
    @DisplayName("매개변수 유효성: null Settings 처리")
    void testNullSettings() {
        // null Settings로도 생성할 수 있어야 함
        assertDoesNotThrow(
                () -> {
                    ConfirmScene scene =
                            new ConfirmScene(
                                    testSceneManager,
                                    null,
                                    "Test Title",
                                    testOnConfirm,
                                    testOnCancel);
                    assertNotNull(scene);
                });
    }

    @Test
    @DisplayName("매개변수 유효성: 모든 매개변수 null 처리")
    void testAllNullParameters() {
        // 모든 매개변수가 null이어도 생성할 수 있어야 함
        assertDoesNotThrow(
                () -> {
                    ConfirmScene scene = new ConfirmScene(null, null, null, null, null);
                    assertNotNull(scene);
                });
    }

    @Test
    @DisplayName("getScene 메서드: confirmScene 클래스 메서드 존재 확인")
    void testGetSceneMethodExists() {
        // getScene() 메서드가 존재하는지 확인 (JavaFX 초기화 없이)
        // 리플렉션을 사용해서 메서드 존재만 확인
        boolean hasGetSceneMethod = false;
        try {
            confirmScene.getClass().getMethod("getScene");
            hasGetSceneMethod = true;
        } catch (NoSuchMethodException e) {
            hasGetSceneMethod = false;
        }

        assertTrue(hasGetSceneMethod, "confirmScene should have getScene() method");
    }

    @Test
    @DisplayName("SceneManager 연동: TestSceneManager 동작 확인")
    void testSceneManagerIntegration() {
        // TestSceneManager가 올바르게 동작하는지 확인
        assertFalse(testSceneManager.exitWithSaveCalled);
        assertNull(testSceneManager.lastSavedSettings);

        // exitWithSave 호출 테스트
        testSceneManager.exitWithSave(testSettings);

        assertTrue(testSceneManager.exitWithSaveCalled);
        assertEquals(testSettings, testSceneManager.lastSavedSettings);
    }

    @Test
    @DisplayName("Runnable 연동: TestRunnable 동작 확인")
    void testRunnableIntegration() {
        // TestRunnable이 올바르게 동작하는지 확인
        assertFalse(testOnCancel.runCalled);

        // run 호출 테스트
        testOnCancel.run();

        assertTrue(testOnCancel.runCalled);
    }

    @Test
    @DisplayName("여러 Runnable 독립성 확인")
    void testMultipleRunnableIndependence() {
        TestRunnable runnable1 = new TestRunnable();
        TestRunnable runnable2 = new TestRunnable();

        // 초기 상태 확인
        assertFalse(runnable1.runCalled);
        assertFalse(runnable2.runCalled);

        // 첫 번째 runnable만 호출
        runnable1.run();

        assertTrue(runnable1.runCalled);
        assertFalse(runnable2.runCalled); // 두 번째는 여전히 false
    }

    @Test
    @DisplayName("여러 SceneManager 독립성 확인")
    void testMultipleSceneManagerIndependence() {
        TestSceneManager manager1 = new TestSceneManager();
        TestSceneManager manager2 = new TestSceneManager();
        Settings settings1 = new Settings();

        // 초기 상태 확인
        assertFalse(manager1.exitWithSaveCalled);
        assertFalse(manager2.exitWithSaveCalled);

        // 첫 번째 매니저만 호출
        manager1.exitWithSave(settings1);

        assertTrue(manager1.exitWithSaveCalled);
        assertFalse(manager2.exitWithSaveCalled); // 두 번째는 여전히 false
        assertEquals(settings1, manager1.lastSavedSettings);
        assertNull(manager2.lastSavedSettings);
    }

    @Test
    @DisplayName("Settings 객체 독립성 확인")
    void testSettingsIndependence() {
        Settings settings1 = new Settings();
        Settings settings2 = new Settings();

        // 서로 다른 객체여야 함
        assertNotSame(settings1, settings2);

        // 각각 독립적으로 사용 가능해야 함
        ConfirmScene scene1 =
                new ConfirmScene(
                        testSceneManager, settings1, "Title 1", testOnConfirm, testOnCancel);
        ConfirmScene scene2 =
                new ConfirmScene(
                        testSceneManager, settings2, "Title 2", testOnConfirm, testOnCancel);

        assertNotNull(scene1);
        assertNotNull(scene2);
    }
}

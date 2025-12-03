package team13.tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// App class 테스트: Tests basic application initialization and lifecycle
@DisplayName("App 테스트")
public class AppTest {

    @BeforeAll
    static void initJavaFX() {
        // Monocle headless 환경 설정
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");

        // JavaFX 툴킷 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // 이미 초기화되었으면 무시
        }
    }

    @Test
    @DisplayName("App 인스턴스를 생성할 수 있는지 확인")
    void testAppInstantiation() {
        assertDoesNotThrow(
                () -> {
                    App app = new App();
                    assertNotNull(app, "App instance should not be null");
                });
    }

    @Test
    @DisplayName("App의 start 메서드가 Stage를 받아 실행될 수 있는지 확인")
    void testAppStart() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        assertDoesNotThrow(
                                () -> {
                                    app.start(stage);
                                },
                                "App.start() should not throw exception");

                        assertNotNull(stage.getScene(), "Stage should have a scene after start");
                        assertEquals("Tetris", stage.getTitle(), "Stage title should be 'Tetris'");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App이 설정을 로드하고 초기 화면을 표시하는지 확인")
    void testAppInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        assertNotNull(stage.getScene(), "Scene should be initialized");
                        assertNotNull(stage.getScene().getRoot(), "Scene should have root node");
                        assertFalse(
                                stage.getScene().getStylesheets().isEmpty(),
                                "Scene should have stylesheets applied");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App이 창 크기를 올바르게 설정하는지 확인 (기본값)")
    void testDefaultWindowSize() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        // 기본값은 MEDIUM (600x700)
                        assertTrue(
                                stage.getWidth() >= 550 && stage.getWidth() <= 650,
                                "Default width should be around 600 (MEDIUM)");
                        assertTrue(
                                stage.getHeight() >= 650 && stage.getHeight() <= 750,
                                "Default height should be around 700 (MEDIUM)");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App이 Stage 타이틀을 설정하는지 확인")
    void testStageTitle() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        assertEquals(
                                "Tetris",
                                stage.getTitle(),
                                "Stage title should be set to 'Tetris'");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App이 여러 번 생성될 수 있는지 확인")
    void testMultipleAppInstances() {
        assertDoesNotThrow(
                () -> {
                    App app1 = new App();
                    App app2 = new App();

                    assertNotNull(app1, "First app instance should not be null");
                    assertNotNull(app2, "Second app instance should not be null");
                    assertNotSame(app1, app2, "Each instance should be different");
                });
    }

    @Test
    @DisplayName("App.start()가 SceneManager를 초기화하는지 확인")
    void testSceneManagerInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        // SceneManager가 정상 동작하면 Scene이 설정됨
                        assertNotNull(
                                stage.getScene(), "SceneManager should initialize and set a scene");
                        assertNotNull(
                                stage.getScene().getRoot(),
                                "Scene should have a root node from SceneManager");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App이 Settings를 로드하는지 확인")
    void testSettingsLoading() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        // Settings 로딩 중 예외가 발생하지 않아야 함
                        assertDoesNotThrow(
                                () -> {
                                    app.start(stage);
                                },
                                "Settings loading should not throw exception");

                        assertNotNull(
                                stage.getScene(), "Scene should be created after loading settings");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App이 CSS 스타일시트를 적용하는지 확인")
    void testStylesheetApplication() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        assertNotNull(stage.getScene(), "Scene should exist");
                        assertFalse(
                                stage.getScene().getStylesheets().isEmpty(),
                                "Stylesheets should be applied to the scene");

                        String stylesheet = stage.getScene().getStylesheets().get(0);
                        assertTrue(
                                stylesheet.contains("application.css")
                                        || stylesheet.contains("colorblind.css"),
                                "Stylesheet should be either application.css or colorblind.css");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("main 메서드가 존재하는지 확인")
    void testMainMethodExists() {
        assertDoesNotThrow(
                () -> {
                    App.class.getMethod("main", String[].class);
                },
                "App should have a main method");
    }

    @Test
    @DisplayName("getCurrentWindowSize 메서드 테스트 - private 메서드 리플렉션")
    void testGetCurrentWindowSizeMethod() throws Exception {
        // given
        App app = new App();

        // when & then - private 메서드 접근 테스트
        java.lang.reflect.Method method =
                App.class.getDeclaredMethod(
                        "getCurrentWindowSize", Stage.class, SceneManager.class);
        assertNotNull(method, "getCurrentWindowSize 메서드가 존재해야 함");
        method.setAccessible(true);

        // Mock SceneManager 생성하여 테스트 (Stage 없이)
        SceneManager mockManager = createMockSceneManager(600);

        String result = (String) method.invoke(app, null, mockManager);
        assertEquals("MEDIUM", result, "600px 너비는 MEDIUM을 반환해야 함");
    }

    @Test
    @DisplayName("창 크기별 getCurrentWindowSize 테스트")
    void testGetCurrentWindowSizeRanges() throws Exception {
        // given
        App app = new App();
        java.lang.reflect.Method method =
                App.class.getDeclaredMethod(
                        "getCurrentWindowSize", Stage.class, SceneManager.class);
        method.setAccessible(true);

        // when & then - SMALL 범위 (450 이하)
        SceneManager smallManager = createMockSceneManager(400);
        String smallResult = (String) method.invoke(app, null, smallManager);
        assertEquals("SMALL", smallResult, "400px는 SMALL을 반환해야 함");

        // MEDIUM 범위 (451-749)
        SceneManager mediumManager = createMockSceneManager(600);
        String mediumResult = (String) method.invoke(app, null, mediumManager);
        assertEquals("MEDIUM", mediumResult, "600px는 MEDIUM을 반환해야 함");

        // LARGE 범위 (750 이상)
        SceneManager largeManager = createMockSceneManager(800);
        String largeResult = (String) method.invoke(app, null, largeManager);
        assertEquals("LARGE", largeResult, "800px는 LARGE를 반환해야 함");
    }

    @Test
    @DisplayName("창 크기 경계값 테스트")
    void testWindowSizeBoundaryValues() throws Exception {
        // given
        App app = new App();
        java.lang.reflect.Method method =
                App.class.getDeclaredMethod(
                        "getCurrentWindowSize", Stage.class, SceneManager.class);
        method.setAccessible(true);

        // when & then - 경계값 테스트
        // 450 (SMALL의 상한)
        SceneManager manager450 = createMockSceneManager(450);
        String result450 = (String) method.invoke(app, null, manager450);
        assertEquals("SMALL", result450, "450px는 SMALL을 반환해야 함");

        // 451 (MEDIUM의 하한)
        SceneManager manager451 = createMockSceneManager(451);
        String result451 = (String) method.invoke(app, null, manager451);
        assertEquals("MEDIUM", result451, "451px는 MEDIUM을 반환해야 함");

        // 749 (MEDIUM의 상한)
        SceneManager manager749 = createMockSceneManager(749);
        String result749 = (String) method.invoke(app, null, manager749);
        assertEquals("MEDIUM", result749, "749px는 MEDIUM을 반환해야 함");

        // 750 (LARGE의 하한)
        SceneManager manager750 = createMockSceneManager(750);
        String result750 = (String) method.invoke(app, null, manager750);
        assertEquals("LARGE", result750, "750px는 LARGE를 반환해야 함");
    }

    @Test
    @DisplayName("다양한 창 크기에 대한 포괄적 테스트")
    void testVariousWindowSizes() throws Exception {
        // given
        App app = new App();
        java.lang.reflect.Method method =
                App.class.getDeclaredMethod(
                        "getCurrentWindowSize", Stage.class, SceneManager.class);
        method.setAccessible(true);

        // when & then
        double[] testWidths = {200, 300, 400, 450, 451, 600, 700, 749, 750, 800, 1000};
        String[] expectedSizes = {
            "SMALL", "SMALL", "SMALL", "SMALL", "MEDIUM", "MEDIUM", "MEDIUM", "MEDIUM", "LARGE",
            "LARGE", "LARGE"
        };

        for (int i = 0; i < testWidths.length; i++) {
            SceneManager manager = createMockSceneManager(testWidths[i]);
            String result = (String) method.invoke(app, null, manager);
            assertEquals(
                    expectedSizes[i],
                    result,
                    String.format("너비 %.0f는 %s를 반환해야 함", testWidths[i], expectedSizes[i]));
        }
    }

    @Test
    @DisplayName("Stage 종료 이벤트 핸들러 등록 테스트")
    void testStageCloseRequestHandler() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        // Stage에 종료 핸들러가 등록되었는지 확인
                        assertNotNull(stage.getOnCloseRequest(), "Stage에 종료 이벤트 핸들러가 등록되어야 함");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("App 시작 시 SceneManager 설정 확인")
    void testSceneManagerConfiguration() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        javafx.application.Platform.runLater(
                () -> {
                    try {
                        App app = new App();
                        Stage stage = new Stage();

                        app.start(stage);

                        // Stage가 보여지는지 확인 (show() 호출됨)
                        assertTrue(stage.isShowing(), "Stage가 표시되어야 함");

                        // 타이틀이 설정되었는지 확인
                        assertEquals("Tetris", stage.getTitle(), "Stage 타이틀이 'Tetris'로 설정되어야 함");
                    } finally {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    @DisplayName("극단적인 창 크기 값 테스트")
    void testExtremeWindowSizes() throws Exception {
        // given
        App app = new App();
        java.lang.reflect.Method method =
                App.class.getDeclaredMethod(
                        "getCurrentWindowSize", Stage.class, SceneManager.class);
        method.setAccessible(true);

        // when & then - 극단적인 값들
        double[] extremeWidths = {1, 50, 100, 1920, 2560, 5000};
        String[] expectedSizes = {"SMALL", "SMALL", "SMALL", "LARGE", "LARGE", "LARGE"};

        for (int i = 0; i < extremeWidths.length; i++) {
            SceneManager manager = createMockSceneManager(extremeWidths[i]);
            String result = (String) method.invoke(app, null, manager);
            assertEquals(
                    expectedSizes[i],
                    result,
                    String.format("극단적 너비 %.0f는 %s를 반환해야 함", extremeWidths[i], expectedSizes[i]));
        }
    }

    @Test
    @DisplayName("App 인스턴스 독립성 테스트")
    void testAppInstanceIndependence() {
        // given & when
        App app1 = new App();
        App app2 = new App();

        // then
        assertNotNull(app1, "첫 번째 App 인스턴스가 null이 아니어야 함");
        assertNotNull(app2, "두 번째 App 인스턴스가 null이 아니어야 함");
        assertNotSame(app1, app2, "각 App 인스턴스는 서로 다른 객체여야 함");
    }

    @Test
    @DisplayName("App 메서드 존재성 테스트")
    void testAppMethodExistence() {
        // given
        Class<App> appClass = App.class;

        // when & then
        assertDoesNotThrow(
                () -> {
                    // start 메서드 확인
                    java.lang.reflect.Method startMethod = appClass.getMethod("start", Stage.class);
                    assertNotNull(startMethod, "start 메서드가 존재해야 함");

                    // main 메서드 확인
                    java.lang.reflect.Method mainMethod =
                            appClass.getMethod("main", String[].class);
                    assertNotNull(mainMethod, "main 메서드가 존재해야 함");
                    assertTrue(
                            java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                            "main 메서드는 static이어야 함");

                    // getCurrentWindowSize 메서드 확인
                    java.lang.reflect.Method getCurrentWindowSizeMethod =
                            appClass.getDeclaredMethod(
                                    "getCurrentWindowSize", Stage.class, SceneManager.class);
                    assertNotNull(getCurrentWindowSizeMethod, "getCurrentWindowSize 메서드가 존재해야 함");
                });
    }

    // Mock SceneManager를 생성하는 헬퍼 메서드
    private SceneManager createMockSceneManager(double width) {
        return new MockSceneManager(width);
    }

    // Mock SceneManager 클래스
    private static class MockSceneManager extends SceneManager {
        private final double width;

        public MockSceneManager(double width) {
            super(null); // Stage는 null로 설정 (테스트용)
            this.width = width;
        }

        @Override
        public double getOriginalWidth() {
            return width;
        }

        @Override
        public void showMainMenu(team13.tetris.config.Settings settings) {
            // Mock implementation
        }

        @Override
        public void setColorBlindMode(boolean colorBlindMode) {
            // Mock implementation
        }

        @Override
        public void setWindowSize(int width, int height) {
            // Mock implementation
        }

        @Override
        public boolean isColorBlindMode() {
            return false;
        }
    }
}

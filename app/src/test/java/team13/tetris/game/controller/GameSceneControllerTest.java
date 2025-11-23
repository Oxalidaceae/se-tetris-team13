package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.input.KeyInputHandler;

// GameSceneController 테스트: Tests core logic, engine interaction, key input handling, and game state
// changes
@DisplayName("GameSceneController 테스트")
public class GameSceneControllerTest {

    private GameSceneController controller;
    private Settings settings;
    private KeyInputHandler keyInputHandler;
    private TestSceneManager mockSceneManager;
    private TestEngine testEngine;

    // 테스트용 SceneManager (메서드 호출 추적)
    private static class TestSceneManager extends SceneManager {
        int showConfirmSceneCalled = 0;
        int restorePreviousSceneCalled = 0;

        public TestSceneManager() {
            super(null); // Stage는 테스트에서 필요 없음
        }

        @Override
        public void showConfirmScene(
                Settings settings, String title, Runnable onConfirm, Runnable onCancel) {
            showConfirmSceneCalled++;
        }

        @Override
        public void restorePreviousScene() {
            restorePreviousSceneCalled++;
        }
    }

    // 테스트용 GameEngine (메서드 호출 추적)
    private static class TestEngine extends GameEngine {
        int startAutoDropCalled = 0;
        int stopAutoDropCalled = 0;
        int moveLeftCalled = 0;
        int moveRightCalled = 0;
        int softDropCalled = 0;
        int hardDropCalled = 0;
        int rotateCWCalled = 0;
        int updateSpeedCalled = 0;

        public TestEngine(Board board, GameStateListener listener) {
            super(board, listener);
        }

        @Override
        public void startAutoDrop() {
            startAutoDropCalled++;
        }

        @Override
        public void stopAutoDrop() {
            stopAutoDropCalled++;
        }

        @Override
        public void moveLeft() {
            moveLeftCalled++;
        }

        @Override
        public void moveRight() {
            moveRightCalled++;
        }

        @Override
        public boolean softDrop() {
            softDropCalled++;
            return super.softDrop();
        }

        @Override
        public void hardDrop() {
            hardDropCalled++;
        }

        @Override
        public void rotateCW() {
            rotateCWCalled++;
        }

        @Override
        public void updateSpeedForLinesCleared(int lines, int totalLines) {
            updateSpeedCalled++;
        }

        void reset() {
            startAutoDropCalled = 0;
            stopAutoDropCalled = 0;
            moveLeftCalled = 0;
            moveRightCalled = 0;
            softDropCalled = 0;
            hardDropCalled = 0;
            rotateCWCalled = 0;
            updateSpeedCalled = 0;
        }
    }

    @BeforeEach
    void setUp() {
        settings = new Settings();
        keyInputHandler = new KeyInputHandler(settings);

        // 테스트용 SceneManager
        mockSceneManager = new TestSceneManager();

        // GameScene을 null로 전달 (UI 의존성 회피)
        controller = new GameSceneController(null, mockSceneManager, settings, keyInputHandler);

        Board board = new Board(10, 20);
        testEngine = new TestEngine(board, controller);
        controller.setEngine(testEngine);
    }

    @AfterEach
    void tearDown() {
        if (testEngine != null) testEngine.shutdown();
        controller = null;
        testEngine = null;
        settings = null;
        keyInputHandler = null;
        mockSceneManager = null;
    }

    // GameController 인터페이스 메서드 테스트
    @Test
    @DisplayName("start: 엔진의 startAutoDrop 호출")
    void testStart() {
        controller.start();
        assertEquals(1, testEngine.startAutoDropCalled);
    }

    @Test
    @DisplayName("moveLeft: 엔진의 moveLeft 호출")
    void testMoveLeft() {
        controller.moveLeft();
        assertEquals(1, testEngine.moveLeftCalled);
    }

    @Test
    @DisplayName("moveRight: 엔진의 moveRight 호출")
    void testMoveRight() {
        controller.moveRight();
        assertEquals(1, testEngine.moveRightCalled);
    }

    @Test
    @DisplayName("softDrop: 엔진의 softDrop 호출")
    void testSoftDrop() {
        controller.softDrop();
        assertEquals(1, testEngine.softDropCalled);
    }

    @Test
    @DisplayName("hardDrop: 엔진의 hardDrop 호출")
    void testHardDrop() {
        controller.hardDrop();
        assertEquals(1, testEngine.hardDropCalled);
    }

    @Test
    @DisplayName("rotateCW: 엔진의 rotateCW 호출")
    void testRotateCW() {
        controller.rotateCW();
        assertEquals(1, testEngine.rotateCWCalled);
    }

    @Test
    @DisplayName("hardDrop throttling: 100ms 이내 연속 호출 제한")
    void testHardDropThrottling() throws InterruptedException {
        // 첫 번째 호출
        controller.hardDrop();
        assertEquals(1, testEngine.hardDropCalled);

        // 즉시 두 번째 호출 (throttle 적용됨)
        controller.hardDrop();
        assertEquals(1, testEngine.hardDropCalled, "100ms 이내 호출은 무시됨");

        // 110ms 대기 후 세 번째 호출
        Thread.sleep(110);
        controller.hardDrop();
        assertEquals(2, testEngine.hardDropCalled, "100ms 이후 호출은 허용됨");
    }

    @Test
    @DisplayName("엔진이 null일 때 start는 안전하게 무시됨")
    void testStartWithNullEngine() {
        controller.setEngine(null);
        assertDoesNotThrow(() -> controller.start());
    }

    @Test
    @DisplayName("엔진이 null일 때 이동 명령들은 안전하게 무시됨")
    void testMovementWithNullEngine() {
        controller.setEngine(null);

        assertDoesNotThrow(() -> controller.moveLeft());
        assertDoesNotThrow(() -> controller.moveRight());
        assertDoesNotThrow(() -> controller.softDrop());
        assertDoesNotThrow(() -> controller.hardDrop());
        assertDoesNotThrow(() -> controller.rotateCW());
    }

    // KeyInputCallback 인터페이스 메서드 테스트
    @Test
    @DisplayName("onLeftPressed: moveLeft 호출")
    void testOnLeftPressed() {
        controller.onLeftPressed();
        assertEquals(1, testEngine.moveLeftCalled);
    }

    @Test
    @DisplayName("onRightPressed: moveRight 호출")
    void testOnRightPressed() {
        controller.onRightPressed();
        assertEquals(1, testEngine.moveRightCalled);
    }

    @Test
    @DisplayName("onRotatePressed: rotateCW 호출")
    void testOnRotatePressed() {
        controller.onRotatePressed();
        assertEquals(1, testEngine.rotateCWCalled);
    }

    @Test
    @DisplayName("onDropPressed: softDrop 호출")
    void testOnDropPressed() {
        controller.onDropPressed();
        assertEquals(1, testEngine.softDropCalled);
    }

    @Test
    @DisplayName("onHardDropPressed: hardDrop 호출")
    void testOnHardDropPressed() {
        controller.onHardDropPressed();
        assertEquals(1, testEngine.hardDropCalled);
    }

    @Test
    @DisplayName("onPausePressed: pause 메서드 호출하지만 UI 의존성으로 제한적 테스트")
    void testOnPausePressed() {
        // pause()는 showPauseWindow()를 호출하고 Platform.runLater 사용
        // UI 의존성으로 인해 실제 동작은 테스트하기 어려움
        // JavaFX가 초기화된 환경에서는 정상 작동, 아니면 예외 발생 가능
        try {
            controller.onPausePressed();
            // JavaFX가 초기화되어 있으면 정상 실행
        } catch (IllegalStateException e) {
            // JavaFX가 초기화되지 않았으면 예외 발생 가능
            assertTrue(
                    e.getMessage().contains("Toolkit") || e.getMessage().contains("toolkit"),
                    "JavaFX Toolkit 관련 예외여야 함");
        }
    }

    // GameStateListener 인터페이스 메서드 테스트
    @Test
    @DisplayName("onBoardUpdated: GameScene이 null이면 NullPointerException")
    void testOnBoardUpdated() {
        Board board = new Board(10, 20);
        assertThrows(NullPointerException.class, () -> controller.onBoardUpdated(board));
    }

    @Test
    @DisplayName("onPieceSpawned: GameScene이 null이면 NullPointerException")
    void testOnPieceSpawned() {
        Tetromino piece = new Tetromino(Tetromino.Kind.I);
        assertThrows(NullPointerException.class, () -> controller.onPieceSpawned(piece, 3, 0));
    }

    @Test
    @DisplayName("onLinesCleared: updateSpeedForLinesCleared 호출 후 NPE")
    void testOnLinesCleared() {
        // updateSpeedForLinesCleared는 호출되지만 gameScene.updateGrid()에서 NPE
        assertThrows(NullPointerException.class, () -> controller.onLinesCleared(2));
        assertEquals(1, testEngine.updateSpeedCalled);
    }

    @Test
    @DisplayName("onNextPiece: GameScene이 null이면 NullPointerException")
    void testOnNextPiece() {
        Tetromino piece = new Tetromino(Tetromino.Kind.T);
        assertThrows(NullPointerException.class, () -> controller.onNextPiece(piece));
    }

    @Test
    @DisplayName("onScoreChanged: GameScene이 null이면 NullPointerException")
    void testOnScoreChanged() {
        assertThrows(NullPointerException.class, () -> controller.onScoreChanged(1000));
    }

    @Test
    @DisplayName("onGameOver: stopAutoDrop 호출 후 NPE")
    void testOnGameOver() {
        // stopAutoDrop은 호출되지만 gameScene.showGameOver()에서 NPE
        assertThrows(NullPointerException.class, () -> controller.onGameOver());
        assertEquals(1, testEngine.stopAutoDropCalled);
    }

    // 게임 상태 변화 테스트
    @Test
    @DisplayName("게임오버 후 모든 이동 명령이 차단됨")
    void testGameOverBlocksMovement() {
        // 게임오버 상태로 만들기 (NPE 무시)
        try {
            controller.onGameOver();
        } catch (NullPointerException e) {
            // 예상된 예외
        }

        // 모든 이동 명령 시도
        testEngine.reset();
        controller.moveLeft();
        controller.moveRight();
        controller.softDrop();
        controller.hardDrop();
        controller.rotateCW();

        // 게임오버 후에는 아무 명령도 엔진에 전달되지 않음
        assertEquals(0, testEngine.moveLeftCalled);
        assertEquals(0, testEngine.moveRightCalled);
        assertEquals(0, testEngine.softDropCalled);
        assertEquals(0, testEngine.hardDropCalled);
        assertEquals(0, testEngine.rotateCWCalled);
    }

    @Test
    @DisplayName("게임오버 후 키 입력도 차단됨")
    void testGameOverBlocksKeyInput() {
        // 게임오버 상태로 만들기
        try {
            controller.onGameOver();
        } catch (NullPointerException e) {
            // 예상된 예외
        }

        // 키 입력 시도
        testEngine.reset();
        controller.onLeftPressed();
        controller.onRightPressed();
        controller.onRotatePressed();
        controller.onDropPressed();
        controller.onHardDropPressed();

        // 모든 키 입력이 차단됨
        assertEquals(0, testEngine.moveLeftCalled);
        assertEquals(0, testEngine.moveRightCalled);
        assertEquals(0, testEngine.rotateCWCalled);
        assertEquals(0, testEngine.softDropCalled);
        assertEquals(0, testEngine.hardDropCalled);
    }

    // 엣지 케이스 테스트
    @Test
    @DisplayName("hardDrop throttling 정확한 타이밍 테스트")
    void testHardDropThrottlingPreciseTiming() throws InterruptedException {
        controller.hardDrop();
        assertEquals(1, testEngine.hardDropCalled);

        // 50ms 대기 (아직 throttle 적용)
        Thread.sleep(50);
        controller.hardDrop();
        assertEquals(1, testEngine.hardDropCalled);

        // 추가 60ms 대기 (총 110ms, throttle 해제)
        Thread.sleep(60);
        controller.hardDrop();
        assertEquals(2, testEngine.hardDropCalled);
    }

    @Test
    @DisplayName("엔진 변경 후 새 엔진에 명령 전달")
    void testEngineChange() {
        // 기존 엔진에 명령
        controller.moveLeft();
        assertEquals(1, testEngine.moveLeftCalled);

        // 새 엔진으로 교체
        Board newBoard = new Board(10, 20);
        TestEngine newEngine = new TestEngine(newBoard, controller);
        controller.setEngine(newEngine);

        // 새 엔진에 명령 전달 확인
        controller.moveRight();
        assertEquals(1, testEngine.moveLeftCalled); // 기존 엔진은 변화 없음
        assertEquals(0, testEngine.moveRightCalled);
        assertEquals(1, newEngine.moveRightCalled); // 새 엔진에 전달됨

        newEngine.shutdown();
    }

    // SceneManager 연동 테스트
    @Test
    @DisplayName("SceneManager와 함께 생성되는지 확인")
    void testSceneManagerIntegration() {
        // 생성자에서 SceneManager를 받았는지 확인
        assertNotNull(controller);

        // SceneManager가 정상적으로 설정되었는지 간접 확인
        // (실제 호출은 pause window에서 사용자 선택에 따라 결정됨)
        assertEquals(0, mockSceneManager.showConfirmSceneCalled);
        assertEquals(0, mockSceneManager.restorePreviousSceneCalled);
    }

    @Test
    @DisplayName("리팩토링된 생성자 시그니처 확인")
    void testRefactoredConstructor() {
        // 새로운 생성자가 SceneManager를 포함하는지 확인
        Settings newSettings = new Settings();
        KeyInputHandler newKeyHandler = new KeyInputHandler(newSettings);
        TestSceneManager newSceneManager = new TestSceneManager();

        GameSceneController newController =
                new GameSceneController(
                        null, // GameScene
                        newSceneManager, // SceneManager
                        newSettings, // Settings
                        newKeyHandler // KeyInputHandler
                        );

        assertNotNull(newController);
    }

    @Test
    @DisplayName("하드드롭 쓰로틀링 기능 테스트 - 기본")
    void testHardDropThrottlingBasic() {
        // given
        controller.setEngine(testEngine);

        // when - 빠른 연속 하드드롭 시도
        controller.hardDrop();
        int firstCount = testEngine.hardDropCalled;

        // 100ms 이내 재시도 (쓰로틀링 적용되어야 함)
        controller.hardDrop();
        int secondCount = testEngine.hardDropCalled;

        // then
        assertEquals(1, firstCount, "첫 번째 하드드롭은 실행되어야 함");
        assertEquals(1, secondCount, "100ms 이내 두 번째 하드드롭은 쓰로틀링되어야 함");
    }

    @Test
    @DisplayName("라인 클리어 시 속도 업데이트 테스트 - GameScene null")
    void testLinesCleared() {
        // given
        controller.setEngine(testEngine);

        // when & then - GameScene이 null이므로 NPE 발생 예상
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onLinesCleared(2);
                },
                "GameScene이 null일 때 NPE 발생");

        // 하지만 속도 업데이트는 여전히 호출되어야 함
        assertEquals(1, testEngine.updateSpeedCalled, "라인 클리어 시 속도 업데이트가 호출되어야 함");
    }

    @Test
    @DisplayName("다중 라인 클리어 시 총 라이인 수 누적 테스트 - GameScene null")
    void testMultipleLinesCleared() {
        // given
        controller.setEngine(testEngine);

        // when & then - 각 호출마다 NPE 발생 예상
        assertThrows(NullPointerException.class, () -> controller.onLinesCleared(1));
        assertThrows(NullPointerException.class, () -> controller.onLinesCleared(3));
        assertThrows(NullPointerException.class, () -> controller.onLinesCleared(2));

        // then - 속도 업데이트는 여전히 호출되어야 함
        assertEquals(3, testEngine.updateSpeedCalled, "각 라인 클리어마다 속도 업데이트가 호출되어야 함");
    }

    @Test
    @DisplayName("게임오버 후 컨트롤 무효화 테스트")
    void testGameOverControlsDisabled() {
        // given
        controller.setEngine(testEngine);

        // when - GameScene이 null이므로 NPE 발생하지만 컨트롤 무효화는 동작해야 함
        try {
            controller.onGameOver();
        } catch (NullPointerException e) {
            // GameScene.showGameOver() 호출 시 NPE 예상됨
        }
        testEngine.reset();

        // 게임오버 후 컨트롤 시도
        controller.moveLeft();
        controller.moveRight();
        controller.softDrop();
        controller.hardDrop();
        controller.rotateCW();

        // then
        assertEquals(0, testEngine.moveLeftCalled, "게임오버 후 왼쪽 이동 비활성화");
        assertEquals(0, testEngine.moveRightCalled, "게임오버 후 오른쪽 이동 비활성화");
        assertEquals(0, testEngine.softDropCalled, "게임오버 후 소프트드롭 비활성화");
        assertEquals(0, testEngine.hardDropCalled, "게임오버 후 하드드롭 비활성화");
        assertEquals(0, testEngine.rotateCWCalled, "게임오버 후 회전 비활성화");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Platform.runLater 사용")
    @DisplayName("일시정지 후 게임오버 시 상태 정리 테스트 - GameScene null")
    void testGameOverAfterPause() {
        // given
        controller.setEngine(testEngine);
        controller.pause(); // 일시정지 상태

        // when & then - GameScene이 null이므로 NPE 발생 예상
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onGameOver();
                },
                "GameScene이 null일 때 onGameOver에서 NPE 발생");

        // then - 그럼에도 자동하강 중지는 호출되어야 함
        assertEquals(1, testEngine.stopAutoDropCalled, "게임오버 시 자동하강 중지 호출");
    }

    @Test
    @DisplayName("키 입력 콜백 인터페이스 구현 테스트")
    void testKeyInputCallbacks() {
        // given
        controller.setEngine(testEngine);

        // when
        controller.onLeftPressed();
        controller.onRightPressed();
        controller.onRotatePressed();
        controller.onDropPressed();
        controller.onHardDropPressed();

        // then
        assertEquals(1, testEngine.moveLeftCalled, "왼쪽 키 콜백이 moveLeft 호출");
        assertEquals(1, testEngine.moveRightCalled, "오른쪽 키 콜백이 moveRight 호출");
        assertEquals(1, testEngine.rotateCWCalled, "회전 키 콜백이 rotateCW 호출");
        assertEquals(1, testEngine.softDropCalled, "드롭 키 콜백이 softDrop 호출");
        assertEquals(1, testEngine.hardDropCalled, "하드드롭 키 콜백이 hardDrop 호출");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Platform.runLater 사용")
    @DisplayName("일시정지 키 콜백 테스트")
    void testPauseKeyCallback() {
        // given
        controller.setEngine(testEngine);

        // when
        controller.onPausePressed();

        // then
        assertEquals(1, testEngine.stopAutoDropCalled, "일시정지 키로 자동하강 중지");
    }

    @Test
    @DisplayName("게임 상태 리스너 인터페이스 구현 테스트 - GameScene null")
    void testGameStateListenerImplementation() {
        // given
        Board testBoard = new Board(10, 20);
        Tetromino testTetromino = Tetromino.of(Tetromino.Kind.T);

        // when & then - GameScene이 null이므로 NPE 발생 예상
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onBoardUpdated(testBoard);
                },
                "onBoardUpdated에서 NPE 발생");

        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onPieceSpawned(testTetromino, 4, 0);
                },
                "onPieceSpawned에서 NPE 발생");

        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onNextPiece(testTetromino);
                },
                "onNextPiece에서 NPE 발생");

        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onScoreChanged(1000);
                },
                "onScoreChanged에서 NPE 발생");
    }

    @Test
    @DisplayName("엔진 없이 컨트롤 호출 시 안전성 테스트")
    void testControlsWithoutEngine() {
        // given - 엔진을 설정하지 않음

        // when & then
        assertDoesNotThrow(
                () -> {
                    controller.start();
                    // controller.pause(); // JavaFX Toolkit not initialized 에러로 인해 제외
                    controller.resume();
                    controller.moveLeft();
                    controller.moveRight();
                    controller.softDrop();
                    controller.hardDrop();
                    controller.rotateCW();
                },
                "엔진이 없는 상태에서도 컨트롤 메서드들이 안전하게 실행되어야 함");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Platform.runLater 사용")
    @DisplayName("여러 번 일시정지/재개 테스트")
    void testMultiplePauseResume() {
        // given
        controller.setEngine(testEngine);

        // when
        controller.pause();
        controller.resume();
        controller.pause();
        controller.resume();

        // then
        assertEquals(2, testEngine.stopAutoDropCalled, "일시정지가 2번 호출되어야 함");
        assertEquals(2, testEngine.startAutoDropCalled, "재개가 2번 호출되어야 함");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Platform.runLater 사용")
    @DisplayName("게임오버 상태에서 일시정지 시도 테스트 - GameScene null")
    void testPauseAfterGameOver() {
        // given
        controller.setEngine(testEngine);

        // when & then - onGameOver에서 NPE 발생 예상
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onGameOver();
                },
                "GameScene이 null일 때 onGameOver에서 NPE 발생");

        testEngine.reset();

        // when
        controller.pause();

        // then
        assertEquals(0, testEngine.stopAutoDropCalled, "게임오버 후에는 일시정지가 동작하지 않아야 함");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Platform.runLater 사용")
    @DisplayName("일시정지 상태에서 재시작 테스트")
    void testStartWhilePaused() {
        // given
        controller.setEngine(testEngine);
        controller.pause();
        testEngine.reset();

        // when
        controller.start();

        // then
        assertEquals(1, testEngine.startAutoDropCalled, "일시정지 상태에서도 시작 호출 가능");
    }

    @Test
    @DisplayName("GameController, KeyInputCallback, GameStateListener 인터페이스 구현 확인")
    void testInterfaceImplementations() {
        // given & when & then
        assertTrue(controller instanceof GameController, "GameController 인터페이스 구현");
        assertTrue(
                controller instanceof KeyInputHandler.KeyInputCallback,
                "KeyInputCallback 인터페이스 구현");
        assertTrue(controller instanceof GameStateListener, "GameStateListener 인터페이스 구현");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Scene 생성")
    @DisplayName("Scene 연결 테스트")
    void testAttachToScene() {
        // given
        javafx.scene.Scene mockScene = new javafx.scene.Scene(new javafx.scene.layout.VBox());

        // when & then
        assertDoesNotThrow(
                () -> {
                    controller.attachToScene(mockScene);
                },
                "Scene 연결이 예외 없이 실행되어야 함");
    }

    @Test
    @DisplayName("하드드롭 스로틀링 상태 확인 - Reflection 사용")
    void testHardDropThrottlingState() {
        // given
        controller.setEngine(testEngine);

        try {
            Field lastHardDropTimeField =
                    GameSceneController.class.getDeclaredField("lastHardDropTime");
            lastHardDropTimeField.setAccessible(true);

            // when - 첫 번째 하드드롭
            controller.onHardDropPressed();
            long firstDropTime = (Long) lastHardDropTimeField.get(controller);

            // then
            assertEquals(1, testEngine.hardDropCalled, "하드드롭이 호출됨");
            assertTrue(firstDropTime > 0, "하드드롭 시간이 기록됨");
        } catch (Exception e) {
            // Reflection 실패 시 기본 동작만 확인
            controller.onHardDropPressed();
            assertEquals(1, testEngine.hardDropCalled, "기본 하드드롭 동작 확인");
        }
    }

    @Test
    @DisplayName("일시정지 상태 토글 테스트 - Reflection 사용")
    void testPauseStateToggle() {
        // given
        controller.setEngine(testEngine);

        try {
            Field pausedField = GameSceneController.class.getDeclaredField("paused");
            pausedField.setAccessible(true);

            // 초기 상태 확인
            assertFalse((Boolean) pausedField.get(controller), "초기에는 일시정지 상태가 아님");

            // when - 일시정지
            try {
                controller.pause();
            } catch (Exception e) {
                // JavaFX 예외 무시하고 계속
            }

            // then
            assertTrue((Boolean) pausedField.get(controller), "pause() 호출 후 일시정지 상태");
            assertEquals(1, testEngine.stopAutoDropCalled, "자동하강 중지 호출");

            // when - 재개
            try {
                controller.resume();
            } catch (Exception e) {
                // JavaFX 예외 무시하고 계속
            }

            // then
            assertFalse((Boolean) pausedField.get(controller), "resume() 호출 후 정상 상태");
            assertEquals(1, testEngine.startAutoDropCalled, "자동하강 시작 호출");
        } catch (Exception e) {
            // Reflection 실패 시 기본 동작만 확인
            try {
                controller.pause();
            } catch (Exception ex) {
                // JavaFX 예외 무시
            }
            assertEquals(1, testEngine.stopAutoDropCalled, "기본 일시정지 동작 확인");
        }
    }

    @Test
    @DisplayName("총 클리어된 라인 수 추적 테스트 - Reflection 사용")
    void testTotalLinesClearedTracking() {
        // given
        controller.setEngine(testEngine);

        try {
            Field totalLinesClearedField =
                    GameSceneController.class.getDeclaredField("totalLinesCleared");
            totalLinesClearedField.setAccessible(true);

            // 초기 상태 확인
            assertEquals(0, (int) totalLinesClearedField.get(controller), "초기 클리어 라인 수는 0");

            // when - 라인 클리어 이벤트 발생 시 NPE 발생하지만 totalLinesCleared는 증가함
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        controller.onLinesCleared(3);
                    },
                    "GameScene이 null일 때 NPE 발생");

            // then - reflection으로 직접 필드 값 확인 (NPE 발생 전에 증가함)
            assertEquals(3, (int) totalLinesClearedField.get(controller), "NPE 발생 전에 라인 수는 증가함");

            // when - 추가 라인 클리어
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        controller.onLinesCleared(2);
                    },
                    "GameScene이 null일 때 또 다시 NPE 발생");

            // then
            assertEquals(5, (int) totalLinesClearedField.get(controller), "추가 라인 클리어 후 총합 5");
        } catch (Exception e) {
            // Reflection 실패 시 NPE 발생 확인으로 대체
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        controller.onLinesCleared(3);
                    },
                    "GameScene이 null일 때 NPE 발생");
        }
    }

    @Test
    @DisplayName("게임오버 상태 설정 테스트 - Reflection 사용")
    void testGameOverStateManagement() {
        // given
        controller.setEngine(testEngine);

        try {
            Field gameOverField = GameSceneController.class.getDeclaredField("gameOver");
            gameOverField.setAccessible(true);

            // 초기 상태 확인
            assertFalse((Boolean) gameOverField.get(controller), "초기에는 게임오버 상태가 아님");

            // when - 게임오버 이벤트
            try {
                controller.onGameOver();
            } catch (Exception e) {
                // JavaFX 예외나 NPE 무시하고 계속
            }

            // then
            assertTrue((Boolean) gameOverField.get(controller), "게임오버 후 상태가 true로 변경");
            assertEquals(1, testEngine.stopAutoDropCalled, "게임오버 시 자동하강 중지");
        } catch (Exception e) {
            // Reflection 실패 시 기본 동작만 확인
            try {
                controller.onGameOver();
            } catch (Exception ex) {
                // JavaFX 예외나 NPE 무시
            }
            assertEquals(1, testEngine.stopAutoDropCalled, "기본 게임오버 동작 확인");
        }
    }

    @Test
    @DisplayName("피스 스폰 시 화면 업데이트 테스트")
    void testPieceSpawnedUpdate() {
        // given
        controller.setEngine(testEngine);
        Tetromino mockTetromino = Tetromino.of(Tetromino.Kind.I);

        // when & then - GameScene이 null이므로 NPE가 발생함을 확인
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onPieceSpawned(mockTetromino, 4, 0);
                },
                "GameScene이 null일 때 onPieceSpawned에서 NPE 발생");

        // when & then - 다음 피스 업데이트도 NPE 발생
        Tetromino nextTetromino = Tetromino.of(Tetromino.Kind.T);
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onNextPiece(nextTetromino);
                },
                "GameScene이 null일 때 onNextPiece에서 NPE 발생");
    }

    @Test
    @org.junit.jupiter.api.Disabled("JavaFX Toolkit not initialized - Platform.runLater 사용")
    @DisplayName("컨트롤러 상태 독립성 테스트")
    void testControllerStateIndependence() {
        // given
        GameSceneController controller2 =
                new GameSceneController(null, mockSceneManager, settings, keyInputHandler);
        TestEngine engine2 = new TestEngine(new Board(10, 20), controller2);

        controller.setEngine(testEngine);
        controller2.setEngine(engine2);

        // when
        controller.pause();
        controller2.start();

        // then
        assertEquals(1, testEngine.stopAutoDropCalled, "첫 번째 컨트롤러 일시정지");
        assertEquals(1, engine2.startAutoDropCalled, "두 번째 컨트롤러 시작");
        assertEquals(0, testEngine.startAutoDropCalled, "첫 번째 컨트롤러는 시작되지 않음");
        assertEquals(0, engine2.stopAutoDropCalled, "두 번째 컨트롤러는 일시정지되지 않음");
    }

    @Test
    @DisplayName("이미 일시정지된 상태에서 pause 재호출 시 무시")
    void testPauseWhenAlreadyPaused() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field pausedField = GameSceneController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);

        // 강제로 paused 상태로 설정 (JavaFX 없이)
        pausedField.set(controller, true);
        testEngine.reset();

        // when - 이미 일시정지된 상태에서 pause 재호출
        try {
            controller.pause();
        } catch (Exception e) {
            // JavaFX 예외 무시
        }

        // then - pause 조건 (!paused)에 걸려서 stopAutoDrop이 호출되지 않아야 함
        assertEquals(0, testEngine.stopAutoDropCalled, "이미 일시정지된 상태에서는 stopAutoDrop이 호출되지 않음");
    }

    @Test
    @DisplayName("게임오버 상태에서 resume 시도 시 무시")
    void testResumeWhenGameOver() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field gameOverField = GameSceneController.class.getDeclaredField("gameOver");
        gameOverField.setAccessible(true);
        Field pausedField = GameSceneController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);

        // 강제로 게임오버 상태로 설정 (JavaFX 없이)
        gameOverField.set(controller, true);
        pausedField.set(controller, true); // 일시정지도 설정
        testEngine.reset();

        // when - 게임오버 상태에서 resume 시도
        controller.resume();

        // then - resume 조건 (!gameOver)에 걸려서 startAutoDrop이 호출되지 않아야 함
        assertEquals(0, testEngine.startAutoDropCalled, "게임오버 상태에서는 startAutoDrop이 호출되지 않음");
    }

    @Test
    @DisplayName("일시정지되지 않은 상태에서 resume 시도 시 무시")
    void testResumeWhenNotPaused() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field pausedField = GameSceneController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);

        // paused는 false인 상태 (초기 상태)
        assertFalse((Boolean) pausedField.get(controller), "초기에는 일시정지 상태가 아님");
        testEngine.reset();

        // when - 일시정지되지 않은 상태에서 resume 시도
        controller.resume();

        // then - resume 조건 (paused)에 걸려서 startAutoDrop이 호출되지 않아야 함
        assertEquals(0, testEngine.startAutoDropCalled, "일시정지되지 않은 상태에서는 startAutoDrop이 호출되지 않음");
    }

    @Test
    @DisplayName("hardDrop throttling - 정확히 100ms 경계 테스트")
    void testHardDropThrottlingBoundary() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field lastHardDropTimeField =
                GameSceneController.class.getDeclaredField("lastHardDropTime");
        lastHardDropTimeField.setAccessible(true);

        // when - 첫 번째 하드드롭
        controller.hardDrop();
        assertEquals(1, testEngine.hardDropCalled, "첫 번째 하드드롭 실행");

        // 강제로 99ms 전 시간으로 설정 (throttling 범위 내)
        lastHardDropTimeField.set(controller, System.currentTimeMillis() - 99);
        testEngine.reset();

        // when - 99ms 후 시도 (throttling 적용되어야 함)
        controller.hardDrop();

        // then
        assertEquals(0, testEngine.hardDropCalled, "99ms 이내에는 throttling 적용");

        // 강제로 100ms 전 시간으로 설정 (throttling 범위 밖)
        lastHardDropTimeField.set(controller, System.currentTimeMillis() - 100);
        testEngine.reset();

        // when - 100ms 후 시도 (throttling 해제되어야 함)
        controller.hardDrop();

        // then
        assertEquals(1, testEngine.hardDropCalled, "100ms 이후에는 throttling 해제");
    }

    @Test
    @DisplayName("게임오버 상태에서 이동 명령 무시 확인")
    void testMovementBlockedWhenGameOver() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field gameOverField = GameSceneController.class.getDeclaredField("gameOver");
        gameOverField.setAccessible(true);

        // 강제로 게임오버 상태로 설정
        gameOverField.set(controller, true);
        testEngine.reset();

        // when - 게임오버 상태에서 모든 이동 명령 시도
        controller.moveLeft();
        controller.moveRight();
        controller.softDrop();
        controller.hardDrop();
        controller.rotateCW();

        // then - 모든 명령이 무시되어야 함
        assertEquals(0, testEngine.moveLeftCalled, "게임오버 시 moveLeft 무시");
        assertEquals(0, testEngine.moveRightCalled, "게임오버 시 moveRight 무시");
        assertEquals(0, testEngine.softDropCalled, "게임오버 시 softDrop 무시");
        assertEquals(0, testEngine.hardDropCalled, "게임오버 시 hardDrop 무시");
        assertEquals(0, testEngine.rotateCWCalled, "게임오버 시 rotateCW 무시");
    }

    @Test
    @DisplayName("onLinesCleared 호출 시 totalLinesCleared 증가 및 속도 업데이트")
    void testOnLinesClearedUpdatesTotal() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field totalLinesClearedField =
                GameSceneController.class.getDeclaredField("totalLinesCleared");
        totalLinesClearedField.setAccessible(true);

        // when & then - 첫 번째 라인 클리어 (GameScene이 null이므로 NPE 발생)
        assertThrows(NullPointerException.class, () -> controller.onLinesCleared(1));
        assertEquals(1, (int) totalLinesClearedField.get(controller), "totalLinesCleared가 1 증가");
        assertEquals(1, testEngine.updateSpeedCalled, "속도 업데이트 호출");

        testEngine.reset();

        // when & then - 두 번째 라인 클리어
        assertThrows(NullPointerException.class, () -> controller.onLinesCleared(3));
        assertEquals(4, (int) totalLinesClearedField.get(controller), "totalLinesCleared가 누적되어 4");
        assertEquals(1, testEngine.updateSpeedCalled, "속도 업데이트 다시 호출");
    }

    @Test
    @DisplayName("onGameOver 호출 시 paused 상태가 false로 리셋")
    void testOnGameOverResetsPausedState() throws Exception {
        // given
        controller.setEngine(testEngine);
        Field pausedField = GameSceneController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);
        Field gameOverField = GameSceneController.class.getDeclaredField("gameOver");
        gameOverField.setAccessible(true);

        // 강제로 일시정지 상태로 설정
        pausedField.set(controller, true);
        assertTrue((Boolean) pausedField.get(controller), "일시정지 상태");

        // when - 게임오버 (GameScene이 null이므로 NPE 발생)
        assertThrows(NullPointerException.class, () -> controller.onGameOver());

        // then - paused가 false로 리셋되어야 함
        assertFalse((Boolean) pausedField.get(controller), "게임오버 시 paused가 false로 리셋");
        assertTrue((Boolean) gameOverField.get(controller), "게임오버 상태는 true");
        assertEquals(1, testEngine.stopAutoDropCalled, "자동하강 중지 호출");
    }
}

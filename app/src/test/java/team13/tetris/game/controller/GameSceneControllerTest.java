package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.input.KeyInputHandler;

/**
 * GameSceneController의 핵심 로직을 테스트합니다.
 * 
 * 참고: GameSceneController는 JavaFX UI와 강하게 결합되어 있어서
 * GameScene을 필요로 하는 메서드들은 NullPointerException이 발생합니다.
 * 
 * 이 테스트는 다음을 검증합니다:
 * - 엔진 연동 및 명령 전달
 * - hardDrop throttling 로직
 * - 게임오버 후 입력 차단
 * - null 안전성
 */
@DisplayName("GameSceneController 테스트")
public class GameSceneControllerTest {

  private GameSceneController controller;
  private Settings settings;
  private KeyInputHandler keyInputHandler;
  private TestEngine testEngine;

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

    // GameScene을 null로 전달 (UI 의존성 회피)
    controller = new GameSceneController(null, settings, keyInputHandler);

    Board board = new Board(10, 20);
    testEngine = new TestEngine(board, controller);
    controller.setEngine(testEngine);
  }

  @AfterEach
  void tearDown() {
    if (testEngine != null) {
      testEngine.shutdown();
    }
    controller = null;
    testEngine = null;
    settings = null;
    keyInputHandler = null;
  }

  // ========== GameController 인터페이스 메서드 테스트 ==========

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

  // ========== KeyInputCallback 인터페이스 메서드 테스트 ==========

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
  @DisplayName("onPausePressed: Platform.runLater로 인해 IllegalStateException 발생")
  void testOnPausePressed() {
    // pause()는 showPauseWindow()를 호출하고 Platform.runLater 사용
    // 단위 테스트 환경에서는 JavaFX Application Thread가 없어서 IllegalStateException 발생
    assertThrows(IllegalStateException.class, () -> controller.onPausePressed());
  }

  @Test
  @DisplayName("onEscPressed: 현재는 빈 구현")
  void testOnEscPressed() {
    assertDoesNotThrow(() -> controller.onEscPressed());
  }

  // ========== GameStateListener 인터페이스 메서드 테스트 ==========

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

  // ========== 게임 상태 변화 테스트 ==========

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

  // ========== 엣지 케이스 테스트 ==========

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
}

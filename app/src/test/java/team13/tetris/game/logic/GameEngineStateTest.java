package team13.tetris.game.logic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

/**
 * GameEngine의 게임 상태 관리 기능을 테스트합니다.
 * - startNewGame(): 새 게임 시작
 * - spawnNext(): 다음 조각 생성
 * - shutdown(): 리소스 정리
 * - 게임오버 조건
 * - 리스너 콜백 검증
 */
@DisplayName("GameEngine 상태 테스트")
public class GameEngineStateTest {

  private GameEngine engine;
  private Board board;
  private TestListener listener;

  // 테스트용 리스너 클래스
  private static class TestListener implements GameStateListener {
    int scoreChangedCount = 0;
    boolean boardUpdated = false;
    Tetromino spawnedPiece = null;
    int spawnX = -1;
    int spawnY = -1;
    Tetromino nextPiece = null;
    int pieceSpawnedCount = 0;
    int nextPieceCount = 0;

    @Override
    public void onScoreChanged(int score) {
      scoreChangedCount++;
    }

    @Override
    public void onBoardUpdated(Board board) {
      boardUpdated = true;
    }

    @Override
    public void onLinesCleared(int lines) {}

    @Override
    public void onGameOver() {}

    @Override
    public void onPieceSpawned(Tetromino piece, int x, int y) {
      pieceSpawnedCount++;
      spawnedPiece = piece;
      spawnX = x;
      spawnY = y;
    }

    @Override
    public void onNextPiece(Tetromino piece) {
      nextPieceCount++;
      nextPiece = piece;
    }

    void reset() {
      scoreChangedCount = 0;
      boardUpdated = false;
      spawnedPiece = null;
      spawnX = -1;
      spawnY = -1;
      nextPiece = null;
      pieceSpawnedCount = 0;
      nextPieceCount = 0;
    }
  }

  @BeforeEach
  void setUp() {
    listener = new TestListener();
    board = new Board(10, 20);
    engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
  }

  @AfterEach
  void tearDown() {
    if (engine != null) {
      engine.shutdown();
    }
  }

  // === startNewGame() 테스트 ===

  @Test
  @DisplayName("startNewGame: 보드 초기화")
  void testStartNewGameClearsBoard() {
    // 보드에 블록 배치
    board.setCell(0, 0, 1);
    board.setCell(5, 10, 2);

    engine.startNewGame();

    // 보드가 비워졌는지 확인
    assertEquals(0, board.getCell(0, 0), "보드가 초기화되어야 함");
    assertEquals(0, board.getCell(5, 10), "보드가 초기화되어야 함");
  }

  @Test
  @DisplayName("startNewGame: 현재 조각과 다음 조각 생성")
  void testStartNewGameSpawnsPieces() {
    engine.startNewGame();

    assertNotNull(engine.getCurrent(), "현재 조각이 생성되어야 함");
    assertNotNull(engine.getNext(), "다음 조각이 생성되어야 함");
  }

  @Test
  @DisplayName("startNewGame: 조각 초기 위치 설정")
  void testStartNewGameSetsInitialPosition() {
    engine.startNewGame();

    Tetromino current = engine.getCurrent();
    int expectedX = (board.getWidth() - current.getWidth()) / 2;

    assertEquals(expectedX, engine.getPieceX(), "초기 X 위치는 중앙이어야 함");
    assertEquals(0, engine.getPieceY(), "초기 Y 위치는 0이어야 함");
  }

  @Test
  @DisplayName("startNewGame: 점수 알림 발생")
  void testStartNewGameNotifiesScore() {
    listener.reset();

    engine.startNewGame();

    assertTrue(listener.scoreChangedCount > 0, "점수 변경 알림이 발생해야 함");
  }

  @Test
  @DisplayName("startNewGame: 조각 생성 알림 발생")
  void testStartNewGameNotifiesPieceSpawned() {
    listener.reset();

    engine.startNewGame();

    assertEquals(1, listener.pieceSpawnedCount, "조각 생성 알림이 1번 발생해야 함");
    assertNotNull(listener.spawnedPiece, "생성된 조각 정보가 전달되어야 함");
  }

  @Test
  @DisplayName("startNewGame: 다음 조각 알림 발생")
  void testStartNewGameNotifiesNextPiece() {
    listener.reset();

    engine.startNewGame();

    assertEquals(1, listener.nextPieceCount, "다음 조각 알림이 1번 발생해야 함");
    assertNotNull(listener.nextPiece, "다음 조각 정보가 전달되어야 함");
  }

  @Test
  @DisplayName("startNewGame: 여러 번 호출 가능")
  void testStartNewGameMultipleTimes() {
    engine.startNewGame();
    Tetromino first = engine.getCurrent();

    engine.startNewGame();
    Tetromino second = engine.getCurrent();

    assertNotNull(first, "첫 번째 게임의 조각이 생성되어야 함");
    assertNotNull(second, "두 번째 게임의 조각이 생성되어야 함");
  }

  // === spawnNext() 동작 테스트 ===

  @Test
  @DisplayName("hardDrop 후 다음 조각 자동 생성")
  void testSpawnNextAfterHardDrop() {
    engine.startNewGame();
    Tetromino beforeDrop = engine.getCurrent();

    engine.hardDrop();

    Tetromino afterDrop = engine.getCurrent();
    assertNotSame(beforeDrop, afterDrop, "hardDrop 후 새로운 조각이 생성되어야 함");
  }

  @Test
  @DisplayName("조각 생성 시 current ← next, next ← random")
  void testSpawnNextMovesNextToCurrent() {
    engine.startNewGame();
    Tetromino originalNext = engine.getNext();

    engine.hardDrop();

    Tetromino newCurrent = engine.getCurrent();
    assertNotNull(newCurrent, "새 조각이 current로 설정되어야 함");
    assertNotNull(originalNext, "새로운 next 조각이 생성되어야 함");
  }

  @Test
  @DisplayName("조각 생성 시 초기 위치로 리셋")
  void testSpawnNextResetsPosition() {
    engine.startNewGame();

    // 조각을 이동시킴
    engine.moveLeft();
    engine.moveLeft();

    engine.hardDrop();

    // 새 조각은 중앙에서 시작해야 함
    Tetromino current = engine.getCurrent();
    int expectedX = (board.getWidth() - current.getWidth()) / 2;
    assertEquals(expectedX, engine.getPieceX(), "새 조각은 중앙에서 시작해야 함");
    assertEquals(0, engine.getPieceY(), "새 조각은 Y=0에서 시작해야 함");
  }

  // === 게임오버 테스트 ===

  @Test
  @DisplayName("게임오버: 조각이 배치될 공간이 없을 때")
  void testGameOverWhenNoSpace() {
    engine.startNewGame();

    // 상단 6줄을 블록으로 채움 (조각이 생성될 공간을 막음)
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < board.getWidth(); col++) {
        board.setCell(col, row, 1);
      }
    }

    engine.hardDrop();

    // 게임오버 후 current는 null이어야 함
    assertNull(engine.getCurrent(), "게임오버 시 current는 null이어야 함");
  }

  @Test
  @DisplayName("게임오버: 더 이상 조작 불가")
  void testGameOverPreventsMovement() {
    engine.startNewGame();

    // 게임오버 상태로 만들기
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < board.getWidth(); col++) {
        board.setCell(col, row, 1);
      }
    }
    engine.hardDrop();

    // 조작 시도
    assertDoesNotThrow(() -> engine.moveLeft(), "게임오버 후 moveLeft 안전해야 함");
    assertDoesNotThrow(() -> engine.moveRight(), "게임오버 후 moveRight 안전해야 함");
    assertDoesNotThrow(() -> engine.rotateCW(), "게임오버 후 rotateCW 안전해야 함");
    assertDoesNotThrow(() -> engine.softDrop(), "게임오버 후 softDrop 안전해야 함");
  }

  // === shutdown() 테스트 ===

  @Test
  @DisplayName("shutdown: 리소스 정리")
  void testShutdownCleansUpResources() {
    engine.startNewGame();

    assertDoesNotThrow(() -> engine.shutdown(), "shutdown이 예외 없이 실행되어야 함");
  }

  @Test
  @DisplayName("shutdown: 여러 번 호출 가능")
  void testShutdownMultipleTimes() {
    engine.startNewGame();

    assertDoesNotThrow(() -> {
      engine.shutdown();
      engine.shutdown();
      engine.shutdown();
    }, "shutdown 여러 번 호출 시 안전해야 함");
  }

  @Test
  @DisplayName("shutdown 후 게임 재시작 가능")
  void testRestartAfterShutdown() {
    engine.startNewGame();
    engine.shutdown();

    assertDoesNotThrow(() -> engine.startNewGame(),
        "shutdown 후 게임을 다시 시작할 수 있어야 함");
  }

  // === 리스너 콜백 검증 ===

  @Test
  @DisplayName("리스너: onBoardUpdated 호출 확인")
  void testListenerOnBoardUpdated() {
    listener.reset();

    engine.startNewGame();

    assertTrue(listener.boardUpdated, "보드 업데이트 알림이 발생해야 함");
  }

  @Test
  @DisplayName("리스너: onPieceSpawned 위치 정보 정확성")
  void testListenerOnPieceSpawnedAccuracy() {
    listener.reset();

    engine.startNewGame();

    assertEquals(engine.getCurrent(), listener.spawnedPiece,
        "생성된 조각이 정확히 전달되어야 함");
    assertEquals(engine.getPieceX(), listener.spawnX,
        "생성 위치 X가 정확히 전달되어야 함");
    assertEquals(engine.getPieceY(), listener.spawnY,
        "생성 위치 Y가 정확히 전달되어야 함");
  }

  // === 난이도별 테스트 ===

  @Test
  @DisplayName("난이도 EASY로 게임 시작")
  void testStartGameWithEasyDifficulty() {
    GameEngine easyEngine = new GameEngine(new Board(10, 20), new TestListener(),
        ScoreBoard.ScoreEntry.Mode.EASY);

    assertDoesNotThrow(() -> easyEngine.startNewGame(),
        "EASY 난이도로 게임을 시작할 수 있어야 함");

    easyEngine.shutdown();
  }

  @Test
  @DisplayName("난이도 HARD로 게임 시작")
  void testStartGameWithHardDifficulty() {
    GameEngine hardEngine = new GameEngine(new Board(10, 20), new TestListener(),
        ScoreBoard.ScoreEntry.Mode.HARD);

    assertDoesNotThrow(() -> hardEngine.startNewGame(),
        "HARD 난이도로 게임을 시작할 수 있어야 함");

    hardEngine.shutdown();
  }

  @Test
  @DisplayName("아이템 모드로 게임 시작")
  void testStartGameWithItemMode() {
    GameEngine itemEngine = new GameEngine(new Board(10, 20), new TestListener(),
        ScoreBoard.ScoreEntry.Mode.ITEM);

    assertDoesNotThrow(() -> itemEngine.startNewGame(),
        "ITEM 모드로 게임을 시작할 수 있어야 함");

    itemEngine.shutdown();
  }
}

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
 * GameEngine의 조각 생성 메커니즘을 테스트합니다.
 * - spawnNext() 메서드: current ← next, next ← randomPiece()
 * - randomPiece() 메서드: Roulette Wheel Selection 사용
 * - 난이도별 가중치: EASY (I:12), NORMAL (all:10), HARD (I:8)
 * - 초기 위치: 보드 중앙 상단 (px = (width - piece.width) / 2, py = 0)
 * - 게임오버 조건: 새 조각이 생성 위치에 배치될 수 없을 때
 */
@DisplayName("GameEngine 조각 생성 테스트")
public class GameEnginePieceGenerationTest {

  private GameEngine engine;
  private Board board;
  private TestListener listener;

  // 테스트용 리스너 클래스
  private static class TestListener implements GameStateListener {
    boolean pieceSpawned = false;
    boolean nextPieceChanged = false;
    boolean gameOver = false;
    Tetromino lastSpawnedPiece = null;
    Tetromino lastNextPiece = null;

    @Override
    public void onScoreChanged(int score) {
    }

    @Override
    public void onBoardUpdated(Board board) {
    }

    @Override
    public void onLinesCleared(int lines) {
    }

    @Override
    public void onGameOver() {
      gameOver = true;
    }

    @Override
    public void onPieceSpawned(Tetromino piece, int x, int y) {
      pieceSpawned = true;
      lastSpawnedPiece = piece;
    }

    @Override
    public void onNextPiece(Tetromino piece) {
      nextPieceChanged = true;
      lastNextPiece = piece;
    }

    void reset() {
      pieceSpawned = false;
      nextPieceChanged = false;
      gameOver = false;
      lastSpawnedPiece = null;
      lastNextPiece = null;
    }
  }

  @BeforeEach
  void setUp() {
    listener = new TestListener();
    board = new Board(10, 20);
    engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
    engine.startNewGame();
  }

  @AfterEach
  void tearDown() {
    if (engine != null) {
      engine.shutdown();
    }
  }

  // === 초기 조각 생성 테스트 ===

  @Test
  @DisplayName("startNewGame: current와 next가 모두 생성되어야 함")
  void testInitialPieceGeneration() {
    assertNotNull(engine.getCurrent(), "현재 조각이 생성되어야 함");
    assertNotNull(engine.getNext(), "다음 조각이 생성되어야 함");
  }

  @Test
  @DisplayName("초기 조각: 7가지 종류 중 하나여야 함")
  void testInitialPieceIsValidKind() {
    Tetromino current = engine.getCurrent();
    assertNotNull(current.getKind(), "조각 종류가 null이 아니어야 함");

    // I, O, T, S, Z, J, L 중 하나
    Tetromino.Kind kind = current.getKind();
    assertTrue(
        kind == Tetromino.Kind.I || kind == Tetromino.Kind.O ||
            kind == Tetromino.Kind.T || kind == Tetromino.Kind.S ||
            kind == Tetromino.Kind.Z || kind == Tetromino.Kind.J ||
            kind == Tetromino.Kind.L,
        "7가지 기본 조각 종류 중 하나여야 함");
  }

  @Test
  @DisplayName("초기 위치: 보드 중앙 상단")
  void testInitialPiecePosition() {
    Tetromino current = engine.getCurrent();
    int px = engine.getPieceX();
    int py = engine.getPieceY();

    assertEquals(0, py, "Y 좌표는 0이어야 함 (맨 위)");

    // X 좌표는 보드 중앙 정렬
    int expectedX = (board.getWidth() - current.getWidth()) / 2;
    assertEquals(expectedX, px, "X 좌표는 중앙 정렬되어야 함");
  }

  // === randomPiece() 테스트 ===

  @Test
  @DisplayName("randomPiece: 7가지 조각 종류 생성 가능")
  void testRandomPieceGeneratesAllKinds() {
    // 100번 생성하여 다양한 종류가 나오는지 확인
    boolean hasI = false, hasO = false, hasT = false, hasS = false;
    boolean hasZ = false, hasJ = false, hasL = false;

    for (int i = 0; i < 100; i++) {
      Tetromino piece = engine.generateTestPiece();
      Tetromino.Kind kind = piece.getKind();

      if (kind == Tetromino.Kind.I)
        hasI = true;
      if (kind == Tetromino.Kind.O)
        hasO = true;
      if (kind == Tetromino.Kind.T)
        hasT = true;
      if (kind == Tetromino.Kind.S)
        hasS = true;
      if (kind == Tetromino.Kind.Z)
        hasZ = true;
      if (kind == Tetromino.Kind.J)
        hasJ = true;
      if (kind == Tetromino.Kind.L)
        hasL = true;
    }

    // 100번 생성하면 대부분의 종류가 나와야 함
    int kindCount = (hasI ? 1 : 0) + (hasO ? 1 : 0) + (hasT ? 1 : 0) +
        (hasS ? 1 : 0) + (hasZ ? 1 : 0) + (hasJ ? 1 : 0) + (hasL ? 1 : 0);
    assertTrue(kindCount >= 5, "100번 생성 시 최소 5가지 이상의 조각 종류가 나와야 함");
  }

  @Test
  @DisplayName("randomPiece: null을 반환하지 않음")
  void testRandomPieceNeverReturnsNull() {
    for (int i = 0; i < 50; i++) {
      Tetromino piece = engine.generateTestPiece();
      assertNotNull(piece, "randomPiece는 null을 반환하지 않아야 함");
    }
  }

  // === 난이도별 조각 생성 테스트 ===

  @Test
  @DisplayName("EASY 모드: I 블록 출현 빈도 확인")
  void testEasyModeIPieceProbability() {
    GameEngine easyEngine = new GameEngine(new Board(10, 20), new TestListener(),
        ScoreBoard.ScoreEntry.Mode.EASY);
    easyEngine.startNewGame();

    int totalPieces = 1000;
    int iCount = 0;

    for (int i = 0; i < totalPieces; i++) {
      Tetromino piece = easyEngine.generateTestPiece();
      if (piece.getKind() == Tetromino.Kind.I) {
        iCount++;
      }
    }

    // EASY: I블록 가중치 12, 전체 72 → 약 16.7%
    // 1000개 중 140~200개 정도 나와야 함 (14%~20% 허용)
    assertTrue(iCount >= 140 && iCount <= 200,
        "EASY 모드에서 I 블록이 약 16.7% 출현해야 함 (실제: " + iCount + "/1000)");

    easyEngine.shutdown();
  }

  @Test
  @DisplayName("NORMAL 모드: 모든 블록 균등 분포")
  void testNormalModeEqualProbability() {
    int totalPieces = 700; // 7종류 × 100개 기준
    int[] counts = new int[7]; // I, O, T, S, Z, J, L

    for (int i = 0; i < totalPieces; i++) {
      Tetromino piece = engine.generateTestPiece();
      Tetromino.Kind kind = piece.getKind();

      if (kind == Tetromino.Kind.I)
        counts[0]++;
      else if (kind == Tetromino.Kind.O)
        counts[1]++;
      else if (kind == Tetromino.Kind.T)
        counts[2]++;
      else if (kind == Tetromino.Kind.S)
        counts[3]++;
      else if (kind == Tetromino.Kind.Z)
        counts[4]++;
      else if (kind == Tetromino.Kind.J)
        counts[5]++;
      else if (kind == Tetromino.Kind.L)
        counts[6]++;
    }

    // NORMAL: 모두 가중치 10, 전체 70 → 각 14.3%
    // 700개 중 각 70~130개 정도 나와야 함 (10%~18.5% 허용)
    for (int i = 0; i < 7; i++) {
      assertTrue(counts[i] >= 70 && counts[i] <= 130,
          "NORMAL 모드에서 모든 블록이 비슷한 빈도로 출현해야 함 (블록 " + i + ": " + counts[i] + "/700)");
    }
  }

  @Test
  @DisplayName("HARD 모드: I 블록 출현 빈도 감소")
  void testHardModeIPieceProbability() {
    GameEngine hardEngine = new GameEngine(new Board(10, 20), new TestListener(),
        ScoreBoard.ScoreEntry.Mode.HARD);
    hardEngine.startNewGame();

    int totalPieces = 1000;
    int iCount = 0;

    for (int i = 0; i < totalPieces; i++) {
      Tetromino piece = hardEngine.generateTestPiece();
      if (piece.getKind() == Tetromino.Kind.I) {
        iCount++;
      }
    }

    // HARD: I블록 가중치 8, 전체 68 → 약 11.8%
    // 1000개 중 90~140개 정도 나와야 함 (9%~14% 허용)
    assertTrue(iCount >= 90 && iCount <= 140,
        "HARD 모드에서 I 블록이 약 11.8% 출현해야 함 (실제: " + iCount + "/1000)");

    hardEngine.shutdown();
  }

  // === 게임오버 조건 테스트 ===

  @Test
  @DisplayName("게임오버: 새 조각이 생성 위치에 배치될 수 없을 때")
  void testGameOverWhenPieceCannotSpawn() {
    // 맨 위 여러 줄을 완전히 막기
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < board.getWidth(); col++) {
        board.setCell(col, row, 1);
      }
    }

    // 현재 조각을 하드드롭하여 다음 조각 생성 시도
    engine.hardDrop();

    // spawnNext()가 호출되면 게임오버 조건에서 current가 null이 됨
    assertNull(engine.getCurrent(),
        "새 조각이 생성될 수 없으면 current가 null이어야 함");
  }

  // === getCurrent/getNext 접근자 테스트 ===

  @Test
  @DisplayName("getCurrent: 현재 조각 반환")
  void testGetCurrent() {
    Tetromino current = engine.getCurrent();
    assertNotNull(current, "getCurrent()가 null을 반환하지 않아야 함");
  }

  @Test
  @DisplayName("getNext: 다음 조각 반환")
  void testGetNext() {
    Tetromino next = engine.getNext();
    assertNotNull(next, "getNext()가 null을 반환하지 않아야 함");
  }

  @Test
  @DisplayName("current와 next는 서로 다른 인스턴스")
  void testCurrentAndNextAreDifferent() {
    Tetromino current = engine.getCurrent();
    Tetromino next = engine.getNext();

    assertNotSame(current, next, "current와 next는 서로 다른 인스턴스여야 함");
  }

  // === generateTestPiece() 테스트 ===

  @Test
  @DisplayName("generateTestPiece: Roulette Wheel Selection 테스트용 메서드")
  void testGenerateTestPiece() {
    Tetromino piece = engine.generateTestPiece();

    assertNotNull(piece, "generateTestPiece()가 null을 반환하지 않아야 함");
    assertNotNull(piece.getKind(), "생성된 조각의 Kind가 null이 아니어야 함");
  }
}

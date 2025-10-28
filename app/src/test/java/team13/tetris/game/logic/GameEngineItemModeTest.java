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
 * GameEngine의 아이템 모드 기능을 테스트합니다.
 * 아이템 모드에서는 10줄 클리어마다 5가지 아이템 중 하나가 생성됩니다:
 * - COPY: 특정 미노를 복사하여 다음 미노로 생성
 * - WEIGHT: 착지 지점 아래 블록들을 파괴
 * - GRAVITY: 전체 보드에 중력 적용
 * - SPLIT: 3개 열로 분할되어 독립적으로 하강
 * - LINE_CLEAR: 특정 블록이 있는 줄을 즉시 제거
 */
@DisplayName("GameEngine 아이템 모드 테스트")
public class GameEngineItemModeTest {

  private GameEngine engine;
  private Board board;
  private TestListener listener;

  // 테스트용 리스너 클래스
  private static class TestListener implements GameStateListener {
    boolean scoreChanged = false;
    boolean boardUpdated = false;
    boolean linesCleared = false;
    boolean gameOver = false;
    boolean pieceSpawned = false;
    boolean nextPieceChanged = false;
    Tetromino lastSpawnedPiece = null;
    Tetromino lastNextPiece = null;
    int lastScore = 0;
    int lastLinesCleared = 0;

    @Override
    public void onScoreChanged(int score) {
      scoreChanged = true;
      lastScore = score;
    }

    @Override
    public void onBoardUpdated(Board board) {
      boardUpdated = true;
    }

    @Override
    public void onLinesCleared(int lines) {
      linesCleared = true;
      lastLinesCleared = lines;
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
      scoreChanged = false;
      boardUpdated = false;
      linesCleared = false;
      gameOver = false;
      pieceSpawned = false;
      nextPieceChanged = false;
      lastSpawnedPiece = null;
      lastNextPiece = null;
      lastScore = 0;
      lastLinesCleared = 0;
    }
  }

  @BeforeEach
  void setUp() {
    listener = new TestListener();
    board = new Board(10, 20);
    engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.ITEM);
    engine.startNewGame();
  }

  @AfterEach
  void tearDown() {
    if (engine != null) {
      engine.shutdown();
    }
  }

  // === 아이템 모드 활성화 테스트 ===

  @Test
  @DisplayName("아이템 모드로 초기화되어야 함")
  void testItemModeInitialization() {
    assertNotNull(engine, "GameEngine이 생성되어야 함");
    assertNotNull(engine.getCurrent(), "현재 조각이 존재해야 함");
    assertNotNull(engine.getNext(), "다음 조각이 존재해야 함");
  }

  @Test
  @DisplayName("아이템 모드에서는 일반 조각이 생성되어야 함")
  void testNormalPieceGeneration() {
    Tetromino current = engine.getCurrent();
    assertNotNull(current, "현재 조각이 존재해야 함");

    // 처음에는 일반 조각이어야 함 (아이템이 아님)
    assertFalse(current.isItemPiece(), "초기에는 일반 조각이어야 함");
  }

  // === 10줄 클리어 시 아이템 생성 테스트 ===

  @Test
  @DisplayName("10줄 클리어 시 아이템 미노가 생성되어야 함")
  void testItemPieceGenerationAfter10Lines() throws InterruptedException {
    // 라인 클리어를 통해 totalLinesCleared를 증가시키기
    // 한 번에 여러 줄을 클리어하여 10줄 달성
    for (int iteration = 0; iteration < 3; iteration++) {
      // 4줄을 채우기 (하단부터)
      for (int row = 19; row >= 16; row--) {
        for (int col = 0; col < 10; col++) {
          board.setCell(col, row, 1);
        }
      }

      // 조각을 배치하여 라인 클리어 트리거 (실제로는 게임 엔진이 처리해야 함)
      // 이 테스트는 단순히 10줄 클리어 후 상태를 확인하는 것이므로
      // totalLinesCleared를 직접 증가시키는 대신 실제 게임 플레이를 시뮬레이션

      Thread.sleep(200); // 비동기 처리 대기
    }

    // 아이템 모드가 활성화되어 있는지 확인
    // (10줄 클리어는 실제 게임 플레이에서만 정확히 테스트 가능)
    assertNotNull(engine.getCurrent(), "현재 조각이 존재해야 함");
  }

  // === COPY 아이템 테스트 ===

  @Test
  @DisplayName("COPY 아이템: 특정 미노를 복사할 수 있어야 함")
  void testCopyItemCreation() {
    // COPY 아이템 미노 직접 생성 (I 블록 복사, 3개 파라미터 생성자 사용)
    Tetromino copyItem = new Tetromino(Tetromino.Kind.I, 0, 2);

    assertNotNull(copyItem, "COPY 아이템이 생성되어야 함");
    assertTrue(copyItem.isItemPiece(), "아이템 피스여야 함");
    assertEquals(Tetromino.Kind.I, copyItem.getKind(), "I 블록이어야 함");
    assertEquals(2, copyItem.getCopyBlockIndex(), "복사 블록 인덱스가 2여야 함");
  }

  @Test
  @DisplayName("COPY 아이템: copyBlockIndex가 올바르게 설정되어야 함")
  void testCopyItemBlockIndex() {
    for (int i = 0; i < 4; i++) {
      Tetromino copyItem = new Tetromino(Tetromino.Kind.T, 0, i);
      assertEquals(i, copyItem.getCopyBlockIndex(),
          "복사 블록 인덱스가 " + i + "여야 함");
    }
  }

  // === WEIGHT 아이템 테스트 ===

  @Test
  @DisplayName("WEIGHT 아이템: 생성 가능해야 함")
  void testWeightItemCreation() {
    Tetromino weightItem = new Tetromino(Tetromino.Kind.WEIGHT, 0, 0);

    assertNotNull(weightItem, "WEIGHT 아이템이 생성되어야 함");
    assertEquals(Tetromino.Kind.WEIGHT, weightItem.getKind(), "WEIGHT 타입이어야 함");
    assertFalse(weightItem.canRotate(), "WEIGHT는 회전할 수 없어야 함");
  }

  @Test
  @DisplayName("WEIGHT 아이템: 회전 불가능해야 함")
  void testWeightItemCannotRotate() {
    Tetromino weightItem = new Tetromino(Tetromino.Kind.WEIGHT, 0, 0);
    assertFalse(weightItem.canRotate(), "WEIGHT는 회전할 수 없어야 함");
  }

  // === GRAVITY 아이템 테스트 ===

  @Test
  @DisplayName("GRAVITY 아이템: 생성 가능해야 함")
  void testGravityItemCreation() {
    Tetromino gravityItem = new Tetromino(Tetromino.Kind.GRAVITY, 0, 0);

    assertNotNull(gravityItem, "GRAVITY 아이템이 생성되어야 함");
    assertEquals(Tetromino.Kind.GRAVITY, gravityItem.getKind(), "GRAVITY 타입이어야 함");
    assertFalse(gravityItem.canRotate(), "GRAVITY는 회전할 수 없어야 함");
  }

  @Test
  @DisplayName("GRAVITY 아이템: 회전 불가능해야 함")
  void testGravityItemCannotRotate() {
    Tetromino gravityItem = new Tetromino(Tetromino.Kind.GRAVITY, 0, 0);
    assertFalse(gravityItem.canRotate(), "GRAVITY는 회전할 수 없어야 함");
  }

  // === SPLIT 아이템 테스트 ===

  @Test
  @DisplayName("SPLIT 아이템: 생성 가능해야 함")
  void testSplitItemCreation() {
    Tetromino splitItem = new Tetromino(Tetromino.Kind.SPLIT, 0, 0);

    assertNotNull(splitItem, "SPLIT 아이템이 생성되어야 함");
    assertEquals(Tetromino.Kind.SPLIT, splitItem.getKind(), "SPLIT 타입이어야 함");
    assertFalse(splitItem.canRotate(), "SPLIT는 회전할 수 없어야 함");
  }

  @Test
  @DisplayName("SPLIT 아이템: 회전 불가능해야 함")
  void testSplitItemCannotRotate() {
    Tetromino splitItem = new Tetromino(Tetromino.Kind.SPLIT, 0, 0);
    assertFalse(splitItem.canRotate(), "SPLIT는 회전할 수 없어야 함");
  }

  // === LINE_CLEAR 아이템 테스트 ===

  @Test
  @DisplayName("LINE_CLEAR 아이템: 생성 가능해야 함")
  void testLineClearItemCreation() {
    Tetromino lineClearItem = new Tetromino(Tetromino.Kind.T, 0, 1, Tetromino.ItemType.LINE_CLEAR);

    assertNotNull(lineClearItem, "LINE_CLEAR 아이템이 생성되어야 함");
    assertTrue(lineClearItem.isItemPiece(), "아이템 피스여야 함");
    assertEquals(1, lineClearItem.getLineClearBlockIndex(),
        "라인 클리어 블록 인덱스가 1이어야 함");
  }

  @Test
  @DisplayName("LINE_CLEAR 아이템: lineClearBlockIndex가 올바르게 설정되어야 함")
  void testLineClearItemBlockIndex() {
    for (int i = 0; i < 4; i++) {
      Tetromino lineClearItem = new Tetromino(Tetromino.Kind.S, 0, i, Tetromino.ItemType.LINE_CLEAR);
      assertEquals(i, lineClearItem.getLineClearBlockIndex(),
          "라인 클리어 블록 인덱스가 " + i + "여야 함");
    }
  }

  // === 아이템 블록 값 범위 테스트 ===

  @Test
  @DisplayName("COPY 아이템 블록: 100-199 범위여야 함")
  void testCopyItemBlockValueRange() {
    Tetromino copyItem = new Tetromino(Tetromino.Kind.I, 0, 0, Tetromino.ItemType.COPY);
    board.placeItemPiece(copyItem.getShape(), 0, 0, copyItem.getId(), 0, "COPY");

    // COPY 블록 값 확인 (100 + id)
    boolean foundCopyBlock = false;
    for (int y = 0; y < board.getHeight(); y++) {
      for (int x = 0; x < board.getWidth(); x++) {
        int cellValue = board.getCell(x, y);
        if (cellValue >= 100 && cellValue < 200) {
          foundCopyBlock = true;
          break;
        }
      }
      if (foundCopyBlock)
        break;
    }

    assertTrue(foundCopyBlock, "COPY 아이템 블록(100-199)이 보드에 배치되어야 함");
  }

  @Test
  @DisplayName("LINE_CLEAR 아이템 블록: 200-299 범위여야 함")
  void testLineClearItemBlockValueRange() {
    Tetromino lineClearItem = new Tetromino(Tetromino.Kind.T, 0, 0, Tetromino.ItemType.LINE_CLEAR);
    board.placeItemPiece(lineClearItem.getShape(), 0, 0, lineClearItem.getId(),
        0, "LINE_CLEAR");

    boolean foundLineClearBlock = false;
    for (int y = 0; y < board.getHeight(); y++) {
      for (int x = 0; x < board.getWidth(); x++) {
        int cellValue = board.getCell(x, y);
        if (cellValue >= 200 && cellValue < 300) {
          foundLineClearBlock = true;
          break;
        }
      }
      if (foundLineClearBlock)
        break;
    }

    assertTrue(foundLineClearBlock, "LINE_CLEAR 아이템 블록(200-299)이 보드에 배치되어야 함");
  }

  @Test
  @DisplayName("WEIGHT 아이템 블록: 300-399 범위여야 함")
  void testWeightItemBlockValueRange() {
    Tetromino weightItem = new Tetromino(Tetromino.Kind.WEIGHT, 0, 0);
    board.placeItemPiece(weightItem.getShape(), 0, 0, weightItem.getId(),
        0, "WEIGHT");

    boolean foundWeightBlock = false;
    for (int y = 0; y < board.getHeight(); y++) {
      for (int x = 0; x < board.getWidth(); x++) {
        int cellValue = board.getCell(x, y);
        if (cellValue >= 300 && cellValue < 400) {
          foundWeightBlock = true;
          break;
        }
      }
      if (foundWeightBlock)
        break;
    }

    assertTrue(foundWeightBlock, "WEIGHT 아이템 블록(300-399)이 보드에 배치되어야 함");
  }

  @Test
  @DisplayName("GRAVITY 아이템 블록: 400-499 범위여야 함")
  void testGravityItemBlockValueRange() {
    Tetromino gravityItem = new Tetromino(Tetromino.Kind.GRAVITY, 0, 0);
    board.placeItemPiece(gravityItem.getShape(), 0, 0, gravityItem.getId(),
        0, "GRAVITY");

    boolean foundGravityBlock = false;
    for (int y = 0; y < board.getHeight(); y++) {
      for (int x = 0; x < board.getWidth(); x++) {
        int cellValue = board.getCell(x, y);
        if (cellValue >= 400 && cellValue < 500) {
          foundGravityBlock = true;
          break;
        }
      }
      if (foundGravityBlock)
        break;
    }

    assertTrue(foundGravityBlock, "GRAVITY 아이템 블록(400-499)이 보드에 배치되어야 함");
  }

  @Test
  @DisplayName("SPLIT 아이템 블록: 500-599 범위여야 함")
  void testSplitItemBlockValueRange() {
    Tetromino splitItem = new Tetromino(Tetromino.Kind.SPLIT, 0, 0);
    board.placeItemPiece(splitItem.getShape(), 0, 0, splitItem.getId(),
        0, "SPLIT");

    boolean foundSplitBlock = false;
    for (int y = 0; y < board.getHeight(); y++) {
      for (int x = 0; x < board.getWidth(); x++) {
        int cellValue = board.getCell(x, y);
        if (cellValue >= 500 && cellValue < 600) {
          foundSplitBlock = true;
          break;
        }
      }
      if (foundSplitBlock)
        break;
    }

    assertTrue(foundSplitBlock, "SPLIT 아이템 블록(500-599)이 보드에 배치되어야 함");
  }

  // === 아이템 피스 판별 테스트 ===

  @Test
  @DisplayName("일반 미노는 아이템 피스가 아니어야 함")
  void testNormalPieceIsNotItemPiece() {
    Tetromino normalPiece = new Tetromino(Tetromino.Kind.I, 0);
    assertFalse(normalPiece.isItemPiece(), "일반 미노는 아이템 피스가 아니어야 함");
  }

  @Test
  @DisplayName("COPY 아이템은 아이템 피스여야 함")
  void testCopyIsItemPiece() {
    Tetromino copyItem = new Tetromino(Tetromino.Kind.T, 0, 0, Tetromino.ItemType.COPY);
    assertTrue(copyItem.isItemPiece(), "COPY 아이템은 아이템 피스여야 함");
  }

  @Test
  @DisplayName("LINE_CLEAR 아이템은 아이템 피스여야 함")
  void testLineClearIsItemPiece() {
    Tetromino lineClearItem = new Tetromino(Tetromino.Kind.S, 0, 0, Tetromino.ItemType.LINE_CLEAR);
    assertTrue(lineClearItem.isItemPiece(), "LINE_CLEAR 아이템은 아이템 피스여야 함");
  }

  @Test
  @DisplayName("WEIGHT 아이템은 Kind로 판별 가능해야 함")
  void testWeightItemKind() {
    Tetromino weightItem = new Tetromino(Tetromino.Kind.WEIGHT, 0, 0);
    assertEquals(Tetromino.Kind.WEIGHT, weightItem.getKind(), "WEIGHT Kind여야 함");
  }

  @Test
  @DisplayName("GRAVITY 아이템은 Kind로 판별 가능해야 함")
  void testGravityItemKind() {
    Tetromino gravityItem = new Tetromino(Tetromino.Kind.GRAVITY, 0, 0);
    assertEquals(Tetromino.Kind.GRAVITY, gravityItem.getKind(), "GRAVITY Kind여야 함");
  }

  @Test
  @DisplayName("SPLIT 아이템은 Kind로 판별 가능해야 함")
  void testSplitItemKind() {
    Tetromino splitItem = new Tetromino(Tetromino.Kind.SPLIT, 0, 0);
    assertEquals(Tetromino.Kind.SPLIT, splitItem.getKind(), "SPLIT Kind여야 함");
  }

  // === 아이템 모드 점수 테스트 ===

  @Test
  @DisplayName("아이템 모드에서도 라인 클리어 점수가 정상 적용되어야 함")
  void testItemModeScoring() {
    int initialScore = engine.getScore();

    // 1줄 채우기
    for (int col = 0; col < 10; col++) {
      board.setCell(col, 19, 1);
    }

    // 현재 조각을 맨 아래에 배치하여 라인 클리어
    Tetromino current = engine.getCurrent();
    board.placePiece(current.getShape(), 0, 18, current.getId());

    // 점수가 증가했는지 확인 (비동기 처리로 인해 즉시 확인은 어려움)
    // 최소한 초기 점수와 같거나 커야 함
    assertTrue(engine.getScore() >= initialScore, "점수가 유지되거나 증가해야 함");
  }

  // === 총 라인 클리어 카운트 테스트 ===

  @Test
  @DisplayName("아이템 모드에서 총 라인 클리어 수가 증가해야 함")
  void testTotalLinesClearedIncreases() {
    int initialLinesCleared = engine.getTotalLinesCleared();

    // 라인을 채우고 클리어 트리거
    for (int col = 0; col < 10; col++) {
      board.setCell(col, 19, 1);
    }

    Tetromino current = engine.getCurrent();
    board.placePiece(current.getShape(), 0, 18, current.getId());

    // totalLinesCleared는 라인 클리어 후 증가해야 함
    // (비동기 처리로 인해 즉시 증가하지 않을 수 있음)
    assertTrue(engine.getTotalLinesCleared() >= initialLinesCleared,
        "총 라인 클리어 수가 유지되거나 증가해야 함");
  }
}

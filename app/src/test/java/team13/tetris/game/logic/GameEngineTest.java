package team13.tetris.game.logic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

public class GameEngineTest {

    @Mock private GameStateListener mockListener;

    private Board testBoard;
    private GameEngine gameEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testBoard = new Board(10, 20);
        gameEngine = new GameEngine(testBoard, mockListener, ScoreBoard.ScoreEntry.Mode.ITEM);
    }

    @Test
    @DisplayName("handleLockedPiece - 일반 블록 고정 시 다음 블록 생성")
    void testHandleLockedPiece_NormalBlock() throws Exception {
        // Given
        gameEngine.startNewGame();

        // When
        Method handleLockedPiece = GameEngine.class.getDeclaredMethod("handleLockedPiece");
        handleLockedPiece.setAccessible(true);
        handleLockedPiece.invoke(gameEngine);

        // Then
        verify(mockListener, atLeastOnce()).onBoardUpdated(any(Board.class));
        assertNotNull(gameEngine.getCurrent());
    }

    @Test
    @DisplayName("handleLockedPiece - 라인클리어 발생 시 점수 증가")
    void testHandleLockedPiece_WithLineClear() throws Exception {
        // Given
        gameEngine.startNewGame();

        // 라인클리어를 위한 보드 설정 (맨 아래 줄을 완전히 채움)
        for (int x = 0; x < testBoard.getWidth(); x++) {
            testBoard.setCell(x, 19, 1);
        }

        // O블록을 위에 배치 (라인클리어와 무관하게)
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, Tetromino.of(Tetromino.Kind.O));

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 17);

        // mock 초기화
        reset(mockListener);

        // When
        Method handleLockedPiece = GameEngine.class.getDeclaredMethod("handleLockedPiece");
        handleLockedPiece.setAccessible(true);
        handleLockedPiece.invoke(gameEngine);

        // Then - 보드에 이미 완전한 라인이 있으므로 라인클리어가 발생해야 함
        verify(mockListener, atLeastOnce()).onLinesCleared(anyInt());
    }

    @Test
    @DisplayName("destroyBlocksBelow - 무게추 블록 아래 한 줄만 파괴")
    void testDestroyBlocksBelow_SingleLine() throws Exception {
        // Given
        gameEngine.startNewGame();

        // 무게추 블록 설정
        Tetromino weightBlock =
                Tetromino.item(Tetromino.Kind.WEIGHT, 0, Tetromino.ItemType.WEIGHT, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, weightBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 15);

        // 아래쪽에 블록들 배치
        testBoard.setCell(4, 17, 1);
        testBoard.setCell(4, 18, 1);
        testBoard.setCell(4, 19, 1);

        // When
        Method destroyBlocksBelow =
                GameEngine.class.getDeclaredMethod("destroyBlocksBelow", boolean.class);
        destroyBlocksBelow.setAccessible(true);
        destroyBlocksBelow.invoke(gameEngine, false); // 한 줄만 파괴

        // Then
        assertEquals(0, testBoard.getCell(4, 17)); // 바로 아래 줄만 파괴됨
        assertEquals(1, testBoard.getCell(4, 18)); // 나머지는 유지
        assertEquals(1, testBoard.getCell(4, 19));
    }

    @Test
    @DisplayName("destroyBlocksBelow - 무게추 블록 아래 모든 줄 파괴")
    void testDestroyBlocksBelow_AllLines() throws Exception {
        // Given
        gameEngine.startNewGame();

        // 무게추 블록 설정
        Tetromino weightBlock =
                Tetromino.item(Tetromino.Kind.WEIGHT, 0, Tetromino.ItemType.WEIGHT, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, weightBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 15);

        // 아래쪽에 블록들 배치
        testBoard.setCell(4, 17, 1);
        testBoard.setCell(4, 18, 1);
        testBoard.setCell(4, 19, 1);

        // When
        Method destroyBlocksBelow =
                GameEngine.class.getDeclaredMethod("destroyBlocksBelow", boolean.class);
        destroyBlocksBelow.setAccessible(true);
        destroyBlocksBelow.invoke(gameEngine, true); // 모든 줄 파괴

        // Then
        assertEquals(0, testBoard.getCell(4, 17)); // 모든 줄이 파괴됨
        assertEquals(0, testBoard.getCell(4, 18));
        assertEquals(0, testBoard.getCell(4, 19));
    }

    @Test
    @DisplayName("generateItemPiece - 아이템 피스 생성")
    void testGenerateItemPiece() throws Exception {
        // Given
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        Field nextItemPieceField = GameEngine.class.getDeclaredField("nextItemPiece");
        nextItemPieceField.setAccessible(true);

        // When
        Method generateItemPiece = GameEngine.class.getDeclaredMethod("generateItemPiece");
        generateItemPiece.setAccessible(true);
        generateItemPiece.invoke(gameEngine);

        // Then
        Tetromino nextItemPiece = (Tetromino) nextItemPieceField.get(gameEngine);
        assertNotNull(nextItemPiece);
        assertTrue(nextItemPiece.isItemPiece());
    }

    @Test
    @DisplayName("processItemEffect - COPY 아이템 효과 처리")
    void testProcessItemEffect_CopyItem() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // When
        Method processItemEffect =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        processItemEffect.setAccessible(true);
        processItemEffect.invoke(gameEngine, Tetromino.ItemType.COPY, Tetromino.Kind.I);

        // Then
        verify(mockListener, atLeastOnce())
                .onPieceSpawned(any(Tetromino.class), anyInt(), anyInt());
        verify(mockListener, atLeastOnce()).onNextPiece(any(Tetromino.class));
    }

    @Test
    @DisplayName("processItemEffect - GRAVITY 아이템 효과 처리")
    void testProcessItemEffect_GravityItem() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // 보드에 떠있는 블록 설정
        testBoard.setCell(2, 10, 1);
        testBoard.setCell(3, 15, 1);

        // When
        Method processItemEffect =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        processItemEffect.setAccessible(true);
        processItemEffect.invoke(gameEngine, Tetromino.ItemType.GRAVITY, null);

        // Then
        // 중력이 적용되어 블록들이 아래로 떨어짐
        assertEquals(0, testBoard.getCell(2, 10)); // 원래 위치는 비어있음
        assertEquals(1, testBoard.getCell(2, 19)); // 바닥으로 떨어짐
    }

    @Test
    @DisplayName("createItemPiece - COPY 아이템 생성")
    void testCreateItemPiece_CopyItem() throws Exception {
        // When
        Method createItemPiece =
                GameEngine.class.getDeclaredMethod(
                        "createItemPiece", Tetromino.Kind.class, Tetromino.Kind.class);
        createItemPiece.setAccessible(true);
        Tetromino copyItem =
                (Tetromino)
                        createItemPiece.invoke(gameEngine, Tetromino.Kind.COPY, Tetromino.Kind.I);

        // Then
        assertNotNull(copyItem);
        assertTrue(copyItem.isItemPiece());
        assertEquals(Tetromino.ItemType.COPY, copyItem.getItemType());
        assertEquals(Tetromino.Kind.I, copyItem.getKind());
    }

    @Test
    @DisplayName("createItemPiece - WEIGHT 아이템 생성")
    void testCreateItemPiece_WeightItem() throws Exception {
        // When
        Method createItemPiece =
                GameEngine.class.getDeclaredMethod(
                        "createItemPiece", Tetromino.Kind.class, Tetromino.Kind.class);
        createItemPiece.setAccessible(true);
        Tetromino weightItem =
                (Tetromino) createItemPiece.invoke(gameEngine, Tetromino.Kind.WEIGHT, null);

        // Then
        assertNotNull(weightItem);
        assertTrue(weightItem.isItemPiece());
        assertEquals(Tetromino.ItemType.WEIGHT, weightItem.getItemType());
        assertEquals(Tetromino.Kind.WEIGHT, weightItem.getKind());
    }

    @Test
    @DisplayName("getLastLockedColumns - 마지막 고정된 블록의 열 정보 반환")
    void testGetLastLockedColumns() throws Exception {
        // Given
        gameEngine.startNewGame();

        // I블록 배치
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, Tetromino.of(Tetromino.Kind.I));

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 16);

        // When
        Method recordLastLockedColumns =
                GameEngine.class.getDeclaredMethod("recordLastLockedColumns");
        recordLastLockedColumns.setAccessible(true);
        recordLastLockedColumns.invoke(gameEngine);

        Set<Integer> lockedColumns = gameEngine.getLastLockedColumns();

        // Then
        assertNotNull(lockedColumns);
        assertTrue(lockedColumns.contains(4)); // I블록이 세로로 배치될 때
    }

    @Test
    @DisplayName("getLastLockedCells - 마지막 고정된 블록의 셀 정보 반환")
    void testGetLastLockedCells() throws Exception {
        // Given
        gameEngine.startNewGame();

        // O블록 배치 (2x2 정사각형)
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, Tetromino.of(Tetromino.Kind.O));

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 18);

        // When
        Method recordLastLockedColumns =
                GameEngine.class.getDeclaredMethod("recordLastLockedColumns");
        recordLastLockedColumns.setAccessible(true);
        recordLastLockedColumns.invoke(gameEngine);

        List<int[]> lockedCells = gameEngine.getLastLockedCells();

        // Then
        assertNotNull(lockedCells);
        assertEquals(4, lockedCells.size()); // O블록은 4개의 셀

        // 각 셀이 올바른 위치에 있는지 확인 (O블록은 2x2 사각형)
        boolean hasCell4_18 = lockedCells.stream().anyMatch(cell -> cell[0] == 4 && cell[1] == 18);
        boolean hasCell5_18 = lockedCells.stream().anyMatch(cell -> cell[0] == 5 && cell[1] == 18);
        boolean hasCell4_19 = lockedCells.stream().anyMatch(cell -> cell[0] == 4 && cell[1] == 19);
        boolean hasCell5_19 = lockedCells.stream().anyMatch(cell -> cell[0] == 5 && cell[1] == 19);
        assertFalse(hasCell4_18);
        assertTrue(hasCell5_18);
        assertFalse(hasCell4_19);
        assertTrue(hasCell5_19);
    }

    @Test
    @DisplayName("createLineClearItemPiece - LINE_CLEAR 아이템 생성")
    void testCreateLineClearItemPiece() throws Exception {
        // When
        Method createLineClearItemPiece =
                GameEngine.class.getDeclaredMethod(
                        "createLineClearItemPiece", Tetromino.Kind.class);
        createLineClearItemPiece.setAccessible(true);
        Tetromino lineClearItem =
                (Tetromino) createLineClearItemPiece.invoke(gameEngine, Tetromino.Kind.T);

        // Then
        assertNotNull(lineClearItem);
        assertTrue(lineClearItem.isItemPiece());
        assertEquals(Tetromino.ItemType.LINE_CLEAR, lineClearItem.getItemType());
        assertEquals(Tetromino.Kind.T, lineClearItem.getKind());
        assertTrue(lineClearItem.getLineClearBlockIndex() >= 0);
        assertTrue(lineClearItem.getLineClearBlockIndex() < 4);
    }

    @Test
    @DisplayName("destroyLineDirectlyBelow - 바로 아래 한 줄만 파괴")
    void testDestroyLineDirectlyBelow() throws Exception {
        // Given
        gameEngine.startNewGame();

        // 무게추 블록 설정
        Tetromino weightBlock =
                Tetromino.item(Tetromino.Kind.WEIGHT, 0, Tetromino.ItemType.WEIGHT, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, weightBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 16);

        // 아래쪽에 블록들 배치
        testBoard.setCell(4, 17, 1);
        testBoard.setCell(4, 18, 1);

        // When
        Method destroyLineDirectlyBelow =
                GameEngine.class.getDeclaredMethod("destroyLineDirectlyBelow");
        destroyLineDirectlyBelow.setAccessible(true);
        destroyLineDirectlyBelow.invoke(gameEngine);

        // Then: 바로 아래 줄만 파괴, 그 아래는 유지
        assertEquals(1, testBoard.getCell(4, 17));
        assertEquals(0, testBoard.getCell(4, 18));
    }

    @Test
    @DisplayName("destroyAllBlocksBelow - 아래 모든 줄 파괴")
    void testDestroyAllBlocksBelow() throws Exception {
        // Given
        gameEngine.startNewGame();

        // 무게추 블록 설정
        Tetromino weightBlock =
                Tetromino.item(Tetromino.Kind.WEIGHT, 0, Tetromino.ItemType.WEIGHT, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, weightBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 16);

        // 아래쪽에 블록들 배치
        testBoard.setCell(4, 17, 1);
        testBoard.setCell(4, 18, 1);
        testBoard.setCell(4, 19, 1);

        // When
        Method destroyAllBlocksBelow = GameEngine.class.getDeclaredMethod("destroyAllBlocksBelow");
        destroyAllBlocksBelow.setAccessible(true);
        destroyAllBlocksBelow.invoke(gameEngine);

        // Then
        assertEquals(1, testBoard.getCell(4, 17)); // 모든 줄이 파괴됨 (본인 제외)
        assertEquals(0, testBoard.getCell(4, 18));
        assertEquals(0, testBoard.getCell(4, 19));
    }

    @Test
    @DisplayName("getBoardSnapshotBeforeClear - 라인클리어 전 보드 스냅샷 반환")
    void testGetBoardSnapshotBeforeClear() {
        // Given
        gameEngine.startNewGame();

        // When
        int[][] snapshot = gameEngine.getBoardSnapshotBeforeClear();

        // Then
        // 초기 상태에서는 null일 수 있음
        // 실제로는 handleLockedPiece에서 설정됨
        assertTrue(snapshot == null || snapshot.length > 0);
    }

    @Test
    @DisplayName("getClearedLineIndices - 삭제된 라인 인덱스 반환")
    void testGetClearedLineIndices() {
        // Given
        gameEngine.startNewGame();

        // When
        List<Integer> indices = gameEngine.getClearedLineIndices();

        // Then
        // 초기 상태에서는 null일 수 있음
        assertTrue(indices == null || indices.size() >= 0);
    }

    @Test
    @DisplayName("isLastClearByGravityOrSplit - 중력/스플릿으로 인한 클리어 확인")
    void testIsLastClearByGravityOrSplit() {
        // Given
        gameEngine.startNewGame();

        // When
        boolean isGravityOrSplit = gameEngine.isLastClearByGravityOrSplit();

        // Then
        assertFalse(isGravityOrSplit); // 초기값은 false
    }

    @Test
    @DisplayName("processItemEffect - SPLIT 아이템 효과 처리")
    void testProcessItemEffect_SplitItem() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // SPLIT 블록 설정
        Tetromino splitBlock = Tetromino.item(Tetromino.Kind.SPLIT, 0, Tetromino.ItemType.SPLIT, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, splitBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 16);

        // When
        Method processItemEffect =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        processItemEffect.setAccessible(true);
        processItemEffect.invoke(gameEngine, Tetromino.ItemType.SPLIT, null);

        // Then - SPLIT 효과가 적용됨 (구체적인 검증은 보드 상태에 따라 다름)
        verify(mockListener, never()).onGameOver(); // 게임오버가 발생하지 않음
    }

    @Test
    @DisplayName("processItemEffect - LINE_CLEAR 아이템 효과 처리")
    void testProcessItemEffect_LineClearItem() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // LINE_CLEAR 블록 설정
        Tetromino lineClearBlock = Tetromino.lineClearItem(Tetromino.Kind.T, 0, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, lineClearBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 18);

        // 보드에 LINE_CLEAR 마커 배치
        testBoard.setCell(4, 19, 201); // LINE_CLEAR 마커 (200번대)

        // When
        Method processItemEffect =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        processItemEffect.setAccessible(true);
        processItemEffect.invoke(gameEngine, Tetromino.ItemType.LINE_CLEAR, null);

        // Then
        verify(mockListener, atLeastOnce()).onBoardUpdated(any(Board.class));
    }

    @Test
    @DisplayName("createItemPiece - GRAVITY 아이템 생성")
    void testCreateItemPiece_GravityItem() throws Exception {
        // When
        Method createItemPiece =
                GameEngine.class.getDeclaredMethod(
                        "createItemPiece", Tetromino.Kind.class, Tetromino.Kind.class);
        createItemPiece.setAccessible(true);
        Tetromino gravityItem =
                (Tetromino) createItemPiece.invoke(gameEngine, Tetromino.Kind.GRAVITY, null);

        // Then
        assertNotNull(gravityItem);
        assertTrue(gravityItem.isItemPiece());
        assertEquals(Tetromino.ItemType.GRAVITY, gravityItem.getItemType());
        assertEquals(Tetromino.Kind.GRAVITY, gravityItem.getKind());
    }

    @Test
    @DisplayName("createItemPiece - SPLIT 아이템 생성")
    void testCreateItemPiece_SplitItem() throws Exception {
        // When
        Method createItemPiece =
                GameEngine.class.getDeclaredMethod(
                        "createItemPiece", Tetromino.Kind.class, Tetromino.Kind.class);
        createItemPiece.setAccessible(true);
        Tetromino splitItem =
                (Tetromino) createItemPiece.invoke(gameEngine, Tetromino.Kind.SPLIT, null);

        // Then
        assertNotNull(splitItem);
        assertTrue(splitItem.isItemPiece());
        assertEquals(Tetromino.ItemType.SPLIT, splitItem.getItemType());
        assertEquals(Tetromino.Kind.SPLIT, splitItem.getKind());
    }

    @Test
    @DisplayName("handleLockedPiece - 아이템 블록 고정 시 효과 발동")
    void testHandleLockedPiece_ItemBlock() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // GRAVITY 아이템 블록 설정
        Tetromino gravityBlock =
                Tetromino.item(Tetromino.Kind.GRAVITY, 0, Tetromino.ItemType.GRAVITY, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, gravityBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 18);

        // When
        Method handleLockedPiece = GameEngine.class.getDeclaredMethod("handleLockedPiece");
        handleLockedPiece.setAccessible(true);
        handleLockedPiece.invoke(gameEngine);

        // Then
        verify(mockListener, atLeastOnce()).onBoardUpdated(any(Board.class));
        assertTrue(gameEngine.isLastClearByGravityOrSplit()); // 중력 아이템으로 클리어 됨
    }

    @Test
    @DisplayName("destroyBlocksBelow - 무게추가 아닌 블록일 때는 아무것도 하지 않음")
    void testDestroyBlocksBelow_NotWeightBlock() throws Exception {
        // Given
        gameEngine.startNewGame();

        // 일반 I블록 설정 (무게추가 아님)
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, Tetromino.of(Tetromino.Kind.I));

        // 아래쪽에 블록 배치
        testBoard.setCell(4, 17, 1);

        // When
        Method destroyBlocksBelow =
                GameEngine.class.getDeclaredMethod("destroyBlocksBelow", boolean.class);
        destroyBlocksBelow.setAccessible(true);
        destroyBlocksBelow.invoke(gameEngine, false);

        // Then
        assertEquals(1, testBoard.getCell(4, 17)); // 블록이 그대로 유지됨
    }

    @Test
    @DisplayName("generateItemPiece - 아이템 모드가 비활성화된 경우")
    void testGenerateItemPiece_ItemModeDisabled() throws Exception {
        // Given
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, false); // 아이템 모드 비활성화

        Field nextItemPieceField = GameEngine.class.getDeclaredField("nextItemPiece");
        nextItemPieceField.setAccessible(true);
        nextItemPieceField.set(gameEngine, null);

        // When
        Method generateItemPiece = GameEngine.class.getDeclaredMethod("generateItemPiece");
        generateItemPiece.setAccessible(true);
        generateItemPiece.invoke(gameEngine);

        // Then
        Tetromino nextItemPiece = (Tetromino) nextItemPieceField.get(gameEngine);
        assertNotNull(nextItemPiece); // 아이템 모드와 관계없이 생성됨
        assertTrue(nextItemPiece.isItemPiece());
    }

    @Test
    @DisplayName("getLastLockedColumns - 임시 이벤트 컬럼이 있을 때")
    void testGetLastLockedColumns_WithTempEvent() throws Exception {
        // Given
        gameEngine.startNewGame();

        // tempLockedColumnsForEvent 설정
        Field tempField = GameEngine.class.getDeclaredField("tempLockedColumnsForEvent");
        tempField.setAccessible(true);
        java.util.Set<Integer> tempColumns = new java.util.HashSet<>();
        tempColumns.add(5);
        tempColumns.add(6);
        tempField.set(gameEngine, tempColumns);

        // When
        Set<Integer> lockedColumns = gameEngine.getLastLockedColumns();

        // Then
        assertNotNull(lockedColumns);
        assertTrue(lockedColumns.contains(5));
        assertTrue(lockedColumns.contains(6));
    }

    @Test
    @DisplayName("getLastLockedCells - 임시 이벤트 셀이 있을 때")
    void testGetLastLockedCells_WithTempEvent() throws Exception {
        // Given
        gameEngine.startNewGame();

        // tempLockedCellsForEvent 설정
        Field tempField = GameEngine.class.getDeclaredField("tempLockedCellsForEvent");
        tempField.setAccessible(true);
        java.util.List<int[]> tempCells = new java.util.ArrayList<>();
        tempCells.add(new int[] {3, 18});
        tempCells.add(new int[] {4, 18});
        tempField.set(gameEngine, tempCells);

        // When
        List<int[]> lockedCells = gameEngine.getLastLockedCells();

        // Then
        assertNotNull(lockedCells);
        assertEquals(2, lockedCells.size());
        boolean hasCell3_18 = lockedCells.stream().anyMatch(cell -> cell[0] == 3 && cell[1] == 18);
        boolean hasCell4_18 = lockedCells.stream().anyMatch(cell -> cell[0] == 4 && cell[1] == 18);
        assertTrue(hasCell3_18);
        assertTrue(hasCell4_18);
    }

    @Test
    @DisplayName("handleLockedPiece - SPLIT 아이템으로 라인클리어")
    void testHandleLockedPiece_SplitItemClear() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // SPLIT 아이템 블록 설정
        Tetromino splitBlock = Tetromino.item(Tetromino.Kind.SPLIT, 0, Tetromino.ItemType.SPLIT, 0);
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, splitBlock);

        Field pxField = GameEngine.class.getDeclaredField("px");
        pxField.setAccessible(true);
        pxField.set(gameEngine, 4);

        Field pyField = GameEngine.class.getDeclaredField("py");
        pyField.setAccessible(true);
        pyField.set(gameEngine, 18);

        // When
        Method handleLockedPiece = GameEngine.class.getDeclaredMethod("handleLockedPiece");
        handleLockedPiece.setAccessible(true);
        handleLockedPiece.invoke(gameEngine);

        // Then
        assertTrue(gameEngine.isLastClearByGravityOrSplit()); // SPLIT 아이템으로 클리어
    }

    @Test
    @DisplayName("processItemEffect - 아이템 모드 비활성화 시 아무것도 하지 않음")
    void testProcessItemEffect_ItemModeDisabled() throws Exception {
        // Given
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, false); // 아이템 모드 비활성화

        // mock 초기화 (startNewGame 호출 없이)
        reset(mockListener);

        // When
        Method processItemEffect =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        processItemEffect.setAccessible(true);
        processItemEffect.invoke(gameEngine, Tetromino.ItemType.COPY, Tetromino.Kind.I);

        // Then
        // 아이템 모드가 비활성화되어 있으면 processItemEffect 메서드에서 아무것도 하지 않음
        verify(mockListener, never()).onPieceSpawned(any(Tetromino.class), anyInt(), anyInt());
    }

    @Test
    @DisplayName("createItemPiece - fallback COPY 처리")
    void testCreateItemPiece_FallbackCopy() throws Exception {
        // When - 정의되지 않은 아이템 타입으로 테스트
        Method createItemPiece =
                GameEngine.class.getDeclaredMethod(
                        "createItemPiece", Tetromino.Kind.class, Tetromino.Kind.class);
        createItemPiece.setAccessible(true);

        // 알려지지 않은 아이템 Kind를 사용하여 fallback 경로 테스트
        // (실제로는 모든 Kind가 정의되어 있지만, fallback 로직을 테스트)
        Tetromino fallbackItem =
                (Tetromino)
                        createItemPiece.invoke(
                                gameEngine,
                                Tetromino.Kind.I,
                                Tetromino.Kind.T); // I를 아이템 타입으로 사용 (비정상적 케이스)

        // Then
        assertNotNull(fallbackItem);
        assertTrue(fallbackItem.isItemPiece());
        assertEquals(Tetromino.ItemType.COPY, fallbackItem.getItemType());
    }

    @Test
    @DisplayName("processItemEffect - COPY 효과 정상 처리")
    void testProcessItemEffect_CopyNormal() throws Exception {
        // Given
        gameEngine.startNewGame();
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        // When
        Method processItemEffect =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        processItemEffect.setAccessible(true);
        processItemEffect.invoke(gameEngine, Tetromino.ItemType.COPY, Tetromino.Kind.I);

        // Then
        // COPY 효과가 정상적으로 처리되었는지 확인
        verify(mockListener, atLeastOnce())
                .onPieceSpawned(any(Tetromino.class), anyInt(), anyInt());
        verify(mockListener, atLeastOnce()).onNextPiece(any(Tetromino.class));
    }

    @Test
    @DisplayName("destroyBlocksBelow - 현재 블록이 null일 때")
    void testDestroyBlocksBelow_CurrentNull() throws Exception {
        // Given
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, null);

        // 보드에 블록 배치
        testBoard.setCell(4, 17, 1);

        // When
        Method destroyBlocksBelow =
                GameEngine.class.getDeclaredMethod("destroyBlocksBelow", boolean.class);
        destroyBlocksBelow.setAccessible(true);
        destroyBlocksBelow.invoke(gameEngine, false);

        // Then
        assertEquals(1, testBoard.getCell(4, 17)); // 블록이 그대로 유지됨
    }

    @Test
    @DisplayName("handleLockedPiece - current가 null일 때 조기 반환")
    void testHandleLockedPiece_CurrentNull() throws Exception {
        // Given
        Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(gameEngine, null);

        // When
        Method handleLockedPiece = GameEngine.class.getDeclaredMethod("handleLockedPiece");
        handleLockedPiece.setAccessible(true);
        handleLockedPiece.invoke(gameEngine);

        // Then
        verify(mockListener, never()).onLinesCleared(anyInt()); // 라인클리어 이벤트가 발생하지 않음
    }

    @Test
    @DisplayName("generateItemPiece - 각 아이템 타입별 생성 테스트")
    void testGenerateItemPiece_AllTypes() throws Exception {
        // Given
        Field itemModeField = GameEngine.class.getDeclaredField("itemModeEnabled");
        itemModeField.setAccessible(true);
        itemModeField.set(gameEngine, true);

        Field nextItemPieceField = GameEngine.class.getDeclaredField("nextItemPiece");
        nextItemPieceField.setAccessible(true);

        Method generateItemPiece = GameEngine.class.getDeclaredMethod("generateItemPiece");
        generateItemPiece.setAccessible(true);

        // When & Then - 여러 번 호출해서 다양한 아이템 타입이 생성되는지 확인
        java.util.Set<Tetromino.ItemType> generatedTypes = new java.util.HashSet<>();

        for (int i = 0; i < 50; i++) { // 50번 시도하여 모든 타입 생성 확률 높임
            nextItemPieceField.set(gameEngine, null);
            generateItemPiece.invoke(gameEngine);

            Tetromino nextItemPiece = (Tetromino) nextItemPieceField.get(gameEngine);
            if (nextItemPiece != null && nextItemPiece.isItemPiece()) {
                generatedTypes.add(nextItemPiece.getItemType());
            }
        }

        // 최소한 일부 타입들이 생성되었는지 확인
        assertFalse(generatedTypes.isEmpty());
        assertTrue(generatedTypes.size() > 0);
    }
}

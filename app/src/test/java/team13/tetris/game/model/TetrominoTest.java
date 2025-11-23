package team13.tetris.game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.game.model.Tetromino.ItemType;
import team13.tetris.game.model.Tetromino.Kind;

// Tetromino 클래스 테스트: Tests Tetromino class functionality
@DisplayName("Tetromino 테스트")
public class TetrominoTest {

    @Test
    @DisplayName("Kind로 Tetromino 생성 시 올바른 ID가 설정되어야 함")
    void testCreateTetrominoWithKind() {
        Tetromino tetromino = new Tetromino(Kind.I);
        assertEquals(1, tetromino.getId());
        assertEquals(Kind.I, tetromino.getKind());

        Tetromino oTetromino = new Tetromino(Kind.O);
        assertEquals(2, oTetromino.getId());
        assertEquals(Kind.O, oTetromino.getKind());
    }

    @Test
    @DisplayName("모든 Kind에 대해 올바른 ID가 할당되어야 함")
    void testAllKindsHaveCorrectIds() {
        assertEquals(1, new Tetromino(Kind.I).getId());
        assertEquals(2, new Tetromino(Kind.O).getId());
        assertEquals(3, new Tetromino(Kind.T).getId());
        assertEquals(4, new Tetromino(Kind.S).getId());
        assertEquals(5, new Tetromino(Kind.Z).getId());
        assertEquals(6, new Tetromino(Kind.J).getId());
        assertEquals(7, new Tetromino(Kind.L).getId());
        assertEquals(8, new Tetromino(Kind.WEIGHT).getId());
        assertEquals(9, new Tetromino(Kind.COPY).getId());
        assertEquals(10, new Tetromino(Kind.GRAVITY).getId());
        assertEquals(11, new Tetromino(Kind.SPLIT).getId());
    }

    @Test
    @DisplayName("회전값을 지정하여 Tetromino를 생성할 수 있어야 함")
    void testCreateTetrominoWithRotation() {
        Tetromino tetromino = new Tetromino(Kind.T, 1);
        assertNotNull(tetromino.getShape());
        assertEquals(Kind.T, tetromino.getKind());
    }

    @Test
    @DisplayName("raw shape로 Tetromino를 생성할 수 있어야 함")
    void testCreateTetrominoWithRawShape() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };
        Tetromino tetromino = new Tetromino(5, shape);

        assertEquals(5, tetromino.getId());
        assertNull(tetromino.getKind());
        assertArrayEquals(shape, tetromino.getShape());
    }

    @Test
    @DisplayName("getShape는 방어적 복사본을 반환해야 함")
    void testGetShapeDefensiveCopy() {
        Tetromino tetromino = new Tetromino(Kind.I);
        int[][] shape1 = tetromino.getShape();
        int[][] shape2 = tetromino.getShape();

        // 서로 다른 객체여야 함
        assertNotSame(shape1, shape2);

        // 수정해도 원본에 영향 없어야 함
        shape1[0][0] = 999;
        assertNotEquals(999, tetromino.getShape()[0][0]);
    }

    @Test
    @DisplayName("I 미노는 4x4 크기를 가져야 함")
    void testITetrominoSize() {
        Tetromino iTetromino = new Tetromino(Kind.I);
        assertEquals(4, iTetromino.getHeight());
        assertEquals(4, iTetromino.getWidth());
    }

    @Test
    @DisplayName("O 미노는 4x4 크기를 가져야 함")
    void testOTetrominoSize() {
        Tetromino oTetromino = new Tetromino(Kind.O);
        assertEquals(4, oTetromino.getHeight());
        assertEquals(4, oTetromino.getWidth());
    }

    @Test
    @DisplayName("T 미노는 4x4 크기를 가져야 함")
    void testTTetrominoSize() {
        Tetromino tTetromino = new Tetromino(Kind.T);
        assertEquals(4, tTetromino.getHeight());
        assertEquals(4, tTetromino.getWidth());
    }

    @Test
    @DisplayName("시계방향 회전이 올바르게 동작해야 함")
    void testRotateClockwise() {
        Tetromino original = new Tetromino(Kind.T, 0);
        Tetromino rotated = original.rotateClockwise();

        assertNotNull(rotated);
        assertEquals(Kind.T, rotated.getKind());
        assertNotSame(original, rotated);
    }

    @Test
    @DisplayName("4번 시계방향 회전 시 원래 모양으로 돌아와야 함")
    void testFourClockwiseRotationsReturnToOriginal() {
        Tetromino original = new Tetromino(Kind.L);
        Tetromino rotated =
                original.rotateClockwise().rotateClockwise().rotateClockwise().rotateClockwise();

        assertArrayEquals(original.getShape(), rotated.getShape());
    }

    @Test
    @DisplayName("O 미노는 회전해도 모양이 변하지 않아야 함")
    void testOTetrominoRotationInvariant() {
        Tetromino original = new Tetromino(Kind.O);
        Tetromino rotated = original.rotateClockwise();

        assertArrayEquals(original.getShape(), rotated.getShape());
    }

    @Test
    @DisplayName("raw shape의 시계방향 회전이 올바르게 동작해야 함")
    void testRawShapeRotateClockwise() {
        int[][] shape = {
            {1, 0},
            {1, 1}
        };
        Tetromino tetromino = new Tetromino(1, shape);
        Tetromino rotated = tetromino.rotateClockwise();

        int[][] expected = {
            {1, 1},
            {1, 0}
        };

        assertArrayEquals(expected, rotated.getShape());
    }

    @Test
    @DisplayName("Kind별로 올바른 block style class를 반환해야 함")
    void testGetBlockStyleClass() {
        assertEquals("block-I", new Tetromino(Kind.I).getBlockStyleClass());
        assertEquals("block-O", new Tetromino(Kind.O).getBlockStyleClass());
        assertEquals("block-T", new Tetromino(Kind.T).getBlockStyleClass());
        assertEquals("block-S", new Tetromino(Kind.S).getBlockStyleClass());
        assertEquals("block-Z", new Tetromino(Kind.Z).getBlockStyleClass());
        assertEquals("block-J", new Tetromino(Kind.J).getBlockStyleClass());
        assertEquals("block-L", new Tetromino(Kind.L).getBlockStyleClass());
    }

    @Test
    @DisplayName("Kind별로 올바른 text style class를 반환해야 함")
    void testGetTextStyleClass() {
        assertEquals("tetris-i-text", new Tetromino(Kind.I).getTextStyleClass());
        assertEquals("tetris-o-text", new Tetromino(Kind.O).getTextStyleClass());
        assertEquals("tetris-t-text", new Tetromino(Kind.T).getTextStyleClass());
        assertEquals("tetris-s-text", new Tetromino(Kind.S).getTextStyleClass());
        assertEquals("tetris-z-text", new Tetromino(Kind.Z).getTextStyleClass());
        assertEquals("tetris-j-text", new Tetromino(Kind.J).getTextStyleClass());
        assertEquals("tetris-l-text", new Tetromino(Kind.L).getTextStyleClass());
    }

    @Test
    @DisplayName("raw shape에서 ID로 block style class를 반환해야 함")
    void testRawShapeBlockStyleClass() {
        int[][] shape = {{1}};
        assertEquals("block-I", new Tetromino(1, shape).getBlockStyleClass());
        assertEquals("block-O", new Tetromino(2, shape).getBlockStyleClass());
        assertEquals("block-T", new Tetromino(3, shape).getBlockStyleClass());
    }

    @Test
    @DisplayName("raw shape에서 ID로 text style class를 반환해야 함")
    void testRawShapeTextStyleClass() {
        int[][] shape = {{1}};
        assertEquals("tetris-i-text", new Tetromino(1, shape).getTextStyleClass());
        assertEquals("tetris-o-text", new Tetromino(2, shape).getTextStyleClass());
        assertEquals("tetris-t-text", new Tetromino(3, shape).getTextStyleClass());
    }

    @Test
    @DisplayName("kindForId는 ID로 Kind를 찾을 수 있어야 함")
    void testKindForId() {
        assertEquals(Kind.I, Tetromino.kindForId(1));
        assertEquals(Kind.O, Tetromino.kindForId(2));
        assertEquals(Kind.T, Tetromino.kindForId(3));
        assertEquals(Kind.S, Tetromino.kindForId(4));
        assertEquals(Kind.Z, Tetromino.kindForId(5));
        assertEquals(Kind.J, Tetromino.kindForId(6));
        assertEquals(Kind.L, Tetromino.kindForId(7));
    }

    @Test
    @DisplayName("kindForId는 존재하지 않는 ID에 대해 null을 반환해야 함")
    void testKindForIdNotFound() {
        assertNull(Tetromino.kindForId(99));
        assertNull(Tetromino.kindForId(-1));
    }

    @Test
    @DisplayName("of 팩토리 메서드는 회전값 0으로 Tetromino를 생성해야 함")
    void testOfFactory() {
        Tetromino tetromino = Tetromino.of(Kind.T);
        assertEquals(Kind.T, tetromino.getKind());
        assertEquals(3, tetromino.getId());
    }

    @Test
    @DisplayName("아이템 미노(COPY) 생성 시 isItemPiece가 true여야 함")
    void testCreateItemPieceCopy() {
        Tetromino itemPiece = Tetromino.item(Kind.T, 0, ItemType.COPY, 1);

        assertTrue(itemPiece.isItemPiece());
        assertEquals(ItemType.COPY, itemPiece.getItemType());
        assertEquals(1, itemPiece.getCopyBlockIndex());
    }

    @Test
    @DisplayName("아이템 미노(WEIGHT)는 회전할 수 없어야 함")
    void testWeightCannotRotate() {
        Tetromino weight = Tetromino.item(Kind.WEIGHT, 0, ItemType.WEIGHT, 0);

        assertFalse(weight.canRotate());
        assertTrue(weight.isItemPiece());
        assertEquals(ItemType.WEIGHT, weight.getItemType());
    }

    @Test
    @DisplayName("아이템 미노(GRAVITY)는 회전할 수 없어야 함")
    void testGravityCannotRotate() {
        Tetromino gravity = Tetromino.item(Kind.GRAVITY, 0, ItemType.GRAVITY, 0);

        assertFalse(gravity.canRotate());
        assertTrue(gravity.isItemPiece());
        assertEquals(ItemType.GRAVITY, gravity.getItemType());
    }

    @Test
    @DisplayName("아이템 미노(SPLIT)는 회전할 수 없어야 함")
    void testSplitCannotRotate() {
        Tetromino split = Tetromino.item(Kind.SPLIT, 0, ItemType.SPLIT, 0);

        assertFalse(split.canRotate());
        assertTrue(split.isItemPiece());
        assertEquals(ItemType.SPLIT, split.getItemType());
    }

    @Test
    @DisplayName("아이템 미노(LINE_CLEAR) 생성 시 올바른 타입이 설정되어야 함")
    void testCreateItemPieceLineClear() {
        Tetromino lineClear = Tetromino.lineClearItem(Kind.T, 0, 2);

        assertTrue(lineClear.isItemPiece());
        assertEquals(ItemType.LINE_CLEAR, lineClear.getItemType());
        assertEquals(2, lineClear.getLineClearBlockIndex());
        assertTrue(lineClear.canRotate());
    }

    @Test
    @DisplayName("일반 미노는 isItemPiece가 false여야 함")
    void testNormalPieceIsNotItem() {
        Tetromino normal = new Tetromino(Kind.I);
        assertFalse(normal.isItemPiece());
        assertNull(normal.getItemType());
        assertEquals(-1, normal.getCopyBlockIndex());
    }

    @Test
    @DisplayName("일반 미노는 회전 가능해야 함")
    void testNormalPieceCanRotate() {
        Tetromino normal = new Tetromino(Kind.T);
        assertTrue(normal.canRotate());
    }

    @Test
    @DisplayName("잠금 상태를 설정하고 가져올 수 있어야 함")
    void testLockState() {
        Tetromino tetromino = new Tetromino(Kind.I);
        assertFalse(tetromino.isLocked());

        tetromino.setLocked(true);
        assertTrue(tetromino.isLocked());

        tetromino.setLocked(false);
        assertFalse(tetromino.isLocked());
    }

    @Test
    @DisplayName("getBlockPositions는 블록이 있는 모든 위치를 반환해야 함")
    void testGetBlockPositions() {
        Tetromino oTetromino = new Tetromino(Kind.O);
        int[][] positions = oTetromino.getBlockPositions();

        assertEquals(4, positions.length); // O 미노는 4개 블록

        // 각 위치가 유효한지 확인
        for (int[] pos : positions) {
            assertEquals(2, pos.length); // [row, col]
            assertTrue(pos[0] >= 0 && pos[0] < 4);
            assertTrue(pos[1] >= 0 && pos[1] < 4);
        }
    }

    @Test
    @DisplayName("I 미노는 4개의 블록 위치를 가져야 함")
    void testITetrominoBlockPositions() {
        Tetromino iTetromino = new Tetromino(Kind.I);
        int[][] positions = iTetromino.getBlockPositions();
        assertEquals(4, positions.length);
    }

    @Test
    @DisplayName("T 미노는 4개의 블록 위치를 가져야 함")
    void testTTetrominoBlockPositions() {
        Tetromino tTetromino = new Tetromino(Kind.T);
        int[][] positions = tTetromino.getBlockPositions();
        assertEquals(4, positions.length);
    }

    @Test
    @DisplayName("COPY 아이템 미노 회전 시 copyBlockIndex가 올바르게 업데이트되어야 함")
    void testCopyItemRotation() {
        Tetromino copyItem = Tetromino.item(Kind.T, 0, ItemType.COPY, 1);

        assertEquals(1, copyItem.getCopyBlockIndex());

        Tetromino rotated = copyItem.rotateClockwise();
        assertTrue(rotated.isItemPiece());
        assertEquals(ItemType.COPY, rotated.getItemType());
        // 회전 후에도 copyBlockIndex가 유효해야 함
        assertTrue(rotated.getCopyBlockIndex() >= 0);
    }

    @Test
    @DisplayName("LINE_CLEAR 아이템 미노 회전 시 lineClearBlockIndex가 올바르게 업데이트되어야 함")
    void testLineClearItemRotation() {
        Tetromino lineClearItem = Tetromino.lineClearItem(Kind.L, 0, 2);

        assertEquals(2, lineClearItem.getLineClearBlockIndex());

        Tetromino rotated = lineClearItem.rotateClockwise();
        assertTrue(rotated.isItemPiece());
        assertEquals(ItemType.LINE_CLEAR, rotated.getItemType());
        // 회전 후에도 lineClearBlockIndex가 유효해야 함
        assertTrue(rotated.getLineClearBlockIndex() >= 0);
    }

    @Test
    @DisplayName("O 미노 COPY 아이템 시계방향 회전 시 올바른 인덱스가 계산되어야 함")
    void testOMinoCopyClockwiseRotation() {
        // O 미노는 블록 위치가 고정: (0,1), (0,2), (1,1), (1,2)
        // 시계방향 회전: 0 -> 1 -> 3 -> 2 -> 0
        Tetromino copyItem0 = Tetromino.item(Kind.O, 0, ItemType.COPY, 0);

        Tetromino rotated1 = copyItem0.rotateClockwise();
        assertEquals(1, rotated1.getCopyBlockIndex());

        Tetromino rotated2 = rotated1.rotateClockwise();
        assertEquals(3, rotated2.getCopyBlockIndex());

        Tetromino rotated3 = rotated2.rotateClockwise();
        assertEquals(2, rotated3.getCopyBlockIndex());

        Tetromino rotated4 = rotated3.rotateClockwise();
        assertEquals(0, rotated4.getCopyBlockIndex());
    }

    @Test
    @DisplayName("I 미노 회전 시 4개 블록이 유지되어야 함")
    void testIMinoRotationMaintainsBlockCount() {
        Tetromino iTetromino = new Tetromino(Kind.I);

        for (int i = 0; i < 4; i++) {
            int[][] positions = iTetromino.getBlockPositions();
            assertEquals(4, positions.length, "회전 " + i + "에서 블록 수가 4개여야 함");
            iTetromino = iTetromino.rotateClockwise();
        }
    }

    @Test
    @DisplayName("모든 Kind의 회전이 정상적으로 동작해야 함")
    void testAllKindsCanRotate() {
        Kind[] normalKinds = {Kind.I, Kind.O, Kind.T, Kind.S, Kind.Z, Kind.J, Kind.L};

        for (Kind kind : normalKinds) {
            Tetromino tetromino = new Tetromino(kind);
            assertDoesNotThrow(
                    () -> {
                        tetromino.rotateClockwise();
                    },
                    kind + " 회전 시 예외가 발생하지 않아야 함");
        }
    }

    @Test
    @DisplayName("회전값이 4 이상이어도 올바르게 정규화되어야 함")
    void testRotationNormalization() {
        Tetromino tetromino1 = new Tetromino(Kind.T, 0);
        Tetromino tetromino2 = new Tetromino(Kind.T, 4);
        Tetromino tetromino3 = new Tetromino(Kind.T, 8);

        assertArrayEquals(tetromino1.getShape(), tetromino2.getShape());
        assertArrayEquals(tetromino1.getShape(), tetromino3.getShape());
    }

    @Test
    @DisplayName("COPY 아이템과 LINE_CLEAR 아이템은 서로 독립적이어야 함")
    void testCopyAndLineClearAreIndependent() {
        Tetromino copyItem = Tetromino.item(Kind.T, 0, ItemType.COPY, 1);
        Tetromino lineClearItem = Tetromino.lineClearItem(Kind.T, 0, 2);

        assertEquals(ItemType.COPY, copyItem.getItemType());
        assertEquals(ItemType.LINE_CLEAR, lineClearItem.getItemType());

        assertEquals(1, copyItem.getCopyBlockIndex());
        assertEquals(-1, copyItem.getLineClearBlockIndex());

        assertEquals(-1, lineClearItem.getCopyBlockIndex());
        assertEquals(2, lineClearItem.getLineClearBlockIndex());
    }

    @Test
    @DisplayName("아이템 종류별 CSS 클래스가 올바르게 설정되어야 함")
    void testItemKindStyleClasses() {
        assertEquals("block-weight", new Tetromino(Kind.WEIGHT).getBlockStyleClass());
        assertEquals("block-copy", new Tetromino(Kind.COPY).getBlockStyleClass());
        assertEquals("block-gravity", new Tetromino(Kind.GRAVITY).getBlockStyleClass());
        assertEquals("block-split", new Tetromino(Kind.SPLIT).getBlockStyleClass());

        assertEquals("tetris-weight-text", new Tetromino(Kind.WEIGHT).getTextStyleClass());
        assertEquals("tetris-copy-text", new Tetromino(Kind.COPY).getTextStyleClass());
        assertEquals("tetris-gravity-text", new Tetromino(Kind.GRAVITY).getTextStyleClass());
        assertEquals("tetris-split-text", new Tetromino(Kind.SPLIT).getTextStyleClass());
    }

    @Test
    @DisplayName("getRotationIndex()가 올바른 회전 인덱스를 반환해야 함")
    void testGetRotationIndex() {
        // 회전값 0으로 생성
        Tetromino tetromino0 = new Tetromino(Kind.T, 0);
        assertEquals(0, tetromino0.getRotationIndex());

        // 회전값 1로 생성
        Tetromino tetromino1 = new Tetromino(Kind.T, 1);
        assertEquals(1, tetromino1.getRotationIndex());

        // 회전값 2로 생성
        Tetromino tetromino2 = new Tetromino(Kind.T, 2);
        assertEquals(2, tetromino2.getRotationIndex());

        // 회전값 3으로 생성
        Tetromino tetromino3 = new Tetromino(Kind.T, 3);
        assertEquals(3, tetromino3.getRotationIndex());
    }

    @Test
    @DisplayName("getRotationIndex()가 회전값을 4로 나눈 나머지를 반환해야 함")
    void testGetRotationIndexModulo() {
        // 회전값 4 (= 0 % 4)
        Tetromino tetromino4 = new Tetromino(Kind.I, 4);
        assertEquals(0, tetromino4.getRotationIndex());

        // 회전값 5 (= 1 % 4)
        Tetromino tetromino5 = new Tetromino(Kind.I, 5);
        assertEquals(1, tetromino5.getRotationIndex());

        // 회전값 8 (= 0 % 4)
        Tetromino tetromino8 = new Tetromino(Kind.L, 8);
        assertEquals(0, tetromino8.getRotationIndex());

        // 회전값 11 (= 3 % 4)
        Tetromino tetromino11 = new Tetromino(Kind.S, 11);
        assertEquals(3, tetromino11.getRotationIndex());
    }

    @Test
    @DisplayName("시계방향 회전 후 getRotationIndex()가 올바르게 증가해야 함")
    void testGetRotationIndexAfterRotation() {
        Tetromino original = new Tetromino(Kind.J, 0);
        assertEquals(0, original.getRotationIndex());

        Tetromino rotated1 = original.rotateClockwise();
        assertEquals(1, rotated1.getRotationIndex());

        Tetromino rotated2 = rotated1.rotateClockwise();
        assertEquals(2, rotated2.getRotationIndex());

        Tetromino rotated3 = rotated2.rotateClockwise();
        assertEquals(3, rotated3.getRotationIndex());

        // 4번째 회전 후 0으로 돌아가야 함
        Tetromino rotated4 = rotated3.rotateClockwise();
        assertEquals(0, rotated4.getRotationIndex());
    }

    @Test
    @DisplayName("raw shape으로 생성된 Tetromino의 getRotationIndex()는 0이어야 함")
    void testRawShapeRotationIndex() {
        int[][] shape = {{1, 1}, {1, 0}};
        Tetromino rawTetromino = new Tetromino(1, shape);
        assertEquals(0, rawTetromino.getRotationIndex());
    }

    @Test
    @DisplayName("LINE_CLEAR 아이템의 getLineClearBlockRow()가 올바른 행을 반환해야 함")
    void testGetLineClearBlockRow() {
        // T 미노의 기본 모양에서 블록 위치 확인
        // T 미노 (rotation 0): {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}
        // 블록 인덱스: 0=(0,1), 1=(1,0), 2=(1,1), 3=(1,2)

        // 블록 인덱스 0 (행 0, 열 1)
        Tetromino lineClear0 = Tetromino.lineClearItem(Kind.T, 0, 0);
        assertEquals(0, lineClear0.getLineClearBlockRow());

        // 블록 인덱스 1 (행 1, 열 0)
        Tetromino lineClear1 = Tetromino.lineClearItem(Kind.T, 0, 1);
        assertEquals(1, lineClear1.getLineClearBlockRow());

        // 블록 인덱스 2 (행 1, 열 1)
        Tetromino lineClear2 = Tetromino.lineClearItem(Kind.T, 0, 2);
        assertEquals(1, lineClear2.getLineClearBlockRow());

        // 블록 인덱스 3 (행 1, 열 2)
        Tetromino lineClear3 = Tetromino.lineClearItem(Kind.T, 0, 3);
        assertEquals(1, lineClear3.getLineClearBlockRow());
    }

    @Test
    @DisplayName("LINE_CLEAR 아이템의 getLineClearBlockCol()이 올바른 열을 반환해야 함")
    void testGetLineClearBlockCol() {
        // T 미노의 기본 모양에서 블록 위치 확인
        // T 미노 (rotation 0): {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}
        // 블록 인덱스: 0=(0,1), 1=(1,0), 2=(1,1), 3=(1,2)

        // 블록 인덱스 0 (행 0, 열 1)
        Tetromino lineClear0 = Tetromino.lineClearItem(Kind.T, 0, 0);
        assertEquals(1, lineClear0.getLineClearBlockCol());

        // 블록 인덱스 1 (행 1, 열 0)
        Tetromino lineClear1 = Tetromino.lineClearItem(Kind.T, 0, 1);
        assertEquals(0, lineClear1.getLineClearBlockCol());

        // 블록 인덱스 2 (행 1, 열 1)
        Tetromino lineClear2 = Tetromino.lineClearItem(Kind.T, 0, 2);
        assertEquals(1, lineClear2.getLineClearBlockCol());

        // 블록 인덱스 3 (행 1, 열 2)
        Tetromino lineClear3 = Tetromino.lineClearItem(Kind.T, 0, 3);
        assertEquals(2, lineClear3.getLineClearBlockCol());
    }

    @Test
    @DisplayName("일반 미노(비아이템)의 getLineClearBlockRow()는 -1을 반환해야 함")
    void testNormalPieceLineClearBlockRow() {
        Tetromino normalPiece = new Tetromino(Kind.I);
        assertEquals(-1, normalPiece.getLineClearBlockRow());
    }

    @Test
    @DisplayName("일반 미노(비아이템)의 getLineClearBlockCol()은 -1을 반환해야 함")
    void testNormalPieceLineClearBlockCol() {
        Tetromino normalPiece = new Tetromino(Kind.O);
        assertEquals(-1, normalPiece.getLineClearBlockCol());
    }

    @Test
    @DisplayName("COPY 아이템의 getLineClearBlockRow()는 -1을 반환해야 함")
    void testCopyItemLineClearBlockRow() {
        Tetromino copyItem = Tetromino.item(Kind.S, 0, ItemType.COPY, 1);
        assertEquals(-1, copyItem.getLineClearBlockRow());
    }

    @Test
    @DisplayName("COPY 아이템의 getLineClearBlockCol()은 -1을 반환해야 함")
    void testCopyItemLineClearBlockCol() {
        Tetromino copyItem = Tetromino.item(Kind.Z, 0, ItemType.COPY, 2);
        assertEquals(-1, copyItem.getLineClearBlockCol());
    }

    @Test
    @DisplayName("I 미노 LINE_CLEAR 아이템의 행/열이 올바르게 설정되어야 함")
    void testIMinoLineClearBlockPosition() {
        // I 미노 (rotation 0): {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}}
        // 블록 인덱스: 0=(1,0), 1=(1,1), 2=(1,2), 3=(1,3)

        Tetromino iLineClear0 = Tetromino.lineClearItem(Kind.I, 0, 0);
        assertEquals(1, iLineClear0.getLineClearBlockRow());
        assertEquals(0, iLineClear0.getLineClearBlockCol());

        Tetromino iLineClear2 = Tetromino.lineClearItem(Kind.I, 0, 2);
        assertEquals(1, iLineClear2.getLineClearBlockRow());
        assertEquals(2, iLineClear2.getLineClearBlockCol());
    }

    @Test
    @DisplayName("O 미노 LINE_CLEAR 아이템의 행/열이 올바르게 설정되어야 함")
    void testOMinoLineClearBlockPosition() {
        // O 미노 (rotation 0): {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}
        // 블록 인덱스: 0=(0,1), 1=(0,2), 2=(1,1), 3=(1,2)

        Tetromino oLineClear0 = Tetromino.lineClearItem(Kind.O, 0, 0);
        assertEquals(0, oLineClear0.getLineClearBlockRow());
        assertEquals(1, oLineClear0.getLineClearBlockCol());

        Tetromino oLineClear3 = Tetromino.lineClearItem(Kind.O, 0, 3);
        assertEquals(1, oLineClear3.getLineClearBlockRow());
        assertEquals(2, oLineClear3.getLineClearBlockCol());
    }

    @Test
    @DisplayName("LINE_CLEAR 아이템 회전 후 행/열 위치가 올바르게 업데이트되어야 함")
    void testLineClearBlockPositionAfterRotation() {
        // T 미노로 테스트
        Tetromino lineClearOriginal = Tetromino.lineClearItem(Kind.T, 0, 0);
        assertEquals(0, lineClearOriginal.getLineClearBlockRow());
        assertEquals(1, lineClearOriginal.getLineClearBlockCol());

        Tetromino rotated = lineClearOriginal.rotateClockwise();
        assertTrue(rotated.isItemPiece());
        assertEquals(ItemType.LINE_CLEAR, rotated.getItemType());

        // 회전 후에도 유효한 행/열 값을 가져야 함 (정확한 값은 회전 로직에 따라 결정됨)
        assertTrue(rotated.getLineClearBlockRow() >= 0);
        assertTrue(rotated.getLineClearBlockCol() >= 0);
    }

    @Test
    @DisplayName("잘못된 블록 인덱스로 LINE_CLEAR 아이템 생성 시 -1을 반환해야 함")
    void testInvalidLineClearBlockIndex() {
        // 유효하지 않은 블록 인덱스 (-1)로 생성
        Tetromino invalidLineClear = Tetromino.lineClearItem(Kind.T, 0, -1);
        assertEquals(-1, invalidLineClear.getLineClearBlockRow());
        assertEquals(-1, invalidLineClear.getLineClearBlockCol());
        assertEquals(-1, invalidLineClear.getLineClearBlockIndex());
    }

    @Test
    @DisplayName("다양한 회전값에서 getRotationIndex()가 올바르게 동작해야 함")
    void testGetRotationIndexWithVariousKinds() {
        Kind[] allKinds = {Kind.I, Kind.O, Kind.T, Kind.S, Kind.Z, Kind.J, Kind.L};

        for (Kind kind : allKinds) {
            for (int rotation = 0; rotation < 8; rotation++) {
                Tetromino tetromino = new Tetromino(kind, rotation);
                assertEquals(
                        rotation % 4,
                        tetromino.getRotationIndex(),
                        kind
                                + " with rotation "
                                + rotation
                                + " should have rotationIndex "
                                + (rotation % 4));
            }
        }
    }

    @Test
    @DisplayName("아이템 미노의 getRotationIndex()가 올바르게 동작해야 함")
    void testItemPieceRotationIndex() {
        // COPY 아이템
        Tetromino copyItem = Tetromino.item(Kind.L, 2, ItemType.COPY, 1);
        assertEquals(2, copyItem.getRotationIndex());

        // LINE_CLEAR 아이템
        Tetromino lineClearItem = Tetromino.lineClearItem(Kind.J, 1, 2);
        assertEquals(1, lineClearItem.getRotationIndex());

        // WEIGHT 아이템
        Tetromino weightItem = Tetromino.item(Kind.WEIGHT, 3, ItemType.WEIGHT, 0);
        assertEquals(3, weightItem.getRotationIndex());
    }
}

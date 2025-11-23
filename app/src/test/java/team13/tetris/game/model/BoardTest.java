package team13.tetris.game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Board 클래스 테스트: Tests Board class functionality
@DisplayName("Board 테스트")
public class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
    }

    @Test
    @DisplayName("보드 생성 시 너비와 높이가 올바르게 설정되어야 함")
    void testBoardCreation() {
        assertEquals(10, board.getWidth());
        assertEquals(20, board.getHeight());
    }

    @Test
    @DisplayName("잘못된 크기로 보드 생성 시 예외가 발생해야 함")
    void testInvalidBoardSize() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Board(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Board(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new Board(10, -1));
    }

    @Test
    @DisplayName("새로 생성된 보드의 모든 셀은 0이어야 함")
    void testBoardInitiallyEmpty() {
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
                assertFalse(board.isOccupied(x, y));
            }
        }
    }

    @Test
    @DisplayName("유효한 범위의 셀 값을 설정하고 가져올 수 있어야 함")
    void testSetAndGetCell() {
        board.setCell(5, 10, 7);
        assertEquals(7, board.getCell(5, 10));
        assertTrue(board.isOccupied(5, 10));

        board.setCell(0, 0, 3);
        assertEquals(3, board.getCell(0, 0));

        board.setCell(9, 19, 5);
        assertEquals(5, board.getCell(9, 19));
    }

    @Test
    @DisplayName("범위를 벗어난 셀 접근 시 -1을 반환해야 함")
    void testGetCellOutOfBounds() {
        assertEquals(-1, board.getCell(-1, 0));
        assertEquals(-1, board.getCell(0, -1));
        assertEquals(-1, board.getCell(10, 0));
        assertEquals(-1, board.getCell(0, 20));
        assertEquals(-1, board.getCell(15, 25));
    }

    @Test
    @DisplayName("범위를 벗어난 셀은 점유된 것으로 간주해야 함")
    void testIsOccupiedOutOfBounds() {
        assertTrue(board.isOccupied(-1, 0));
        assertTrue(board.isOccupied(0, -1));
        assertTrue(board.isOccupied(10, 0));
        assertTrue(board.isOccupied(0, 20));
    }

    @Test
    @DisplayName("범위를 벗어난 셀 설정은 무시되어야 함")
    void testSetCellOutOfBounds() {
        board.setCell(-1, 0, 5);
        board.setCell(0, -1, 5);
        board.setCell(10, 0, 5);
        board.setCell(0, 20, 5);

        // 보드가 여전히 비어있는지 확인
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
            }
        }
    }

    @Test
    @DisplayName("clear 메서드는 모든 셀을 0으로 초기화해야 함")
    void testClear() {
        // 보드에 값 설정
        board.setCell(0, 0, 1);
        board.setCell(5, 10, 2);
        board.setCell(9, 19, 3);

        board.clear();

        // 모든 셀이 0인지 확인
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
            }
        }
    }

    @Test
    @DisplayName("placePiece는 주어진 모양을 보드에 배치해야 함")
    void testPlacePiece() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };

        board.placePiece(shape, 3, 5, 7);

        assertEquals(7, board.getCell(3, 5));
        assertEquals(7, board.getCell(4, 5));
        assertEquals(7, board.getCell(3, 6));
        assertEquals(7, board.getCell(4, 6));
    }

    @Test
    @DisplayName("placePiece는 0이 아닌 값만 배치해야 함")
    void testPlacePieceIgnoresZeros() {
        int[][] shape = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 0, 0}
        };

        board.placePiece(shape, 2, 3, 5);

        assertEquals(0, board.getCell(2, 3));
        assertEquals(5, board.getCell(3, 3));
        assertEquals(0, board.getCell(4, 3));
        assertEquals(5, board.getCell(2, 4));
        assertEquals(5, board.getCell(3, 4));
        assertEquals(5, board.getCell(4, 4));
        assertEquals(0, board.getCell(2, 5));
        assertEquals(0, board.getCell(3, 5));
        assertEquals(0, board.getCell(4, 5));
    }

    @Test
    @DisplayName("placePiece는 범위를 벗어난 부분을 무시해야 함")
    void testPlacePieceOutOfBounds() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };

        // 왼쪽 경계를 벗어남
        board.placePiece(shape, -1, 0, 3);
        assertEquals(3, board.getCell(0, 0));
        assertEquals(3, board.getCell(0, 1));

        board.clear();

        // 오른쪽 경계를 벗어남
        board.placePiece(shape, 9, 0, 4);
        assertEquals(4, board.getCell(9, 0));
        assertEquals(4, board.getCell(9, 1));

        board.clear();

        // 아래쪽 경계를 벗어남
        board.placePiece(shape, 0, 19, 5);
        assertEquals(5, board.getCell(0, 19));
        assertEquals(5, board.getCell(1, 19));
    }

    @Test
    @DisplayName("placeItemPiece - COPY 타입은 지정된 블록만 100번대 값으로 설정해야 함")
    void testPlaceItemPieceCopy() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };

        board.placeItemPiece(shape, 3, 5, 7, 2, "COPY");

        // 첫 3개 블록은 일반(7), 3번째 블록(인덱스 2)은 아이템(107)
        assertEquals(7, board.getCell(3, 5)); // 블록 0
        assertEquals(7, board.getCell(4, 5)); // 블록 1
        assertEquals(107, board.getCell(3, 6)); // 블록 2 (아이템)
        assertEquals(7, board.getCell(4, 6)); // 블록 3
    }

    @Test
    @DisplayName("placeItemPiece - LINE_CLEAR 타입은 지정된 블록만 200번대 값으로 설정해야 함")
    void testPlaceItemPieceLineClear() {
        int[][] shape = {{1, 1, 1}};

        board.placeItemPiece(shape, 0, 0, 5, 1, "LINE_CLEAR");

        assertEquals(5, board.getCell(0, 0)); // 블록 0
        assertEquals(205, board.getCell(1, 0)); // 블록 1 (아이템)
        assertEquals(5, board.getCell(2, 0)); // 블록 2
    }

    @Test
    @DisplayName("placeItemPiece - WEIGHT 타입은 모든 블록을 300번대 값으로 설정해야 함")
    void testPlaceItemPieceWeight() {
        int[][] shape = {{1, 1}};

        board.placeItemPiece(shape, 2, 3, 4, 0, "WEIGHT");

        assertEquals(304, board.getCell(2, 3));
        assertEquals(304, board.getCell(3, 3));
    }

    @Test
    @DisplayName("placeItemPiece - GRAVITY 타입은 모든 블록을 400번대 값으로 설정해야 함")
    void testPlaceItemPieceGravity() {
        int[][] shape = {{1}};

        board.placeItemPiece(shape, 5, 5, 6, 0, "GRAVITY");

        assertEquals(406, board.getCell(5, 5));
    }

    @Test
    @DisplayName("placeItemPiece - SPLIT 타입은 모든 블록을 500번대 값으로 설정해야 함")
    void testPlaceItemPieceSplit() {
        int[][] shape = {{1, 1, 1}};

        board.placeItemPiece(shape, 1, 1, 2, 0, "SPLIT");

        assertEquals(502, board.getCell(1, 1));
        assertEquals(502, board.getCell(2, 1));
        assertEquals(502, board.getCell(3, 1));
    }

    @Test
    @DisplayName("fits는 빈 공간에 모양을 놓을 수 있는지 확인해야 함")
    void testFitsEmptySpace() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };

        assertTrue(board.fits(shape, 0, 0));
        assertTrue(board.fits(shape, 5, 10));
        assertTrue(board.fits(shape, 8, 18)); // 경계 안쪽
    }

    @Test
    @DisplayName("fits는 이미 블록이 있는 곳에는 false를 반환해야 함")
    void testFitsOccupiedSpace() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };

        board.setCell(3, 5, 1);

        assertFalse(board.fits(shape, 2, 4)); // (3,5)와 겹침
        assertTrue(board.fits(shape, 4, 5)); // 겹치지 않음
    }

    @Test
    @DisplayName("fits는 범위를 벗어나면 false를 반환해야 함")
    void testFitsOutOfBounds() {
        int[][] shape = {
            {1, 1},
            {1, 1}
        };

        assertFalse(board.fits(shape, -1, 0)); // 왼쪽 벗어남
        assertFalse(board.fits(shape, 9, 0)); // 오른쪽 벗어남
        assertFalse(board.fits(shape, 0, -1)); // 위쪽 벗어남
        assertFalse(board.fits(shape, 0, 19)); // 아래쪽 벗어남
    }

    @Test
    @DisplayName("fits는 0인 셀은 무시해야 함")
    void testFitsIgnoresZeros() {
        int[][] shape = {
            {0, 1, 0},
            {1, 1, 1}
        };

        board.setCell(3, 5, 1); // shape의 (0,0) 위치에 블록이 있지만 shape[0][0]은 0

        assertTrue(board.fits(shape, 3, 5)); // 0은 무시되므로 fit
    }

    @Test
    @DisplayName("getFullLineIndices는 가득 찬 행의 인덱스를 반환해야 함")
    void testGetFullLineIndices() {
        // 행 19를 가득 채움
        for (int x = 0; x < board.getWidth(); x++) board.setCell(x, 19, 1);

        List<Integer> fullLines = board.getFullLineIndices();
        assertEquals(1, fullLines.size());
        assertTrue(fullLines.contains(19));
    }

    @Test
    @DisplayName("getFullLineIndices는 여러 가득 찬 행을 모두 반환해야 함")
    void testGetFullLineIndicesMultiple() {
        // 행 15, 17, 19를 가득 채움
        for (int x = 0; x < board.getWidth(); x++) {
            board.setCell(x, 15, 1);
            board.setCell(x, 17, 2);
            board.setCell(x, 19, 3);
        }

        List<Integer> fullLines = board.getFullLineIndices();
        assertEquals(3, fullLines.size());
        assertTrue(fullLines.contains(15));
        assertTrue(fullLines.contains(17));
        assertTrue(fullLines.contains(19));
    }

    @Test
    @DisplayName("getFullLineIndices는 빈 보드에서 빈 리스트를 반환해야 함")
    void testGetFullLineIndicesEmpty() {
        List<Integer> fullLines = board.getFullLineIndices();
        assertTrue(fullLines.isEmpty());
    }

    @Test
    @DisplayName("fillLineWith는 지정된 행을 값으로 채워야 함")
    void testFillLineWith() {
        board.fillLineWith(10, 5);

        for (int x = 0; x < board.getWidth(); x++) assertEquals(5, board.getCell(x, 10));
    }

    @Test
    @DisplayName("fillLineWith는 범위를 벗어난 행을 무시해야 함")
    void testFillLineWithOutOfBounds() {
        board.fillLineWith(-1, 5);
        board.fillLineWith(20, 5);

        // 보드가 여전히 비어있는지 확인
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
            }
        }
    }

    @Test
    @DisplayName("clearFullLines는 가득 찬 행을 제거하고 위 블록을 내려야 함")
    void testClearFullLines() {
        // 맨 아래 행을 가득 채움
        for (int x = 0; x < board.getWidth(); x++) board.setCell(x, 19, 1);
        // 그 위에 일부 블록 배치
        board.setCell(3, 18, 2);
        board.setCell(4, 18, 2);

        int cleared = board.clearFullLines();

        assertEquals(1, cleared);
        // 행 19는 이제 행 18의 내용을 가져야 함
        assertEquals(0, board.getCell(0, 19));
        assertEquals(2, board.getCell(3, 19));
        assertEquals(2, board.getCell(4, 19));
        // 맨 위 행은 0이어야 함
        assertEquals(0, board.getCell(0, 0));
    }

    @Test
    @DisplayName("clearFullLines는 여러 행을 제거할 수 있어야 함")
    void testClearFullLinesMultiple() {
        // 행 18과 19를 가득 채움
        for (int x = 0; x < board.getWidth(); x++) {
            board.setCell(x, 18, 1);
            board.setCell(x, 19, 2);
        }

        int cleared = board.clearFullLines();

        assertEquals(2, cleared);
        // 맨 아래 두 행은 비어야 함
        for (int x = 0; x < board.getWidth(); x++) {
            assertEquals(0, board.getCell(x, 18));
            assertEquals(0, board.getCell(x, 19));
        }
    }

    @Test
    @DisplayName("clearFullLines는 아이템 블록이 있을 때 콜백을 호출해야 함")
    void testClearFullLinesWithItemCallback() {
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        // 행 19를 아이템 블록(100번대)으로 가득 채움
        for (int x = 0; x < board.getWidth(); x++) board.setCell(x, 19, 105);

        board.clearFullLines(() -> callbackCalled.set(true));

        assertTrue(callbackCalled.get());
    }

    @Test
    @DisplayName("clearFullLines는 일반 블록만 있으면 콜백을 호출하지 않아야 함")
    void testClearFullLinesWithoutItemCallback() {
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        // 행 19를 일반 블록으로 가득 채움
        for (int x = 0; x < board.getWidth(); x++) board.setCell(x, 19, 5);

        board.clearFullLines(() -> callbackCalled.set(true));

        assertFalse(callbackCalled.get());
    }

    @Test
    @DisplayName("clearLinesAndReturnCount는 clearFullLines와 동일하게 동작해야 함")
    void testClearLinesAndReturnCount() {
        for (int x = 0; x < board.getWidth(); x++) board.setCell(x, 19, 1);

        int count = board.clearLinesAndReturnCount();

        assertEquals(1, count);
        // 행이 제거되었는지 확인
        for (int x = 0; x < board.getWidth(); x++) assertEquals(0, board.getCell(x, 19));
    }

    @Test
    @DisplayName("snapshot은 현재 보드 상태의 복사본을 반환해야 함")
    void testSnapshot() {
        board.setCell(3, 5, 7);
        board.setCell(6, 10, 3);

        int[][] snap = board.snapshot();

        assertEquals(board.getHeight(), snap.length);
        assertEquals(board.getWidth(), snap[0].length);
        assertEquals(7, snap[5][3]);
        assertEquals(3, snap[10][6]);
    }

    @Test
    @DisplayName("snapshot 반환값 수정은 원본 보드에 영향을 주지 않아야 함")
    void testSnapshotDefensiveCopy() {
        int[][] snap = board.snapshot();
        snap[5][3] = 999;

        assertEquals(0, board.getCell(3, 5)); // 원본은 변경되지 않음
    }

    @Test
    @DisplayName("applyGravity는 떠있는 블록을 아래로 떨어뜨려야 함")
    void testApplyGravity() {
        // 맨 아래에 블록 배치
        board.setCell(3, 19, 1);
        // 중간에 떠있는 블록 배치
        board.setCell(3, 15, 2);
        board.setCell(3, 10, 3);

        board.applyGravity();

        // 블록들이 아래로 쌓여야 함
        assertEquals(1, board.getCell(3, 19));
        assertEquals(2, board.getCell(3, 18));
        assertEquals(3, board.getCell(3, 17));
        // 위쪽은 비어있어야 함
        assertEquals(0, board.getCell(3, 10));
        assertEquals(0, board.getCell(3, 15));
    }

    @Test
    @DisplayName("applyGravity는 각 열을 독립적으로 처리해야 함")
    void testApplyGravityMultipleColumns() {
        // 열 2
        board.setCell(2, 19, 1);
        board.setCell(2, 15, 2);

        // 열 5
        board.setCell(5, 18, 3);
        board.setCell(5, 10, 4);

        board.applyGravity();

        // 열 2
        assertEquals(1, board.getCell(2, 19));
        assertEquals(2, board.getCell(2, 18));
        assertEquals(0, board.getCell(2, 15));

        // 열 5
        assertEquals(3, board.getCell(5, 19));
        assertEquals(4, board.getCell(5, 18));
        assertEquals(0, board.getCell(5, 10));
    }

    @Test
    @DisplayName("동시 접근 시 데이터 무결성이 보장되어야 함")
    void testThreadSafety() throws InterruptedException {
        final int THREADS = 10;
        final CountDownLatch latch = new CountDownLatch(THREADS);
        final AtomicBoolean failed = new AtomicBoolean(false);

        for (int i = 0; i < THREADS; i++) {
            final int threadId = i;
            new Thread(
                            () -> {
                                try {
                                    for (int j = 0; j < 100; j++) {
                                        int x = (threadId + j) % board.getWidth();
                                        int y = (threadId * 2 + j) % board.getHeight();
                                        board.setCell(x, y, threadId);
                                        int value = board.getCell(x, y);
                                        if (value != 0 && value != threadId) failed.set(true);
                                    }
                                } finally {
                                    latch.countDown();
                                }
                            })
                    .start();
        }

        latch.await();
        assertFalse(failed.get(), "동시 접근 시 데이터 불일치가 발생하지 않아야 함");
    }

    @Test
    @DisplayName("극단적인 보드 크기에서도 정상 동작해야 함")
    void testExtremeBoardSizes() {
        // 최소 크기
        Board minBoard = new Board(1, 1);
        assertEquals(1, minBoard.getWidth());
        assertEquals(1, minBoard.getHeight());
        minBoard.setCell(0, 0, 5);
        assertEquals(5, minBoard.getCell(0, 0));

        // 큰 크기
        Board largeBoard = new Board(50, 100);
        assertEquals(50, largeBoard.getWidth());
        assertEquals(100, largeBoard.getHeight());
        largeBoard.setCell(49, 99, 7);
        assertEquals(7, largeBoard.getCell(49, 99));
    }

    @Test
    @DisplayName("보드 상태가 올바르게 변경되어야 함")
    void testSequentialModification() {
        board.setCell(0, 0, 1);
        assertEquals(1, board.getCell(0, 0));
        assertEquals(0, board.getCell(1, 1));

        board.setCell(1, 1, 2);
        assertEquals(1, board.getCell(0, 0)); // 이전 값 유지
        assertEquals(2, board.getCell(1, 1)); // 새 값 설정
    }

    @Test
    @DisplayName("applyGravity 후 블록이 올바른 순서로 쌓여야 함")
    void testApplyGravityOrder() {
        // 위에서부터 1, 2, 3으로 배치 (중간에 빈 공간 있음)
        board.setCell(0, 5, 1);
        board.setCell(0, 10, 2);
        board.setCell(0, 15, 3);

        board.applyGravity();

        // 아래부터 위에서 아래로 내려온 순서대로 쌓여야 함: 3(가장 아래), 2, 1 순서
        assertEquals(3, board.getCell(0, 19)); // 15에서 내려온 블록 (가장 아래)
        assertEquals(2, board.getCell(0, 18)); // 10에서 내려온 블록
        assertEquals(1, board.getCell(0, 17)); // 5에서 내려온 블록

        // 위쪽은 모두 비어있어야 함
        for (int y = 0; y < 17; y++) {
            assertEquals(0, board.getCell(0, y));
        }
    }

    @Test
    @DisplayName("여러 열에서 서로 다른 높이의 블록 중력 적용")
    void testComplexGravityScenario() {
        // 패턴 생성: 각 열마다 다른 개수의 블록
        for (int col = 0; col < board.getWidth(); col++) {
            for (int i = 0; i < col + 1; i++) {
                board.setCell(col, i * 2, col + 1); // 간격을 두고 배치
            }
        }

        board.applyGravity();

        // 각 열의 바닥부터 블록이 쌓여있어야 함
        for (int col = 0; col < board.getWidth(); col++) {
            int expectedBlocks = col + 1;

            // 바닥부터 예상 개수만큼 블록이 있어야 함
            for (int i = 0; i < expectedBlocks; i++) {
                int y = board.getHeight() - 1 - i;
                assertEquals(col + 1, board.getCell(col, y), "열 " + col + ", 행 " + y + "에 블록이 없음");
            }

            // 나머지는 비어있어야 함
            for (int y = 0; y < board.getHeight() - expectedBlocks; y++) {
                assertEquals(0, board.getCell(col, y), "열 " + col + ", 행 " + y + "가 비어있지 않음");
            }
        }
    }

    @Test
    @DisplayName("중력 적용 시 블록 값이 보존되어야 함")
    void testGravityPreservesBlockValues() {
        int[] testValues = {1, 2, 3, 4, 5, 6, 7};
        int col = 3;

        // 다양한 값의 블록을 여러 높이에 배치
        for (int i = 0; i < testValues.length; i++) {
            board.setCell(col, i * 3, testValues[i]);
        }

        board.applyGravity();

        // 바닥부터 역순으로 값이 보존되어야 함 (위에서 아래로 내려오는 순서)
        for (int i = 0; i < testValues.length; i++) {
            int y = board.getHeight() - 1 - i;
            int expectedValue = testValues[testValues.length - 1 - i]; // 역순
            assertEquals(
                    expectedValue,
                    board.getCell(col, y),
                    "블록 값이 보존되지 않음: 예상 " + expectedValue + ", 실제 " + board.getCell(col, y));
        }
    }

    @Test
    @DisplayName("빈 보드에서 중력 적용해도 변화가 없어야 함")
    void testGravityOnEmptyBoard() {
        // 빈 보드 확인
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
            }
        }

        board.applyGravity();

        // 여전히 빈 보드여야 함
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
            }
        }
    }

    @Test
    @DisplayName("이미 바닥에 있는 블록은 중력 적용 후에도 그대로 있어야 함")
    void testGravityOnBottomBlocks() {
        // 바닥 줄에 블록 배치
        for (int x = 0; x < board.getWidth(); x++) {
            board.setCell(x, board.getHeight() - 1, x + 1);
        }

        board.applyGravity();

        // 바닥 줄 블록은 그대로 있어야 함
        for (int x = 0; x < board.getWidth(); x++) {
            assertEquals(x + 1, board.getCell(x, board.getHeight() - 1));
        }

        // 다른 줄은 모두 비어있어야 함
        for (int y = 0; y < board.getHeight() - 1; y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                assertEquals(0, board.getCell(x, y));
            }
        }
    }

    @Test
    @DisplayName("보드 크기 속성이 올바르게 설정되어야 함")
    void testBoardDimensions() {
        assertEquals(10, board.getWidth(), "보드 너비가 올바르지 않음");
        assertEquals(20, board.getHeight(), "보드 높이가 올바르지 않음");

        // 다양한 크기의 보드 테스트
        Board smallBoard = new Board(5, 8);
        assertEquals(5, smallBoard.getWidth());
        assertEquals(8, smallBoard.getHeight());

        Board largeBoard = new Board(15, 25);
        assertEquals(15, largeBoard.getWidth());
        assertEquals(25, largeBoard.getHeight());
    }

    @Test
    @DisplayName("연속적인 중력 적용이 idempotent해야 함")
    void testMultipleGravityApplications() {
        // 블록 배치
        board.setCell(2, 5, 1);
        board.setCell(2, 10, 2);
        board.setCell(7, 8, 3);

        // 첫 번째 중력 적용
        board.applyGravity();

        // 첫 번째 적용 후 상태 저장
        int col2_19_first = board.getCell(2, 19);
        int col2_18_first = board.getCell(2, 18);
        int col7_19_first = board.getCell(7, 19);

        // 두 번째 중력 적용
        board.applyGravity();

        // 결과가 같아야 함
        assertEquals(col2_19_first, board.getCell(2, 19), "연속 중력 적용 후 결과가 다름");
        assertEquals(col2_18_first, board.getCell(2, 18), "연속 중력 적용 후 결과가 다름");
        assertEquals(col7_19_first, board.getCell(7, 19), "연속 중력 적용 후 결과가 다름");
    }
}

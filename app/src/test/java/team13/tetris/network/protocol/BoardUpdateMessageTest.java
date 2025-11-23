package team13.tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.Queue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardUpdateMessageTest {

    @Test
    @DisplayName("BoardUpdateMessage 생성 테스트")
    void testCreateBoardUpdateMessage() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.BOARD_UPDATE, message.getType(), "메시지 타입이 BOARD_UPDATE여야 함");
        assertEquals("Player1", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Player1", message.getPlayerId(), "플레이어 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("보드 상태 가져오기 테스트")
    void testGetBoardState() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        int[][] retrievedBoard = message.getBoardState();

        assertNotNull(retrievedBoard, "보드 상태가 null이 아니어야 함");
        assertEquals(board.length, retrievedBoard.length, "보드 행 개수가 일치해야 함");
        assertEquals(board[0].length, retrievedBoard[0].length, "보드 열 개수가 일치해야 함");
    }

    @Test
    @DisplayName("보드 깊은 복사 테스트 - 외부 수정 방지")
    void testBoardDeepCopy() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        int[][] retrievedBoard = message.getBoardState();
        retrievedBoard[0][0] = 999; // 외부에서 수정

        int[][] secondRetrieved = message.getBoardState();
        assertNotEquals(999, secondRetrieved[0][0], "보드는 깊은 복사되어야 하므로 외부 수정이 반영되지 않아야 함");
    }

    @Test
    @DisplayName("현재 블록 정보 가져오기 테스트")
    void testGetCurrentPieceInfo() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        2,
                        false,
                        null,
                        -1,
                        3,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        assertEquals(5, message.getCurrentPieceX(), "현재 블록 X 좌표가 올바르게 설정되어야 함");
        assertEquals(10, message.getCurrentPieceY(), "현재 블록 Y 좌표가 올바르게 설정되어야 함");
        assertEquals(1, message.getCurrentPieceType(), "현재 블록 타입이 올바르게 설정되어야 함");
        assertEquals(2, message.getCurrentPieceRotation(), "현재 블록 회전이 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("다음 블록 타입 가져오기 테스트")
    void testGetNextPieceType() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        7,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        assertEquals(7, message.getNextPieceType(), "다음 블록 타입이 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("공격받을 블록 가져오기 테스트")
    void testGetIncomingBlocks() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();
        incomingBlocks.add(new int[][] {{1, 2}, {3, 4}});
        incomingBlocks.add(new int[][] {{5, 6}, {7, 8}});

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        Queue<int[][]> retrieved = message.getIncomingBlocks();

        assertNotNull(retrieved, "공격받을 블록이 null이 아니어야 함");
        assertEquals(2, retrieved.size(), "공격받을 블록 개수가 일치해야 함");
    }

    @Test
    @DisplayName("공격받을 블록 null 처리 테스트")
    void testGetIncomingBlocksNull() {
        int[][] board = createTestBoard();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1", board, 5, 10, 1, 0, false, null, -1, 2, false, null, -1, null,
                        1000, 10, 2);

        Queue<int[][]> retrieved = message.getIncomingBlocks();

        assertNotNull(retrieved, "null로 전달해도 빈 큐가 반환되어야 함");
        assertTrue(retrieved.isEmpty(), "빈 큐여야 함");
    }

    @Test
    @DisplayName("점수 및 게임 정보 가져오기 테스트")
    void testGetScoreAndGameInfo() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        5000,
                        25,
                        5);

        assertEquals(5000, message.getScore(), "점수가 올바르게 설정되어야 함");
        assertEquals(25, message.getLinesCleared(), "삭제한 줄 수가 올바르게 설정되어야 함");
        assertEquals(5, message.getLevel(), "레벨이 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("null 보드 처리 테스트")
    void testNullBoard() {
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        null,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        assertNull(message.getBoardState(), "null 보드는 null로 반환되어야 함");
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();
        incomingBlocks.add(new int[][] {{1, 2}, {3, 4}});

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        String toString = message.toString();

        assertNotNull(toString, "toString 결과가 null이 아니어야 함");
        assertTrue(toString.contains("Player1"), "toString에 플레이어 ID가 포함되어야 함");
        assertTrue(toString.contains("score=1000"), "toString에 점수가 포함되어야 함");
        assertTrue(toString.contains("linesCleared=10"), "toString에 삭제한 줄 수가 포함되어야 함");
        assertTrue(toString.contains("level=2"), "toString에 레벨이 포함되어야 함");
        assertTrue(toString.contains("incomingBlocksCount=1"), "toString에 공격받을 블록 개수가 포함되어야 함");
    }

    @Test
    @DisplayName("타임스탬프 존재 테스트")
    void testTimestampExists() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        assertTrue(message.getTimestamp() > 0, "타임스탬프가 설정되어야 함");
    }

    @Test
    @DisplayName("보드 행이 null인 경우 처리 테스트")
    void testBoardWithNullRow() {
        int[][] board = new int[20][];
        board[0] = new int[] {1, 2, 3};
        board[1] = null; // null 행
        board[2] = new int[] {4, 5, 6};

        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        int[][] retrieved = message.getBoardState();

        assertNotNull(retrieved, "보드가 null이 아니어야 함");
        assertNotNull(retrieved[0], "첫 번째 행은 null이 아니어야 함");
        assertNull(retrieved[1], "두 번째 행은 null이어야 함");
        assertNotNull(retrieved[2], "세 번째 행은 null이 아니어야 함");
    }

    @Test
    @DisplayName("공격받을 블록 복사 테스트 - 외부 수정 방지")
    void testIncomingBlocksDeepCopy() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();
        incomingBlocks.add(new int[][] {{1, 2}, {3, 4}});

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1000,
                        10,
                        2);

        Queue<int[][]> retrieved = message.getIncomingBlocks();
        retrieved.add(new int[][] {{9, 9}, {9, 9}}); // 외부에서 수정

        Queue<int[][]> secondRetrieved = message.getIncomingBlocks();
        assertEquals(1, secondRetrieved.size(), "공격받을 블록은 복사되어야 하므로 외부 수정이 반영되지 않아야 함");
    }

    @Test
    @DisplayName("모든 게임 정보가 0인 경우 테스트")
    void testZeroGameInfo() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        0,
                        0,
                        0,
                        0,
                        false,
                        null,
                        -1,
                        0,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        0,
                        0,
                        0);

        assertEquals(0, message.getCurrentPieceX(), "X 좌표가 0이어야 함");
        assertEquals(0, message.getCurrentPieceY(), "Y 좌표가 0이어야 함");
        assertEquals(0, message.getCurrentPieceType(), "블록 타입이 0이어야 함");
        assertEquals(0, message.getCurrentPieceRotation(), "회전이 0이어야 함");
        assertEquals(0, message.getNextPieceType(), "다음 블록 타입이 0이어야 함");
        assertEquals(0, message.getScore(), "점수가 0이어야 함");
        assertEquals(0, message.getLinesCleared(), "삭제한 줄 수가 0이어야 함");
        assertEquals(0, message.getLevel(), "레벨이 0이어야 함");
    }

    @Test
    @DisplayName("음수 값 처리 테스트")
    void testNegativeValues() {
        int[][] board = createTestBoard();
        Queue<int[][]> incomingBlocks = new LinkedList<>();

        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "Player1",
                        board,
                        -5,
                        -10,
                        -1,
                        -2,
                        false,
                        null,
                        -1,
                        -3,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        -1000,
                        -10,
                        -2);

        assertEquals(-5, message.getCurrentPieceX(), "음수 X 좌표가 저장되어야 함");
        assertEquals(-10, message.getCurrentPieceY(), "음수 Y 좌표가 저장되어야 함");
        assertEquals(-1, message.getCurrentPieceType(), "음수 블록 타입이 저장되어야 함");
        assertEquals(-1000, message.getScore(), "음수 점수가 저장되어야 함");
    }

    private int[][] createTestBoard() {
        int[][] board = new int[20][10];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = (i + j) % 2;
            }
        }
        return board;
    }
}

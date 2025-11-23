package team13.tetris.game.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// AsciiBoardRenderer 테스트: Tests ASCII rendering of board and tetrominoes
@DisplayName("AsciiBoardRenderer 테스트")
public class AsciiBoardRendererTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
    }

    @Test
    @DisplayName("빈 보드를 렌더링할 수 있는지 확인")
    void testRenderEmptyBoard() {
        // given
        String result = AsciiBoardRenderer.render(board, null, 0, 0);

        // then
        assertNotNull(result, "렌더링 결과가 null이 아니어야 함");
        assertTrue(result.contains("X"), "테두리 문자 X를 포함해야 함");

        // 첫 줄과 마지막 줄은 테두리 (XXXXXXXXXXXX - 10칸 + 좌우 테두리 2칸 = 12칸)
        String[] lines = result.split("\n");
        assertEquals(22, lines.length, "20행 + 상하 테두리 2줄 = 22줄");
        assertEquals("XXXXXXXXXXXX", lines[0], "상단 테두리");
        assertEquals("XXXXXXXXXXXX", lines[21], "하단 테두리");

        // 중간 줄들은 'X X' 형태 (좌우 테두리 + 10칸 공백)
        for (int i = 1; i <= 20; i++) {
            assertTrue(lines[i].startsWith("X"), "각 행은 X로 시작해야 함");
            assertTrue(lines[i].endsWith("X"), "각 행은 X로 끝나야 함");
            assertEquals(12, lines[i].length(), "각 행은 12문자여야 함");
        }
    }

    @Test
    @DisplayName("보드에 블록이 있을 때 O로 렌더링되는지 확인")
    void testRenderBoardWithBlocks() {
        // given - 보드 하단에 블록 배치
        board.setCell(0, 19, 1); // 좌측 하단
        board.setCell(5, 19, 2); // 중앙 하단
        board.setCell(9, 19, 3); // 우측 하단

        // when
        String result = AsciiBoardRenderer.render(board, null, 0, 0);

        // then
        String[] lines = result.split("\n");
        String bottomLine = lines[20]; // 하단에서 두 번째 줄 (인덱스 20)

        assertEquals('X', bottomLine.charAt(0), "좌측 테두리");
        assertEquals('■', bottomLine.charAt(1), "블록은 ■로 표시");
        assertEquals('■', bottomLine.charAt(6), "블록은 ■로 표시");
        assertEquals('■', bottomLine.charAt(10), "블록은 ■로 표시");
        assertEquals('X', bottomLine.charAt(11), "우측 테두리");
    }

    @Test
    @DisplayName("현재 테트로미노를 포함하여 렌더링하는지 확인")
    void testRenderWithCurrentTetromino() {
        // given - I 블록을 상단 중앙에 배치
        Tetromino iPiece = new Tetromino(Tetromino.Kind.I, 0);
        int px = 3; // x 위치
        int py = 0; // y 위치

        // when
        String result = AsciiBoardRenderer.render(board, iPiece, px, py);

        // then
        String[] lines = result.split("\n");

        // I 블록의 첫 번째 회전 상태: 가로로 긴 형태 (두 번째 줄에 4개)
        // {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}}
        String secondRow = lines[2]; // py=0이면 shape[1]이 lines[2]에 해당

        assertTrue(secondRow.contains("■"), "현재 테트로미노가 ■로 렌더링되어야 함");
    }

    @Test
    @DisplayName("보드 크기가 올바르게 반영되는지 확인")
    void testRenderDifferentBoardSizes() {
        // given - 작은 보드
        Board smallBoard = new Board(5, 5);

        // when
        String result = AsciiBoardRenderer.render(smallBoard, null, 0, 0);

        // then
        String[] lines = result.split("\n");
        assertEquals(7, lines.length, "5행 + 상하 테두리 2줄 = 7줄");
        assertEquals("XXXXXXX", lines[0], "상단 테두리 (5 + 2)");
        assertEquals("XXXXXXX", lines[6], "하단 테두리");
    }

    @Test
    @DisplayName("null 테트로미노로 렌더링해도 오류가 없는지 확인")
    void testRenderWithNullTetromino() {
        // when
        assertDoesNotThrow(
                () -> {
                    String result = AsciiBoardRenderer.render(board, null, 0, 0);
                    assertNotNull(result);
                },
                "null 테트로미노로 렌더링해도 예외가 발생하지 않아야 함");
    }

    @Test
    @DisplayName("테트로미노 위치가 보드 밖일 때 처리되는지 확인")
    void testRenderTetrominoOutOfBounds() {
        // given
        Tetromino oPiece = new Tetromino(Tetromino.Kind.O, 0);

        // when - 보드 밖의 위치
        assertDoesNotThrow(
                () -> {
                    String result1 = AsciiBoardRenderer.render(board, oPiece, -5, -5);
                    assertNotNull(result1);

                    String result2 = AsciiBoardRenderer.render(board, oPiece, 100, 100);
                    assertNotNull(result2);
                },
                "보드 밖의 위치에서도 렌더링이 가능해야 함");
    }

    // renderPreview 메서드 테스트
    @Test
    @DisplayName("테트로미노 미리보기가 4x4 그리드로 렌더링되는지 확인")
    void testRenderPreview() {
        // given
        Tetromino oPiece = new Tetromino(Tetromino.Kind.O, 0);

        // when
        String preview = AsciiBoardRenderer.renderPreview(oPiece);

        // then
        assertNotNull(preview, "미리보기 결과가 null이 아니어야 함");
        String[] lines = preview.split("\n");
        assertEquals(4, lines.length, "미리보기는 4줄이어야 함");

        for (String line : lines) assertEquals(4, line.length(), "각 줄은 4문자여야 함");

        assertTrue(preview.contains("■"), "테트로미노 블록이 ■로 표시되어야 함");
    }

    @Test
    @DisplayName("null 테트로미노 미리보기가 빈 4x4 그리드로 렌더링되는지 확인")
    void testRenderPreviewNull() {
        // when
        String preview = AsciiBoardRenderer.renderPreview(null);

        // then
        assertNotNull(preview, "null 입력에도 결과가 있어야 함");
        String[] lines = preview.split("\n");
        assertEquals(4, lines.length, "4줄이어야 함");

        for (String line : lines) assertEquals("    ", line, "빈 공간 4칸이어야 함");
    }

    @Test
    @DisplayName("I 블록 미리보기가 중앙에 배치되는지 확인")
    void testRenderPreviewIPiece() {
        // given
        Tetromino iPiece = new Tetromino(Tetromino.Kind.I, 0);

        // when
        String preview = AsciiBoardRenderer.renderPreview(iPiece);

        // then
        String[] lines = preview.split("\n");
        assertEquals(4, lines.length, "4줄이어야 함");

        // I 블록의 기본 형태: 두 번째 줄에 가로로 4개
        // {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}}
        assertEquals("    ", lines[0], "첫 줄은 비어있어야 함");
        assertEquals("■■■■", lines[1], "두 번째 줄에 4개 블록");
        assertEquals("    ", lines[2], "세 번째 줄은 비어있어야 함");
        assertEquals("    ", lines[3], "네 번째 줄은 비어있어야 함");
    }

    @Test
    @DisplayName("O 블록 미리보기가 중앙에 배치되는지 확인")
    void testRenderPreviewOPiece() {
        // given
        Tetromino oPiece = new Tetromino(Tetromino.Kind.O, 0);

        // when
        String preview = AsciiBoardRenderer.renderPreview(oPiece);

        // then
        String[] lines = preview.split("\n");

        // O 블록: 2x2 크기, 중앙에 배치
        // {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}
        assertEquals(" ■■ ", lines[0], "첫 줄에 중앙 배치된 2개 블록");
        assertEquals(" ■■ ", lines[1], "두 번째 줄에 중앙 배치된 2개 블록");
        assertEquals("    ", lines[2], "세 번째 줄은 비어있어야 함");
        assertEquals("    ", lines[3], "네 번째 줄은 비어있어야 함");
    }

    @Test
    @DisplayName("T 블록 미리보기가 올바르게 렌더링되는지 확인")
    void testRenderPreviewTPiece() {
        // given
        Tetromino tPiece = new Tetromino(Tetromino.Kind.T, 0);

        // when
        String preview = AsciiBoardRenderer.renderPreview(tPiece);

        // then
        String[] lines = preview.split("\n");

        // T 블록: {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}
        assertEquals(" ■  ", lines[0], "첫 줄에 T자 상단");
        assertEquals("■■■ ", lines[1], "두 번째 줄에 T자 하단");
        assertEquals("    ", lines[2], "세 번째 줄은 비어있어야 함");
        assertEquals("    ", lines[3], "네 번째 줄은 비어있어야 함");
    }

    @Test
    @DisplayName("회전된 테트로미노 미리보기가 올바르게 렌더링되는지 확인")
    void testRenderPreviewRotated() {
        // given - I 블록 90도 회전
        Tetromino iPieceRotated = new Tetromino(Tetromino.Kind.I, 1);

        // when
        String preview = AsciiBoardRenderer.renderPreview(iPieceRotated);

        // then
        String[] lines = preview.split("\n");

        // I 블록 회전 1: 세로로 긴 형태
        // {{0,0,1,0},{0,0,1,0},{0,0,1,0},{0,0,1,0}}
        for (int i = 0; i < 4; i++) {
            assertTrue(lines[i].contains("■"), "각 줄에 블록이 있어야 함");
            assertEquals(1, countOccurrences(lines[i], '■'), "각 줄에 정확히 1개의 블록");
        }
    }

    @Test
    @DisplayName("여러 종류의 테트로미노 미리보기가 모두 4x4 형식인지 확인")
    void testRenderPreviewAllPieces() {
        // given - 모든 기본 테트로미노
        Tetromino[] pieces = {
            new Tetromino(Tetromino.Kind.I, 0),
            new Tetromino(Tetromino.Kind.O, 0),
            new Tetromino(Tetromino.Kind.T, 0),
            new Tetromino(Tetromino.Kind.S, 0),
            new Tetromino(Tetromino.Kind.Z, 0),
            new Tetromino(Tetromino.Kind.J, 0),
            new Tetromino(Tetromino.Kind.L, 0)
        };

        // when & then
        for (Tetromino piece : pieces) {
            String preview = AsciiBoardRenderer.renderPreview(piece);
            String[] lines = preview.split("\n");

            assertEquals(4, lines.length, piece.getKind() + " 미리보기는 4줄이어야 함");
            for (String line : lines)
                assertEquals(4, line.length(), piece.getKind() + " 각 줄은 4문자여야 함");
            assertTrue(preview.contains("■"), piece.getKind() + " 블록이 포함되어야 함");
        }
    }

    @Test
    @DisplayName("render 메서드가 테두리 없이 테트로미노만 렌더링하지 않는지 확인")
    void testRenderAlwaysIncludesBorders() {
        // given
        Board smallBoard = new Board(3, 3);

        // when
        String result = AsciiBoardRenderer.render(smallBoard, null, 0, 0);

        // then
        String[] lines = result.split("\n");
        assertTrue(lines[0].startsWith("X") && lines[0].endsWith("X"), "첫 줄은 테두리여야 함");
        assertTrue(
                lines[lines.length - 1].startsWith("X") && lines[lines.length - 1].endsWith("X"),
                "마지막 줄은 테두리여야 함");
    }

    @Test
    @DisplayName("복잡한 보드 상태를 올바르게 렌더링하는지 확인")
    void testRenderComplexBoardState() {
        // given - 보드 하단에 복잡한 패턴 생성
        for (int x = 0; x < 10; x++) {
            if (x % 2 == 0) board.setCell(x, 19, 1);
        }
        for (int x = 0; x < 10; x++) {
            if (x % 3 == 0) board.setCell(x, 18, 2);
        }

        // when
        String result = AsciiBoardRenderer.render(board, null, 0, 0);

        // then
        String[] lines = result.split("\n");
        String row19 = lines[20]; // 19번째 행
        String row18 = lines[19]; // 18번째 행

        // 패턴 확인
        assertEquals('■', row19.charAt(1), "짝수 인덱스에 블록");
        assertEquals(' ', row19.charAt(2), "홀수 인덱스는 빈 공간");
        assertEquals('■', row18.charAt(1), "3의 배수 인덱스에 블록");
    }

    // Helper method
    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
}

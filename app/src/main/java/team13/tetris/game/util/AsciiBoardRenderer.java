package team13.tetris.game.util;

import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// 보드를 ASCII 형식으로 렌더링하는 유틸리티입니다. 테두리는 'X', 블록은 '■'로 표현합니다.
public final class AsciiBoardRenderer {
    private AsciiBoardRenderer() {}

    public static String render(Board board, Tetromino current, int px, int py) {
        int rows = board.getHeight();
        int cols = board.getWidth();
        StringBuilder sb = new StringBuilder();

        // top border
        for (int c = 0; c < cols + 2; c++) sb.append('X');

        sb.append('\n');

        for (int r = 0; r < rows; r++) {
            sb.append('X');

            for (int c = 0; c < cols; c++) {
                boolean occupied = board.getCell(c, r) != 0;
                // check current piece
                if (!occupied && current != null) {
                    int[][] shape = current.getShape();
                    int sr = r - py;
                    int sc = c - px;

                    if (sr >= 0
                            && sr < shape.length
                            && sc >= 0
                            && sc < shape[0].length
                            && shape[sr][sc] != 0) occupied = true;
                }

                sb.append(occupied ? '■' : ' ');
            }

            sb.append('X');
            sb.append('\n');
        }

        // bottom border
        for (int c = 0; c < cols + 2; c++) sb.append('X');

        sb.append('\n');

        return sb.toString();
    }

    // 단일 테트로미노의 4x4 미리보기를 렌더링합니다 (테두리 없음).
    // 모양이 4x4보다 작을 경우 중앙에 배치됩니다.
    public static String renderPreview(Tetromino piece) {
        StringBuilder sb = new StringBuilder();
        int size = 4;
        // default empty 4x4
        char[][] grid = new char[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = ' ';
            }
        }

        if (piece != null) {
            int[][] shape = piece.getShape();
            int h = shape.length;
            int w = shape[0].length;
            int offR = Math.max(0, (size - h) / 2);
            int offC = Math.max(0, (size - w) / 2);

            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    if (shape[r][c] != 0) {
                        int rr = offR + r;
                        int cc = offC + c;

                        if (rr >= 0 && rr < size && cc >= 0 && cc < size) grid[rr][cc] = '■';
                    }
                }
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                sb.append(grid[r][c]);
            }

            sb.append('\n');
        }

        return sb.toString();
    }
}

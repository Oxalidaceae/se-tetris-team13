package team13.tetris.game.util;

import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// ASCII 보드 렌더링 (테두리: X, 블록: O)
public final class AsciiBoardRenderer {
    private AsciiBoardRenderer() {
    }

    // 보드와 현재 미노를 ASCII로 렌더링
    public static String render(Board board, Tetromino current, int px, int py) {
        int rows = board.getHeight();
        int cols = board.getWidth();
        StringBuilder sb = new StringBuilder();

        for (int c = 0; c < cols + 2; c++)
            sb.append('X');
        sb.append('\n');

        for (int r = 0; r < rows; r++) {
            sb.append('X');
            for (int c = 0; c < cols; c++) {
                boolean occupied = board.getCell(c, r) != 0;
                if (!occupied && current != null) {
                    int[][] shape = current.getShape();
                    int sr = r - py;
                    int sc = c - px;
                    if (sr >= 0 && sr < shape.length && sc >= 0 && sc < shape[0].length) {
                        if (shape[sr][sc] != 0)
                            occupied = true;
                    }
                }
                sb.append(occupied ? 'O' : ' ');
            }
            sb.append('X');
            sb.append('\n');
        }

        for (int c = 0; c < cols + 2; c++)
            sb.append('X');
        sb.append('\n');

        return sb.toString();
    }

    // 4x4 미노 미리보기 렌더링 (중앙 정렬)
    public static String renderPreview(Tetromino piece) {
        StringBuilder sb = new StringBuilder();
        int size = 4;
        // 4x4 빈 그리드 초기화
        char[][] grid = new char[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = ' ';

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
                        if (rr >= 0 && rr < size && cc >= 0 && cc < size)
                            grid[rr][cc] = 'O';
                    }
                }
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++)
                sb.append(grid[r][c]);
            sb.append('\n');
        }

        return sb.toString();
    }
}

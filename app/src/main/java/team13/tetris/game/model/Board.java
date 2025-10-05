package team13.tetris.game.model;

/**
 * 게임에서 사용되는 보드를 표현하는 클래스
 * 행(row) x 열(col) 크기의 2차원 정수 배열을 사용
 * 배열 값은 0이면 빈 칸, 0이 아닌 양수는 해당 칸에 존재하는 블록의 id(또는 색)를 의미합니다.
 *
 * 외부에서 내부 배열을 직접 변경하면 게임 상태가 손상될 수 있으므로,
 * 접근 시 동기화와 방어적 복사를 통해 안전성을 보장
 */
public class Board {
    private final int width;
    private final int height;
    private final int[][] cells;
    // cells 배열에 대한 동시 접근을 보호하는 락입니다. 모든 읽기/쓰기 연산은 이 락으로 감싸집니다.
    private final Object lock = new Object();

    public Board(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid board size");
        this.width = width;
        this.height = height;
        this.cells = new int[height][width];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return -1;
        synchronized (lock) {
            return cells[y][x];
        }
    }

    public boolean isOccupied(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        synchronized (lock) {
            return cells[y][x] != 0;
        }
    }

    public void setCell(int x, int y, int value) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        synchronized (lock) {
            cells[y][x] = value;
        }
    }

    public void clear() {
        synchronized (lock) {
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) cells[r][c] = 0;
            }
        }
    }

    /**
     * 주어진 모양(shape)을 보드에 놓습니다.
     *
     * @param shape  블록을 1로 표시한 2차원 배열
     * @param px     보드에서 모양의 왼쪽 열(열 인덱스)
     * @param py     보드에서 모양의 위쪽 행(행 인덱스)
     * @param value  채워진 셀에 저장할 값(예: 블록 id)
     *
     * 보드 경계를 넘어가는 칸은 무시합니다. 이 메서드는 내부 락을 사용하여 동기화됩니다.
     */
    public void placePiece(int[][] shape, int px, int py, int value) {
        synchronized (lock) {
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int x = px + c;
                        int y = py + r;
                        if (x >= 0 && x < width && y >= 0 && y < height) {
                            cells[y][x] = value;
                        }
                    }
                }
            }
        }
    }

    /**
     * 특정 위치(px, py)에 shape를 놓을 수 있는지 검사합니다.
     *
     * 검사 조건:
     * - shape의 각 블록 셀(1)이 보드 범위를 벗어나지 않아야 합니다.
     * - 해당 위치에 이미 다른 블록이 있지 않아야 합니다.
     *
     * 위 조건을 모두 만족하면 true, 아니면 false를 반환합니다.
     */
    public boolean fits(int[][] shape, int px, int py) {
        synchronized (lock) {
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int x = px + c;
                        int y = py + r;
                        if (x < 0 || x >= width || y < 0 || y >= height) return false;
                        if (cells[y][x] != 0) return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * 가득 찬(모든 열이 블록으로 채워진) 행을 찾아 제거합니다.
     * 제거된 행 위에 있는 모든 행을 한 칸씩 아래로 내리고,
     * 맨 위 행은 0으로 채웁니다.
     *
     * @return 제거된 행의 개수
     */
    public int clearFullLines() {
        synchronized (lock) {
            int cleared = 0;
            for (int r = height - 1; r >= 0; r--) {
                boolean full = true;
                for (int c = 0; c < width; c++) {
                    if (cells[r][c] == 0) { full = false; break; }
                }
                if (full) {
                    // shift everything above down
                    for (int rr = r; rr > 0; rr--) {
                        System.arraycopy(cells[rr - 1], 0, cells[rr], 0, width);
                    }
                    // clear top row
                    for (int c = 0; c < width; c++) cells[0][c] = 0;
                    cleared++;
                    r++; // recheck same row index as lines moved down
                }
            }
            return cleared;
        }
    }

    /**
     * {@link #clearFullLines()}를 호출하여 가득 찬 행을 제거하고,
     * 제거된 행 수를 그대로 반환하는 편의 메서드입니다.
     */
    public int clearLinesAndReturnCount() {
        return clearFullLines();
    }

    /**
     * 현재 보드 상태를 2차원 정수 배열로 복사하여 반환합니다.
     * 반환되는 배열은 내부 배열(cells)의 방어적 복사본이므로,
     * 호출자가 반환값을 수정하더라도 내부 상태에는 영향이 없습니다.
     *
     * 이 메서드는 내부 락을 사용해 복사 시점의 일관된 스냅샷을 제공합니다.
     *
     * @return 보드 상태의 복사본 (rows x cols)
     */
    public int[][] snapshot() {
        int[][] snap = new int[height][width];
        synchronized (lock) {
            for (int r = 0; r < height; r++) System.arraycopy(cells[r], 0, snap[r], 0, width);
        }
        return snap;
    }
}

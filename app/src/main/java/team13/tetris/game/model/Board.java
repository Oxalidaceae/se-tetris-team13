package team13.tetris.game.model;

public class Board {
    private final int width;
    private final int height;
    private final int[][] cells;
    private final Object lock = new Object();

    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid board size");
        }
        this.width = width;
        this.height = height;
        this.cells = new int[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

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
                for (int c = 0; c < width; c++) {
                    cells[r][c] = 0;
                }
            }
        }
    }

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
    
    public void placeItemPiece(
        int[][] shape,
        int px,
        int py,
        int value,
        int itemBlockIndex,
        String itemType
    ) {
        synchronized (lock) {
            int blockCount = 0;
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int x = px + c;
                        int y = py + r;
                        if (x >= 0 && x < width && y >= 0 && y < height) {
                            int itemValue;
                            // COPY와 LINE_CLEAR는 특정 블록만 아이템 블록으로 설정
                            if ((itemType.equals("COPY") || itemType.equals("LINE_CLEAR")) && blockCount == itemBlockIndex) {
                                switch (itemType) {
                                    case "COPY":
                                        itemValue = 100 + value; // COPY: 100번대
                                        break;
                                    case "LINE_CLEAR":
                                        itemValue = 200 + value; // LINE_CLEAR: 200번대
                                        break;
                                    default:
                                        itemValue = 100 + value; // 기본값
                                        break;
                                }
                            } else if (itemType.equals("WEIGHT") || itemType.equals("GRAVITY") || itemType.equals("SPLIT")) {
                                // WEIGHT, GRAVITY, SPLIT은 모든 블록이 아이템 블록
                                switch (itemType) {
                                    case "WEIGHT":
                                        itemValue = 300 + value; // WEIGHT: 300번대
                                        break;
                                    case "GRAVITY":
                                        itemValue = 400 + value; // GRAVITY: 400번대
                                        break;
                                    case "SPLIT":
                                        itemValue = 500 + value; // SPLIT: 500번대
                                        break;
                                    default:
                                        itemValue = value; // 기본값
                                        break;
                                }
                            } else {
                                // COPY/LINE_CLEAR의 일반 블록 또는 기타
                                itemValue = value;
                            }
                            cells[y][x] = itemValue;
                        }
                        blockCount++;
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
                        if (x < 0 || x >= width || y < 0 || y >= height)
                            return false;
                        if (cells[y][x] != 0)
                            return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * 현재 보드에서 가득 찬 모든 행의 인덱스를 찾아 반환합니다.
     */
    public java.util.List<Integer> getFullLineIndices() {
        synchronized (lock) {
            java.util.List<Integer> fullLines = new java.util.ArrayList<>();
            for (int r = height - 1; r >= 0; r--) {
                boolean full = true;
                for (int c = 0; c < width; c++) {
                    if (cells[r][c] == 0) {
                        full = false;
                        break;
                    }
                }
                if (full) {
                    fullLines.add(r);
                }
            }
            return fullLines;
        }
    }

    /**
     * 지정된 행 전체를 주어진 값으로 채웁니다.
     */
    public void fillLineWith(int row, int value) {
        synchronized (lock) {
            if (row < 0 || row >= height)
                return;
            for (int c = 0; c < width; c++) {
                cells[row][c] = value;
            }
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
        return clearFullLines(null);
    }
    
    /**
     * 가득 찬(모든 열이 블록으로 채워진) 행을 찾아 제거합니다.
     * 아이템 블록(100번대 값)이 있는 행이 제거될 때 콜백을 호출합니다.
     *
     * @param itemCallback 아이템 블록이 제거될 때 호출되는 콜백
     * @return 제거된 행의 개수
     */
    public int clearFullLines(Runnable itemCallback) {
        synchronized (lock) {
            int cleared = 0;
            
            for (int r = height - 1; r >= 0; r--) {
                boolean full = true;
                boolean hasItemBlock = false;
                
                // 범위 체크
                if (r < 0 || r >= height) {
                    continue;
                }
                
                // 행을 먼저 복사해서 안전하게 처리
                int[] currentRow = new int[width];
                try {
                    System.arraycopy(cells[r], 0, currentRow, 0, width);
                } catch (Exception e) {
                    continue;
                }
                
                for (int c = 0; c < width; c++) {
                    if (c < 0 || c >= width) {
                        continue;
                    }
                    
                    if (currentRow[c] == 0) {
                        full = false;
                        break;
                    }
                    // 아이템 블록 감지 (100번대 값)
                    if (currentRow[c] >= 100 && currentRow[c] < 200) {
                        hasItemBlock = true;
                    }
                }
                
                if (full) {
                    // 아이템 블록이 있으면 콜백 호출
                    if (hasItemBlock && itemCallback != null) {
                        itemCallback.run();
                    }
                    
                    // shift everything above down
                    for (int rr = r; rr > 0; rr--) {
                        if (rr - 1 >= 0 && rr < height) {
                            for (int c = 0; c < width; c++) {
                                cells[rr][c] = cells[rr - 1][c];
                            }
                        }
                    }
                    // clear top row
                    for (int c = 0; c < width; c++)
                        cells[0][c] = 0;
                    
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
            for (int r = 0; r < height; r++)
                System.arraycopy(cells[r], 0, snap[r], 0, width);
        }
        return snap;
    }
    
    /**
     * 중력 효과를 적용하여 떠있는 블록들을 아래로 떨어뜨립니다.
     * 무게추로 블록이 파괴된 후 호출됩니다.
     */
    public void applyGravity() {
        synchronized (lock) {
            for (int col = 0; col < width; col++) {
                // 각 열에 대해 아래서부터 빈 공간을 찾아 블록을 떨어뜨림
                int writePos = height - 1; // 쓰기 위치 (아래서부터)
                
                for (int row = height - 1; row >= 0; row--) {
                    if (cells[row][col] != 0) {
                        // 블록이 있으면 writePos로 이동
                        if (row != writePos) {
                            cells[writePos][col] = cells[row][col];
                            cells[row][col] = 0;
                        }
                        writePos--;
                    }
                }
            }
        }
    }
}

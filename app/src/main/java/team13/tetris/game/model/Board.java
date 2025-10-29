package team13.tetris.game.model;

public class Board {
    private final int width;
    private final int height;
    private final int[][] cells;
    private final Object lock = new Object();

    public Board(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Invalid board size");
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
                for (int c = 0; c < width; c++) cells[r][c] = 0;
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
                            if ((itemType.equals("COPY") || itemType.equals("LINE_CLEAR"))
                                && blockCount == itemBlockIndex) {
                                switch (itemType) {
                                    case "COPY":
                                        itemValue = 100 + value;
                                        break;
                                    case "LINE_CLEAR":
                                        itemValue = 200 + value;
                                        break;
                                    default:
                                        itemValue = 100 + value;
                                        break;
                                }
                            } else if (itemType.equals("WEIGHT")
                                || itemType.equals("GRAVITY")
                                || itemType.equals("SPLIT")) {
                                switch (itemType) {
                                    case "WEIGHT":
                                        itemValue = 300 + value;
                                        break;
                                    case "GRAVITY":
                                        itemValue = 400 + value;
                                        break;
                                    case "SPLIT":
                                        itemValue = 500 + value;
                                        break;
                                    default:
                                        itemValue = value;
                                        break;
                                }
                            } else {
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
                if (full) fullLines.add(r);
            }
            return fullLines;
        }
    }

    public void fillLineWith(int row, int value) {
        synchronized (lock) {
            if (row < 0 || row >= height) return;
            for (int c = 0; c < width; c++) cells[row][c] = value;
        }
    }

    public int clearFullLines() {
        return clearFullLines(null);
    }

    public int clearFullLines(Runnable itemCallback) {
        synchronized (lock) {
            int cleared = 0;

            for (int r = height - 1; r >= 0; r--) {
                boolean full = true;
                boolean hasItemBlock = false;

                if (r < 0 || r >= height) continue;

                int[] currentRow = new int[width];
                try {
                    System.arraycopy(cells[r], 0, currentRow, 0, width);
                } catch (Exception e) {
                    continue;
                }

                for (int c = 0; c < width; c++) {
                    if (c < 0 || c >= width) continue;

                    if (currentRow[c] == 0) {
                        full = false;
                        break;
                    }
                    if (currentRow[c] >= 100 && currentRow[c] < 200) hasItemBlock = true;
                }

                if (full) {
                    if (hasItemBlock && itemCallback != null) itemCallback.run();

                    for (int rr = r; rr > 0; rr--) {
                        if (rr - 1 >= 0 && rr < height) {
                            for (int c = 0; c < width; c++) cells[rr][c] = cells[rr - 1][c];
                        }
                    }
                    for (int c = 0; c < width; c++) cells[0][c] = 0;

                    cleared++;
                    r++;
                }
            }
            return cleared;
        }
    }

    public int clearLinesAndReturnCount() {
        return clearFullLines();
    }

    public int[][] snapshot() {
        int[][] snap = new int[height][width];
        synchronized (lock) {
            for (int r = 0; r < height; r++) {
                System.arraycopy(cells[r], 0, snap[r], 0, width);
            }
        }
        return snap;
    }

    public void applyGravity() {
        synchronized (lock) {
            for (int col = 0; col < width; col++) {
                int writePos = height - 1;

                for (int row = height - 1; row >= 0; row--) {
                    if (cells[row][col] != 0) {
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

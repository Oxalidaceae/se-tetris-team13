package team13.tetris.scenes;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import team13.tetris.config.Settings;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// GameScene과 VersusGameScene의 공통 기능을 제공하는 추상 베이스 클래스
public abstract class BaseGameScene {
    protected static final double BOARD_CELL_SIZE = 28.0;
    protected static final double PREVIEW_CELL_SIZE = 22.0;
    protected static final String FILLED_SYMBOL = "";
    
    protected final Settings settings;

    protected BaseGameScene(Settings settings) { this.settings = settings; }

    // 보드 그리드 생성
    protected GridPane createBoardGrid(Board board) {
        int w = board.getWidth();
        int h = board.getHeight();

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("board-grid");

        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                CellView cell = new CellView(BOARD_CELL_SIZE, settings);

                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setBorder();
                } else {
                    cell.setEmpty();
                }

                grid.add(cell, gx, gy);
            }
        }

        return grid;
    }

    // 프리뷰 그리드 생성 (4x4)
    protected GridPane createPreviewGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("preview-grid");

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = new CellView(PREVIEW_CELL_SIZE, settings);
                cell.setEmpty();
                grid.add(cell, c, r);
            }
        }

        return grid;
    }

    // GridPane에서 특정 위치의 노드 가져오기
    protected Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            int r = rowIndex == null ? 0 : rowIndex;
            int c = colIndex == null ? 0 : colIndex;

            if (r == row && c == column) return node;
        }

        return null;
    }

    // 셀을 비움
    protected void applyCellEmpty(CellView cell) { if (cell != null) cell.setEmpty(); }

    // 셀에 블록 채우기
    protected void fillCell(CellView cell, String symbol, String blockClass, String textClass) {
        if (cell != null) cell.setBlock(symbol, blockClass, textClass);
    }

    // Tetromino Kind에 해당하는 블록 스타일 클래스 반환
    protected String blockClassForKind(Tetromino.Kind kind) {
        return (kind != null) ? kind.getBlockStyleClass() : "block";
    }

    // Tetromino Kind에 해당하는 텍스트 스타일 클래스 반환
    protected String textClassForKind(Tetromino.Kind kind) {
        return (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
    }

    // CellView 생성 (서브클래스에서 커스터마이징 가능)
    protected CellView makeCellView(double size, boolean preview) {
        CellView cell = new CellView(size, settings);
        if (preview) cell.getStyleClass().add("preview-cell");
        return cell;
    }
    
    // 고스트 블록 렌더링 - 공통 메서드
    protected void renderGhostBlock(int[][] shape, int px, int py, int ghostY, int w, int h, GridPane boardGrid) {
        if (ghostY != -1 && ghostY != py) {
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int bx = px + c;
                        int by = ghostY + r;
                        if (bx >= 0 && bx < w && by >= 0 && by < h) {
                            CellView cell = (CellView) getNodeByRowColumnIndex(by + 1, bx + 1, boardGrid);
                            if (cell != null) {
                                // 고스트 블록은 반투명하게 표시
                                cell.setBlock("", "block-ghost", "tetris-ghost-text");
                            }
                        }
                    }
                }
            }
        }
    }
}

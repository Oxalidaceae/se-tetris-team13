package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

public class GameScene {
    private final SceneManager manager;
    private final Settings settings;
    private GameEngine engine;
    private final ScoreBoard.ScoreEntry.Mode difficulty;
    private final HBox root;
    private Scene scene;
    private final GridPane boardGrid;
    private final GridPane previewGrid;
    private final Label scoreLabel;
    private final Label itemModeLabel;
    private static final double BOARD_CELL_SIZE = 28.0;
    private static final double PREVIEW_CELL_SIZE = 22.0;
    private static final String FILLED_SYMBOL = "■";

    public GameScene(
            SceneManager manager,
            Settings settings,
            GameEngine engine,
            ScoreBoard.ScoreEntry.Mode difficulty) {
        this.manager = manager;
        this.settings = settings;
        this.engine = engine;
        this.difficulty = difficulty;

        root = new HBox(12);
        root.getStyleClass().add("game-root");

        Board board = engine.getBoard();
        int w = board.getWidth();
        int h = board.getHeight();
        boardGrid = new GridPane();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.getStyleClass().add("board-grid");

        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                CellView cell = makeCellView(BOARD_CELL_SIZE, false);

                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setBorder();
                } else {
                    cell.setEmpty();
                }

                boardGrid.add(cell, gx, gy);
            }
        }
        previewGrid = new GridPane();
        previewGrid.setHgap(0);
        previewGrid.setVgap(0);
        previewGrid.getStyleClass().add("preview-grid");

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = makeCellView(PREVIEW_CELL_SIZE, true);
                cell.setEmpty();
                previewGrid.add(cell, c, r);
            }
        }

        scoreLabel = new Label("Score:\n0");
        scoreLabel.getStyleClass().add("score-label");
        itemModeLabel = new Label("");
        itemModeLabel.getStyleClass().add("item-mode-label");

        VBox right = new VBox(8, previewGrid, scoreLabel);
        right.getStyleClass().add("right-panel");

        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        root.getChildren().addAll(boardGrid, right);
        updateGrid();
    }

    private CellView makeCellView(double size, boolean preview) {
        CellView cell = new CellView(size, settings);
        if (preview) cell.getStyleClass().add("preview-cell");
        return cell;
    }

    public Scene createScene() {
        this.scene = new Scene(root);

        return scene;
    }

    public Scene getScene() {
        return scene;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void requestFocus() {
        Platform.runLater(() -> {
            if (scene != null) scene.getRoot().requestFocus();
        });
    }

    public void updateGrid() {
        if (engine == null) return;

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();
        Platform.runLater(() -> {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = b.getCell(x, y);
                    CellView cell = (CellView) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                    if (cell == null) continue;

                    if (val == 0) {
                        applyCellEmpty(cell);
                    } else if (val < 0) {
                        fillCell(cell, FILLED_SYMBOL, "block-flash", "tetris-flash-text");
                    } else if (val >= 100 && val < 200) {
                        // COPY 아이템 블록 (100번대 값)
                        fillCell(cell, "C", "item-copy-block", "item-copy-block");
                    } else if (val >= 200 && val < 300) {
                        // LINE_CLEAR 아이템 블록 (200번대 값)
                        fillCell(cell, "L", "item-copy-block", "item-copy-block");
                    } else if (val >= 300 && val < 400) {
                        // WEIGHT 아이템 블록 (300번대 값)
                        Tetromino.Kind kind = Tetromino.kindForId(val - 300);
                        fillCell(cell, "W", blockClassForKind(kind), textClassForKind(kind));
                    } else if (val >= 400 && val < 500) {
                        // GRAVITY 아이템 블록 (400번대 값)
                        Tetromino.Kind kind = Tetromino.kindForId(val - 400);
                        fillCell(cell, "G", blockClassForKind(kind), textClassForKind(kind));
                    } else if (val >= 500 && val < 600) {
                        // SPLIT 아이템 블록 (500번대 값)
                        Tetromino.Kind kind = Tetromino.kindForId(val - 500);
                        fillCell(cell, "S", blockClassForKind(kind), textClassForKind(kind));
                    } else {
                        // 일반 블록
                        Tetromino.Kind kind = Tetromino.kindForId(val);
                        fillCell(cell, FILLED_SYMBOL, blockClassForKind(kind), textClassForKind(kind));
                    }
                }
            }

            Tetromino cur = engine.getCurrent();
            if (cur != null) {
                int[][] shape = cur.getShape();
                int px = engine.getPieceX();
                int py = engine.getPieceY();
                String baseBlockClass = cur.getBlockStyleClass();
                String baseTextClass = cur.getTextStyleClass();
                if (baseBlockClass == null || baseBlockClass.isBlank()) {
                    baseBlockClass = blockClassForKind(cur.getKind());
                }
                if (baseTextClass == null || baseTextClass.isBlank()) {
                    baseTextClass = textClassForKind(cur.getKind());
                }
                int blockIndex = 0;

                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] == 0) {
                            continue;
                        }

                        int x = px + c;
                        int y = py + r;

                        if (x < 0 || x >= w || y < 0 || y >= h) {
                            blockIndex++;
                            continue;
                        }

                        CellView cell = (CellView) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                        if (cell == null) {
                            blockIndex++;
                            continue;
                        }

                        if (cur.isItemPiece()) {
                            Tetromino.ItemType itemType = cur.getItemType();
                            if (itemType == Tetromino.ItemType.COPY && blockIndex == cur.getCopyBlockIndex()) {
                                fillCell(cell, "C", baseBlockClass, "item-copy-block");
                            } else if (itemType == Tetromino.ItemType.LINE_CLEAR && blockIndex == cur.getLineClearBlockIndex()) {
                                fillCell(cell, "L", baseBlockClass, "item-copy-block");
                            } else if (itemType == Tetromino.ItemType.WEIGHT) {
                                fillCell(cell, "W", baseBlockClass, baseTextClass);
                            } else if (itemType == Tetromino.ItemType.GRAVITY) {
                                fillCell(cell, "G", baseBlockClass, baseTextClass);
                            } else if (itemType == Tetromino.ItemType.SPLIT) {
                                fillCell(cell, "S", baseBlockClass, baseTextClass);
                            } else {
                                fillCell(cell, FILLED_SYMBOL, baseBlockClass, baseTextClass);
                            }
                        } else {
                            fillCell(cell, FILLED_SYMBOL, baseBlockClass, baseTextClass);
                        }
                        blockIndex++;
                    }
                }
            }

            // 다음 블록 미리보기
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    CellView cell = (CellView) getNodeByRowColumnIndex(r, c, previewGrid);
                    if (cell != null) applyCellEmpty(cell);
                }
            }

            Tetromino next = engine.getNext();
            if (next != null) {
                int[][] s = next.getShape();
                String baseBlockClass = next.getBlockStyleClass();
                String baseTextClass = next.getTextStyleClass();
                if (baseBlockClass == null || baseBlockClass.isBlank()) {
                    baseBlockClass = blockClassForKind(next.getKind());
                }
                if (baseTextClass == null || baseTextClass.isBlank()) {
                    baseTextClass = textClassForKind(next.getKind());
                }
                int blockIndex = 0;

                for (int r = 0; r < s.length && r < 4; r++) {
                    for (int c = 0; c < s[r].length && c < 4; c++) {
                        if (s[r][c] == 0) {
                            continue;
                        }

                        CellView cell = (CellView) getNodeByRowColumnIndex(r, c, previewGrid);
                        if (cell == null) {
                            blockIndex++;
                            continue;
                        }

                        if (next.isItemPiece()) {
                            Tetromino.ItemType itemType = next.getItemType();
                            if (itemType == Tetromino.ItemType.COPY && blockIndex == next.getCopyBlockIndex()) {
                                fillCell(cell, "C", baseBlockClass, "item-copy-block");
                            } else if (itemType == Tetromino.ItemType.LINE_CLEAR && blockIndex == next.getLineClearBlockIndex()) {
                                fillCell(cell, "L", baseBlockClass, "item-copy-block");
                            } else if (itemType == Tetromino.ItemType.WEIGHT) {
                                fillCell(cell, "W", baseBlockClass, baseTextClass);
                            } else if (itemType == Tetromino.ItemType.GRAVITY) {
                                fillCell(cell, "G", baseBlockClass, baseTextClass);
                            } else if (itemType == Tetromino.ItemType.SPLIT) {
                                fillCell(cell, "S", baseBlockClass, baseTextClass);
                            } else {
                                fillCell(cell, FILLED_SYMBOL, baseBlockClass, baseTextClass);
                            }
                        } else {
                            fillCell(cell, FILLED_SYMBOL, baseBlockClass, baseTextClass);
                        }
                        blockIndex++;
                    }
                }
            }

            scoreLabel.setText("Score:\n" + engine.getScore());
        });
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            int r = rowIndex == null ? 0 : rowIndex;
            int c = colIndex == null ? 0 : colIndex;

            if (r == row && c == column) return node;
        }

        return null;
    }

    public void showGameOver() {
        if (manager != null) manager.showGameOver(settings, engine.getScore(), difficulty);
    }

    private void applyCellEmpty(CellView cell) {
        if (cell != null) cell.setEmpty();
    }

    private void fillCell(CellView cell, String symbol, String blockClass, String textClass) {
        if (cell != null) cell.setBlock(symbol, blockClass, textClass);
    }

    private String blockClassForKind(Tetromino.Kind kind) {
        return (kind != null) ? kind.getBlockStyleClass() : "block";
    }

    private String textClassForKind(Tetromino.Kind kind) {
        return (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
    }

    private static final class CellView extends StackPane {
        private final Rectangle rect;
        private final Canvas patternCanvas;
        private final Label label;
        private final Settings settings;

        private CellView(double size, Settings settings) {
            this.settings = settings;
            setMinSize(size, size);
            setPrefSize(size, size);
            setMaxSize(size, size);
            setAlignment(Pos.CENTER);
            getStyleClass().add("cell");

            rect = new Rectangle(size, size);
            rect.getStyleClass().add("cell-rect");
            rect.setStrokeWidth(0);
            rect.widthProperty().bind(widthProperty());
            rect.heightProperty().bind(heightProperty());

            patternCanvas = new Canvas(size, size);
            patternCanvas.widthProperty().bind(widthProperty());
            patternCanvas.heightProperty().bind(heightProperty());
            patternCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawPattern());
            patternCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawPattern());

            label = new Label(" ");
            label.setAlignment(Pos.CENTER);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            label.getStyleClass().add("cell-text");

            getChildren().addAll(rect, patternCanvas, label);

            setEmpty();
        }

        private String currentPattern = null;

        private void redrawPattern() {
            if (currentPattern == null || currentPattern.equals("none")) {
                clearCanvas();
                return;
            }

            double w = patternCanvas.getWidth();
            double h = patternCanvas.getHeight();
            GraphicsContext gc = patternCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, w, h);
            gc.setStroke(Color.rgb(0, 0, 0, 0.7));
            gc.setLineWidth(1);

            switch (currentPattern) {
                case "horizontal": // S - 수평 줄무늬
                    for (double y = 0; y < h; y += 5) {
                        gc.strokeLine(0, y, w, y);
                    }
                    break;
                case "vertical": // J - 수직 줄무늬
                    for (double x = 0; x < w; x += 5) {
                        gc.strokeLine(x, 0, x, h);
                    }
                    break;
                case "diagonal-right": // I - 빗살무늬 ↗
                    for (double offset = -h; offset < w + h; offset += 5) {
                        gc.strokeLine(offset, h, offset + h, 0);
                    }
                    break;
                case "diagonal-left": // T - 빗살무늬 ↖
                    for (double offset = -h; offset < w + h; offset += 5) {
                        gc.strokeLine(offset, 0, offset + h, h);
                    }
                    break;
                case "diagonal-right-wide": // Z - 빗살무늬 ↗ (넓은 간격)
                    for (double offset = -h; offset < w + h; offset += 7) {
                        gc.strokeLine(offset, h, offset + h, 0);
                    }
                    break;
                case "diagonal-left-wide": // L - 빗살무늬 ↖ (넓은 간격)
                    for (double offset = -h; offset < w + h; offset += 7) {
                        gc.strokeLine(offset, 0, offset + h, h);
                    }
                    break;
            }
        }

        private void clearCanvas() {
            GraphicsContext gc = patternCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, patternCanvas.getWidth(), patternCanvas.getHeight());
        }

        private void clearDynamicStyles() {
            ObservableList<String> rectClasses = rect.getStyleClass();
            rectClasses.removeIf(name -> name.startsWith("block-") || name.startsWith("item-") || name.equals("cell-empty") || name.equals("cell-border"));

            ObservableList<String> labelClasses = label.getStyleClass();
            labelClasses.removeIf(name -> name.startsWith("tetris-") || name.startsWith("item-") || name.equals("cell-empty") || name.equals("cell-border"));
        }

        private void setEmpty() {
            clearDynamicStyles();
            currentPattern = null;
            clearCanvas();
            if (!rect.getStyleClass().contains("cell-empty")) rect.getStyleClass().add("cell-empty");
            if (!label.getStyleClass().contains("cell-empty")) label.getStyleClass().add("cell-empty");
            label.setText(" ");
        }

        private void setBlock(String symbol, String blockClass, String textClass) {
            clearDynamicStyles();
            if (blockClass != null && !blockClass.isBlank() && !rect.getStyleClass().contains(blockClass)) {
                rect.getStyleClass().add(blockClass);
            }
            if (textClass != null && !textClass.isBlank() && !label.getStyleClass().contains(textClass)) {
                label.getStyleClass().add(textClass);
            }
            label.setText(symbol == null ? "" : symbol);
            
            // 색맹 모드에서 패턴 적용
            if (blockClass != null && settings.isColorBlindMode()) {
                applyPattern(blockClass);
            } else {
                currentPattern = null;
                clearCanvas();
            }
        }

        private void applyPattern(String blockClass) {
            switch (blockClass) {
                case "block-I":
                    currentPattern = "diagonal-right";
                    break;
                case "block-O":
                    currentPattern = "none";
                    break;
                case "block-T":
                    currentPattern = "diagonal-left";
                    break;
                case "block-S":
                    currentPattern = "horizontal";
                    break;
                case "block-Z":
                    currentPattern = "diagonal-right-wide";
                    break;
                case "block-J":
                    currentPattern = "vertical";
                    break;
                case "block-L":
                    currentPattern = "diagonal-left-wide";
                    break;
                default:
                    currentPattern = null;
            }
            redrawPattern();
        }

        private void setBorder() {
            clearDynamicStyles();
            currentPattern = null;
            clearCanvas();
            if (!rect.getStyleClass().contains("cell-border")) rect.getStyleClass().add("cell-border");
            if (!label.getStyleClass().contains("cell-border")) label.getStyleClass().add("cell-border");
            label.setText("X");
        }
    }
}

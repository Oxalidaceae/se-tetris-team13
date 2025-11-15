package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

public class GameScene extends BaseGameScene {
    private final SceneManager manager;
    private GameEngine engine;
    private final ScoreBoard.ScoreEntry.Mode difficulty;
    private final HBox root;
    private Scene scene;
    private final GridPane boardGrid;
    private final GridPane previewGrid;
    private final Label scoreLabel;
    private final Label itemModeLabel;

    public GameScene(
            SceneManager manager,
            Settings settings,
            GameEngine engine,
            ScoreBoard.ScoreEntry.Mode difficulty) {
        super(settings);
        this.manager = manager;
        this.engine = engine;
        this.difficulty = difficulty;

        root = new HBox(12);
        root.getStyleClass().add("game-root");

        Board board = engine.getBoard();
        boardGrid = createBoardGrid(board);
        previewGrid = createPreviewGrid();

        scoreLabel = new Label("Score:\n0");
        scoreLabel.getStyleClass().add("score-label");
        itemModeLabel = new Label("");
        itemModeLabel.getStyleClass().add("item-mode-label");

        VBox right = new VBox(8, previewGrid, scoreLabel);
        right.getStyleClass().add("right-panel");
        right.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        root.getChildren().addAll(boardGrid, right);
        updateGrid();
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
                        // COPY 아이템 블록 (100번대 값) - 원래 블록 색상 유지
                        Tetromino.Kind kind = Tetromino.kindForId(val - 100);
                        fillCell(cell, "C", blockClassForKind(kind), "item-copy-block");
                    } else if (val >= 200 && val < 300) {
                        // LINE_CLEAR 아이템 블록 (200번대 값) - 원래 블록 색상 유지
                        Tetromino.Kind kind = Tetromino.kindForId(val - 200);
                        fillCell(cell, "L", blockClassForKind(kind), "item-copy-block");
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
                    } else if (val >= 1 && val <= 7) {
                        // 일반 블록 (1~7)
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
                int ghostY = engine.getGhostY();
                String baseBlockClass = cur.getBlockStyleClass();
                String baseTextClass = cur.getTextStyleClass();
                if (baseBlockClass == null || baseBlockClass.isBlank()) {
                    baseBlockClass = blockClassForKind(cur.getKind());
                }
                if (baseTextClass == null || baseTextClass.isBlank()) {
                    baseTextClass = textClassForKind(cur.getKind());
                }
                
                // 고스트 블록 그리기 (현재 블록보다 먼저 그려서 뒤에 표시됨)
                renderGhostBlock(shape, px, py, ghostY, w, h, boardGrid);
                
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
                
                // 블록의 실제 크기 계산
                int minRow = 4, maxRow = -1, minCol = 4, maxCol = -1;
                for (int r = 0; r < s.length; r++) {
                    for (int c = 0; c < s[r].length; c++) {
                        if (s[r][c] != 0) {
                            if (r < minRow) minRow = r;
                            if (r > maxRow) maxRow = r;
                            if (c < minCol) minCol = c;
                            if (c > maxCol) maxCol = c;
                        }
                    }
                }
                
                // 블록을 4x4 그리드 중앙에 배치하기 위한 오프셋 계산
                int blockHeight = maxRow - minRow + 1;
                int blockWidth = maxCol - minCol + 1;
                int offsetRow = (4 - blockHeight) / 2;
                int offsetCol = (4 - blockWidth) / 2;
                
                int blockIndex = 0;

                for (int r = 0; r < s.length && r < 4; r++) {
                    for (int c = 0; c < s[r].length && c < 4; c++) {
                        if (s[r][c] == 0) {
                            continue;
                        }

                        // 중앙 정렬을 위해 오프셋 적용
                        int displayRow = r - minRow + offsetRow;
                        int displayCol = c - minCol + offsetCol;
                        
                        CellView cell = (CellView) getNodeByRowColumnIndex(displayRow, displayCol, previewGrid);
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

    public void showGameOver() {
        if (manager != null) manager.showGameOver(settings, engine.getScore(), difficulty);
    }
}

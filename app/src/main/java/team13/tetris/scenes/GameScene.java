package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

// 게임 플레이 화면 및 보드 렌더링
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

    public GameScene(SceneManager manager, Settings settings, GameEngine engine,
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

        // 메인 보드 그리드 생성
        boardGrid = new GridPane();
        boardGrid.getStyleClass().add("board-grid");

        // 테두리 포함 그리드 초기화
        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                Label cell = makeCellLabel();
                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setText("X");
                    applyCellBorder(cell);
                }
                boardGrid.add(cell, gx, gy);
            }
        }

        // 다음 블록 미리보기 영역
        previewGrid = new GridPane();
        previewGrid.getStyleClass().add("preview-grid");
        for (int r = 0; r < 4; r++)
            for (int c = 0; c < 4; c++)
                previewGrid.add(makeCellLabel(), c, r);

        scoreLabel = new Label("Score:\n0");
        scoreLabel.getStyleClass().add("score-label");

        itemModeLabel = new Label("");
        itemModeLabel.getStyleClass().add("item-mode-label");

        // 우측 패널 구성
        VBox right = new VBox(8, previewGrid, scoreLabel);
        right.getStyleClass().add("right-panel");

        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        root.getChildren().addAll(boardGrid, right);

        updateGrid();
    }

    private Label makeCellLabel() {
        Label lbl = new Label(" ");
        lbl.setAlignment(Pos.CENTER);
        lbl.getStyleClass().add("cell");
        return lbl;
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
            if (scene != null)
                scene.getRoot().requestFocus();
        });
    }

    public void updateItemModeInfo(int totalLinesCleared) {
    }

    // 보드 상태를 UI에 반영
    public void updateGrid() {
        if (engine == null)
            return;

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();

        Platform.runLater(() -> {
            // 고정된 보드 블록 렌더링
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = b.getCell(x, y);
                    Label cell = (Label) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                    if (val == 0) {
                        cell.setText(" ");
                        applyCellEmpty(cell);
                    } else if (val < 0) {
                        cell.setText("O");
                        applyCellBlockText(cell, "tetris-flash-text");
                    } else if (val >= 100 && val < 200) {
                        cell.setText("C");
                        applyCellBlockText(cell, "item-copy-block");
                    } else if (val >= 200 && val < 300) {
                        cell.setText("L");
                        applyCellBlockText(cell, "item-copy-block");
                    } else if (val >= 300 && val < 400) {
                        cell.setText("W");
                        Tetromino.Kind kind = Tetromino.kindForId(val - 300);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        applyCellBlockText(cell, textClass);
                    } else if (val >= 400 && val < 500) {
                        cell.setText("G");
                        Tetromino.Kind kind = Tetromino.kindForId(val - 400);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        applyCellBlockText(cell, textClass);
                    } else if (val >= 500 && val < 600) {
                        cell.setText("S");
                        Tetromino.Kind kind = Tetromino.kindForId(val - 500);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        applyCellBlockText(cell, textClass);
                    } else {
                        Tetromino.Kind kind = Tetromino.kindForId(val);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        cell.setText("O");
                        applyCellBlockText(cell, textClass);
                    }
                }
            }

            // 현재 떨어지는 블록 오버레이
            Tetromino cur = engine.getCurrent();
            if (cur != null) {
                int[][] shape = cur.getShape();
                int px = engine.getPieceX();
                int py = engine.getPieceY();
                String textClass = cur.getTextStyleClass();
                int blockIndex = 0;

                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            int x = px + c;
                            int y = py + r;
                            if (x >= 0 && x < w && y >= 0 && y < h) {
                                Label cell = (Label) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);

                                if (cur.isItemPiece()) {
                                    if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY
                                            && blockIndex == cur.getCopyBlockIndex()) {
                                        cell.setText("C");
                                        applyCellBlockText(cell, "item-copy-block");
                                    } else if (cur
                                            .getItemType() == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR
                                            && blockIndex == cur.getLineClearBlockIndex()) {
                                        cell.setText("L");
                                        applyCellBlockText(cell, "item-copy-block");
                                    } else if (cur
                                            .getItemType() == team13.tetris.game.model.Tetromino.ItemType.WEIGHT) {
                                        cell.setText("W");
                                        applyCellBlockText(cell, textClass);
                                    } else if (cur
                                            .getItemType() == team13.tetris.game.model.Tetromino.ItemType.GRAVITY) {
                                        cell.setText("G");
                                        applyCellBlockText(cell, textClass);
                                    } else if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.SPLIT) {
                                        cell.setText("S");
                                        applyCellBlockText(cell, textClass);
                                    } else {
                                        cell.setText("O");
                                        applyCellBlockText(cell, textClass);
                                    }
                                } else {
                                    cell.setText("O");
                                    applyCellBlockText(cell, textClass);
                                }
                            }
                            blockIndex++;
                        }
                    }
                }
            }

            // 다음 블록 미리보기
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    Label cell = (Label) getNodeByRowColumnIndex(r, c, previewGrid);
                    cell.setText(" ");
                    applyCellEmpty(cell);
                }
            }

            Tetromino next = engine.getNext();
            if (next != null) {
                int[][] s = next.getShape();
                String textClass = next.getTextStyleClass();
                int blockIndex = 0;

                for (int r = 0; r < s.length && r < 4; r++) {
                    for (int c = 0; c < s[r].length && c < 4; c++) {
                        if (s[r][c] != 0) {
                            Label cell = (Label) getNodeByRowColumnIndex(r, c, previewGrid);

                            if (next.isItemPiece()) {
                                if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY
                                        && blockIndex == next.getCopyBlockIndex()) {
                                    cell.setText("C");
                                    applyCellBlockText(cell, "item-copy-block");
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR
                                        && blockIndex == next.getLineClearBlockIndex()) {
                                    cell.setText("L");
                                    applyCellBlockText(cell, "item-copy-block");
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.WEIGHT) {
                                    cell.setText("W");
                                    applyCellBlockText(cell, textClass);
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.GRAVITY) {
                                    cell.setText("G");
                                    applyCellBlockText(cell, textClass);
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.SPLIT) {
                                    cell.setText("S");
                                    applyCellBlockText(cell, textClass);
                                } else {
                                    cell.setText("O");
                                    applyCellBlockText(cell, textClass);
                                }
                            } else {
                                cell.setText("O");
                                applyCellBlockText(cell, textClass);
                            }
                            blockIndex++;
                        }
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
            if (r == row && c == column)
                return node;
        }
        return null;
    }

    public void showGameOver() {
        if (manager != null)
            manager.showGameOver(settings, engine.getScore(), difficulty);
    }

    // CSS 스타일 적용 헬퍼
    private void applyCellEmpty(Label cell) {
        var sc = cell.getStyleClass();
        sc.removeIf(s -> s.startsWith("tetris-") || s.equals("cell-border"));
        if (!sc.contains("cell-empty"))
            sc.add("cell-empty");
    }

    private void applyCellBorder(Label cell) {
        var sc = cell.getStyleClass();
        sc.removeIf(s -> s.startsWith("tetris-") || s.equals("cell-empty"));
        if (!sc.contains("cell-border"))
            sc.add("cell-border");
    }

    private void applyCellBlockText(Label cell, String textClass) {
        var sc = cell.getStyleClass();
        sc.removeIf(s -> s.startsWith("tetris-") || s.equals("cell-empty"));
        if (!sc.contains(textClass))
            sc.add(textClass);
    }
}

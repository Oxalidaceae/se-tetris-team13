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
import javafx.scene.text.Font;
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
    private final Label itemModeLabel; // 아이템 모드 정보 표시용

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

        // 메인보드 생성
        boardGrid = new GridPane();
        boardGrid.getStyleClass().add("board-grid");

        // 테두리 포함 (w+2)x(h+2) 그리드
        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                Label cell = makeCellLabel();
                // border cells
                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setText("X");
                    applyCellBorder(cell); // CSS 클래스 적용
                }
                boardGrid.add(cell, gx, gy);
            }
        }

        // 미리보기 영역
        previewGrid = new GridPane();
        previewGrid.getStyleClass().add("preview-grid");
        for (int r = 0; r < 4; r++)
            for (int c = 0; c < 4; c++)
                previewGrid.add(makeCellLabel(), c, r);

        // 점수 레이블
        scoreLabel = new Label("Score:\n0");
        scoreLabel.setFont(Font.font("Monospaced", 14));
        scoreLabel.getStyleClass().add("score-label");

        // 아이템 모드 정보 레이블
        itemModeLabel = new Label("");
        itemModeLabel.setFont(Font.font("Monospaced", 12));
        itemModeLabel.getStyleClass().add("item-mode-label");
        
        // 아이템 모드일 때만 표시
        VBox right;
        if (difficulty == ScoreBoard.ScoreEntry.Mode.ITEM) {
            itemModeLabel.setText("Lines: 0/10\nNext Item: 10");
            right = new VBox(8, previewGrid, scoreLabel, itemModeLabel);
        } else {
            right = new VBox(8, previewGrid, scoreLabel);
        }
        right.getStyleClass().add("right-panel");

        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        root.getChildren().addAll(boardGrid, right);

        // 초기 렌더링
        updateGrid();
    }

    // 셀 레이블 생성 헬퍼
    private Label makeCellLabel() {
        Label lbl = new Label(" ");
        lbl.setMinSize(20, 16);
        lbl.setPrefSize(20, 16);
        lbl.setAlignment(Pos.CENTER);
        lbl.getStyleClass().add("cell");
        return lbl;
    }

    // Scene 생성
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

    // 포커스 요청 (게임 시작시 호출)
    public void requestFocus() {
        Platform.runLater(() -> {
            if (scene != null)
                scene.getRoot().requestFocus();
        });
    }

    // 아이템 모드 정보 업데이트
    public void updateItemModeInfo(int totalLinesCleared) {
        if (difficulty == ScoreBoard.ScoreEntry.Mode.ITEM) {
            Platform.runLater(() -> {
                int currentCycle = totalLinesCleared % 10;
                int nextItem = 10 - currentCycle;
                if (nextItem == 10) nextItem = 0; // 정확히 10의 배수일 때
                itemModeLabel.setText("Lines: " + currentCycle + "/10\nNext Item: " + nextItem);
            });
        }
    }

    public void updateGrid() {
        if (engine == null)
            return;

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();

        Platform.runLater(() -> {
            // 1) 고정된 보드 타일
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
                        // COPY 아이템 블록 (100번대 값)
                        cell.setText("C");
                        applyCellBlockText(cell, "item-copy-block");
                    } else if (val >= 200 && val < 300) {
                        // LINE_CLEAR 아이템 블록 (200번대 값)
                        cell.setText("L");
                        applyCellBlockText(cell, "item-copy-block");
                    } else if (val >= 300 && val < 400) {
                        // WEIGHT 아이템 블록 (300번대 값)
                        cell.setText("W");
                        Tetromino.Kind kind = Tetromino.kindForId(val - 300);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        applyCellBlockText(cell, textClass);
                    } else if (val >= 400 && val < 500) {
                        // GRAVITY 아이템 블록 (400번대 값)
                        cell.setText("G");
                        Tetromino.Kind kind = Tetromino.kindForId(val - 400);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        applyCellBlockText(cell, textClass);
                    } else if (val >= 500 && val < 600) {
                        // SPLIT 아이템 블록 (500번대 값)
                        cell.setText("S");
                        Tetromino.Kind kind = Tetromino.kindForId(val - 500);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        applyCellBlockText(cell, textClass);
                    } else {
                        // 일반 블록
                        Tetromino.Kind kind = Tetromino.kindForId(val);
                        String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                        cell.setText("O");
                        applyCellBlockText(cell, textClass);
                    }
                }
            }

            // 2) 현재 조각 오버레이
            Tetromino cur = engine.getCurrent();
            if (cur != null) {
                int[][] shape = cur.getShape();
                int px = engine.getPieceX();
                int py = engine.getPieceY();
                String textClass = cur.getTextStyleClass();
                int blockIndex = 0; // 블록 인덱스 카운터
                
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            int x = px + c;
                            int y = py + r;
                            if (x >= 0 && x < w && y >= 0 && y < h) {
                                Label cell = (Label) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                                
                                // 아이템 미노 표시 로직
                                if (cur.isItemPiece()) {
                                    if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY && blockIndex == cur.getCopyBlockIndex()) {
                                        // COPY 아이템: 특정 블록만 C 표시
                                        cell.setText("C");
                                        applyCellBlockText(cell, "item-copy-block");
                                    } else if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR && blockIndex == cur.getLineClearBlockIndex()) {
                                        // LINE_CLEAR 아이템: 특정 블록만 L 표시
                                        cell.setText("L");
                                        applyCellBlockText(cell, "item-copy-block");
                                    } else if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.WEIGHT) {
                                        // WEIGHT 아이템: 모든 블록 W 표시
                                        cell.setText("W");
                                        applyCellBlockText(cell, textClass);
                                    } else if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.GRAVITY) {
                                        // GRAVITY 아이템: 모든 블록 G 표시
                                        cell.setText("G");
                                        applyCellBlockText(cell, textClass);
                                    } else if (cur.getItemType() == team13.tetris.game.model.Tetromino.ItemType.SPLIT) {
                                        // SPLIT 아이템: 모든 블록 S 표시
                                        cell.setText("S");
                                        applyCellBlockText(cell, textClass);
                                    } else {
                                        // 기타 아이템 블록은 O 표시
                                        cell.setText("O");
                                        applyCellBlockText(cell, textClass);
                                    }
                                } else {
                                    // 일반 미노는 O 표시
                                    cell.setText("O");
                                    applyCellBlockText(cell, textClass);
                                }
                            }
                            blockIndex++;
                        }
                    }
                }
            }

            // 3) 다음 블록 미리보기
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
                int blockIndex = 0; // 블록 인덱스 카운터
                
                for (int r = 0; r < s.length && r < 4; r++) {
                    for (int c = 0; c < s[r].length && c < 4; c++) {
                        if (s[r][c] != 0) {
                            Label cell = (Label) getNodeByRowColumnIndex(r, c, previewGrid);
                            
                            // 아이템 미노 표시 로직
                            if (next.isItemPiece()) {
                                if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY && blockIndex == next.getCopyBlockIndex()) {
                                    // COPY 아이템: 특정 블록만 C 표시
                                    cell.setText("C");
                                    applyCellBlockText(cell, "item-copy-block");
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR && blockIndex == next.getLineClearBlockIndex()) {
                                    // LINE_CLEAR 아이템: 특정 블록만 L 표시
                                    cell.setText("L");
                                    applyCellBlockText(cell, "item-copy-block");
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.WEIGHT) {
                                    // WEIGHT 아이템: 모든 블록 W 표시
                                    cell.setText("W");
                                    applyCellBlockText(cell, textClass);
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.GRAVITY) {
                                    // GRAVITY 아이템: 모든 블록 G 표시
                                    cell.setText("G");
                                    applyCellBlockText(cell, textClass);
                                } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.SPLIT) {
                                    // SPLIT 아이템: 모든 블록 S 표시
                                    cell.setText("S");
                                    applyCellBlockText(cell, textClass);
                                } else {
                                    // 기타 아이템 블록은 O 표시
                                    cell.setText("O");
                                    applyCellBlockText(cell, textClass);
                                }
                            } else {
                                // 일반 미노는 O 표시
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

    // 그리드에서 특정 행,열의 노드를 가져오는 헬퍼
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

    // 게임 오버 표시 (Controller에서 호출)
    public void showGameOver() {
        Platform.runLater(() -> scoreLabel.setText("GAME OVER\n" + engine.getScore()));
        manager.showGameOver(settings, engine.getScore(), difficulty);
    }

    // 스타일 헬퍼
    private void applyCellEmpty(Label cell) {
        var sc = cell.getStyleClass();
        // 기존 블록/테두리 텍스트 클래스 제거
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

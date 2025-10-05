package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

/**
 * Grid 기반의 GameScene으로, 각 셀을 Label로 렌더링하여 개별 색상을 적용할 수 있게 합니다 (Canvas 사용 금지).
 * 미리보기는 4x4 크기의 GridPane으로 표시됩니다.
 */
public class GameScene implements GameStateListener {
    private final GameEngine engine;
    private final HBox root;
    private Scene scene;

    private final GridPane boardGrid; // width x height grid of Labels
    private final GridPane previewGrid; // 4x4 preview
    private final Label scoreLabel;
    private boolean paused = false;
    private boolean gameOver = false;

    public GameScene(GameEngine engine) {
        this.engine = engine;
        this.root = new HBox(12);

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();

    // 플레이 가능한 영역 주위에 1셀 테두리를 만들어 사용자의 요청대로 'X' 문자를 테두리로 표시합니다.
        boardGrid = new GridPane();
        boardGrid.setStyle("-fx-background-color: black; -fx-padding: 6;");

    // 그리드 크기 = (w + 2) x (h + 2)
        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                Label cell = makeCellLabel();
                // border cells
                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setText("X");
                    cell.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                }
                boardGrid.add(cell, gx, gy);
            }
        }

        previewGrid = new GridPane();
        previewGrid.setStyle("-fx-background-color: black; -fx-padding: 6;");
        for (int r = 0; r < 4; r++) for (int c = 0; c < 4; c++) previewGrid.add(makeCellLabel(), c, r);

        scoreLabel = new Label("Score:\n0");
        scoreLabel.setFont(Font.font("Monospaced", 14));
        scoreLabel.setStyle("-fx-text-fill: darkred; -fx-padding: 8;");

        VBox right = new VBox(8, previewGrid, scoreLabel);
        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        root.getChildren().addAll(boardGrid, right);

    // 초기 렌더링
        updateGrid();
    }

    private Label makeCellLabel() {
        Label lbl = new Label(" ");
        lbl.setMinSize(20, 16);
        lbl.setPrefSize(20, 16);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
        return lbl;
    }

    public Scene createScene() {
        this.scene = new Scene(root);
        scene.setOnKeyPressed(ev -> {
            KeyCode k = ev.getCode();
            if (k == KeyCode.LEFT) engine.moveLeft();
            else if (k == KeyCode.RIGHT) engine.moveRight();
            else if (k == KeyCode.UP) engine.rotateCW();
            else if (k == KeyCode.DOWN) engine.softDrop();
            else if (k == KeyCode.X) engine.hardDrop();
            else if (k == KeyCode.P) {
                // toggle pause: show modal pause dialog
                if (!paused) {
                    paused = true;
                    // stop auto-drop while paused
                    engine.stopAutoDrop();
                    showPauseWindow();
                }
            }
        });
        return scene;
    }

    public Scene getScene() { return scene; }

    public void requestFocus() {
        Platform.runLater(() -> { if (scene != null) scene.getRoot().requestFocus(); });
    }

    private void updateGrid() {
        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();

        Platform.runLater(() -> {
            // paint static board cells (mapped to internal grid offset by +1,+1 because of border)
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = b.getCell(x, y);
                    Label cell = (Label)getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                    if (val == 0) {
                        cell.setText(" ");
                        cell.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                    } else {
                        Tetromino.Kind kind = Tetromino.kindForId(val);
                        String color = (kind != null) ? kind.getColorCss() : "white";
                        cell.setText("O");
                        // show colored character on black background (no grid lines)
                        cell.setStyle("-fx-background-color: black; -fx-text-fill: " + color + "; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                    }
                }
            }

            // overlay current falling piece on top (drawn as O but not stored in board)
            Tetromino cur = engine.getCurrent();
            if (cur != null) {
                int[][] shape = cur.getShape();
                int px = engine.getPieceX();
                int py = engine.getPieceY();
                String color = cur.getColorCss();
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                                if (shape[r][c] != 0) {
                                    int x = px + c;
                                    int y = py + r;
                                    if (x >= 0 && x < w && y >= 0 && y < h) {
                                        // map into grid with +1 offset for border
                                        Label cell = (Label)getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                                        cell.setText("O");
                                        // falling piece: colored character (no grid lines)
                                        cell.setStyle("-fx-background-color: black; -fx-text-fill: " + color + "; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                                    }
                                }
                    }
                }
            }

            // preview
            for (int r = 0; r < 4; r++) for (int c = 0; c < 4; c++) {
                Label cell = (Label)getNodeByRowColumnIndex(r, c, previewGrid);
                cell.setText(" ");
                cell.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
            Tetromino next = engine.getNext();
            if (next != null) {
                int[][] s = next.getShape();
                String color = next.getColorCss();
                // center the 4x4 preview: shapes are already in 4x4 but just in case
                for (int r = 0; r < s.length && r < 4; r++) for (int c = 0; c < s[r].length && c < 4; c++) {
                    if (s[r][c] != 0) {
                        Label cell = (Label)getNodeByRowColumnIndex(r, c, previewGrid);
                        cell.setText("O");
                        // preview: colored character on black (no grid lines)
                        cell.setStyle("-fx-background-color: black; -fx-text-fill: " + color + "; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                    }
                }
            }

            scoreLabel.setText("Score:\n" + engine.getScore());
        });
    }

    // helper to fetch node by row/col (GridPane stores col=x, row=y)
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

    @Override
    public void onBoardUpdated(Board board) { updateGrid(); }

    @Override
    public void onPieceSpawned(Tetromino tetromino, int px, int py) { updateGrid(); }

    @Override
    public void onLinesCleared(int lines) { updateGrid(); }

    @Override
    public void onGameOver() {
        gameOver = true;
        Platform.runLater(() -> scoreLabel.setText("GAME OVER\n" + engine.getScore()));
    }

    @Override
    public void onNextPiece(Tetromino next) { updateGrid(); }

    @Override
    public void onScoreChanged(int score) { updateGrid(); }

    // --- pause dialog -------------------------------------------------
    private void showPauseWindow() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(scene.getWindow());

        Label resume = new Label("Resume");
        Label quit = new Label("Quit");
        resume.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
        quit.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
        if (gameOver) {
            // indicate resume is disabled when the game is over
            resume.setStyle("-fx-text-fill: gray; -fx-font-size: 14px; -fx-padding: 8px;");
        }

        VBox box = new VBox(8, resume, quit);
        box.setStyle("-fx-background-color: black; -fx-padding: 12px;");
        box.setAlignment(javafx.geometry.Pos.CENTER);

        Scene ds = new Scene(box);
        ds.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.UP || ev.getCode() == KeyCode.DOWN) {
                // toggle selection
                boolean selectResume = resume.getStyle().contains("-fx-font-weight: bold");
                if (selectResume) {
                    // switch selection to quit
                    resume.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
                    quit.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-padding: 8px; -fx-font-weight: bold;");
                } else {
                    resume.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-padding: 8px; -fx-font-weight: bold;");
                    quit.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
                }
            } else if (ev.getCode() == KeyCode.ENTER) {
                // if resume is highlighted, close and resume; if quit, exit app
                boolean resumeSelected = resume.getStyle().contains("-fx-font-weight: bold");
                dialog.close();
                paused = false;
                if (resumeSelected) {
                    // if game is over, resume should be disabled
                    if (!gameOver) engine.startAutoDrop();
                } else {
                    // quit the application
                    javafx.application.Platform.exit();
                }
            }
        });

        // default highlight resume
        resume.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-padding: 8px; -fx-font-weight: bold;");

        dialog.setScene(ds);
        dialog.setTitle("Paused");
        dialog.setWidth(200);
        dialog.setHeight(140);
        dialog.showAndWait();
    }
}


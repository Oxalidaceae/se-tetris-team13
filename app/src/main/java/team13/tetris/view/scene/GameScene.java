package team13.tetris.view.scene;

import java.util.Arrays;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import team13.tetris.controller.GameSessionController;
import team13.tetris.controller.KeyInputHandler;
import team13.tetris.model.game.Tetromino;
import team13.tetris.view.SceneManager;

public class GameScene {
    private static final int CELL_SIZE = 28;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;

    private final SceneManager manager;

    private GameSessionController controller;
    private KeyInputHandler keyInputHandler;

    private Scene scene;
    private GridPane boardGrid;
    private StackPane[][] boardCells;
    private GridPane nextPieceGrid;
    private StackPane[][] nextPieceCells;
    private Label scoreLabel;
    private Label statusLabel;
    private boolean paused = false;

    public GameScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene buildScene(GameSessionController controller, KeyInputHandler keyInputHandler) {
        this.controller = controller;
        this.keyInputHandler = keyInputHandler;

        scoreLabel = new Label("Score: 0");
        String rotateKey = manager.getSettings().getKeyRotate();
        if (rotateKey == null || rotateKey.isBlank()) {
            rotateKey = "Z";
        }
        statusLabel = new Label("Use arrow keys to move, " + rotateKey.toUpperCase() + " to rotate");

        boardGrid = createBoardGrid();
        nextPieceGrid = createNextPieceGrid();

        Button backBtn = new Button("Back to Menu");
        backBtn.setOnAction(e -> {
            controller.stop();
            manager.showMainMenu();
        });

        Button pauseBtn = new Button("Pause/Resume");
        pauseBtn.setOnAction(e -> togglePause());

        VBox rightPanel = new VBox(15,
            new Label("Next"),
            nextPieceGrid,
            pauseBtn,
            backBtn
        );
        rightPanel.setAlignment(Pos.TOP_CENTER);

        BorderPane root = new BorderPane();
        root.setTop(new HBox(scoreLabel));
        BorderPane.setAlignment(scoreLabel, Pos.CENTER);
        root.setCenter(boardGrid);
        root.setRight(rightPanel);
        root.setBottom(statusLabel);
        BorderPane.setAlignment(statusLabel, Pos.CENTER);

        scene = new Scene(root, 600, 700);
        scene.setOnKeyPressed(this::handleKey);
        return scene;
    }

    private void handleKey(javafx.scene.input.KeyEvent event) {
        KeyCode code = event.getCode();
        if (controller == null || keyInputHandler == null) return;

        if (keyInputHandler.isLeftClicked(code)) {
            controller.moveLeft();
        } else if (keyInputHandler.isRightClicked(code)) {
            controller.moveRight();
        } else if (keyInputHandler.isDropClicked(code)) {
            controller.softDrop();
        } else if (keyInputHandler.isHardDropClicked(code)) {
            controller.hardDrop();
        } else if (keyInputHandler.isRotateClicked(code)) {
            controller.rotateCW();
        } else if (keyInputHandler.isPauseClicked(code)) {
            togglePause();
        } else if (keyInputHandler.isEscClicked(code)) {
            controller.stop();
            manager.showMainMenu();
        }
        event.consume();
    }

    private void togglePause() {
        if (!controller.isRunning()) {
            return;
        }
        if (paused) {
            controller.resume();
            statusLabel.setText("Resumed");
        } else {
            controller.pause();
            statusLabel.setText("Paused");
        }
        paused = !paused;
    }

    private GridPane createBoardGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        boardCells = new StackPane[BOARD_HEIGHT][BOARD_WIDTH];
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
                StackPane cell = createCell();
                boardCells[r][c] = cell;
                grid.add(cell, c, r);
            }
        }
        return grid;
    }

    private GridPane createNextPieceGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        nextPieceCells = new StackPane[4][4];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                StackPane cell = createCell();
                cell.setPrefSize(CELL_SIZE - 6, CELL_SIZE - 6);
                nextPieceCells[r][c] = cell;
                grid.add(cell, c, r);
            }
        }
        return grid;
    }

    private StackPane createCell() {
        StackPane cell = new StackPane();
        Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
        rect.setArcWidth(6);
        rect.setArcHeight(6);
        rect.setFill(Color.web("#1f1f1f"));
        rect.setStroke(Color.web("#2c2c2c"));
        cell.getChildren().add(rect);
        return cell;
    }

    public void renderBoard(int[][] snapshot, Tetromino current, int px, int py) {
        int[][] display = Arrays.stream(snapshot)
            .map(row -> Arrays.copyOf(row, row.length))
            .toArray(int[][]::new);

        if (current != null) {
            int[][] shape = current.getShape();
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int gx = px + c;
                        int gy = py + r;
                        if (gy >= 0 && gy < display.length && gx >= 0 && gx < display[gy].length) {
                            display[gy][gx] = current.getId();
                        }
                    }
                }
            }
        }

        int rows = Math.min(display.length, BOARD_HEIGHT);
        int cols = rows > 0 ? Math.min(display[0].length, BOARD_WIDTH) : 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                updateCell(boardCells[r][c], display[r][c]);
            }
        }
    }

    public void showNextPiece(Tetromino next) {
        int[][] shape = next != null ? next.getShape() : new int[4][4];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int value = (r < shape.length && c < shape[r].length) ? shape[r][c] * (next != null ? next.getId() : 0) : 0;
                updateCell(nextPieceCells[r][c], value);
            }
        }
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void handleLinesCleared(int lines) {
        if (lines > 0) {
            statusLabel.setText(lines + " lines cleared!");
        }
    }

    private void updateCell(StackPane cell, int id) {
        Rectangle rect = (Rectangle) cell.getChildren().get(0);
        if (id <= 0) {
            rect.setFill(Color.web("#1f1f1f"));
            return;
        }
        String colorHex = colorForId(id);
        rect.setFill(Color.web(colorHex));
    }

    private String colorForId(int id) {
        Tetromino.Kind kind = Tetromino.kindForId(id);
        if (kind == null) {
            return "#cccccc";
        }
        if (!manager.isColorBlindMode()) {
            return switch (kind) {
                case I -> "#00BCD4";
                case O -> "#FFC107";
                case T -> "#9C27B0";
                case S -> "#4CAF50";
                case Z -> "#F44336";
                case J -> "#3F51B5";
                case L -> "#FF9800";
            };
        }
        return switch (kind) {
            case I -> "#ff6b6b";
            case O -> "#45ffb5";
            case T -> "#ffd93d";
            case S -> "#6a5acd";
            case Z -> "#ffa3d7";
            case J -> "#00bcd4";
            case L -> "#9ccc65";
        };
    }

    public Scene getScene() {
        return scene;
    }
}

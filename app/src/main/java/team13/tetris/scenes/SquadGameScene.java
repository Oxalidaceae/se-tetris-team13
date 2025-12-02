package team13.tetris.scenes;

import java.util.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

/**
 * Squad PVP Game Scene (3 players)
 *
 * <p>Layout: - Left: Player's own board (full size) - Right: 2 opponent boards (40% size, stacked
 * vertically)
 */
public class SquadGameScene extends BaseGameScene {
    @SuppressWarnings("unused")
    private final SceneManager manager;

    private final GameEngine localEngine;
    private Scene scene;

    // Opponent states (2 opponents)
    private final OpponentState opponent1 = new OpponentState();
    private final OpponentState opponent2 = new OpponentState();

    // UI components
    private final HBox root;
    private final GridPane boardGridLocal;
    private final GridPane boardGridOpponent1;
    private final GridPane boardGridOpponent2;

    private final GridPane previewLocal;
    private final GridPane previewOpponent1;
    private final GridPane previewOpponent2;

    private final GridPane incomingLocal;
    private final GridPane incomingOpponent1;
    private final GridPane incomingOpponent2;

    private final Label scoreLabelLocal;
    private final Label scoreLabelOpponent1;
    private final Label scoreLabelOpponent2;
    private Label gameOverLabel; // GAME OVER label for spectator mode

    private static final double OPP_BOARD_SCALE = 0.25;

    // Cache for cell views
    private final Map<String, CellView> boardCacheLocal = new HashMap<>();
    private final Map<String, CellView> boardCacheOpp1 = new HashMap<>();
    private final Map<String, CellView> boardCacheOpp2 = new HashMap<>();
    private final Map<String, CellView> previewCacheLocal = new HashMap<>();
    private final Map<String, CellView> previewCacheOpp1 = new HashMap<>();
    private final Map<String, CellView> previewCacheOpp2 = new HashMap<>();
    private final Map<String, Label> incomingCacheLocal = new HashMap<>();
    private final Map<String, Label> incomingCacheOpp1 = new HashMap<>();
    private final Map<String, Label> incomingCacheOpp2 = new HashMap<>();

    // Player names
    private final String localName;
    private String opponent1Name = "Opponent 1";
    private String opponent2Name = "Opponent 2";

    private static class OpponentState {
        int[][] board = null;
        int pieceX = 0;
        int pieceY = 0;
        int pieceType = 0;
        int rotation = 0;
        int nextPiece = 0;
        Queue<int[][]> incomingQueue = new LinkedList<>();
        int score = 0;
        boolean isAlive = true;
    }

    public SquadGameScene(
            SceneManager manager, Settings settings, GameEngine localEngine, String localName) {
        super(settings);

        this.manager = manager;
        this.localEngine = localEngine;
        this.localName = localName;

        // 1) AnchorPane 생성
        AnchorPane rootPane = new AnchorPane();

        // 2) 기존 HBox 생성
        root = new HBox(15);
        root.getStyleClass().add("game-root");
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setAlignment(Pos.TOP_LEFT);
        root.setFillHeight(true);

        // 3) HBox를 AnchorPane에 넣기
        rootPane.getChildren().add(root);

        // 4) HBox가 AnchorPane의 전체 공간을 4면 다 차지하도록 Anchor 지정
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);

        // Local player panel (left side, full size)
        Board boardL = localEngine.getBoard();
        boardGridLocal = createBoardGrid(boardL, boardCacheLocal);
        previewLocal = createPreviewGrid(previewCacheLocal);
        incomingLocal = createIncomingGrid(incomingCacheLocal);

        scoreLabelLocal = new Label(localName + "\nScore: 0");
        scoreLabelLocal.getStyleClass().add("score-label");

        VBox localBox = new VBox(8);
        HBox localGame = new HBox(8);
        VBox rightLocal = createRightPanel(previewLocal, scoreLabelLocal, incomingLocal);
        localGame.getChildren().addAll(boardGridLocal, rightLocal);
        localBox.getChildren().add(localGame);

        // Right side: 2 opponent boards (40% size, stacked vertically)
        VBox opponentsBox = new VBox(5); // 간격 최소화
        opponentsBox.setAlignment(Pos.TOP_CENTER); // 위쪽 정렬
        opponentsBox.setPadding(new Insets(0, 0, 0, 0));
        opponentsBox.setMaxHeight(Double.MAX_VALUE); // 최대 높이 제한 해제

        // Opponent 1 (top)
        boardGridOpponent1 = createSmallBoardGrid(boardL, boardCacheOpp1);
        previewOpponent1 = createSmallPreviewGrid(previewCacheOpp1);
        incomingOpponent1 = createSmallIncomingGrid(incomingCacheOpp1);
        scoreLabelOpponent1 = new Label(opponent1Name + "\nScore: 0");
        scoreLabelOpponent1.getStyleClass().add("score-label-small");
        scoreLabelOpponent1.setStyle("-fx-font-size: 11px; -fx-padding: 2px;");

        VBox opp1Box = createOpponentPanelCompact(boardGridOpponent1, scoreLabelOpponent1);
        VBox.setMargin(opp1Box, new Insets(0, 0, 0, 0));

        // Opponent 2 (bottom)
        boardGridOpponent2 = createSmallBoardGrid(boardL, boardCacheOpp2);
        previewOpponent2 = createSmallPreviewGrid(previewCacheOpp2);
        incomingOpponent2 = createSmallIncomingGrid(incomingCacheOpp2);
        scoreLabelOpponent2 = new Label(opponent2Name + "\nScore: 0");
        scoreLabelOpponent2.getStyleClass().add("score-label-small");
        scoreLabelOpponent2.setStyle("-fx-font-size: 11px; -fx-padding: 2px;");

        VBox opp2Box = createOpponentPanelCompact(boardGridOpponent2, scoreLabelOpponent2);
        VBox.setMargin(opp2Box, new Insets(0, 0, 0, 0));

        opponentsBox.getChildren().addAll(opp1Box, opp2Box);

        // Wrap opponentsBox in a new VBox (rightSide) - full height container
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.TOP_CENTER); // 위쪽 정렬
        rightSide.setPrefHeight(Double.MAX_VALUE); // 선호 높이를 최대로 설정
        rightSide.setMaxHeight(Double.MAX_VALUE); // 최대 높이 제한 해제
        rightSide.setFillWidth(true);
        rightSide.setTranslateX(20); // Move 20px to the right to prevent covering incoming board
        VBox.setVgrow(rightSide, Priority.ALWAYS);

        VBox.setVgrow(opponentsBox, Priority.NEVER); // opponentsBox는 내용물 크기만큼만
        opponentsBox.setAlignment(Pos.TOP_CENTER);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        rightSide.getChildren().addAll(opponentsBox, spacer);

        HBox.setHgrow(rightSide, Priority.ALWAYS);

        root.getChildren().addAll(localBox, rightSide);

        scene = new Scene(rootPane);

        updateGrid();
    }

    private GridPane createBoardGrid(Board board, Map<String, CellView> cache) {
        int w = board.getWidth();
        int h = board.getHeight();

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("board-grid");

        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                CellView cell = new CellView(BOARD_CELL_SIZE, settings);
                String key = gy + "," + gx;
                cache.put(key, cell);

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

    // 40% size board for opponents
    private GridPane createSmallBoardGrid(Board board, Map<String, CellView> cache) {
        int w = board.getWidth();
        int h = board.getHeight();

        double smallSize = BOARD_CELL_SIZE * 0.3;

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("board-grid-small");

        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                // 기본 생성 (CSS 기준 크기)
                CellView cell = new CellView(BOARD_CELL_SIZE, settings);

                // ★ 상대 보드에서만 CSS 위에 덮어씌우는 inline size
                cell.setStyle(
                        "-fx-min-width: "
                                + smallSize
                                + "px;"
                                + "-fx-pref-width: "
                                + smallSize
                                + "px;"
                                + "-fx-max-width: "
                                + smallSize
                                + "px;"
                                + "-fx-min-height: "
                                + smallSize
                                + "px;"
                                + "-fx-pref-height: "
                                + smallSize
                                + "px;"
                                + "-fx-max-height: "
                                + smallSize
                                + "px;");

                String key = gy + "," + gx;
                cache.put(key, cell);

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

    private GridPane createPreviewGrid(Map<String, CellView> cache) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("preview-grid");

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = new CellView(PREVIEW_CELL_SIZE, settings);
                cell.setEmpty();
                String key = c + "," + r;
                cache.put(key, cell);
                grid.add(cell, c, r);
            }
        }

        return grid;
    }

    private GridPane createSmallPreviewGrid(Map<String, CellView> cache) {
        double smallSize = PREVIEW_CELL_SIZE * 0.4;
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("preview-grid-small");

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = new CellView(smallSize, settings);
                cell.setEmpty();
                String key = c + "," + r;
                cache.put(key, cell);
                grid.add(cell, c, r);
            }
        }

        return grid;
    }

    private GridPane createIncomingGrid(Map<String, Label> cache) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("incoming-grid");

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label label = new Label("");
                label.setMinSize(15, 15);
                label.setMaxSize(15, 15);
                label.setPrefSize(15, 15);
                label.setAlignment(Pos.CENTER);
                label.setStyle(
                        "-fx-background-color: transparent; -fx-border-color: #333; -fx-border-width: 0.3;");
                cache.put(r + "," + c, label);
                grid.add(label, c, r);
            }
        }

        return grid;
    }

    private GridPane createSmallIncomingGrid(Map<String, Label> cache) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("incoming-grid-small");

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label label = new Label("");
                label.setMinSize(6, 6);
                label.setMaxSize(6, 6);
                label.setPrefSize(6, 6);
                label.setAlignment(Pos.CENTER);
                label.setStyle(
                        "-fx-background-color: transparent; -fx-border-color: #333; -fx-border-width: 0.3; -fx-font-size: 4px;");
                cache.put(r + "," + c, label);
                grid.add(label, c, r);
            }
        }

        return grid;
    }

    private VBox createRightPanel(GridPane preview, Label scoreLabel, GridPane incoming) {
        Label incomingLabel = new Label("Incoming:");
        incomingLabel.getStyleClass().add("label");

        // Create GAME OVER label (initially hidden)
        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("label");
        gameOverLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        gameOverLabel.setVisible(false);

        VBox rightPanel = new VBox(10, preview, scoreLabel, incomingLabel, incoming, gameOverLabel);
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setAlignment(Pos.TOP_CENTER);
        HBox.setMargin(rightPanel, new Insets(0, 0, 0, 30));

        return rightPanel;
    }

    private VBox createOpponentPanelCompact(GridPane boardGrid, Label scoreLabel) {
        VBox panel = new VBox(2); // 최소 간격
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(0, 0, 0, 0));
        panel.getChildren().addAll(scoreLabel, boardGrid);
        return panel;
    }

    public Scene getScene() {
        return scene;
    }

    public void requestFocus() {
        Platform.runLater(
                () -> {
                    if (scene != null) scene.getRoot().requestFocus();
                });
    }

    // Update opponent 1 state
    public void updateOpponent1(
            int[][] board,
            int pieceX,
            int pieceY,
            int pieceType,
            int rotation,
            int nextPiece,
            Queue<int[][]> incoming,
            int score) {
        opponent1.board = board;
        opponent1.pieceX = pieceX;
        opponent1.pieceY = pieceY;
        opponent1.pieceType = pieceType;
        opponent1.rotation = rotation;
        opponent1.nextPiece = nextPiece;
        opponent1.incomingQueue =
                (incoming != null) ? new LinkedList<>(incoming) : new LinkedList<>();
        opponent1.score = score;

        updateGrid();
    }

    // Update opponent 2 state
    public void updateOpponent2(
            int[][] board,
            int pieceX,
            int pieceY,
            int pieceType,
            int rotation,
            int nextPiece,
            Queue<int[][]> incoming,
            int score) {
        opponent2.board = board;
        opponent2.pieceX = pieceX;
        opponent2.pieceY = pieceY;
        opponent2.pieceType = pieceType;
        opponent2.rotation = rotation;
        opponent2.nextPiece = nextPiece;
        opponent2.incomingQueue =
                (incoming != null) ? new LinkedList<>(incoming) : new LinkedList<>();
        opponent2.score = score;

        updateGrid();
    }

    /** Gray out local board when player dies (spectator mode) */
    public void setLocalBoardGrayedOut(boolean grayed) {
        Platform.runLater(
                () -> {
                    if (grayed) {
                        // Change all local board cells to gray
                        Board board = localEngine.getBoard();
                        int w = board.getWidth();
                        int h = board.getHeight();

                        for (int y = 0; y < h; y++) {
                            for (int x = 0; x < w; x++) {
                                CellView cell = boardCacheLocal.get((y + 1) + "," + (x + 1));
                                if (cell != null) {
                                    int v = board.getCell(x, y);
                                    if (v != 0) {
                                        // Make it gray
                                        cell.setBlock("", "block-gray", "tetris-text");
                                    }
                                }
                            }
                        }
                        // Force a full redraw to ensure gray shows up
                        updateLocalGrid();
                    }
                });
    }

    /** Show/hide GAME OVER label */
    public void showGameOverLabel(boolean show) {
        Platform.runLater(
                () -> {
                    if (gameOverLabel != null) {
                        gameOverLabel.setVisible(show);
                    }
                });
    }

    public void setOpponent1Name(String name) {
        this.opponent1Name = name;
        Platform.runLater(() -> scoreLabelOpponent1.setText(name + "\nScore: " + opponent1.score));
    }

    public void setOpponent2Name(String name) {
        this.opponent2Name = name;
        Platform.runLater(() -> scoreLabelOpponent2.setText(name + "\nScore: " + opponent2.score));
    }

    public void setOpponent1Alive(boolean alive) {
        opponent1.isAlive = alive;
        if (!alive) {
            Platform.runLater(
                    () -> {
                        boardGridOpponent1.setOpacity(0.5);
                        scoreLabelOpponent1.setText(opponent1Name + "\nELIMINATED");
                    });
        }
    }

    public void setOpponent2Alive(boolean alive) {
        opponent2.isAlive = alive;
        if (!alive) {
            Platform.runLater(
                    () -> {
                        boardGridOpponent2.setOpacity(0.5);
                        scoreLabelOpponent2.setText(opponent2Name + "\nELIMINATED");
                    });
        }
    }

    public void updateLocalGrid() {
        updateGrid();
    }

    public void updateLocalPreview(int nextPieceType) {
        Platform.runLater(
                () -> {
                    drawNextPiece(previewCacheLocal, nextPieceType);
                });
    }

    private void updateGrid() {
        Platform.runLater(
                () -> {
                    // Update local board
                    updateBoardGrid(localEngine.getBoard(), boardCacheLocal, localEngine);

                    // Update local preview (next piece)
                    Tetromino next = localEngine.getNext();
                    if (next != null) {
                        drawNextPiece(previewCacheLocal, next.getId());
                    }

                    // Update local score
                    scoreLabelLocal.setText(localName + "\nScore: " + localEngine.getScore());

                    // Update opponents
                    if (opponent1.board != null && opponent1.isAlive) {
                        updateOpponentGrid(opponent1, boardCacheOpp1);
                        drawNextPiece(previewCacheOpp1, opponent1.nextPiece);
                        scoreLabelOpponent1.setText(opponent1Name + "\nScore: " + opponent1.score);
                    }

                    if (opponent2.board != null && opponent2.isAlive) {
                        updateOpponentGrid(opponent2, boardCacheOpp2);
                        drawNextPiece(previewCacheOpp2, opponent2.nextPiece);
                        scoreLabelOpponent2.setText(opponent2Name + "\nScore: " + opponent2.score);
                    }
                });
    }

    private void updateBoardGrid(Board board, Map<String, CellView> cache, GameEngine engine) {
        int w = board.getWidth();
        int h = board.getHeight();

        // 1) Draw board cells
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = board.getCell(x, y);
                CellView cell = cache.get((y + 1) + "," + (x + 1));
                if (cell == null) continue;

                applyCellValue(cell, v);
            }
        }

        // 2) Draw ghost piece
        drawGhostPiece(cache, engine, w, h);

        // 3) Draw current falling piece
        Tetromino cur = engine.getCurrent();
        if (cur != null) {
            drawFallingPiece(cache, cur, engine.getPieceX(), engine.getPieceY(), w, h);
        }
    }

    private void drawFallingPiece(
            Map<String, CellView> cache, Tetromino cur, int px, int py, int w, int h) {
        int[][] shape = cur.getShape();
        String blockClass = cur.getBlockStyleClass();
        String textClass = cur.getTextStyleClass();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int bx = px + c;
                    int by = py + r;

                    if (bx >= 0 && bx < w && by >= 0 && by < h) {
                        CellView cell = cache.get((by + 1) + "," + (bx + 1));
                        if (cell != null) {
                            fillCell(cell, "", blockClass, textClass);
                        }
                    }
                }
            }
        }
    }

    private void drawGhostPiece(Map<String, CellView> cache, GameEngine engine, int w, int h) {
        Tetromino cur = engine.getCurrent();
        if (cur == null) return;

        int ghostY = engine.getGhostY();
        if (ghostY < 0) return;

        int[][] shape = cur.getShape();
        int px = engine.getPieceX();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int bx = px + c;
                    int by = ghostY + r;
                    if (bx >= 0 && bx < w && by >= 0 && by < h) {
                        CellView cell = cache.get((by + 1) + "," + (bx + 1));
                        if (cell != null) {
                            fillCell(cell, "", "block-ghost", "tetris-ghost-text");
                        }
                    }
                }
            }
        }
    }

    private void applyCellValue(CellView cell, int v) {
        if (v == 0) {
            cell.setEmpty();
        } else if (v == 1000) {
            cell.setBlock("", "block-gray", "tetris-gray-text");
        } else if (v >= 1 && v <= 7) {
            Tetromino.Kind k = Tetromino.kindForId(v);
            if (k != null) {
                fillCell(cell, "", k.getBlockStyleClass(), k.getTextStyleClass());
            }
        } else if (v < 0) {
            fillCell(cell, "", "block-flash", "tetris-flash-text");
        }
    }

    protected void fillCell(CellView cell, String text, String blockClass, String textClass) {
        cell.setBlock(text, blockClass, textClass);
    }

    public void updateLocalIncomingGrid(Queue<int[][]> incomingQueue) {
        Platform.runLater(
                () -> {
                    updateIncoming(incomingCacheLocal, incomingQueue);
                });
    }

    private void updateIncoming(Map<String, Label> cache, Queue<int[][]> queue) {
        // Initialize
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label cell = cache.get(r + "," + c);
                if (cell != null) {
                    cell.setText(" ");
                    cell.setStyle(
                            "-fx-background-color: transparent; -fx-border-color: #333; -fx-border-width: 0.3;");
                }
            }
        }

        if (queue == null || queue.isEmpty()) return;

        int row = 9;
        List<int[][]> patterns = new ArrayList<>(queue);

        for (int i = patterns.size() - 1; i >= 0; i--) {
            int[][] pat = patterns.get(i);

            for (int r = pat.length - 1; r >= 0 && row >= 0; r--) {
                for (int c = 0; c < 10; c++) {
                    if (pat[r][c] != 0) {
                        Label cell = cache.get(row + "," + c);
                        if (cell != null) {
                            cell.setText("■");
                            cell.setStyle(
                                    "-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: #333; -fx-border-width: 0.3; -fx-font-size: 8px;");
                        }
                    }
                }
                row--;
            }
        }
    }

    private void updateOpponentGrid(OpponentState opp, Map<String, CellView> cache) {
        if (opp.board == null) return;

        int h = opp.board.length;
        int w = opp.board[0].length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = opp.board[y][x];
                CellView cell = cache.get((y + 1) + "," + (x + 1));
                if (cell != null) {
                    applyCellValue(cell, v);
                }
            }
        }
    }

    private void drawNextPiece(Map<String, CellView> cache, int pieceType) {
        // Clear the 4x4 preview grid
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = cache.get(r + "," + c);
                if (cell != null) {
                    cell.setEmpty();
                }
            }
        }

        if (pieceType < 0) return;

        Tetromino.Kind kind = Tetromino.kindForId(pieceType);
        if (kind == null) return;

        Tetromino piece = new Tetromino(kind);
        int[][] shape = piece.getShape();

        // Find bounding box
        int minR = 4, maxR = -1, minC = 4, maxC = -1;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    minR = Math.min(minR, r);
                    maxR = Math.max(maxR, r);
                    minC = Math.min(minC, c);
                    maxC = Math.max(maxC, c);
                }
            }
        }

        // Center in 4x4 grid
        int h = maxR - minR + 1;
        int w = maxC - minC + 1;
        int offR = (4 - h) / 2;
        int offC = (4 - w) / 2;

        String blockClass = piece.getBlockStyleClass();
        String textClass = piece.getTextStyleClass();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int rr = r - minR + offR;
                    int cc = c - minC + offC;
                    CellView cell = cache.get(rr + "," + cc);
                    if (cell != null) {
                        fillCell(cell, "", blockClass, textClass);
                    }
                }
            }
        }
    }
}

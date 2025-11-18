package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

import java.util.*;

// 네트워크 대전 게임 화면 (VersusGameScene과 동일한 디자인 유지)
public class NetworkGameScene {
    @SuppressWarnings("unused")
    private final SceneManager manager;
    private final Settings settings;
    private final GameEngine localEngine;   // 실제 엔진

    private Scene scene;

    // Remote 상태 캐싱 (서버에서 받아오는 값)
    private int[][] remoteBoard = null;
    private int remotePieceX = 0;
    private int remotePieceY = 0;
    private int remotePieceType = 0;
    private int remoteRotation = 0;
    private int remoteNext = 0;
    private Queue<int[][]> remoteIncomingQueue = new LinkedList<>();
    private int remoteScore = 0;

    // UI 루트
    private final HBox root;

    // 캐싱용 맵
    private final Map<String, CellView> boardCacheLocal = new HashMap<>();
    private final Map<String, CellView> boardCacheRemote = new HashMap<>();
    private final Map<String, CellView> previewCacheLocal = new HashMap<>();
    private final Map<String, CellView> previewCacheRemote = new HashMap<>();
    private final Map<String, Label> incomingCacheLocal = new HashMap<>();
    private final Map<String, Label> incomingCacheRemote = new HashMap<>();

    // UI 컴포넌트
    private final GridPane boardGridLocal;
    private final GridPane boardGridRemote;

    private final GridPane previewLocal;
    private final GridPane previewRemote;

    private final GridPane incomingLocal;
    private final GridPane incomingRemote;

    private final Label scoreLabelLocal;
    private final Label scoreLabelRemote;
    private final Label timerLabelLocal;
    private final Label timerLabelRemote;

    // 이름표
    private final String localName;
    private final String remoteName;

    // Throttle
    private volatile boolean updatePending = false;
    private final boolean timerMode;


    public NetworkGameScene(SceneManager manager,
                            Settings settings,
                            GameEngine localEngine,
                            String localName,
                            String remoteName,
                            boolean timerMode) {

        this.manager = manager;
        this.settings = settings;
        this.localEngine = localEngine;
        this.localName = localName;
        this.remoteName = remoteName;
        this.timerMode = timerMode;

        root = new HBox(20);
        root.getStyleClass().add("game-root");

        // Local Panel 
        Board boardL = localEngine.getBoard();
        boardGridLocal = createBoardGrid(boardL, boardCacheLocal);
        previewLocal = createPreviewGrid(previewCacheLocal);
        incomingLocal = createIncomingGrid(incomingCacheLocal);

        scoreLabelLocal = new Label(localName + "\nScore: 0");
        scoreLabelLocal.getStyleClass().add("score-label");
        timerLabelLocal = new Label("Time: 120");
        timerLabelLocal.getStyleClass().add("label-title");


        VBox localBox = new VBox(12);
        HBox localGame = new HBox(12);
        VBox rightL = createRightPanel(previewLocal, scoreLabelLocal, incomingLocal, timerLabelLocal);
        localGame.getChildren().addAll(boardGridLocal, rightL);
        localBox.getChildren().add(localGame);


        // Remote Panel
        boardGridRemote = createBoardGrid(boardL, boardCacheRemote);
        previewRemote = createPreviewGrid(previewCacheRemote);
        incomingRemote = createIncomingGrid(incomingCacheRemote);

        scoreLabelRemote = new Label(remoteName + "\nScore: 0");
        scoreLabelRemote.getStyleClass().add("score-label");
        timerLabelRemote = new Label("Time: 120");
        timerLabelRemote.getStyleClass().add("label-title");

        VBox remoteBox = new VBox(12);
        HBox remoteGame = new HBox(12);
        VBox rightR = createRightPanel(previewRemote, scoreLabelRemote, incomingRemote, timerLabelRemote);
        remoteGame.getChildren().addAll(boardGridRemote, rightR);
        remoteBox.getChildren().add(remoteGame);

        // Add both players
        root.getChildren().addAll(localBox, remoteBox);

        scene = new Scene(root);

        // 초기 한번 업데이트
        updateGrid();
    }

    private VBox createRightPanel(GridPane previewGrid, Label scoreLabel, GridPane incomingGrid, Label timerLabel) {
        Label incomingLabel = new Label("Incoming:");
        incomingLabel.getStyleClass().add("label");

        String windowSize = settings.getWindowSize();
        VBox rightPanel;

        if (timerMode) {
            if ("SMALL".equals(windowSize)) {
                rightPanel = new VBox(8, previewGrid, scoreLabel, timerLabel, incomingLabel, incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            } else if ("MEDIUM".equals(windowSize)) {
                rightPanel = new VBox(10, previewGrid, scoreLabel, timerLabel, incomingLabel, incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 30));
            } else { // LARGE
                rightPanel = new VBox(12, previewGrid, scoreLabel, timerLabel, incomingLabel, incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            }
        } else {
            if ("SMALL".equals(windowSize)) {
                rightPanel = new VBox(8, previewGrid, scoreLabel, incomingLabel, incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            } else if ("MEDIUM".equals(windowSize)) {
                rightPanel = new VBox(10, previewGrid, scoreLabel, incomingLabel, incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 30));
            } else { // LARGE
                rightPanel = new VBox(12, previewGrid, scoreLabel, incomingLabel, incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            }
        }
        
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setAlignment(Pos.TOP_CENTER);
        
        return rightPanel;
    }

    public Scene getScene() {
        return scene;
    }

    public void requestFocus() {
        Platform.runLater(() -> {
            if (scene != null)
                scene.getRoot().requestFocus();
        });
    }

    public void updateTimer(int seconds) {
        if (timerMode) {
            Platform.runLater(() -> {
                timerLabelLocal.setText("Time: " + seconds);
                timerLabelRemote.setText("Time: " + seconds);
                if (seconds <= 30) {
                    timerLabelLocal.setStyle("-fx-text-fill: red;");
                    timerLabelRemote.setStyle("-fx-text-fill: red;");
                } else {
                    // 30초 이상일 때 기본 스타일로 되돌림
                    timerLabelLocal.setStyle(""); 
                    timerLabelRemote.setStyle("");
                }
            });
        }
    }

    // Remote 상태 갱신 (서버 → 클라이언트)
    public void updateRemoteBoardState(
            int[][] board,
            int pieceX,
            int pieceY,
            int pieceType,
            int rotation,
            int nextPiece,
            Queue<int[][]> incoming,
            int score,
            int lines
    ) {
        this.remoteBoard = board;
        this.remotePieceX = pieceX;
        this.remotePieceY = pieceY;
        this.remotePieceType = pieceType;
        this.remoteRotation = rotation;
        this.remoteNext = nextPiece;
        this.remoteIncomingQueue = (incoming != null) ? new LinkedList<>(incoming) : new LinkedList<>();
        this.remoteScore = score;

        updateGrid();
    }
    
    public void updateLocalGrid() {
        updateGrid();
    }
    
    public void setConnected(boolean connected) {
        // 연결 상태 표시 (필요시 UI 업데이트)
    }

    // Main UI 업데이트 (Local + Remote)
    public void updateGrid() {
        if (updatePending) return;
        updatePending = true;

        Platform.runLater(() -> {
            updateLocalUI();
            updateRemoteUI();
            updatePending = false;
        });
    }

    // Local Player UI Update
    private void updateLocalUI() {
        Board board = localEngine.getBoard();
        Map<String, CellView> cache = boardCacheLocal;

        int w = board.getWidth();
        int h = board.getHeight();

        // 1) 보드 셀 그리기
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = board.getCell(x, y);
                CellView cell = cache.get((y + 1) + "," + (x + 1));
                if (cell == null) continue;

                applyCellValue(cell, v);
            }
        }

        // 2) 현재 떨어지는 미노 표시
        Tetromino cur = localEngine.getCurrent();
        if (cur != null)
            drawFallingPiece(cache, cur, localEngine.getPieceX(), localEngine.getPieceY(), w, h);

        // 3) 고스트 미노 표시
        drawGhostPiece(cache, localEngine, w, h);

        // 4) Next 업데이트
        drawNext(previewCacheLocal, localEngine.getNext());

        // 5) 점수
        scoreLabelLocal.setText(localName + "\nScore:\n" + localEngine.getScore());

        // 6) Incoming - 외부에서 updateLocalIncomingGrid 호출로 업데이트
    }
    
    // Local Player의 incoming grid 업데이트 (NetworkGameController에서 호출)
    public void updateLocalIncomingGrid(Queue<int[][]> incomingQueue) {
        Platform.runLater(() -> {
            updateIncoming(incomingCacheLocal, incomingQueue);
        });
    }

    // Remote Player UI Update
    private void updateRemoteUI() {
        if (remoteBoard == null) return;

        int[][] board = remoteBoard;
        Map<String, CellView> cache = boardCacheRemote;

        int h = board.length;
        int w = board[0].length;

        // 1) 원본 보드 그리기
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = board[y][x];
                CellView cell = cache.get((y + 1) + "," + (x + 1));
                if (cell == null) continue;

                applyCellValue(cell, v);
            }
        }

        // 2) 떨어지는 미노 (서버에서 받은 데이터로 렌더)
        if (remotePieceType > 0) {
            Tetromino.Kind kind = Tetromino.kindForId(remotePieceType);
            if (kind == null) return;
            
            Tetromino t = Tetromino.of(kind);
            // 회전 적용 (remoteRotation 횟수만큼)
            for (int i = 0; i < remoteRotation; i++) {
                t = t.rotateClockwise();
            }
            int[][] shape = t.getShape();

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {

                        int bx = remotePieceX + c;
                        int by = remotePieceY + r;

                        if (bx >= 0 && bx < w && by >= 0 && by < h) {
                            CellView cell = cache.get((by + 1) + "," + (bx + 1));
                            if (cell != null)
                                cell.setBlock("O", t.getBlockStyleClass(), t.getTextStyleClass());
                        }
                    }
                }
            }
        }

        // 3) Next 표시
        Tetromino next = null;
        if (remoteNext > 0) {
            Tetromino.Kind kind = Tetromino.kindForId(remoteNext);
            if (kind != null) {
                next = Tetromino.of(kind);
            }
        }

        drawNext(previewCacheRemote, next);

        // 4) 점수
        scoreLabelRemote.setText(remoteName + "\nScore:\n" + remoteScore);

        // 5) Incoming
        updateIncoming(incomingCacheRemote, remoteIncomingQueue);
    }

    // 공통 그리기 로직 (Local/Remote 공용)
    private void applyCellValue(CellView cell, int v) {
        if (v == 0) {
            cell.setEmpty();
        } else if (v == 1000) {
            cell.setBlock(" ", "block-gray", "tetris-gray-text");
        } else if (v >= 1 && v <= 7) {
            Tetromino.Kind k = Tetromino.kindForId(v);
            cell.setBlock("O", k.getBlockStyleClass(), k.getTextStyleClass());
        } else if (v < 0) {
            cell.setBlock("O", "block-flash", "tetris-flash-text");
        }
    }

    private void drawFallingPiece(Map<String, CellView> cache,
                                  Tetromino cur, int px, int py, int w, int h) {
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
                        if (cell != null)
                            cell.setBlock("O", blockClass, textClass);
                    }
                }
            }
        }
    }

    private void drawGhostPiece(Map<String, CellView> cache,
                                GameEngine engine, int w, int h) {

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
                        if (cell != null)
                            cell.setBlock("O", "block-ghost", "tetris-ghost-text");
                    }
                }
            }
        }
    }

    private void drawNext(Map<String, CellView> cache, Tetromino next) {
        // 4x4 클리어
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = cache.get(r + "," + c);
                if (cell != null) cell.setEmpty();
            }
        }
        if (next == null) return;

        int[][] shape = next.getShape();
        int minR = 4, maxR = -1;
        int minC = 4, maxC = -1;

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

        int h = maxR - minR + 1;
        int w = maxC - minC + 1;

        int offR = (4 - h) / 2;
        int offC = (4 - w) / 2;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int rr = r - minR + offR;
                    int cc = c - minC + offC;

                    CellView cell = cache.get(rr + "," + cc);
                    if (cell != null)
                        cell.setBlock("O",
                                next.getBlockStyleClass(),
                                next.getTextStyleClass());
                }
            }
        }
    }

    private void updateIncoming(Map<String, Label> cache, Queue<int[][]> queue) {
        // 초기화
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label cell = cache.get(r + "," + c);
                if (cell != null) {
                    cell.setText(" ");
                    cell.setStyle("-fx-background-color: transparent; -fx-border-color: #333; -fx-border-width: 0.3;");
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
                            cell.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: #333; -fx-border-width: 0.3; -fx-font-size: 8px;");
                        }
                    }
                }
                row--;
            }
            if (row < 0) break;
        }
    }

    // Board / Preview / Incoming Grid 생성
    private GridPane createBoardGrid(Board board, Map<String, CellView> cache) {
        int w = board.getWidth();
        int h = board.getHeight();

        GridPane grid = new GridPane();
        grid.getStyleClass().add("board-grid");

        double cellSize;
        switch (settings.getWindowSize()) {
            case "SMALL":
                cellSize = 21;
                break;
            case "LARGE":
                cellSize = 39;
                break;
            default: // MEDIUM
                cellSize = 30;
                break;
        }

        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                CellView cell = new CellView(cellSize, settings);

                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1)
                    cell.setBorder();

                grid.add(cell, gx, gy);
                cache.put(gy + "," + gx, cell);
            }
        }
        return grid;
    }

    private GridPane createPreviewGrid(Map<String, CellView> cache) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("preview-grid");

        double cellSize;
        switch (settings.getWindowSize()) {
            case "SMALL":
                cellSize = 21;
                break;
            case "LARGE":
                cellSize = 39;
                break;
            default: // MEDIUM
                cellSize = 30;
                break;
        }

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = new CellView(cellSize, settings);
                grid.add(cell, c, r);
                cache.put(r + "," + c, cell);
            }
        }
        return grid;
    }

    private GridPane createIncomingGrid(Map<String, Label> cache) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-border-color: gray; -fx-background-color: #1a1a1a;");

        double cellSize = ("LARGE".equals(settings.getWindowSize()) ? 22 :
                "MEDIUM".equals(settings.getWindowSize()) ? 17 : 13);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label cell = new Label(" ");
                cell.setAlignment(Pos.CENTER);
                cell.setPrefSize(cellSize, cellSize);
                cell.setStyle("-fx-border-color: #333; -fx-border-width: 0.3;");
                grid.add(cell, c, r);
                cache.put(r + "," + c, cell);
            }
        }
        return grid;
    }

    // CellView 내부 클래스
    private static final class CellView extends StackPane {

        private final Rectangle rect;
        private final Canvas patternCanvas;
        private final Label label;
        private final Settings settings;

        private String currentPattern = null;

        public CellView(double size, Settings settings) {
            this.settings = settings;

            setMinSize(size, size);
            setPrefSize(size, size);
            setMaxSize(size, size);

            setAlignment(Pos.CENTER);

            rect = new Rectangle(size, size);
            rect.getStyleClass().add("cell-rect");
            rect.widthProperty().bind(widthProperty());
            rect.heightProperty().bind(heightProperty());

            patternCanvas = new Canvas(size, size);
            patternCanvas.widthProperty().bind(widthProperty());
            patternCanvas.heightProperty().bind(heightProperty());
            patternCanvas.widthProperty().addListener((a,b,c) -> redrawPattern());
            patternCanvas.heightProperty().addListener((a,b,c) -> redrawPattern());

            label = new Label(" ");
            label.setAlignment(Pos.CENTER);
            label.getStyleClass().add("cell-text");

            getChildren().addAll(rect, patternCanvas, label);

            setEmpty();
        }

        private void clearDynamicStyles() {
            rect.getStyleClass().removeIf(x ->
                    x.startsWith("block-") || x.startsWith("item-") ||
                            x.equals("cell-empty") || x.equals("cell-border")
            );
            label.getStyleClass().removeIf(x ->
                    x.startsWith("tetris-") || x.startsWith("item-") ||
                            x.equals("cell-empty") || x.equals("cell-border")
            );
        }

        public void setEmpty() {
            clearDynamicStyles();
            currentPattern = null;
            clearCanvas();

            if (!rect.getStyleClass().contains("cell-empty"))
                rect.getStyleClass().add("cell-empty");

            if (!label.getStyleClass().contains("cell-empty"))
                label.getStyleClass().add("cell-empty");

            label.setText(" ");
        }

        public void setBorder() {
            clearDynamicStyles();
            currentPattern = null;
            clearCanvas();

            rect.getStyleClass().add("cell-border");
            label.getStyleClass().add("cell-border");

            label.setText("X");
        }

        public void setBlock(String symbol, String blockClass, String textClass) {
            clearDynamicStyles();

            if (blockClass != null && !blockClass.isBlank())
                rect.getStyleClass().add(blockClass);

            if (textClass != null && !textClass.isBlank())
                label.getStyleClass().add(textClass);

            boolean isItem = symbol.matches("[CLWGS]");
            boolean isGhost = blockClass.equals("block-ghost");

            if ((settings.isColorBlindMode() && !isItem) || isGhost) {
                label.setText(" ");
            } else {
                label.setText(symbol);
            }

            if (settings.isColorBlindMode()) {
                applyPattern(blockClass);
            } else {
                currentPattern = null;
                clearCanvas();
            }
        }

        private void applyPattern(String blockClass) {

            switch (blockClass) {
                case "block-I": currentPattern = "diagR"; break;
                case "block-T": currentPattern = "diagL"; break;
                case "block-S": currentPattern = "hori"; break;
                case "block-Z": currentPattern = "diagRwide"; break;
                case "block-J": currentPattern = "vert"; break;
                case "block-L": currentPattern = "diagLwide"; break;
                default: currentPattern = null;
            }
            redrawPattern();
        }

        private void redrawPattern() {
            if (currentPattern == null) {
                clearCanvas();
                return;
            }
            double w = patternCanvas.getWidth();
            double h = patternCanvas.getHeight();
            GraphicsContext gc = patternCanvas.getGraphicsContext2D();
            gc.clearRect(0,0,w,h);
            gc.setStroke(Color.rgb(0,0,0,0.7));
            gc.setLineWidth(1);

            switch (currentPattern) {
                case "hori":
                    for (double y=0; y<h; y+=5) gc.strokeLine(0,y,w,y);
                    break;
                case "vert":
                    for (double x=0; x<w; x+=5) gc.strokeLine(x,0,x,h);
                    break;
                case "diagR":
                    for (double o=-h; o<w+h; o+=5) gc.strokeLine(o,h,o+h,0);
                    break;
                case "diagL":
                    for (double o=-h; o<w+h; o+=5) gc.strokeLine(o,0,o+h,h);
                    break;
                case "diagRwide":
                    for (double o=-h; o<w+h; o+=7) gc.strokeLine(o,h,o+h,0);
                    break;
                case "diagLwide":
                    for (double o=-h; o<w+h; o+=7) gc.strokeLine(o,0,o+h,h);
                    break;
            }
        }

        private void clearCanvas() {
            patternCanvas.getGraphicsContext2D()
                    .clearRect(0,0,
                            patternCanvas.getWidth(), patternCanvas.getHeight());
        }
    }
    
    // 상대방 점수 반환 (게임 오버 시 사용)
    public int getOpponentScore() {
        return remoteScore;
    }
}

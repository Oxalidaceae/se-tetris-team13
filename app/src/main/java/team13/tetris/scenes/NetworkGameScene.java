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

// 네트워크 대전 게임 화면 (VersusGameScene과 동일한 디자인 유지)
public class NetworkGameScene extends BaseGameScene {
    @SuppressWarnings("unused")
    private final SceneManager manager;

    private final GameEngine localEngine; // 실제 엔진

    private Scene scene;

    // Remote 상태 캠싱 (서버에서 받아오는 값)
    private int[][] remoteBoard = null;
    private int remotePieceX = 0;
    private int remotePieceY = 0;
    private int remotePieceType = 0;
    private int remoteRotation = 0;
    private boolean remotePieceIsItem = false;
    private String remotePieceItemType = null;
    private int remotePieceItemBlockIndex = -1;
    private int remoteNext = 0;
    private boolean remoteNextIsItem = false;
    private String remoteNextItemType = null;
    private int remoteNextItemBlockIndex = -1;
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

    // 네트워크 지연 상태 표시
    private final Label networkLagLabel;

    // 이름표
    private final String localName;
    private final String remoteName;

    // Throttle
    private volatile boolean updatePending = false;
    private final boolean timerMode;

    public NetworkGameScene(
            SceneManager manager,
            Settings settings,
            GameEngine localEngine,
            String localName,
            String remoteName,
            boolean timerMode) {
        super(settings);

        this.manager = manager;
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

        // 네트워크 지연 상태 표시 라벨 생성
        networkLagLabel = new Label("The game is being delayed");
        networkLagLabel.getStyleClass().add("label");
        networkLagLabel.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
        networkLagLabel.setVisible(false); // 초기에는 숨김
        networkLagLabel.setManaged(false); // 레이아웃에서 제외

        VBox localBox = new VBox(12);
        HBox localGame = new HBox(12);
        VBox rightL =
                createRightPanel(previewLocal, scoreLabelLocal, incomingLocal, timerLabelLocal);
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
        VBox rightR =
                createRightPanel(previewRemote, scoreLabelRemote, incomingRemote, timerLabelRemote);
        remoteGame.getChildren().addAll(boardGridRemote, rightR);
        remoteBox.getChildren().add(remoteGame);

        // Add both players
        root.getChildren().addAll(localBox, remoteBox);

        scene = new Scene(root);

        // 초기 한번 업데이트
        updateGrid();
    }

    private VBox createRightPanel(
            GridPane previewGrid, Label scoreLabel, GridPane incomingGrid, Label timerLabel) {
        Label incomingLabel = new Label("Incoming:");
        incomingLabel.getStyleClass().add("label");

        String windowSize = settings.getWindowSize();
        VBox rightPanel;

        if (timerMode) {
            if ("SMALL".equals(windowSize)) {
                rightPanel =
                        new VBox(
                                8,
                                previewGrid,
                                scoreLabel,
                                timerLabel,
                                incomingLabel,
                                incomingGrid,
                                networkLagLabel);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            } else if ("MEDIUM".equals(windowSize)) {
                rightPanel =
                        new VBox(
                                10,
                                previewGrid,
                                scoreLabel,
                                timerLabel,
                                incomingLabel,
                                incomingGrid,
                                networkLagLabel);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 30));
            } else { // LARGE
                rightPanel =
                        new VBox(
                                12,
                                previewGrid,
                                scoreLabel,
                                timerLabel,
                                incomingLabel,
                                incomingGrid,
                                networkLagLabel);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            }
        } else {
            if ("SMALL".equals(windowSize)) {
                rightPanel =
                        new VBox(
                                8,
                                previewGrid,
                                scoreLabel,
                                incomingLabel,
                                incomingGrid,
                                networkLagLabel);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            } else if ("MEDIUM".equals(windowSize)) {
                rightPanel =
                        new VBox(
                                10,
                                previewGrid,
                                scoreLabel,
                                incomingLabel,
                                incomingGrid,
                                networkLagLabel);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 30));
            } else { // LARGE
                rightPanel =
                        new VBox(
                                12,
                                previewGrid,
                                scoreLabel,
                                incomingLabel,
                                incomingGrid,
                                networkLagLabel);
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
        Platform.runLater(
                () -> {
                    if (scene != null) scene.getRoot().requestFocus();
                });
    }

    public void updateTimer(int seconds) {
        if (timerMode) {
            Platform.runLater(
                    () -> {
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
            boolean pieceIsItem,
            String pieceItemType,
            int pieceItemBlockIndex,
            int nextPiece,
            boolean nextIsItem,
            String nextItemType,
            int nextItemBlockIndex,
            Queue<int[][]> incoming,
            int score,
            int lines) {
        this.remoteBoard = board;
        this.remotePieceX = pieceX;
        this.remotePieceY = pieceY;
        this.remotePieceType = pieceType;
        this.remoteRotation = rotation;
        this.remotePieceIsItem = pieceIsItem;
        this.remotePieceItemType = pieceItemType;
        this.remotePieceItemBlockIndex = pieceItemBlockIndex;
        this.remoteNext = nextPiece;
        this.remoteNextIsItem = nextIsItem;
        this.remoteNextItemType = nextItemType;
        this.remoteNextItemBlockIndex = nextItemBlockIndex;
        this.remoteIncomingQueue =
                (incoming != null) ? new LinkedList<>(incoming) : new LinkedList<>();
        this.remoteScore = score;

        updateGrid();
    }

    public void updateLocalGrid() {
        updateGrid();
    }

    public void setConnected(boolean connected) {
        // 연결 상태 표시 (필요시 UI 업데이트)
    }

    // 네트워크 지연 상태 표시/숨김
    public void setNetworkLagStatus(boolean isLagging) {
        Platform.runLater(
                () -> {
                    networkLagLabel.setVisible(isLagging);
                    networkLagLabel.setManaged(isLagging); // 레이아웃에 포함/제외
                });
    }

    // Main UI 업데이트 (Local + Remote)
    public void updateGrid() {
        if (updatePending) return;
        updatePending = true;

        Platform.runLater(
                () -> {
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

        // 2) 고스트 미노 표시 (먼저 그림)
        drawGhostPiece(cache, localEngine, w, h);

        // 3) 현재 떨어지는 미노 표시 (고스트 위에 그림)
        Tetromino cur = localEngine.getCurrent();
        if (cur != null)
            drawFallingPiece(cache, cur, localEngine.getPieceX(), localEngine.getPieceY(), w, h);

        // 4) Next 업데이트
        drawNext(previewCacheLocal, localEngine.getNext());

        // 5) 점수
        scoreLabelLocal.setText(localName + "\nScore:\n" + localEngine.getScore());

        // 6) Incoming - 외부에서 updateLocalIncomingGrid 호출로 업데이트
    }

    // Local Player의 incoming grid 업데이트 (NetworkGameController에서 호출)
    public void updateLocalIncomingGrid(Queue<int[][]> incomingQueue) {
        Platform.runLater(
                () -> {
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

        // 2) 떨어지는 미노 (서버에서 받은 데이터로 렌더, 아이템 정보 포함)
        if (remotePieceType > 0) {
            Tetromino.Kind kind = Tetromino.kindForId(remotePieceType);
            if (kind == null) return;

            Tetromino t;
            // 아이템 정보가 있으면 아이템 테트로미노 생성 (rotation 값을 직접 사용)
            if (remotePieceIsItem && remotePieceItemType != null) {
                Tetromino.ItemType itemType = Tetromino.ItemType.valueOf(remotePieceItemType);
                t = Tetromino.item(kind, remoteRotation, itemType, remotePieceItemBlockIndex);
            } else {
                t = Tetromino.of(kind);
                // 일반 블록은 수동 회전 필요
                for (int i = 0; i < remoteRotation; i++) {
                    t = t.rotateClockwise();
                }
            }

            // drawFallingPiece와 동일한 방식으로 렌더링
            int[][] shape = t.getShape();
            String blockClass = t.getBlockStyleClass();
            String textClass = t.getTextStyleClass();
            int blockIndex = 0;

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int bx = remotePieceX + c;
                        int by = remotePieceY + r;

                        if (bx >= 0 && bx < w && by >= 0 && by < h) {
                            CellView cell = cache.get((by + 1) + "," + (bx + 1));
                            if (cell != null) {
                                // 아이템 블록 표시 지원
                                applyItemMinoDisplay(cell, t, blockIndex, blockClass, textClass);
                            }
                        }
                        blockIndex++;
                    }
                }
            }
        }

        // 3) Next 표시 (아이템 정보 포함)
        Tetromino next = null;
        if (remoteNext > 0) {
            Tetromino.Kind kind = Tetromino.kindForId(remoteNext);
            if (kind != null) {
                // 아이템 정보가 있으면 아이템 테트로미노 생성 (Next는 항상 rotation 0)
                if (remoteNextIsItem && remoteNextItemType != null) {
                    Tetromino.ItemType itemType = Tetromino.ItemType.valueOf(remoteNextItemType);
                    next = Tetromino.item(kind, 0, itemType, remoteNextItemBlockIndex);
                } else {
                    next = Tetromino.of(kind);
                }
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
            applyCellEmpty(cell);
        } else if (v == 1000) {
            cell.setBlock(" ", "block-gray", "tetris-gray-text");
        } else if (v >= 100 && v < 200) {
            // COPY 아이템: C 표시
            Tetromino.Kind kind = Tetromino.kindForId(v - 100);
            String blockClass = (kind != null) ? kind.getBlockStyleClass() : "block-item";
            cell.setBlock("C", blockClass, "item-copy-block");
        } else if (v >= 200 && v < 300) {
            // LINE_CLEAR 아이템: L 표시
            Tetromino.Kind kind = Tetromino.kindForId(v - 200);
            String blockClass = (kind != null) ? kind.getBlockStyleClass() : "block-item";
            cell.setBlock("L", blockClass, "item-copy-block");
        } else if (v >= 300 && v < 400) {
            // WEIGHT 아이템: W 표시
            Tetromino.Kind kind = Tetromino.kindForId(v - 300);
            String blockClass = (kind != null) ? kind.getBlockStyleClass() : "block-generic";
            String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
            cell.setBlock("W", blockClass, textClass);
        } else if (v >= 400 && v < 500) {
            // GRAVITY 아이템: G 표시
            Tetromino.Kind kind = Tetromino.kindForId(v - 400);
            String blockClass = (kind != null) ? kind.getBlockStyleClass() : "block-generic";
            String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
            cell.setBlock("G", blockClass, textClass);
        } else if (v >= 500 && v < 600) {
            // SPLIT 아이템: S 표시
            Tetromino.Kind kind = Tetromino.kindForId(v - 500);
            String blockClass = (kind != null) ? kind.getBlockStyleClass() : "block-generic";
            String textClass = (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
            cell.setBlock("S", blockClass, textClass);
        } else if (v >= 1 && v <= 7) {
            Tetromino.Kind k = Tetromino.kindForId(v);
            fillCell(cell, "O", blockClassForKind(k), textClassForKind(k));
        } else if (v < 0) {
            fillCell(cell, "O", "block-flash", "tetris-flash-text");
        }
    }

    private void drawFallingPiece(
            Map<String, CellView> cache, Tetromino cur, int px, int py, int w, int h) {
        int[][] shape = cur.getShape();
        String blockClass = cur.getBlockStyleClass();
        String textClass = cur.getTextStyleClass();

        int blockIndex = 0;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int bx = px + c;
                    int by = py + r;

                    if (bx >= 0 && bx < w && by >= 0 && by < h) {
                        CellView cell = cache.get((by + 1) + "," + (bx + 1));
                        if (cell != null) {
                            // 아이템 블록 표시 지원
                            applyItemMinoDisplay(cell, cur, blockIndex, blockClass, textClass);
                            // // Local Player의 떨어지는 미노도 일반 미노와 같이 테트로미노 종류 글자 표시
                            // String symbol = cur.getKind().name();
                            // fillCell(cell, symbol, blockClass, textClass);
                        }
                    }
                    blockIndex++;
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
                        if (cell != null) fillCell(cell, "O", "block-ghost", "tetris-ghost-text");
                    }
                }
            }
        }
    }

    // Remote Player용 Next 렌더링 (아이템 정보 없이 O로만 표시)
    private void drawRemoteNext(Map<String, CellView> cache, Tetromino next) {
        // 4x4 클리어
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                CellView cell = cache.get(r + "," + c);
                if (cell != null) applyCellEmpty(cell);
            }
        }
        if (next == null) return;

        int[][] shape = next.getShape();
        String blockClass = next.getBlockStyleClass();
        String textClass = next.getTextStyleClass();

        for (int r = 0; r < shape.length && r < 4; r++) {
            for (int c = 0; c < shape[r].length && c < 4; c++) {
                if (shape[r][c] != 0) {
                    CellView cell = cache.get(r + "," + c);
                    if (cell != null) {
                        // Remote는 아이템 정보가 없으므로 O로 표시
                        cell.setBlock("O", blockClass, textClass);
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
                if (cell != null) applyCellEmpty(cell);
            }
        }
        if (next == null) return;

        int[][] shape = next.getShape();
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

        int h = maxR - minR + 1;
        int w = maxC - minC + 1;
        int offR = (4 - h) / 2;
        int offC = (4 - w) / 2;

        int blockIndex = 0;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int rr = r - minR + offR;
                    int cc = c - minC + offC;
                    CellView cell = cache.get(rr + "," + cc);
                    if (cell != null) {
                        // 아이템 블록 표시 지원
                        applyItemMinoDisplay(
                                cell,
                                next,
                                blockIndex,
                                next.getBlockStyleClass(),
                                next.getTextStyleClass());
                    }
                    blockIndex++;
                }
            }
        }
    }

    // 아이템 미노 표시 로직 (VersusGameScene과 동일)
    private void applyItemMinoDisplay(
            CellView cell,
            Tetromino tetromino,
            int blockIndex,
            String blockClass,
            String textClass) {
        if (tetromino.isItemPiece()) {
            if (tetromino.getItemType() == Tetromino.ItemType.COPY
                    && blockIndex == tetromino.getCopyBlockIndex()) {
                // COPY 아이템: 특정 블록만 C 표시, 원래 블록 색상 유지
                cell.setBlock("C", blockClass, "item-copy-block");
            } else if (tetromino.getItemType() == Tetromino.ItemType.LINE_CLEAR
                    && blockIndex == tetromino.getLineClearBlockIndex()) {
                // LINE_CLEAR 아이템: 특정 블록만 L 표시, 원래 블록 색상 유지
                cell.setBlock("L", blockClass, "item-copy-block");
            } else if (tetromino.getItemType() == Tetromino.ItemType.WEIGHT) {
                // WEIGHT 아이템: 모든 블록 W 표시
                cell.setBlock("W", blockClass, textClass);
            } else if (tetromino.getItemType() == Tetromino.ItemType.GRAVITY) {
                // GRAVITY 아이템: 모든 블록 G 표시
                cell.setBlock("G", blockClass, textClass);
            } else if (tetromino.getItemType() == Tetromino.ItemType.SPLIT) {
                // SPLIT 아이템: 모든 블록 S 표시
                cell.setBlock("S", blockClass, textClass);
            } else {
                // 기타 아이템 블록은 일반 미노와 동일하게 표시
                String symbol = (tetromino.getKind() != null) ? tetromino.getKind().name() : "O";
                cell.setBlock(symbol, blockClass, textClass);
            }
        } else {
            // 일반 미노는 테트로미노 종류 글자 표시
            String symbol = (tetromino.getKind() != null) ? tetromino.getKind().name() : "O";
            cell.setBlock(symbol, blockClass, textClass);
        }
    }

    private void updateIncoming(Map<String, Label> cache, Queue<int[][]> queue) {
        // 초기화
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

                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) cell.setBorder();

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
        grid.getStyleClass().add("incoming-grid");
        grid.setStyle(
                "-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #1a1a1a;");
        grid.setHgap(0);
        grid.setVgap(0);

        double cellSize =
                ("LARGE".equals(settings.getWindowSize())
                        ? 22
                        : "MEDIUM".equals(settings.getWindowSize()) ? 17 : 13);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label cell = new Label(" ");
                cell.setAlignment(Pos.CENTER);
                cell.setPrefSize(cellSize, cellSize);
                cell.setMinSize(cellSize, cellSize);
                cell.setMaxSize(cellSize, cellSize);
                cell.setStyle("-fx-border-color: #333; -fx-border-width: 0.3;");
                grid.add(cell, c, r);
                cache.put(r + "," + c, cell);
            }
        }
        return grid;
    }

    // 상대방 점수 반환 (게임 오버 시 사용)
    public int getOpponentScore() {
        return remoteScore;
    }
}

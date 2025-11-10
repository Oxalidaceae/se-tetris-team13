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
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

import java.util.HashMap;
import java.util.Map;

public class VersusGameScene {
    private final SceneManager manager;
    private final Settings settings;
    private GameEngine engine1; // Player 1
    private GameEngine engine2; // Player 2
    private final HBox root;
    private Scene scene;
    private final boolean timerMode;
    private Label timerLabel1; // Player 1 타이머 표시용
    private Label timerLabel2; // Player 2 타이머 표시용
    
    // UI 업데이트 throttle을 위한 변수
    private volatile boolean updatePending = false;
    
    // 셀 캐싱 (성능 최적화)
    private final Map<String, Label> cellCache1 = new HashMap<>();
    private final Map<String, Label> cellCache2 = new HashMap<>();
    private final Map<String, Label> previewCache1 = new HashMap<>();
    private final Map<String, Label> previewCache2 = new HashMap<>();
    private final Map<String, Label> incomingCache1 = new HashMap<>();
    private final Map<String, Label> incomingCache2 = new HashMap<>();
    
    // Player 1 UI
    private final GridPane boardGrid1;
    private final GridPane previewGrid1;
    private final GridPane incomingGrid1; // 넘어올 블록 표시
    private final Label scoreLabel1;
    
    // Player 2 UI
    private final GridPane boardGrid2;
    private final GridPane previewGrid2;
    private final GridPane incomingGrid2; // 넘어올 블록 표시
    private final Label scoreLabel2;

    public VersusGameScene(
            SceneManager manager,
            Settings settings,
            GameEngine engine1,
            GameEngine engine2,
            boolean timerMode) {
        this.manager = manager;
        this.settings = settings;
        this.engine1 = engine1;
        this.engine2 = engine2;
        this.timerMode = timerMode;

        root = new HBox(20);
        root.getStyleClass().add("game-root");

        // Player 1 보드
        Board board1 = engine1.getBoard();
        boardGrid1 = createBoardGrid(board1);
        previewGrid1 = createPreviewGrid();
        incomingGrid1 = createIncomingGrid(); // 넘어올 블록 표시
        scoreLabel1 = new Label("Player 1\nScore: 0");
        scoreLabel1.getStyleClass().add("score-label");
        
        Label incomingLabel1 = new Label("Incoming:");
        incomingLabel1.getStyleClass().add("label");

        VBox player1Panel = new VBox(12);
        HBox player1Game = new HBox(12);
        
        // 타이머 모드인 경우 Player 1 타이머 추가
        if (timerMode) {
            timerLabel1 = new Label("Time: 120");
            timerLabel1.getStyleClass().add("label-title");
            timerLabel1.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
            VBox right1 = new VBox(8, previewGrid1, scoreLabel1, incomingLabel1, incomingGrid1, timerLabel1);
            right1.getStyleClass().add("right-panel");
            player1Game.getChildren().addAll(boardGrid1, right1);
        } else {
            VBox right1 = new VBox(8, previewGrid1, scoreLabel1, incomingLabel1, incomingGrid1);
            right1.getStyleClass().add("right-panel");
            player1Game.getChildren().addAll(boardGrid1, right1);
        }
        player1Panel.getChildren().add(player1Game);

        // Player 2 보드
        Board board2 = engine2.getBoard();
        boardGrid2 = createBoardGrid(board2);
        previewGrid2 = createPreviewGrid();
        incomingGrid2 = createIncomingGrid(); // 넘어올 블록 표시
        scoreLabel2 = new Label("Player 2\nScore: 0");
        scoreLabel2.getStyleClass().add("score-label");
        
        Label incomingLabel2 = new Label("Incoming:");
        incomingLabel2.getStyleClass().add("label");

        VBox player2Panel = new VBox(12);
        HBox player2Game = new HBox(12);
        
        // 타이머 모드인 경우 Player 2 타이머 추가
        if (timerMode) {
            timerLabel2 = new Label("Time: 120");
            timerLabel2.getStyleClass().add("label-title");
            timerLabel2.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
            VBox right2 = new VBox(8, previewGrid2, scoreLabel2, incomingLabel2, incomingGrid2, timerLabel2);
            right2.getStyleClass().add("right-panel");
            player2Game.getChildren().addAll(boardGrid2, right2);
        } else {
            VBox right2 = new VBox(8, previewGrid2, scoreLabel2, incomingLabel2, incomingGrid2);
            right2.getStyleClass().add("right-panel");
            player2Game.getChildren().addAll(boardGrid2, right2);
        }
        player2Panel.getChildren().add(player2Game);

        HBox.setHgrow(player1Panel, Priority.ALWAYS);
        HBox.setHgrow(player2Panel, Priority.ALWAYS);
        
        root.getChildren().addAll(player1Panel, player2Panel);
        
        // incoming grid 초기화 (빈 상태로)
        updateIncomingGrid(1, new java.util.LinkedList<>());
        updateIncomingGrid(2, new java.util.LinkedList<>());
        
        updateGrid();
    }

    private GridPane createBoardGrid(Board board) {
        int w = board.getWidth();
        int h = board.getHeight();

        GridPane grid = new GridPane();
        grid.getStyleClass().add("board-grid");
        
        // 캐시 맵 선택
        Map<String, Label> cache = (grid == boardGrid1 || boardGrid1 == null) ? cellCache1 : cellCache2;

        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                Label cell = makeCellLabel();

                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setText("X");
                    applyCellBorder(cell);
                }

                grid.add(cell, gx, gy);
                // 캐시에 저장
                cache.put(gy + "," + gx, cell);
            }
        }

        return grid;
    }

    private GridPane createPreviewGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("preview-grid");
        
        // 캐시 맵 선택
        Map<String, Label> cache = (grid == previewGrid1 || previewGrid1 == null) ? previewCache1 : previewCache2;

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                Label cell = makeCellLabel();
                grid.add(cell, c, r);
                // 캐시에 저장
                cache.put(r + "," + c, cell);
            }
        }

        return grid;
    }

    private GridPane createIncomingGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("incoming-grid");
        grid.setStyle("-fx-border-color: gray; -fx-border-width: 2; -fx-background-color: #1a1a1a;");
        
        // 캐시 맵 선택
        Map<String, Label> cache = (grid == incomingGrid1 || incomingGrid1 == null) ? incomingCache1 : incomingCache2;

        // 10x10 크기로 넘어올 블록을 보여줌
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label cell = makeCellLabel();
                // 셀 크기를 작게 조정
                cell.setMinSize(20, 20);
                cell.setMaxSize(20, 20);
                cell.setPrefSize(20, 20);
                // 초기에는 테두리만
                cell.setStyle("-fx-border-color: #333; -fx-border-width: 0.5; -fx-background-color: transparent;");
                grid.add(cell, c, r);
                // 캐시에 저장
                cache.put(r + "," + c, cell);
            }
        }

        return grid;
    }

    private Label makeCellLabel() {
        Label lbl = new Label(" ");
        lbl.setAlignment(Pos.CENTER);
        lbl.getStyleClass().add("cell");
        return lbl;
    }

    public Scene createScene() {
        // Settings에 따라 동적으로 Scene 크기 설정
        int sceneWidth;
        int sceneHeight;
        
        switch (settings.getWindowSize()) {
            case "SMALL" -> {
                sceneWidth = 800;
                sceneHeight = 500;
            }
            case "LARGE" -> {
                sceneWidth = 1600;
                sceneHeight = 900;
            }
            default -> {  // MEDIUM
                sceneWidth = 1200;
                sceneHeight = 700;
            }
        }
        
        this.scene = new Scene(root, sceneWidth, sceneHeight);
        return scene;
    }

    public Scene getScene() {
        return scene;
    }

    public void setEngine1(GameEngine engine) {
        this.engine1 = engine;
    }

    public void setEngine2(GameEngine engine) {
        this.engine2 = engine;
    }

    public void requestFocus() {
        Platform.runLater(() -> {
            if (scene != null) scene.getRoot().requestFocus();
        });
    }

    public void updateGrid() {
        // 이미 업데이트가 예약되어 있으면 스킵
        if (updatePending) {
            return;
        }
        updatePending = true;
        
        Platform.runLater(() -> {
            updatePlayerGrid(engine1, boardGrid1, previewGrid1, scoreLabel1, "Player 1");
            updatePlayerGrid(engine2, boardGrid2, previewGrid2, scoreLabel2, "Player 2");
            updatePending = false;
        });
    }

    public void updateTimer(int remainingSeconds) {
        if (timerMode && timerLabel1 != null && timerLabel2 != null) {
            Platform.runLater(() -> {
                timerLabel1.setText("Time: " + remainingSeconds);
                timerLabel2.setText("Time: " + remainingSeconds);
                
                // 시간이 30초 이하면 빨간색으로 표시
                if (remainingSeconds <= 30) {
                    timerLabel1.setStyle("-fx-font-size: 20px; -fx-text-fill: red;");
                    timerLabel2.setStyle("-fx-font-size: 20px; -fx-text-fill: red;");
                }
            });
        }
    }

    public void updateIncomingGrid(int playerNumber, java.util.Queue<int[][]> incomingBlocks) {
        Map<String, Label> incomingCache = (playerNumber == 1) ? incomingCache1 : incomingCache2;
        
        Platform.runLater(() -> {
            // 모든 셀 초기화
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    // 캐시에서 직접 가져오기
                    Label cell = incomingCache.get(r + "," + c);
                    if (cell != null) {
                        cell.setText(" ");
                        applyCellEmpty(cell);
                    }
                }
            }
            
            // 큐에 있는 모든 공격 패턴을 아래에서부터 쌓아서 표시 (최대 10줄)
            // 먼저 들어온 것이 맨 아래, 나중에 들어온 것이 위에 표시
            if (!incomingBlocks.isEmpty()) {
                int currentRow = 9; // 맨 아래부터 시작
                
                // Queue를 리스트로 변환 후 역순으로 순회 (나중 것을 먼저 그림)
                java.util.List<int[][]> blockList = new java.util.ArrayList<>(incomingBlocks);
                for (int i = blockList.size() - 1; i >= 0; i--) {
                    int[][] pattern = blockList.get(i);
                    // 패턴을 아래에서부터 위로 그림
                    for (int r = pattern.length - 1; r >= 0 && currentRow >= 0; r--) {
                        for (int c = 0; c < 10; c++) {
                            if (pattern[r][c] != 0) {
                                // 캐시에서 직접 가져오기
                                Label cell = incomingCache.get(currentRow + "," + c);
                                if (cell != null) {
                                    cell.setText("■");
                                    cell.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: #333; -fx-border-width: 0.5;");
                                }
                            }
                        }
                        currentRow--;
                    }
                    
                    // 10줄을 초과하면 중단
                    if (currentRow < 0) break;
                }
            }
        });
    }

    private void updatePlayerGrid(GameEngine engine, GridPane boardGrid, GridPane previewGrid, Label scoreLabel, String playerName) {
        if (engine == null) return;

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();
        
        // 캐시 선택
        Map<String, Label> boardCache = (boardGrid == boardGrid1) ? cellCache1 : cellCache2;
        Map<String, Label> prevCache = (previewGrid == previewGrid1) ? previewCache1 : previewCache2;
        
        // Platform.runLater 제거 - 이미 updateGrid()에서 처리됨
        // 보드 업데이트
        for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = b.getCell(x, y);
                    // 캐시에서 직접 가져오기 (getNodeByRowColumnIndex 대신)
                    Label cell = boardCache.get((y + 1) + "," + (x + 1));
                    if (cell == null) continue;

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
                    } else if (val >= 1000) {
                        // 상대방에게서 넘어온 회색 블록
                        cell.setText("O");
                        applyCellBlockText(cell, "tetris-gray-text");
                    } else {
                        Tetromino.Kind kind = Tetromino.kindForId(val);
                        if (kind != null) {
                            cell.setText("O");
                            applyCellBlockText(cell, kind.getTextStyleClass());
                        }
                    }
                }
            }

            // 현재 떨어지는 블록 표시
            Tetromino current = engine.getCurrent();
            if (current != null) {
                int[][] shape = current.getShape();
                int px = engine.getPieceX();
                int py = engine.getPieceY();
                String textClass = current.getTextStyleClass();
                int blockIndex = 0;
                
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            int bx = px + c;
                            int by = py + r;
                            if (bx >= 0 && bx < w && by >= 0 && by < h) {
                                // 캐시에서 직접 가져오기
                                Label cell = boardCache.get((by + 1) + "," + (bx + 1));
                                if (cell != null) {
                                    // 아이템 미노 표시 로직
                                    if (current.isItemPiece()) {
                                        if (current.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY
                                                && blockIndex == current.getCopyBlockIndex()) {
                                            // COPY 아이템: 특정 블록만 C 표시
                                            cell.setText("C");
                                            applyCellBlockText(cell, "item-copy-block");
                                        } else if (current.getItemType() == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR
                                                && blockIndex == current.getLineClearBlockIndex()) {
                                            // LINE_CLEAR 아이템: 특정 블록만 L 표시
                                            cell.setText("L");
                                            applyCellBlockText(cell, "item-copy-block");
                                        } else if (current.getItemType() == team13.tetris.game.model.Tetromino.ItemType.WEIGHT) {
                                            // WEIGHT 아이템: 모든 블록 W 표시
                                            cell.setText("W");
                                            applyCellBlockText(cell, textClass);
                                        } else if (current.getItemType() == team13.tetris.game.model.Tetromino.ItemType.GRAVITY) {
                                            // GRAVITY 아이템: 모든 블록 G 표시
                                            cell.setText("G");
                                            applyCellBlockText(cell, textClass);
                                        } else if (current.getItemType() == team13.tetris.game.model.Tetromino.ItemType.SPLIT) {
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
                            }
                            blockIndex++;
                        }
                    }
                }
            }

            // Next 블록 표시
            Tetromino next = engine.getNext();
            if (next != null) {
                int[][] nextShape = next.getShape();
                String textClass = next.getTextStyleClass();
                int blockIndex = 0;
                
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        // 캐시에서 직접 가져오기
                        Label cell = prevCache.get(r + "," + c);
                        if (cell != null) {
                            if (r < nextShape.length && c < nextShape[r].length && nextShape[r][c] != 0) {
                                // 아이템 미노 표시 로직
                                if (next.isItemPiece()) {
                                    if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY
                                            && blockIndex == next.getCopyBlockIndex()) {
                                        // COPY 아이템: 특정 블록만 C 표시
                                        cell.setText("C");
                                        applyCellBlockText(cell, "item-copy-block");
                                    } else if (next.getItemType() == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR
                                            && blockIndex == next.getLineClearBlockIndex()) {
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
                            } else {
                                cell.setText(" ");
                                applyCellEmpty(cell);
                            }
                        }
                    }
                }
            }

            // 점수 업데이트
            scoreLabel.setText(playerName + "\nScore: " + engine.getScore());
    }

    private void applyCellEmpty(Label cell) {
        cell.getStyleClass().removeAll("tetris-i-text", "tetris-o-text", "tetris-t-text",
                "tetris-s-text", "tetris-z-text", "tetris-j-text", "tetris-l-text",
                "tetris-generic-text", "tetris-flash-text", "item-copy-block",
                "tetris-border", "tetris-gray-text");
        cell.getStyleClass().add("cell");
        // incoming 그리드의 빈 셀은 투명 배경으로
        cell.setStyle("-fx-border-color: #333; -fx-border-width: 0.5; -fx-background-color: transparent;");
    }

    private void applyCellBorder(Label cell) {
        cell.getStyleClass().removeAll("tetris-i-text", "tetris-o-text", "tetris-t-text",
                "tetris-s-text", "tetris-z-text", "tetris-j-text", "tetris-l-text",
                "tetris-generic-text", "tetris-flash-text", "item-copy-block",
                "tetris-gray-text");
        cell.getStyleClass().add("tetris-border");
    }

    private void applyCellBlockText(Label cell, String styleClass) {
        cell.getStyleClass().removeAll("tetris-i-text", "tetris-o-text", "tetris-t-text",
                "tetris-s-text", "tetris-z-text", "tetris-j-text", "tetris-l-text",
                "tetris-generic-text", "tetris-flash-text", "item-copy-block",
                "tetris-border", "tetris-gray-text");
        cell.getStyleClass().add(styleClass);
    }
}

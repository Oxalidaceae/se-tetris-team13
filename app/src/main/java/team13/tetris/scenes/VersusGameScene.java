package team13.tetris.scenes;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
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

public class VersusGameScene extends BaseGameScene {
    // 향후 확장을 위해 보존
    @SuppressWarnings("unused")
    private final SceneManager manager;

    private GameEngine engine1; // Player 1
    private GameEngine engine2; // Player 2
    private final HBox root;
    private Scene scene;
    private final boolean timerMode;
    private Label timerLabel1; // Player 1 타이머 표시용
    private Label timerLabel2; // Player 2 타이머 표시용
    private boolean timeWarningSoundPlayed = false; // 30초 경고음 재생 여부

    // UI 업데이트 throttle을 위한 변수
    private volatile boolean updatePending = false;

    // incoming grid용 캐시만 유지
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
        super(settings);
        this.manager = manager;
        this.engine1 = engine1;
        this.engine2 = engine2;
        this.timerMode = timerMode;

        root = new HBox(20);
        root.getStyleClass().add("game-root");

        // Player 1 보드 초기화
        Board board1 = engine1.getBoard();
        boardGrid1 = createBoardGrid(board1);
        previewGrid1 = createPreviewGrid();
        incomingGrid1 = createIncomingGrid();
        scoreLabel1 = new Label("Player 1\nScore: 0");

        // Player 2 보드 초기화
        Board board2 = engine2.getBoard();
        boardGrid2 = createBoardGrid(board2);
        previewGrid2 = createPreviewGrid();
        incomingGrid2 = createIncomingGrid();
        scoreLabel2 = new Label("Player 2\nScore: 0");

        // Player 1과 Player 2 UI 생성
        VBox player1Panel = createPlayerUI(1, engine1, timerMode);
        VBox player2Panel = createPlayerUI(2, engine2, timerMode);

        HBox.setHgrow(player1Panel, Priority.ALWAYS);
        HBox.setHgrow(player2Panel, Priority.ALWAYS);

        root.getChildren().addAll(player1Panel, player2Panel);

        // incoming grid 초기화 (빈 상태로)
        updateIncomingGrid(1, new java.util.LinkedList<>());
        updateIncomingGrid(2, new java.util.LinkedList<>());

        updateGrid();
    }

    // 플레이어별 UI를 생성하는 공통 메서드
    private VBox createPlayerUI(int playerNumber, GameEngine engine, boolean timerMode) {
        // 플레이어별 변수 설정
        GridPane boardGrid, previewGrid, incomingGrid;
        Label scoreLabel, timerLabel = null;

        if (playerNumber == 1) {
            boardGrid = boardGrid1;
            previewGrid = previewGrid1;
            incomingGrid = incomingGrid1;
            scoreLabel = scoreLabel1;
            if (timerMode) {
                if (timerLabel1 == null) timerLabel1 = new Label("Time: 120");
                timerLabel = timerLabel1;
            }
        } else {
            boardGrid = boardGrid2;
            previewGrid = previewGrid2;
            incomingGrid = incomingGrid2;
            scoreLabel = scoreLabel2;
            if (timerMode) {
                if (timerLabel2 == null) timerLabel2 = new Label("Time: 120");
                timerLabel = timerLabel2;
            }
        }

        // 공통 스타일 설정
        scoreLabel.getStyleClass().add("score-label");
        scoreLabel.setPrefWidth(150);

        Label incomingLabel = new Label("Incoming:");
        incomingLabel.getStyleClass().add("label");

        // 화면 크기에 따라 incoming 글자 크기 조정
        String incomingFontSize = getIncomingFontSize();
        incomingLabel.setStyle("-fx-font-size: " + incomingFontSize + ";");

        VBox playerPanel = new VBox(12);
        HBox playerGame = new HBox(12);

        // 타이머 모드인 경우 타이머 라벨 설정
        if (timerMode && timerLabel != null) {
            timerLabel.getStyleClass().add("label-title");
            timerLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        }

        // 화면 크기별 레이아웃 구성
        VBox rightPanel =
                createRightPanel(
                        previewGrid,
                        scoreLabel,
                        timerLabel,
                        incomingLabel,
                        incomingGrid,
                        timerMode);
        playerGame.getChildren().addAll(boardGrid, rightPanel);
        playerPanel.getChildren().add(playerGame);

        return playerPanel;
    }

    // 화면 크기에 따른 incoming 폰트 크기 반환
    private String getIncomingFontSize() {
        String windowSize = settings.getWindowSize();
        if ("MEDIUM".equals(windowSize)) {
            return "12px";
        } else if ("LARGE".equals(windowSize)) {
            return "16px";
        }
        return "10px"; // 기본 크기
    }

    // 오른쪽 패널 (preview, score, timer, incoming) 생성
    private VBox createRightPanel(
            GridPane previewGrid,
            Label scoreLabel,
            Label timerLabel,
            Label incomingLabel,
            GridPane incomingGrid,
            boolean timerMode) {
        String windowSize = settings.getWindowSize();
        VBox rightPanel;

        if (timerMode && timerLabel != null) {
            // 타이머 모드: 타이머 포함
            if ("SMALL".equals(windowSize)) {
                rightPanel =
                        new VBox(
                                6,
                                previewGrid,
                                scoreLabel,
                                timerLabel,
                                incomingLabel,
                                incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            } else if ("MEDIUM".equals(windowSize)) {
                rightPanel =
                        new VBox(
                                8,
                                previewGrid,
                                scoreLabel,
                                timerLabel,
                                incomingLabel,
                                incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 30));
            } else { // LARGE
                rightPanel =
                        new VBox(
                                10,
                                previewGrid,
                                scoreLabel,
                                timerLabel,
                                incomingLabel,
                                incomingGrid);
                HBox.setMargin(rightPanel, new Insets(0, 0, 0, 50));
            }
        } else {
            // 일반 모드: 타이머 제외
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

    private GridPane createIncomingGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("incoming-grid");
        grid.setStyle(
                "-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #1a1a1a;");
        grid.setHgap(0);
        grid.setVgap(0);

        // 캐시 맵 선택
        Map<String, Label> cache =
                (grid == incomingGrid1 || incomingGrid1 == null) ? incomingCache1 : incomingCache2;

        // 화면 크기에 따라 셀 크기 조정
        double cellSize = 25; // LARGE 크기
        double fontSize = 30; // LARGE 폰트 크기

        if ("SMALL".equals(settings.getWindowSize())) {
            cellSize = 13;
            fontSize = 8;
        } else if ("MEDIUM".equals(settings.getWindowSize())) {
            cellSize = 15;
            fontSize = 9;
        }

        // 10x10 크기로 넘어올 블록을 보여줌
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                // incoming 전용 셀 생성
                Label cell = new Label(" ");
                cell.setAlignment(Pos.CENTER);
                cell.setMinSize(cellSize, cellSize);
                cell.setMaxSize(cellSize, cellSize);
                cell.setPrefSize(cellSize, cellSize);
                cell.setStyle(
                        "-fx-border-color: #333; -fx-border-width: 0.3; -fx-background-color: transparent; -fx-font-size: "
                                + fontSize
                                + "px; -fx-padding: 0;");
                grid.add(cell, c, r);
                // 캐시에 저장
                cache.put(r + "," + c, cell);
            }
        }

        return grid;
    }

    public Scene createScene() {
        // SceneManager에서 이미 stage 크기를 설정했으므로 Scene만 생성
        this.scene = new Scene(root);

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
        Platform.runLater(
                () -> {
                    if (scene != null) scene.getRoot().requestFocus();
                });
    }

    public void updateGrid() {
        // 이미 업데이트가 예약되어 있으면 스킵
        if (updatePending) return;
        updatePending = true;

        Platform.runLater(
                () -> {
                    updatePlayerGrid(engine1, boardGrid1, previewGrid1, scoreLabel1, "Player 1");
                    updatePlayerGrid(engine2, boardGrid2, previewGrid2, scoreLabel2, "Player 2");
                    updatePending = false;
                });
    }

    public void updateTimer(int remainingSeconds) {
        if (timerMode && timerLabel1 != null && timerLabel2 != null) {
            Platform.runLater(
                    () -> {
                        timerLabel1.setText("Time: " + remainingSeconds);
                        timerLabel2.setText("Time: " + remainingSeconds);

                        // 시간이 30초 이하면 빨간색으로 표시
                        if (remainingSeconds <= 30) {
                            timerLabel1.setStyle("-fx-font-size: 20px; -fx-text-fill: red;");
                            timerLabel2.setStyle("-fx-font-size: 20px; -fx-text-fill: red;");
                            
                            // 30초가 되는 순간 한 번만 효과음 재생
                            if (!timeWarningSoundPlayed) {
                                team13.tetris.audio.SoundManager.getInstance().playEffect("time");
                                timeWarningSoundPlayed = true;
                            }
                        }
                    });
        }
    }

    public void updateIncomingGrid(int playerNumber, java.util.Queue<int[][]> incomingBlocks) {
        Map<String, Label> incomingCache = (playerNumber == 1) ? incomingCache1 : incomingCache2;

        Platform.runLater(
                () -> {
                    // 모든 셀 초기화 (incoming grid 전용 작은 스타일)
                    for (int r = 0; r < 10; r++) {
                        for (int c = 0; c < 10; c++) {
                            // 캐시에서 직접 가져오기
                            Label cell = incomingCache.get(r + "," + c);
                            if (cell != null) {
                                cell.setText(" ");
                                // incoming grid 전용 빈 셀 스타일
                                cell.setStyle(
                                        "-fx-border-color: #333; -fx-border-width: 0.3; -fx-background-color: transparent; -fx-font-size: 6px; -fx-padding: 0;");
                            }
                        }
                    }

                    // 큐에 있는 모든 공격 패턴을 아래에서부터 쌓아서 표시 (최대 10줄)
                    // 먼저 들어온 것이 맨 아래, 나중에 들어온 것이 위에 표시
                    if (!incomingBlocks.isEmpty()) {
                        int currentRow = 9; // 맨 아래부터 시작

                        // Queue를 리스트로 변환 후 역순으로 순회 (나중 것을 먼저 그림)
                        java.util.List<int[][]> blockList =
                                new java.util.ArrayList<>(incomingBlocks);
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
                                            cell.setStyle(
                                                    "-fx-background-color: gray; -fx-text-fill: white; -fx-border-color: #333; -fx-border-width: 0.3; -fx-font-size: 6px; -fx-padding: 0;");
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

    private void updatePlayerGrid(
            GameEngine engine,
            GridPane boardGrid,
            GridPane previewGrid,
            Label scoreLabel,
            String playerName) {
        if (engine == null) return;

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();

        // 보드 업데이트
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = b.getCell(x, y);
                // getNodeByRowColumnIndex 사용
                CellView cell = (CellView) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                if (cell == null) continue;

                if (val == 0) {
                    applyCellEmpty(cell);
                } else if (val < 0) {
                    applyCellBlockText(cell, "tetris-flash-text");
                } else if (val >= 100 && val < 200) {
                    // COPY 아이템: C 표시
                    Tetromino.Kind kind = Tetromino.kindForId(val - 100);
                    String blockClass = (kind != null) ? "block-" + kind.name() : "block-item";
                    cell.setBlock("C", blockClass, "item-copy-block");
                } else if (val >= 200 && val < 300) {
                    // LINE_CLEAR 아이템: L 표시
                    Tetromino.Kind kind = Tetromino.kindForId(val - 200);
                    String blockClass = (kind != null) ? "block-" + kind.name() : "block-item";
                    cell.setBlock("L", blockClass, "item-copy-block");
                } else if (val >= 300 && val < 400) {
                    // WEIGHT 아이템
                    Tetromino.Kind kind = Tetromino.kindForId(val - 300);
                    String blockClass =
                            (kind != null) ? kind.getBlockStyleClass() : "block-generic";
                    String textClass =
                            (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                    cell.setBlock("W", blockClass, textClass);
                } else if (val >= 400 && val < 500) {
                    // GRAVITY 아이템
                    Tetromino.Kind kind = Tetromino.kindForId(val - 400);
                    String blockClass =
                            (kind != null) ? kind.getBlockStyleClass() : "block-generic";
                    String textClass =
                            (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                    cell.setBlock("G", blockClass, textClass);
                } else if (val >= 500 && val < 600) {
                    // SPLIT 아이템
                    Tetromino.Kind kind = Tetromino.kindForId(val - 500);
                    String blockClass =
                            (kind != null) ? kind.getBlockStyleClass() : "block-generic";
                    String textClass =
                            (kind != null) ? kind.getTextStyleClass() : "tetris-generic-text";
                    cell.setBlock("S", blockClass, textClass);
                } else if (val >= 1000) {
                    // 상대방에게서 넘어온 회색 블록
                    applyCellBlockText(cell, "tetris-gray-text");
                } else if (val >= 1 && val <= 7) {
                    // 일반 블록 (1~7)
                    Tetromino.Kind kind = Tetromino.kindForId(val);
                    if (kind != null) {
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
            int ghostY = engine.getGhostY();
            String textClass = current.getTextStyleClass();
            String blockClass = current.getBlockStyleClass();

            // 고스트 블록 그리기 (현재 블록보다 먼저 그려서 뒤에 표시됨)
            renderGhostBlock(shape, px, py, ghostY, w, h, boardGrid);

            int blockIndex = 0;

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int bx = px + c;
                        int by = py + r;
                        if (bx >= 0 && bx < w && by >= 0 && by < h) {
                            // getNodeByRowColumnIndex 사용
                            CellView cell =
                                    (CellView) getNodeByRowColumnIndex(by + 1, bx + 1, boardGrid);
                            if (cell != null) {
                                // 아이템 미노 표시 로직
                                applyItemMinoDisplay(
                                        cell, current, blockIndex, blockClass, textClass);
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
            String blockClass = next.getBlockStyleClass();

            // 블록의 실제 크기 계산 (솔로 모드와 동일)
            int minRow = 4, maxRow = -1, minCol = 4, maxCol = -1;
            for (int r = 0; r < nextShape.length; r++) {
                for (int c = 0; c < nextShape[r].length; c++) {
                    if (nextShape[r][c] != 0) {
                        if (r < minRow) minRow = r;
                        if (r > maxRow) maxRow = r;
                        if (c < minCol) minCol = c;
                        if (c > maxCol) maxCol = c;
                    }
                }
            }

            // 블록을 4x4 그리드 중앙에 배치하기 위한 오프셋 계산 (솔로 모드와 동일)
            int blockHeight = maxRow - minRow + 1;
            int blockWidth = maxCol - minCol + 1;
            int offsetRow = (4 - blockHeight) / 2;
            int offsetCol = (4 - blockWidth) / 2;

            int blockIndex = 0;

            // 먼저 모든 셀을 비움
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    CellView cell = (CellView) getNodeByRowColumnIndex(r, c, previewGrid);
                    if (cell != null) {
                        applyCellEmpty(cell);
                    }
                }
            }

            // 블록 그리기
            for (int r = 0; r < nextShape.length; r++) {
                for (int c = 0; c < nextShape[r].length; c++) {
                    if (nextShape[r][c] != 0) {
                        // 중앙 정렬을 위해 오프셋 적용 (솔로 모드와 동일)
                        int displayRow = r - minRow + offsetRow;
                        int displayCol = c - minCol + offsetCol;

                        // getNodeByRowColumnIndex 사용
                        CellView cell =
                                (CellView)
                                        getNodeByRowColumnIndex(
                                                displayRow, displayCol, previewGrid);
                        if (cell != null) {
                            // 아이템 미노 표시 로직
                            applyItemMinoDisplay(cell, next, blockIndex, blockClass, textClass);
                            blockIndex++;
                        }
                    }
                }
            }
        }

        // 점수 업데이트
        scoreLabel.setText(playerName + "\nScore:\n" + engine.getScore());
    }

    // 아이템 미노 표시 로직 - 공통 메서드
    private void applyItemMinoDisplay(
            CellView cell,
            Tetromino tetromino,
            int blockIndex,
            String blockClass,
            String textClass) {
        if (tetromino.isItemPiece()) {
            if (tetromino.getItemType() == team13.tetris.game.model.Tetromino.ItemType.COPY
                    && blockIndex == tetromino.getCopyBlockIndex()) {
                // COPY 아이템: 특정 블록만 C 표시, 원래 블록 색상 유지
                cell.setBlock("C", blockClass, "item-copy-block");
            } else if (tetromino.getItemType()
                            == team13.tetris.game.model.Tetromino.ItemType.LINE_CLEAR
                    && blockIndex == tetromino.getLineClearBlockIndex()) {
                // LINE_CLEAR 아이템: 특정 블록만 L 표시, 원래 블록 색상 유지
                cell.setBlock("L", blockClass, "item-copy-block");
            } else if (tetromino.getItemType()
                    == team13.tetris.game.model.Tetromino.ItemType.WEIGHT) {
                // WEIGHT 아이템: 모든 블록 W 표시
                cell.setBlock("W", blockClass, textClass);
            } else if (tetromino.getItemType()
                    == team13.tetris.game.model.Tetromino.ItemType.GRAVITY) {
                // GRAVITY 아이템: 모든 블록 G 표시
                cell.setBlock("G", blockClass, textClass);
            } else if (tetromino.getItemType()
                    == team13.tetris.game.model.Tetromino.ItemType.SPLIT) {
                // SPLIT 아이템: 모든 블록 S 표시
                cell.setBlock("S", blockClass, textClass);
            } else {
                // 기타 아이템 블록은 O 표시
                cell.setBlock("O", blockClass, textClass);
            }
        } else {
            // 일반 미노는 O 표시
            cell.setBlock("O", blockClass, textClass);
        }
    }

    private void applyCellBlockText(CellView cell, String blockClass) {
        // blockClass에서 symbol과 스타일 추출
        String symbol = " ";
        String cssBlockClass = blockClass;
        String textClass = blockClass;

        // block 타입별로 symbol 매핑
        if (blockClass.contains("tetris-i-text")) {
            symbol = "I";
            cssBlockClass = "block-I";
            textClass = "tetris-i-text";
        } else if (blockClass.contains("tetris-o-text")) {
            symbol = "O";
            cssBlockClass = "block-O";
            textClass = "tetris-o-text";
        } else if (blockClass.contains("tetris-t-text")) {
            symbol = "T";
            cssBlockClass = "block-T";
            textClass = "tetris-t-text";
        } else if (blockClass.contains("tetris-s-text")) {
            symbol = "S";
            cssBlockClass = "block-S";
            textClass = "tetris-s-text";
        } else if (blockClass.contains("tetris-z-text")) {
            symbol = "Z";
            cssBlockClass = "block-Z";
            textClass = "tetris-z-text";
        } else if (blockClass.contains("tetris-j-text")) {
            symbol = "J";
            cssBlockClass = "block-J";
            textClass = "tetris-j-text";
        } else if (blockClass.contains("tetris-l-text")) {
            symbol = "L";
            cssBlockClass = "block-L";
            textClass = "tetris-l-text";
        } else if (blockClass.contains("tetris-flash-text")) {
            symbol = "O";
            cssBlockClass = "block-flash";
            textClass = "tetris-flash-text";
        } else if (blockClass.contains("item-copy-block")) {
            symbol = "?";
            cssBlockClass = "block-item";
            textClass = "item-copy-block";
        } else if (blockClass.contains("tetris-gray-text")) {
            symbol = " ";
            cssBlockClass = "block-gray";
            textClass = "tetris-gray-text";
        }

        cell.setBlock(symbol, cssBlockClass, textClass);
    }
}

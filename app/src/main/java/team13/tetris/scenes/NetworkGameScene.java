package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;

/**
 * 네트워크 대전용 게임 화면
 * - 왼쪽: 내 화면 (GameEngine 제어)
 * - 오른쪽: 상대 화면 (네트워크로 받은 보드 상태)
 */
public class NetworkGameScene {
    private final SceneManager manager;
    private final Settings settings;
    private final GameEngine engine;
    
    private final VBox root;
    private Scene scene;
    
    // 왼쪽 (내 화면)
    private final GridPane myGrid;
    private final Label myNameLabel;
    private final Label myScoreLabel;
    
    // 오른쪽 (상대 화면)
    private final GridPane opponentGrid;
    private final Label opponentNameLabel;
    private final Label opponentScoreLabel;
    
    // 연결 상태
    private final Label connectionStatusLabel;
    private boolean connected = false;
    
    private static final double CELL_SIZE = 24.0;
    
    public NetworkGameScene(SceneManager manager, Settings settings, 
                           GameEngine engine, String myName, String opponentName) {
        this.manager = manager;
        this.settings = settings;
        this.engine = engine;
        
        // 상단: 연결 상태
        connectionStatusLabel = new Label("Connecting...");
        connectionStatusLabel.getStyleClass().add("connection-status");
        connectionStatusLabel.setAlignment(Pos.CENTER);
        connectionStatusLabel.setMaxWidth(Double.MAX_VALUE);
        
        // 내 화면
        myNameLabel = new Label(myName);
        myNameLabel.getStyleClass().add("player-name-label");
        myScoreLabel = new Label("Score: 0");
        myScoreLabel.getStyleClass().add("score-label");
        
        Board board = engine.getBoard();
        myGrid = createBoardGrid(board.getWidth(), board.getHeight());
        
        VBox myPanel = new VBox(5, myNameLabel, myScoreLabel, myGrid);
        myPanel.setAlignment(Pos.TOP_CENTER);
        myPanel.setPadding(new Insets(10));
        myPanel.getStyleClass().add("player-panel");
        
        // 상대 화면
        opponentNameLabel = new Label(opponentName);
        opponentNameLabel.getStyleClass().add("player-name-label");
        opponentScoreLabel = new Label("Score: 0");
        opponentScoreLabel.getStyleClass().add("score-label");
        
        opponentGrid = createBoardGrid(10, 20);  // 상대도 같은 크기
        
        VBox opponentPanel = new VBox(5, opponentNameLabel, opponentScoreLabel, opponentGrid);
        opponentPanel.setAlignment(Pos.TOP_CENTER);
        opponentPanel.setPadding(new Insets(10));
        opponentPanel.getStyleClass().add("player-panel");
        
        // 화면 레이아웃
        HBox gameArea = new HBox(20, myPanel, opponentPanel);
        gameArea.setAlignment(Pos.CENTER);
        HBox.setHgrow(myPanel, Priority.ALWAYS);
        HBox.setHgrow(opponentPanel, Priority.ALWAYS);
        
        root = new VBox(10, connectionStatusLabel, gameArea);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("network-game-root");
        
        updateLocalGrid();
    }
    
    /**
     * 보드 그리드 생성
     */
    private GridPane createBoardGrid(int width, int height) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("board-grid");
        
        // 테두리 포함
        for (int y = 0; y < height + 2; y++) {
            for (int x = 0; x < width + 2; x++) {
                CellView cell = new CellView(CELL_SIZE, settings);
                
                if (x == 0 || x == width + 1 || y == 0 || y == height + 1) {
                    cell.setBorder();
                } else {
                    cell.setEmpty();
                }
                
                grid.add(cell, x, y);
            }
        }
        
        return grid;
    }
    
    /**
     * Scene 생성
     */
    public Scene createScene() {
        this.scene = new Scene(root, 900, 700);
        return scene;
    }
    
    public Scene getScene() {
        if (scene == null) {
            createScene();
        }
        return scene;
    }
    
    public void requestFocus() {
        Platform.runLater(() -> {
            if (scene != null) scene.getRoot().requestFocus();
        });
    }
    
    /**
     * 연결 상태 설정
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
        Platform.runLater(() -> {
            if (connected) {
                connectionStatusLabel.setText("Connected!");
                connectionStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                connectionStatusLabel.setText("Disconnected");
                connectionStatusLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }
    
    /**
     * 내 화면 업데이트 (GameEngine에서)
     */
    public void updateLocalGrid() {
        if (engine == null) return;
        
        Board board = engine.getBoard();
        int w = board.getWidth();
        int h = board.getHeight();
        
        Platform.runLater(() -> {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = board.getCell(x, y);
                    CellView cell = (CellView) getNodeByRowColumnIndex(y + 1, x + 1, myGrid);
                    if (cell != null) {
                        renderCell(cell, val);
                    }
                }
            }
            
            myScoreLabel.setText("Score: " + engine.getScore());
        });
    }
    
    /**
     * 상대 화면 업데이트 (네트워크 데이터)
     */
    public void updateRemoteBoardState(int[][] boardState, int pieceX, int pieceY, 
                                       int pieceType, int pieceRotation,
                                       int nextPieceType, java.util.Queue<int[][]> incomingBlocks,
                                       int score, int linesCleared) {
        Platform.runLater(() -> {
            int h = boardState.length;
            int w = boardState[0].length;
            
            // 보드 상태 렌더링
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = boardState[y][x];
                    CellView cell = (CellView) getNodeByRowColumnIndex(y + 1, x + 1, opponentGrid);
                    if (cell != null) {
                        renderCell(cell, val);
                    }
                }
            }
            
            // 현재 떨어지는 블록 렌더링
            if (pieceType > 0) {
                team13.tetris.game.model.Tetromino.Kind kind = team13.tetris.game.model.Tetromino.kindForId(pieceType);
                if (kind != null) {
                    int[][] shape = kind.getRotation(pieceRotation % 4);
                    for (int r = 0; r < shape.length; r++) {
                        for (int c = 0; c < shape[r].length; c++) {
                            if (shape[r][c] != 0) {
                                int bx = pieceX + c;
                                int by = pieceY + r;
                                if (bx >= 0 && bx < w && by >= 0 && by < h) {
                                    CellView cell = (CellView) getNodeByRowColumnIndex(by + 1, bx + 1, opponentGrid);
                                    if (cell != null) {
                                        cell.setNormalBlock(pieceType);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            opponentScoreLabel.setText("Score: " + score + " | Lines: " + linesCleared);
        });
    }
    
    /**
     * 셀 렌더링
     */
    private void renderCell(CellView cell, int val) {
        if (val == 0) {
            cell.setEmpty();
        } else if (val < 0) {
            // 플래시 효과
            cell.setFlash();
        } else if (val >= 1000) {
            // 공격 블록 (회색)
            cell.setAttackBlock();
        } else if (val >= 100) {
            // 아이템 블록
            int itemType = val / 100;
            int baseValue = val % 100;
            cell.setItemBlock(itemType, baseValue);
        } else {
            // 일반 블록 (1-7)
            cell.setNormalBlock(val);
        }
    }
    
    /**
     * GridPane에서 특정 행/열의 노드 가져오기
     */
    private Node getNodeByRowColumnIndex(int row, int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            if (r == null) r = 0;
            if (c == null) c = 0;
            if (r == row && c == column) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * CellView: 테트리스 셀 표시용 컴포넌트
     */
    private static final class CellView extends StackPane {
        private final Rectangle rect;
        private final Canvas patternCanvas;
        private final Label label;
        private final Settings settings;
        private String currentPattern = null;

        private CellView(double size, Settings settings) {
            this.settings = settings;
            setMinSize(size, size);
            setPrefSize(size, size);
            setMaxSize(size, size);
            setAlignment(Pos.CENTER);
            getStyleClass().add("cell");

            rect = new Rectangle(size, size);
            rect.getStyleClass().add("cell-rect");
            rect.setStrokeWidth(0);
            rect.widthProperty().bind(widthProperty());
            rect.heightProperty().bind(heightProperty());

            patternCanvas = new Canvas(size, size);
            patternCanvas.widthProperty().bind(widthProperty());
            patternCanvas.heightProperty().bind(heightProperty());
            patternCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawPattern());
            patternCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawPattern());

            label = new Label(" ");
            label.setAlignment(Pos.CENTER);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            label.getStyleClass().add("cell-text");

            getChildren().addAll(rect, patternCanvas, label);

            setEmpty();
        }

        private void redrawPattern() {
            if (currentPattern == null || currentPattern.equals("none")) {
                clearCanvas();
                return;
            }

            double w = patternCanvas.getWidth();
            double h = patternCanvas.getHeight();
            GraphicsContext gc = patternCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, w, h);
            gc.setStroke(Color.rgb(0, 0, 0, 0.7));
            gc.setLineWidth(1);

            switch (currentPattern) {
                case "horizontal":
                    for (double y = 0; y < h; y += 5) {
                        gc.strokeLine(0, y, w, y);
                    }
                    break;
                case "vertical":
                    for (double x = 0; x < w; x += 5) {
                        gc.strokeLine(x, 0, x, h);
                    }
                    break;
                case "diagonal-right":
                    for (double offset = -h; offset < w + h; offset += 5) {
                        gc.strokeLine(offset, h, offset + h, 0);
                    }
                    break;
                case "diagonal-left":
                    for (double offset = -h; offset < w + h; offset += 5) {
                        gc.strokeLine(offset, 0, offset + h, h);
                    }
                    break;
                case "diagonal-right-wide":
                    for (double offset = -h; offset < w + h; offset += 7) {
                        gc.strokeLine(offset, h, offset + h, 0);
                    }
                    break;
                case "diagonal-left-wide":
                    for (double offset = -h; offset < w + h; offset += 7) {
                        gc.strokeLine(offset, 0, offset + h, h);
                    }
                    break;
            }
        }

        private void clearCanvas() {
            GraphicsContext gc = patternCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, patternCanvas.getWidth(), patternCanvas.getHeight());
        }

        private void clearDynamicStyles() {
            ObservableList<String> rectClasses = rect.getStyleClass();
            rectClasses.removeIf(name -> name.startsWith("block-") || name.startsWith("item-") || 
                                       name.equals("cell-empty") || name.equals("cell-border") ||
                                       name.equals("attack-block") || name.equals("flash-block"));

            ObservableList<String> labelClasses = label.getStyleClass();
            labelClasses.removeIf(name -> name.startsWith("tetris-") || name.startsWith("item-") || 
                                        name.equals("cell-empty") || name.equals("cell-border") ||
                                        name.equals("attack-text") || name.equals("flash-text"));
        }

        void setEmpty() {
            clearDynamicStyles();
            currentPattern = null;
            clearCanvas();
            if (!rect.getStyleClass().contains("cell-empty")) rect.getStyleClass().add("cell-empty");
            if (!label.getStyleClass().contains("cell-empty")) label.getStyleClass().add("cell-empty");
            label.setText(" ");
        }

        void setBorder() {
            clearDynamicStyles();
            currentPattern = null;
            clearCanvas();
            if (!rect.getStyleClass().contains("cell-border")) rect.getStyleClass().add("cell-border");
            if (!label.getStyleClass().contains("cell-border")) label.getStyleClass().add("cell-border");
            label.setText("X");
        }

        void setFlash() {
            clearDynamicStyles();
            if (!rect.getStyleClass().contains("flash-block")) rect.getStyleClass().add("flash-block");
            if (!label.getStyleClass().contains("flash-text")) label.getStyleClass().add("flash-text");
            label.setText("*");
        }

        void setAttackBlock() {
            clearDynamicStyles();
            if (!rect.getStyleClass().contains("attack-block")) rect.getStyleClass().add("attack-block");
            if (!label.getStyleClass().contains("attack-text")) label.getStyleClass().add("attack-text");
            label.setText("▓");
        }

        void setNormalBlock(int val) {
            clearDynamicStyles();
            String blockClass = blockClassForValue(val);
            if (blockClass != null && !rect.getStyleClass().contains(blockClass)) {
                rect.getStyleClass().add(blockClass);
            }
            
            // 색맹 모드에서 패턴 적용
            if (settings.isColorBlindMode() && blockClass != null) {
                applyPattern(blockClass);
            } else {
                currentPattern = null;
                clearCanvas();
            }
            
            label.setText(" ");
        }

        void setItemBlock(int itemType, int baseValue) {
            clearDynamicStyles();
            String blockClass = blockClassForValue(baseValue);
            if (blockClass != null && !rect.getStyleClass().contains(blockClass)) {
                rect.getStyleClass().add(blockClass);
            }
            
            // 아이템 표시
            String symbol = "";
            switch (itemType) {
                case 1: symbol = "C"; break;  // COPY
                case 2: symbol = "L"; break;  // LINE_CLEAR
                case 3: symbol = "W"; break;  // WEIGHT
                case 4: symbol = "G"; break;  // GRAVITY
                case 5: symbol = "S"; break;  // SPLIT
            }
            
            if (!label.getStyleClass().contains("item-text")) {
                label.getStyleClass().add("item-text");
            }
            label.setText(symbol);
            
            // 색맹 모드에서 패턴 적용
            if (settings.isColorBlindMode() && blockClass != null) {
                applyPattern(blockClass);
            } else {
                currentPattern = null;
                clearCanvas();
            }
        }

        private String blockClassForValue(int val) {
            switch (val) {
                case 1: return "block-I";
                case 2: return "block-O";
                case 3: return "block-T";
                case 4: return "block-S";
                case 5: return "block-Z";
                case 6: return "block-J";
                case 7: return "block-L";
                default: return null;
            }
        }

        private void applyPattern(String blockClass) {
            switch (blockClass) {
                case "block-I":
                    currentPattern = "diagonal-right";
                    break;
                case "block-O":
                    currentPattern = "none";
                    break;
                case "block-T":
                    currentPattern = "diagonal-left";
                    break;
                case "block-S":
                    currentPattern = "horizontal";
                    break;
                case "block-Z":
                    currentPattern = "diagonal-right-wide";
                    break;
                case "block-J":
                    currentPattern = "vertical";
                    break;
                case "block-L":
                    currentPattern = "diagonal-left-wide";
                    break;
                default:
                    currentPattern = null;
            }
            redrawPattern();
        }
    }
}

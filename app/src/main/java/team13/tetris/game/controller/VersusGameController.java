package team13.tetris.game.controller;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.scenes.VersusGameScene;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VersusGameController {
    private final VersusGameScene gameScene;
    private final SceneManager sceneManager;
    private final Settings settings;
    private final GameEngine engine1; // Player 1
    private final GameEngine engine2; // Player 2
    
    // 각 플레이어별 독립적인 키 상태 추적
    private final Set<String> player1PressedKeys = new HashSet<>();
    private final Set<KeyCode> player2PressedKeys = new HashSet<>();
    
    // 각 키별 첫 입력 시간 추적 (초기 지연용)
    private final java.util.Map<String, Long> player1KeyPressTime = new java.util.HashMap<>();
    private final java.util.Map<KeyCode, Long> player2KeyPressTime = new java.util.HashMap<>();
    
    // 입력 처리 타이머
    private Timeline inputTimer;
    private static final long INPUT_DELAY_MS = 50; // 50ms (반복 간격)
    private static final long INITIAL_DELAY_MS = 500; // 500ms (첫 반복까지 지연)
    
    private final Player1Listener player1Listener;
    private final Player2Listener player2Listener;
    
    private boolean gameOver1 = false;
    private boolean gameOver2 = false;
    private boolean paused = false;
    
    // 넘어온 블록 큐 (LIFO - 가장 최근 것부터 처리)
    private final Queue<int[][]> incomingBlocksForPlayer1 = new LinkedList<>();
    private final Queue<int[][]> incomingBlocksForPlayer2 = new LinkedList<>();
    
    // 대전 모드 속도 공유 변수
    private int totalLinesCleared = 0; // 두 플레이어가 지운 총 줄 수
    private int lastSpeedLevel = 0; // 마지막 속도 레벨
    
    // 타이머 모드 변수
    private final boolean timerMode;
    private final boolean itemMode;
    private java.util.concurrent.ScheduledExecutorService timerExecutor;
    private int remainingSeconds = 120; // 2분 = 120초

    public VersusGameController(
            VersusGameScene gameScene,
            SceneManager sceneManager,
            Settings settings,
            GameEngine engine1,
            GameEngine engine2,
            boolean timerMode,
            boolean itemMode) {
        this.gameScene = gameScene;
        this.sceneManager = sceneManager;
        this.settings = settings;
        this.engine1 = engine1;
        this.engine2 = engine2;
        this.timerMode = timerMode;
        this.itemMode = itemMode;
        
        this.player1Listener = new Player1Listener();
        this.player2Listener = new Player2Listener();
        
        // 입력 처리 타이머 시작
        startInputTimer();
        
        // 타이머 모드인 경우 타이머 시작
        if (timerMode) {
            startTimer();
        }
    }
    
    private void pause() {
        if (!paused && !gameOver1 && !gameOver2) {
            paused = true;
            
            // 입력 타이머 정지
            if (inputTimer != null) {
                inputTimer.pause();
            }
            
            // 게임 엔진 정지
            engine1.stopAutoDrop();
            engine2.stopAutoDrop();
            
            // 타이머 모드인 경우 타이머 일시정지
            if (timerMode && timerExecutor != null && !timerExecutor.isShutdown()) {
                timerExecutor.shutdown();
            }
            
            showPauseWindow();
        }
    }
    
    private void resume() {
        if (paused && !gameOver1 && !gameOver2) {
            paused = false;
            
            // 입력 타이머 재개
            if (inputTimer != null) {
                inputTimer.play();
            }
            
            // 게임 엔진 재개
            engine1.startAutoDrop();
            engine2.startAutoDrop();
            
            // 타이머 모드인 경우 타이머 재시작
            if (timerMode) {
                startTimer();
            }
        }
    }
    
    private void showPauseWindow() {
        javafx.application.Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(gameScene.getScene().getWindow());

            Label resumeLabel = new Label("Resume");
            Label quit = new Label("Quit");

            // CSS 클래스 부여
            resumeLabel.getStyleClass().add("pause-option");
            quit.getStyleClass().add("pause-option");

            VBox box = new VBox(8, resumeLabel, quit);
            box.getStyleClass().add("pause-box");
            box.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(box);
            dialogScene.getStylesheets().addAll(gameScene.getScene().getStylesheets());

            // 선택 상태 관리
            final int[] selected = new int[]{0}; // 기본 Resume 선택
            applySelection(resumeLabel, quit, selected[0]);

            dialogScene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.UP || ev.getCode() == KeyCode.DOWN) {
                    selected[0] = (selected[0] == 0) ? 1 : 0;
                    applySelection(resumeLabel, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.ENTER) {
                    dialog.close();
                    if (selected[0] == 0) {
                        resume();
                    } else {
                        // Quit 선택 시 ExitScene으로 이동
                        sceneManager.showExitScene(settings, () -> {
                            paused = true;
                            showPauseWindow();
                        });
                    }
                } else if (ev.getCode() == KeyCode.ESCAPE) {
                    // ESC로 Resume
                    dialog.close();
                    resume();
                }
            });

            dialog.setScene(dialogScene);
            dialog.setTitle("Paused");
            dialog.setWidth(220);
            dialog.setHeight(150);
            dialog.showAndWait();
        });
    }
    
    private void applySelection(Label resume, Label quit, int selectedIndex) {
        if (selectedIndex == 0) {
            resume.getStyleClass().remove("selected");
            quit.getStyleClass().remove("selected");
            resume.getStyleClass().add("selected");
        } else {
            resume.getStyleClass().remove("selected");
            quit.getStyleClass().remove("selected");
            quit.getStyleClass().add("selected");
        }
    }
    
    private void startInputTimer() {
        inputTimer = new Timeline(new KeyFrame(Duration.millis(INPUT_DELAY_MS), event -> {
            processInputs();
        }));
        inputTimer.setCycleCount(Timeline.INDEFINITE);
        inputTimer.play();
    }
    
    private void processInputs() {
        // 일시정지 중에는 입력 처리하지 않음
        if (paused) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Player 1 입력 처리
        if (!gameOver1) {
            if (player1PressedKeys.contains(settings.getKeyLeft())) {
                if (shouldProcessKey(player1KeyPressTime, settings.getKeyLeft(), currentTime)) {
                    engine1.moveLeft();
                }
            }
            if (player1PressedKeys.contains(settings.getKeyRight())) {
                if (shouldProcessKey(player1KeyPressTime, settings.getKeyRight(), currentTime)) {
                    engine1.moveRight();
                }
            }
            if (player1PressedKeys.contains(settings.getKeyDown())) {
                if (shouldProcessKey(player1KeyPressTime, settings.getKeyDown(), currentTime)) {
                    engine1.softDrop();
                }
            }
            if (player1PressedKeys.contains(settings.getKeyRotate())) {
                if (shouldProcessKey(player1KeyPressTime, settings.getKeyRotate(), currentTime)) {
                    engine1.rotateCW();
                }
            }
            if (player1PressedKeys.contains(settings.getKeyDrop())) {
                if (shouldProcessKey(player1KeyPressTime, settings.getKeyDrop(), currentTime)) {
                    engine1.hardDrop();
                }
            }
        }
        
        // Player 2 입력 처리
        if (!gameOver2) {
            // Player 2 키들을 KeyCode로 변환하여 확인
            try {
                KeyCode leftKey2 = KeyCode.valueOf(settings.getKeyLeftP2());
                KeyCode rightKey2 = KeyCode.valueOf(settings.getKeyRightP2());
                KeyCode downKey2 = KeyCode.valueOf(settings.getKeyDownP2());
                KeyCode rotateKey2 = KeyCode.valueOf(settings.getKeyRotateP2());
                KeyCode dropKey2 = KeyCode.valueOf(settings.getKeyDropP2());
                
                if (player2PressedKeys.contains(leftKey2)) {
                    if (shouldProcessKey(player2KeyPressTime, leftKey2, currentTime)) {
                        engine2.moveLeft();
                    }
                }
                if (player2PressedKeys.contains(rightKey2)) {
                    if (shouldProcessKey(player2KeyPressTime, rightKey2, currentTime)) {
                        engine2.moveRight();
                    }
                }
                if (player2PressedKeys.contains(downKey2)) {
                    if (shouldProcessKey(player2KeyPressTime, downKey2, currentTime)) {
                        engine2.softDrop();
                    }
                }
                if (player2PressedKeys.contains(rotateKey2)) {
                    if (shouldProcessKey(player2KeyPressTime, rotateKey2, currentTime)) {
                        engine2.rotateCW();
                    }
                }
                if (player2PressedKeys.contains(dropKey2)) {
                    if (shouldProcessKey(player2KeyPressTime, dropKey2, currentTime)) {
                        engine2.hardDrop();
                    }
                }
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 KeyCode인 경우 무시
                System.err.println("Invalid Player 2 key configuration: " + e.getMessage());
            }
        }
    }
    
    // 키가 처리되어야 하는지 확인 (첫 입력 후 500ms 지연, 이후 50ms 간격)
    private <T> boolean shouldProcessKey(java.util.Map<T, Long> keyPressTimeMap, T key, long currentTime) {
        Long pressTime = keyPressTimeMap.get(key);
        if (pressTime == null) {
            return false; // 키가 눌리지 않음
        }
        
        long elapsedTime = currentTime - pressTime;
        
        // 첫 입력은 즉시 처리되고, INITIAL_DELAY_MS 이전에는 반복하지 않음
        if (elapsedTime < INITIAL_DELAY_MS) {
            return false;
        }
        
        // INITIAL_DELAY_MS 이후에는 INPUT_DELAY_MS 간격으로 처리
        return true;
    }
    
    private void startTimer() {
        timerExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(() -> {
            remainingSeconds--;
            gameScene.updateTimer(remainingSeconds);
            
            if (remainingSeconds <= 0) {
                // 시간 종료 - 점수가 높은 사람이 승리
                timerExecutor.shutdown();
                checkTimeUp();
            }
        }, 1, 1, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    private void checkTimeUp() {
        // 입력 타이머 정지
        if (inputTimer != null) {
            inputTimer.stop();
        }
        
        engine1.stopAutoDrop();
        engine2.stopAutoDrop();
        
        int score1 = engine1.getScore();
        int score2 = engine2.getScore();
        
        javafx.application.Platform.runLater(() -> {
            if (score1 > score2) {
                sceneManager.showVersusGameOver(settings, "Player 1", score1, score2, timerMode, itemMode);
            } else if (score2 > score1) {
                sceneManager.showVersusGameOver(settings, "Player 2", score2, score1, timerMode, itemMode);
            } else {
                // 무승부
                sceneManager.showVersusGameOver(settings, "Draw", score1, score2, timerMode, itemMode);
            }
        });
    }

    public void attachToScene(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        String keyString = code.toString();
        long currentTime = System.currentTimeMillis();
        
        // ESC or Pause key to pause
        if (code == KeyCode.ESCAPE || keyString.equals(settings.getPause())) {
            if (!paused && !gameOver1 && !gameOver2) {
                pause();
            }
            return;
        }
        
        // 일시정지 중에는 게임 입력 처리하지 않음
        if (paused) {
            return;
        }
        
        // 키 상태 업데이트 및 첫 입력 시간 기록
        // Player 1 keys
        if (keyString.equals(settings.getKeyLeft()) ||
            keyString.equals(settings.getKeyRight()) ||
            keyString.equals(settings.getKeyDown()) ||
            keyString.equals(settings.getKeyRotate()) ||
            keyString.equals(settings.getKeyDrop())) {
            
            // 첫 입력인 경우 즉시 처리하고 시간 기록
            if (!player1PressedKeys.contains(keyString)) {
                player1PressedKeys.add(keyString);
                player1KeyPressTime.put(keyString, currentTime);
                
                // 첫 입력은 즉시 실행
                if (!gameOver1) {
                    if (keyString.equals(settings.getKeyLeft())) {
                        engine1.moveLeft();
                    } else if (keyString.equals(settings.getKeyRight())) {
                        engine1.moveRight();
                    } else if (keyString.equals(settings.getKeyDown())) {
                        engine1.softDrop();
                    } else if (keyString.equals(settings.getKeyRotate())) {
                        engine1.rotateCW();
                    } else if (keyString.equals(settings.getKeyDrop())) {
                        engine1.hardDrop();
                    }
                }
            }
        }
        
        // Player 2 keys
        if (keyString.equals(settings.getKeyLeftP2()) ||
            keyString.equals(settings.getKeyRightP2()) ||
            keyString.equals(settings.getKeyDownP2()) ||
            keyString.equals(settings.getKeyRotateP2()) ||
            keyString.equals(settings.getKeyDropP2())) {
            
            // 첫 입력인 경우 즉시 처리하고 시간 기록
            if (!player2PressedKeys.contains(code)) {
                player2PressedKeys.add(code);
                player2KeyPressTime.put(code, currentTime);
                
                // 첫 입력은 즉시 실행
                if (!gameOver2) {
                    if (keyString.equals(settings.getKeyLeftP2())) {
                        engine2.moveLeft();
                    } else if (keyString.equals(settings.getKeyRightP2())) {
                        engine2.moveRight();
                    } else if (keyString.equals(settings.getKeyDownP2())) {
                        engine2.softDrop();
                    } else if (keyString.equals(settings.getKeyRotateP2())) {
                        engine2.rotateCW();
                    } else if (keyString.equals(settings.getKeyDropP2())) {
                        engine2.hardDrop();
                    }
                }
            }
        }
    }
    
    private void handleKeyRelease(KeyEvent event) {
        KeyCode code = event.getCode();
        String keyString = code.toString();
        
        // 키를 뗄 때 각 플레이어의 상태에서 제거
        player1PressedKeys.remove(keyString);
        player2PressedKeys.remove(code);
        
        // 시간 기록도 제거
        player1KeyPressTime.remove(keyString);
        player2KeyPressTime.remove(code);
    }

    public Player1Listener getPlayer1Listener() {
        return player1Listener;
    }

    public Player2Listener getPlayer2Listener() {
        return player2Listener;
    }

    // Player 1 리스너
    public class Player1Listener implements GameStateListener {
        @Override
        public void onBoardUpdated(Board board) {
            gameScene.updateGrid();
        }

        @Override
        public void onPieceSpawned(Tetromino piece, int px, int py) {
            // 블록이 생성될 때 큐에 있는 모든 공격을 한 번에 전달 (FIFO - 먼저 들어온 것부터)
            while (!incomingBlocksForPlayer1.isEmpty()) {
                int[][] attackPattern = incomingBlocksForPlayer1.poll();
                addIncomingBlockToBoard(engine1, attackPattern);
            }
            // 큐를 비운 후 상태 업데이트
            gameScene.updateIncomingGrid(1, incomingBlocksForPlayer1);
            gameScene.updateGrid();
        }

        @Override
        public void onNextPiece(Tetromino nextPiece) {
            gameScene.updateGrid();
        }

        @Override
        public void onScoreChanged(int newScore) {
            gameScene.updateGrid();
        }

        @Override
        public void onLinesCleared(int count) {
            // 총 줄 수 증가 및 속도 체크
            updateSharedSpeed(count);
            
            // 2줄 이상 지웠으면 상대방에게 공격
            if (count >= 2) {
                int[][] attackPattern = createAttackPattern(count, engine1);
                incomingBlocksForPlayer2.add(attackPattern);
                gameScene.updateIncomingGrid(2, incomingBlocksForPlayer2);
            }
        }

        @Override
        public void onGameOver() {
            gameOver1 = true;
            checkWinner();
        }
    }

    // Player 2 리스너
    public class Player2Listener implements GameStateListener {
        @Override
        public void onBoardUpdated(Board board) {
            gameScene.updateGrid();
        }

        @Override
        public void onPieceSpawned(Tetromino piece, int px, int py) {
            // 블록이 생성될 때 큐에 있는 모든 공격을 한 번에 전달 (FIFO - 먼저 들어온 것부터)
            while (!incomingBlocksForPlayer2.isEmpty()) {
                int[][] attackPattern = incomingBlocksForPlayer2.poll();
                addIncomingBlockToBoard(engine2, attackPattern);
            }
            // 큐를 비운 후 상태 업데이트
            gameScene.updateIncomingGrid(2, incomingBlocksForPlayer2);
            gameScene.updateGrid();
        }

        @Override
        public void onNextPiece(Tetromino nextPiece) {
            gameScene.updateGrid();
        }

        @Override
        public void onScoreChanged(int newScore) {
            gameScene.updateGrid();
        }

        @Override
        public void onLinesCleared(int count) {
            // 총 줄 수 증가 및 속도 체크
            updateSharedSpeed(count);
            
            // 2줄 이상 지웠으면 상대방에게 공격
            if (count >= 2) {
                int[][] attackPattern = createAttackPattern(count, engine2);
                incomingBlocksForPlayer1.add(attackPattern);
                gameScene.updateIncomingGrid(1, incomingBlocksForPlayer1);
            }
        }

        @Override
        public void onGameOver() {
            gameOver2 = true;
            checkWinner();
        }
    }

    // 공격 패턴 생성 (삭제된 줄에서 마지막 블록의 정확한 위치만 제외)
    private int[][] createAttackPattern(int lines, GameEngine engine) {
        Board board = engine.getBoard();
        int width = board.getWidth();
        
        // 마지막으로 고정된 블록의 실제 셀 위치들 가져오기 [x, y]
        java.util.List<int[]> lockedCells = engine.getLastLockedCells();
        
        // 삭제된 줄의 인덱스 가져오기
        java.util.List<Integer> clearedLineIndices = engine.getClearedLineIndices();
        
        // 회색 블록으로 채운 패턴 생성 (마지막 블록의 정확한 위치만 비움)
        int[][] pattern = new int[lines][width];
        
        // 일단 모두 회색 블록으로 채움
        for (int r = 0; r < lines; r++) {
            for (int c = 0; c < width; c++) {
                pattern[r][c] = 1000; // 회색 블록
            }
        }
        
        // 마지막 블록이 삭제된 줄에 있는 경우, 해당 위치만 비움
        if (clearedLineIndices != null) {
            for (int[] cell : lockedCells) {
                int cellX = cell[0];
                int cellY = cell[1];
                
                // 이 셀이 삭제된 줄에 있는지 확인
                for (int i = 0; i < clearedLineIndices.size(); i++) {
                    if (clearedLineIndices.get(i) == cellY) {
                        // 삭제된 줄의 몇 번째 줄인지 찾아서 패턴에 반영
                        pattern[i][cellX] = 0; // 빈 공간
                        break;
                    }
                }
            }
        }
        
        return pattern;
    }
    
    // 대전 모드 공유 속도 업데이트 (20줄마다 속도 증가, 두 플레이어 합산)
    private void updateSharedSpeed(int linesCleared) {
        totalLinesCleared += linesCleared;
        
        // 20줄마다 속도 증가 (기존의 2배)
        int currentSpeedLevel = totalLinesCleared / 20;
        
        if (currentSpeedLevel > lastSpeedLevel) {
            // 두 엔진 모두 속도 증가
            engine1.getGameTimer().increaseSpeed();
            engine2.getGameTimer().increaseSpeed();
            
            // 두 엔진의 드롭 간격 동기화
            double newInterval = engine1.getDropIntervalSeconds();
            engine1.setDropIntervalSeconds(newInterval);
            engine2.setDropIntervalSeconds(newInterval);
            
            lastSpeedLevel = currentSpeedLevel;
            
            System.out.println("대전 모드 속도 증가! 총 줄 수: " + totalLinesCleared +
                " - 속도 레벨: " + currentSpeedLevel +
                " - 속도: " + String.format("%.1f", engine1.getGameTimer().getSpeedFactor()) + "x");
        }
    }

    // 넘어온 블록을 보드에 추가
    private void addIncomingBlockToBoard(GameEngine engine, int[][] pattern) {
        Board board = engine.getBoard();
        int width = board.getWidth();
        int height = board.getHeight();
        int lines = pattern.length;
        
        // 기존 블록들을 위로 올림
        for (int y = 0; y < height - lines; y++) {
            for (int x = 0; x < width; x++) {
                board.setCell(x, y, board.getCell(x, y + lines));
            }
        }
        
        // 맨 아래에 넘어온 블록 추가
        for (int i = 0; i < lines; i++) {
            int targetRow = height - lines + i;
            for (int x = 0; x < width; x++) {
                board.setCell(x, targetRow, pattern[i][x]);
            }
        }
    }

    private void checkWinner() {
        // 타이머가 있으면 정지
        if (timerMode && timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdown();
        }
        
        if (gameOver1 && !gameOver2) {
            // Player 2 승리
            engine2.stopAutoDrop();
            javafx.application.Platform.runLater(() -> {
                sceneManager.showVersusGameOver(
                    settings,
                    "Player 2",
                    engine2.getScore(),
                    engine1.getScore(),
                    timerMode,
                    itemMode
                );
            });
        } else if (gameOver2 && !gameOver1) {
            // Player 1 승리
            engine1.stopAutoDrop();
            javafx.application.Platform.runLater(() -> {
                sceneManager.showVersusGameOver(
                    settings,
                    "Player 1",
                    engine1.getScore(),
                    engine2.getScore(),
                    timerMode,
                    itemMode
                );
            });
        } else if (gameOver1 && gameOver2) {
            // 무승부 (동시 게임오버)
            int higherScore = Math.max(engine1.getScore(), engine2.getScore());
            int lowerScore = Math.min(engine1.getScore(), engine2.getScore());
            String winner = (engine1.getScore() >= engine2.getScore()) ? "Player 1" : "Player 2";
            
            javafx.application.Platform.runLater(() -> {
                sceneManager.showVersusGameOver(
                    settings,
                    winner,
                    higherScore,
                    lowerScore,
                    timerMode,
                    itemMode
                );
            });
        }
    }
}

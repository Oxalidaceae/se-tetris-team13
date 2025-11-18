package team13.tetris.game.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.network.client.TetrisClient;
import team13.tetris.network.server.TetrisServer;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;
import team13.tetris.scenes.NetworkGameScene;
import team13.tetris.scenes.NetworkLobbyScene;

import java.io.IOException;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkGameController implements ClientMessageListener, ServerMessageListener {
    private final SceneManager manager;
    private final Settings settings;
    private final boolean isHost;
    private final String serverIP;
    
    // 네트워크
    private TetrisServer server;  // 호스트인 경우만 사용
    private TetrisClient client;  // 클라이언트인 경우만 사용
    private String myPlayerId;
    private String opponentPlayerId;
    
    // 로비/게임
    private NetworkLobbyScene lobbyScene;
    private NetworkGameScene gameScene;

    // 게임 엔진 (내 화면만 제어)
    private GameEngine myEngine;

    // 상태 플래그
    private boolean gameStarted = false;
    private boolean paused = false;
    private boolean itemMode = false;
    private boolean timerMode = false;
    private boolean myReady = false;

    // 카운트다운
    private Timeline countdownTimeline;
    private final IntegerProperty countdownSeconds = new SimpleIntegerProperty();

    // 타이머 모드
    private ScheduledExecutorService timerExecutor;
    private int remainingSeconds = 120;
    
    // 내 incoming 공격 큐 (내가 받은 공격이 여기 저장됨)
    private final Queue<int[][]> myIncomingBlocks = new LinkedList<>();
    
    public NetworkGameController(SceneManager manager, Settings settings, boolean isHost, String serverIP) {
        this.manager = manager;
        this.settings = settings;
        this.isHost = isHost;
        this.serverIP = serverIP;
    }
    
    
    // 로비 초기화 및 네트워크 연결
    public void initializeLobby() {
        lobbyScene = new NetworkLobbyScene(manager, settings, isHost);
        
        if (isHost) {
            // 서버 시작
            myPlayerId = "Host";
            try {
                server = new TetrisServer(myPlayerId);
                server.setHostMessageListener(this);
                server.start();
                
                lobbyScene.setStatusText("Server started. Waiting for client...\nYour IP: " + TetrisServer.getServerIP());
            } catch (IOException e) {
                lobbyScene.setStatusText("Failed to start server: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            // 클라이언트로 연결
            myPlayerId = "Client";
            connectToServer();
        }
        
        // 준비 버튼 핸들러
        lobbyScene.getReadyButton().setOnAction(e -> handleReadyButton());
        
        // Cancel 버튼 핸들러
        lobbyScene.setOnCancelCallback(this::disconnect);
        
        manager.changeScene(lobbyScene.getScene());
    }
    
    // 클라이언트에서 서버로 접속
    private void connectToServer() {
        client = new TetrisClient(myPlayerId, serverIP);
        client.setMessageListener(this);
        
        Platform.runLater(() -> {
            lobbyScene.setStatusText("Connecting...");
        });
        
        new Thread(() -> {
            if (client.connect()) {
                Platform.runLater(() -> {
                    lobbyScene.setStatusText("Connected to server!\nBoth players must be ready to start the game.");
                });
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Connection Failed");
                    alert.setHeaderText("Could not connect to the server.");
                    alert.setContentText("Please check the IP address and make sure the host is ready.");
                    alert.showAndWait();
                    manager.showHostOrJoin(settings);
                });
            }
        }).start();
    }
    
    
    // 준비 버튼 클릭 처리
    private void handleReadyButton() {
        myReady = !myReady;
        lobbyScene.setMyReady(myReady);
        
        if (myReady) {
            lobbyScene.setModeSelectionDisabled(true);
            if (isHost) {
                // 서버는 게임 모드 선택 전송
                GameModeMessage.GameMode mode = lobbyScene.getSelectedGameMode();
                this.itemMode = (mode == GameModeMessage.GameMode.ITEM);
                this.timerMode = (mode == GameModeMessage.GameMode.TIMER);
                server.selectGameMode(mode);

                String modeStr = "Normal Mode";
                if (itemMode) modeStr = "Item Mode";
                if (timerMode) modeStr = "Timer Mode";
                lobbyScene.setGameMode(modeStr);
                
                server.setHostReady();
            } else {
                // 클라이언트는 준비 상태만 전송
                client.requestReady();
            }
        } else {
            lobbyScene.setModeSelectionDisabled(false);
            if (isHost) {
                server.setHostUnready();
            } else {
                client.requestUnready();
            }
        }
    }
    
    // 게임 시작
    private void startGame() {
        if (gameStarted) {
            return;
        }
        gameStarted = true;
         paused = false;
        
        // 게임 리스너 생성
        GameStateListener listener = new GameStateListener() {
            @Override
            public void onBoardUpdated(Board board) {
                // 내 화면 업데이트
                if (gameScene != null) {
                    gameScene.updateLocalGrid();
                }
                
                // 상대에게 전송
                sendMyBoardState();
            }
            
            @Override
            public void onPieceSpawned(Tetromino tetromino, int px, int py) {
                // 로컬 대전과 동일: 새 블록 생성 시 큐에 있는 모든 공격을 한 번에 적용 (FIFO)
                while (!myIncomingBlocks.isEmpty()) {
                    int[][] attackPattern = myIncomingBlocks.poll();
                    addIncomingBlockToBoard(myEngine, attackPattern);
                }
                
                // 큐를 비운 후 incoming 그리드 업데이트 (VersusGameController와 동일)
                if (gameScene != null) {
                    gameScene.updateLocalIncomingGrid(myIncomingBlocks);
                    gameScene.updateLocalGrid();
                }
                
                // 보드 상태 전송
                sendMyBoardState();
            }
            
            @Override
            public void onLinesCleared(int linesCleared) {
                if (linesCleared >= 2) {
                    // 2줄 이상 지웠을 때 로컬 대전과 동일하게 정확한 공격 패턴 생성
                    int[][] attackPattern = createAttackPattern(linesCleared, myEngine);
                    
                    // 네트워크로 공격 패턴 전송 (상대방이 onAttackReceived에서 받음)
                    sendAttackPattern(attackPattern);
                }
            }
            
            @Override
            public void onGameOver() {
                handleLocalGameOver("You lose!");
            }
            
            @Override
            public void onNextPiece(Tetromino next) {
                // UI 업데이트 정도만 필요하면 여기에서 호출 가능
            }
            
            @Override
            public void onScoreChanged(int score) {
                // 점수는 updateLocalGrid()에서 같이 처리
            }
        };
        
        // GameEngine 생성 (아이템 모드 지원)
        Board myBoard = new Board(10, 20);
        team13.tetris.data.ScoreBoard.ScoreEntry.Mode mode;
        if (timerMode) {
            mode = team13.tetris.data.ScoreBoard.ScoreEntry.Mode.TIMER;
        } else if (itemMode) {
            mode = team13.tetris.data.ScoreBoard.ScoreEntry.Mode.ITEM;
        } else {
            mode = team13.tetris.data.ScoreBoard.ScoreEntry.Mode.NORMAL;
        }
        myEngine = new GameEngine(myBoard, listener, mode);
        
        // 게임 화면 생성
        gameScene = new NetworkGameScene(
            manager, 
            settings, 
            myEngine,
            isHost ? "You\n(Host)" : "You\n(Client)",
            isHost ? "Opponent\n(Client)" : "Opponent\n(Host)",
            timerMode
        );
        
        // 키 입력 핸들러
        gameScene.getScene().setOnKeyPressed(this::handleKeyPress);
        
        // 화면 전환 전에 대전 모드 창 크기 적용
        manager.applyVersusWindowSize(settings);
        manager.changeScene(gameScene.getScene());
        gameScene.requestFocus();
        gameScene.setConnected(true);
        
        // 게임 시작
        myEngine.startNewGame();
        gameScene.updateLocalGrid();
    }

    private void startTimer() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(() -> {
            remainingSeconds--;
            gameScene.updateTimer(remainingSeconds);
            
            if (remainingSeconds <= 0) {
                timerExecutor.shutdown();
                Platform.runLater(() -> handleLocalGameOver("Time's Up!"));
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    // 키 입력 처리
    private void handleKeyPress(KeyEvent event) {
        if (!gameStarted || myEngine == null) return;

        KeyCode code = event.getCode();
        String keyString = code.toString();
        
        String leftKey = settings.getKeyLeft();
        String rightKey = settings.getKeyRight();
        String downKey = settings.getKeyDown();
        String rotateKey = settings.getKeyRotate();
        String dropKey = settings.getKeyDrop();
        String pauseKey = settings.getPause();
        
        // Player 1 키로 내 보드 조작
        if (keyString.equals(leftKey)) {
            myEngine.moveLeft();
        } else if (keyString.equals(rightKey)) {
            myEngine.moveRight();
        } else if (keyString.equals(downKey)) {
            myEngine.softDrop();
        } else if (keyString.equals(rotateKey)) {
            myEngine.rotateCW();
        } else if (keyString.equals(dropKey)) {
            myEngine.hardDrop();
        } else if (keyString.equals(pauseKey) || code == KeyCode.ESCAPE) {
            togglePause();
        }
    }
    
    
    // 내 보드 상태 전송
    private void sendMyBoardState() {
        if (myEngine == null || !gameStarted) return;
        
        // P2P에서는 보드 상태 + 현재/다음 블록 + incoming blocks 전송
        int[][] boardState = myEngine.getBoard().snapshot();
        int score = myEngine.getScore();
        int lines = myEngine.getTotalLinesCleared();

        int pieceX = myEngine.getPieceX();
        int pieceY = myEngine.getPieceY();

        Tetromino current = myEngine.getCurrent();
        int pieceType = -1;
        int pieceRotation = 0;
        if (current != null && current.getKind() != null) {
            pieceType = current.getKind().getId();
            pieceRotation = current.getRotationIndex();
        }
        
        // 다음 블록 정보
        Tetromino next = myEngine.getNext();
        int nextPieceType = -1;
        if (next != null && next.getKind() != null) {
            nextPieceType = next.getKind().getId();
        }
        
        // 내 incoming 블록 큐를 전송 (상대방 화면에서 "내가 받을 공격" 표시용)
        Queue<int[][]> incomingBlocks = new LinkedList<>(myIncomingBlocks);
        
         if (isHost && server != null) {
            server.sendHostBoardUpdate(
                    boardState,
                    pieceX,
                    pieceY,
                    pieceType,
                    pieceRotation,
                    nextPieceType,
                    incomingBlocks,
                    score,
                    lines,
                    0 // level 사용 안 함
            );
         }else if (!isHost && client != null) {
            client.sendBoardUpdate(
                    boardState,
                    pieceX,
                    pieceY,
                    pieceType,
                    pieceRotation,
                    nextPieceType,
                    incomingBlocks,
                    score,
                    lines,
                    0
            );
        }
    }

    // 내가 줄을 지웠을 때 상대에게 전송 (더 이상 사용 안 함 - sendAttackPattern 사용)
    @Deprecated
    private void sendAttack(int clearedLines) {
        if (!gameStarted || clearedLines <= 0) return;

        if (isHost && server != null) {
            server.sendHostAttack(clearedLines);
        } else if (!isHost && client != null) {
            // targetPlayerId는 서버에서 무시됨(브로드캐스트)
            client.sendAttack(opponentPlayerId != null ? opponentPlayerId : "Opponent", clearedLines);
        }
    }
    
    // 공격 패턴을 상대방에게 전송 (로컬 대전과 동일하게)
    private void sendAttackPattern(int[][] attackPattern) {
        if (!gameStarted || attackPattern == null || attackPattern.length == 0) return;
        
        int lines = attackPattern.length;
        
        if (isHost && server != null) {
            // 서버는 AttackMessage를 직접 생성하여 전송
            AttackMessage attackMsg = new AttackMessage(myPlayerId, lines, lines, attackPattern);
            server.broadcastMessage(attackMsg);
        } else if (!isHost && client != null) {
            // 클라이언트도 AttackMessage 생성하여 전송
            AttackMessage attackMsg = new AttackMessage(myPlayerId, lines, lines, attackPattern);
            client.sendMessage(attackMsg);
        }
    }

    // 일시정지/재개 (네트워크 동기화)
    private void togglePause() {
        if (!gameStarted || myEngine == null) return;

        if (!paused) {
            // 로컬 먼저 멈추고 네트워크로 PAUSE 전파
            applyLocalPause();
            sendPauseToNetwork();
        } else {
            applyLocalResume();
            sendResumeToNetwork();
        }
    }

    private void applyLocalPause() {
        if (paused) return;
        paused = true;
        if (myEngine != null) {
            myEngine.stopAutoDrop();
        }
        if (timerMode && timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdownNow();
        }
        // 필요하다면 별도의 Pause UI를 NetworkGameScene에 추가 가능
    }

    private void applyLocalResume() {
        if (!paused) return;
        paused = false;
        if (myEngine != null) {
            myEngine.startAutoDrop();
        }
        if (timerMode) {
            startTimer();
        }
    }

    private void sendPauseToNetwork() {
        if (isHost && server != null) {
            server.pauseGameAsHost();
        } else if (!isHost && client != null) {
            client.pauseGame();
        }
    }

    private void sendResumeToNetwork() {
        if (isHost && server != null) {
            server.resumeGameAsHost();
        } else if (!isHost && client != null) {
            client.resumeGame();
        }
    }
    
    
    // 게임 오버 처리 (네트워크 동기화)
    private void handleLocalGameOver(String reason) {
        if (!gameStarted) return;
        gameStarted = false;
        if (myEngine != null) {
            myEngine.stopAutoDrop();
        }
        if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdownNow();
        }

        // 네트워크로 GAME_OVER 알림
        if (isHost && server != null) {
            server.broadcastGameOverToOthers(myPlayerId, reason);
        } else if (!isHost && client != null) {
            ConnectionMessage msg = ConnectionMessage.createGameOver(myPlayerId, reason);
            client.sendMessage(msg);
        }

        // 로컬 UI 처리 - 네트워크 모드로 VersusGameOverScene 호출
        Platform.runLater(() -> {
            int myScore = myEngine != null ? myEngine.getScore() : 0;
            int opponentScore = gameScene != null ? gameScene.getOpponentScore() : 0;
            manager.showVersusGameOver(
                settings, 
                "Opponent", // 진 경우이므로 상대가 이겼음
                opponentScore, // winner score
                myScore, // loser score  
                false, // timerMode
                itemMode,
                "You", // currentPlayer (나는 진 사람)
                true // isNetworkMode
            );
        });
    }

    // 상대/서버로부터 GAME_OVER 받았을 때 처리
    private void handleRemoteGameOver(String reason) {
        if (!gameStarted) return;
        gameStarted = false;
        if (myEngine != null) {
            myEngine.stopAutoDrop();
        }
        if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdownNow();
        }

        Platform.runLater(() -> {
            int myScore = myEngine != null ? myEngine.getScore() : 0;
            int opponentScore = gameScene != null ? gameScene.getOpponentScore() : 0;
            manager.showVersusGameOver(
                settings,
                "You", // 상대가 진 경우이므로 내가 이겼음
                myScore, // winner score
                opponentScore, // loser score
                false, // timerMode
                itemMode,
                "Opponent", // currentPlayer (상대는 진 사람)
                true // isNetworkMode
            );
        });
    }
    
    // ClientMessageListener 구현 
    @Override
    public void onConnectionAccepted() {
        if (!isHost) {
            opponentPlayerId = "Host";
        }
    }
    
    @Override
    public void onConnectionRejected(String reason) {
        Platform.runLater(() -> {
            lobbyScene.setStatusText("Connection rejected: " + reason);
        });
    }
    
    @Override
    public void onPlayerReady(String playerId) {     
        // 상대방의 준비 상태 업데이트
        if (!playerId.equals(myPlayerId)) {
            opponentPlayerId = playerId;
            Platform.runLater(() -> {
                lobbyScene.setOpponentReady(true);
            });
        }
    }

    @Override
    public void onPlayerUnready(String playerId) {
        if (!playerId.equals(myPlayerId)) {
            Platform.runLater(() -> {
                lobbyScene.setOpponentReady(false);
            });
        }
    }
    
    @Override
    public void onGameStart() {
        // 서버에서 GAME_START 방송 → 클라이언트/호스트 모두 여기로 옴
        Platform.runLater(() -> {
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            startGame();
            if (timerMode) {
                startTimer();
            }
        });
    }

    @Override
    public void onCountdownStart() {
        Platform.runLater(() -> {
            lobbyScene.setControlsDisabled(true);
            lobbyScene.setStatusText("Start soon...");
            countdownSeconds.set(5);

            // 버튼 텍스트를 카운트다운에 바인딩
            lobbyScene.getReadyButton().textProperty().bind(countdownSeconds.asString());

            countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                countdownSeconds.set(countdownSeconds.get() - 1);
            }));
            countdownTimeline.setCycleCount(5);
            countdownTimeline.setOnFinished(e -> {
                lobbyScene.getReadyButton().textProperty().unbind();
                lobbyScene.getReadyButton().setText("Starting...");
            });
            countdownTimeline.play();
        });
    }
    
    @Override
    public void onGameOver(String reason) {
        handleRemoteGameOver(reason);
    }
    
    @Override
    public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
        if (gameScene == null){
            return;
        } 
        
       Platform.runLater(() -> gameScene.updateRemoteBoardState(
                boardUpdate.getBoardState(),
                boardUpdate.getCurrentPieceX(),
                boardUpdate.getCurrentPieceY(),
                boardUpdate.getCurrentPieceType(),
                boardUpdate.getCurrentPieceRotation(),
                boardUpdate.getNextPieceType(),
                boardUpdate.getIncomingBlocks(),
                boardUpdate.getScore(),
                boardUpdate.getLinesCleared()
        ));
        
    }
    
    @Override
    public void onAttackReceived(AttackMessage attackMessage) {
        // 상대방이 보낸 공격 패턴 수신
        int[][] attackPattern = attackMessage.getAttackPattern();
        if (attackPattern == null || attackPattern.length == 0 || myEngine == null) return;
        
        // 로컬 대전과 동일: 공격 패턴을 큐에 추가 (다음 블록 생성 시 적용)
        Platform.runLater(() -> {
            myIncomingBlocks.add(attackPattern);
            
            // UI 업데이트: 내 incoming 그리드 표시
            if (gameScene != null) {
                gameScene.updateLocalIncomingGrid(myIncomingBlocks);
            }
            
            // 내 보드 상태 전송 (상대방이 내 incoming을 볼 수 있도록)
            sendMyBoardState();
        });
    }
    
    @Override
    public void onGamePaused() {
        Platform.runLater(this::applyLocalPause);
    }
    
    @Override
    public void onGameResumed() {
        Platform.runLater(this::applyLocalResume);
    }
    
    @Override
    public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
        this.itemMode = (gameMode == GameModeMessage.GameMode.ITEM);
        this.timerMode = (gameMode == GameModeMessage.GameMode.TIMER);
        Platform.runLater(() -> {
            String modeStr = "Normal Mode";
            if (itemMode) {
                modeStr = "Item Mode";
            } else if (timerMode) {
                modeStr = "Timer Mode";
            }
            lobbyScene.setGameMode(modeStr);
        });
    }
    
    @Override
    public void onError(String error) {
        System.err.println("Network error: " + error);
        if (lobbyScene != null) {
            lobbyScene.setStatusText(error);
        }
    }
    
    @Override
    public void onServerDisconnected(String reason) {
        Platform.runLater(() -> {
            // 서버 연결 종료 알림 팝업 표시
            showDisconnectionAlert("Server Disconnected", reason);
            // 네트워크 정리 및 메인 메뉴로 복귀
            cleanupAndReturnToMenu();
        });
    }
    
    // ServerMessageListener 구현
    
    @Override
    public void onClientConnected(String clientId) {
        opponentPlayerId = clientId;
        Platform.runLater(() -> {
            lobbyScene.setStatusText("Client connected!\nBoth players must be ready to start the game.");
        });
    }
    
    @Override
    public void onClientDisconnected(String clientId) {
        Platform.runLater(() -> {
            if (lobbyScene != null) {
                lobbyScene.setStatusText("Client disconnected");
                // 상대 Ready 상태 초기화
                lobbyScene.setOpponentReady(false);
            }
  
            if (gameStarted) {
                showDisconnectionAlert("Client Disconnected", "The opponent has left the game.");
                cleanupAndReturnToMenu();
            }
        });
    }

    // 공격 패턴 생성 (VersusGameController와 동일한 로직)
    private int[][] createAttackPattern(int lines, GameEngine engine) {
        Board board = engine.getBoard();
        int width = board.getWidth();
        
        // 중력/스플릿 블록으로 인한 라인클리어인지 확인
        boolean isGravityOrSplitClear = engine.isLastClearByGravityOrSplit();
        
        // 회색 블록으로 채운 패턴 생성
        int[][] pattern = new int[lines][width];
        
        // 일단 모두 회색 블록으로 채움
        for (int r = 0; r < lines; r++) {
            for (int c = 0; c < width; c++) {
                pattern[r][c] = 1000; // 회색 블록
            }
        }
        
        if (isGravityOrSplitClear) {
            // 중력/스플릿 블록: 각 줄마다 랜덤한 한 칸을 빈 공간으로
            java.util.Random random = new java.util.Random();
            for (int r = 0; r < lines; r++) {
                int randomCol = random.nextInt(width); // 0 ~ width-1 랜덤 선택
                pattern[r][randomCol] = 0; // 빈 공간
            }
        } else {
            // 일반 블록: 마지막 블록의 정확한 위치만 제외
            java.util.List<int[]> lockedCells = engine.getLastLockedCells();
            java.util.List<Integer> clearedLineIndices = engine.getClearedLineIndices();
            
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
        }
        
        return pattern;
    }
    
    // 넘어온 블록을 보드에 추가 (VersusGameController와 동일한 로직)
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

    private void showPauseWindow() {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(gameScene.getScene().getWindow());

            Label resume = new Label("Resume");
            Label mainMenu = new Label("Main Menu");
            Label quit = new Label("Quit");

            // CSS 클래스 부여
            resume.getStyleClass().add("pause-option");
            mainMenu.getStyleClass().add("pause-option");
            quit.getStyleClass().add("pause-option");

            VBox box = new VBox(8, resume, mainMenu, quit);
            box.getStyleClass().add("pause-box");
            box.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(box);
            dialogScene.getStylesheets().addAll(gameScene.getScene().getStylesheets());

            // 선택 상태 관리
            final int[] selected = new int[]{0}; // 기본 Resume 선택
            applySelection(resume, mainMenu, quit, selected[0]);

            dialogScene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.UP) {
                    selected[0] = (selected[0] == 0) ? 0 : selected[0] - 1;
                    applySelection(resume, mainMenu, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.DOWN) {
                    selected[0] = (selected[0] == 2) ? 2 : selected[0] + 1;
                    applySelection(resume, mainMenu, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.ENTER) {
                    dialog.close();

                    if (selected[0] == 0) {
                        // Resume 선택
                        applyLocalResume();
                        sendResumeToNetwork();
                    } else if (selected[0] == 1) {
                        // Main Menu 선택
                        manager.showConfirmScene(
                            settings,
                            "Return to Main Menu?",
                            () -> {
                                disconnect();
                                manager.showMainMenu(settings);
                            },
                            () -> {
                                manager.restorePreviousScene();
                                paused = true;
                                showPauseWindow();
                            }
                        );
                    } else {
                        // Quit 선택
                        manager.showConfirmScene(
                            settings,
                            "Exit Game?",
                            () -> {
                                disconnect();
                                manager.exitWithSave(settings);
                            },
                            () -> {
                                manager.restorePreviousScene();
                                paused = true;
                                showPauseWindow();
                            }
                        );
                    }
                } else if (ev.getCode() == KeyCode.ESCAPE) {
                    // ESC로 Resume
                    dialog.close();
                    applyLocalResume();
                    sendResumeToNetwork();
                }
            });

            dialog.setScene(dialogScene);
            dialog.setTitle("Paused");
            dialog.setWidth(220);
            dialog.setHeight(150);
            dialog.showAndWait();
        });
    }

    private void applySelection(Label resume, Label mainMenu, Label quit, int selectedIndex) {
        // 모든 라벨에서 selected 클래스 제거
        resume.getStyleClass().remove("selected");
        mainMenu.getStyleClass().remove("selected");
        quit.getStyleClass().remove("selected");
        
        // 선택된 라벨에만 selected 클래스 추가
        if (selectedIndex == 0) {
            resume.getStyleClass().add("selected");
        } else if (selectedIndex == 1) {
            mainMenu.getStyleClass().add("selected");
        } else {
            quit.getStyleClass().add("selected");
        }
    }

    // 정리 / 종료
    public void disconnect() {
        gameStarted = false;

        if (myEngine != null) {
            myEngine.stopAutoDrop();
        }
        if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdownNow();
        }

        if (isHost) {
            if (server != null && server.isRunning()) {
                server.stop();
            }
        } else {
            if (client != null) {
                client.disconnect();
            }
        }
    }
    
    // 연결 종료 알림 팝업 표시
    private void showDisconnectionAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // 네트워크 정리 및 메인 메뉴로 복귀
    private void cleanupAndReturnToMenu() {
        disconnect();
        manager.showMainMenu(settings);
    }
}

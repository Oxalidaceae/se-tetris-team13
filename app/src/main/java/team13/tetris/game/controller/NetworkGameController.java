package team13.tetris.game.controller;

import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
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

public class NetworkGameController implements ClientMessageListener, ServerMessageListener {
    private final SceneManager manager;
    private final Settings settings;
    private final boolean isHost;
    private final String serverIP;
    
    // 네트워크
    private TetrisServer server;  // 호스트인 경우만 사용
    private TetrisClient client;
    private String myPlayerId;
    private String opponentPlayerId;
    
    // 게임
    private GameEngine myEngine;
    private NetworkGameScene gameScene;
    private NetworkLobbyScene lobbyScene;
    private boolean gameStarted = false;
    private boolean itemMode = false;
    
    // 공격 큐
    private final Queue<Integer> incomingAttacks = new LinkedList<>();
    
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
                
                lobbyScene.setStatusText("Server started. Waiting for client...");
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
        lobbyScene.getReadyButton().setOnAction(e -> {
            handleReadyButton();
        });
        
        manager.changeScene(lobbyScene.getScene());
    }
    
    
    // 서버에 연결 (클라이언트)
    private void connectToServer() {
        client = new TetrisClient(myPlayerId, serverIP != null ? serverIP : "127.0.0.1");
        client.setMessageListener(this);
        
        Platform.runLater(() -> {
            lobbyScene.setStatusText("Connecting...");
        });
        
        new Thread(() -> {
            if (client.connect()) {
                Platform.runLater(() -> {
                    lobbyScene.setStatusText("Connected to server!");
                });
            }
        }).start();
    }
    
    
    // 준비 버튼 클릭 처리
    private void handleReadyButton() {
        lobbyScene.setMyReady(true);
        
        if (isHost) {
            // 서버는 게임 모드 선택 전송
            itemMode = lobbyScene.isItemMode();
            GameModeMessage.GameMode mode = itemMode ? 
                GameModeMessage.GameMode.ITEM : GameModeMessage.GameMode.NORMAL;
            server.selectGameMode(mode);
            
            // 자신의 준비 상태 전송 (PLAYER_READY 메시지)
            ConnectionMessage readyMsg = ConnectionMessage.createPlayerReady(myPlayerId);
            server.broadcastMessage(readyMsg);
        } else {
            // 클라이언트는 준비 상태만 전송
            ConnectionMessage readyMsg = ConnectionMessage.createPlayerReady(myPlayerId);
            client.sendMessage(readyMsg);
        }
        
        startSynchronizedGame();
    }
    
    
    // 동기화된 게임 시작 (서버에서만 호출)
    private void startSynchronizedGame() {
        if (!isHost){
            return;
        }
        
        ConnectionMessage gameStartMsg = ConnectionMessage.createGameStart(myPlayerId);
        server.broadcastMessage(gameStartMsg);
    }
    
    // 게임 시작
    private void startGame() {
        if (gameStarted) return;
        gameStarted = true;
        
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
            public void onPieceSpawned(team13.tetris.game.model.Tetromino tetromino, int px, int py) {
                // 공격 블록 관련 리스너??
            }
            
            @Override
            public void onLinesCleared(int linesCleared) {
                if (linesCleared > 0) {
                    // 공격 전송 (1줄당 1라인 공격)
                    sendAttack(linesCleared);
                }
            }
            
            @Override
            public void onGameOver() {
                handleGameOver("You lose!");
            }
            
            @Override
            public void onNextPiece(team13.tetris.game.model.Tetromino next) {}
            
            @Override
            public void onScoreChanged(int score) {}
        };
        
        // 게임 엔진 생성 (리스너 전달)
        Board myBoard = new Board(10, 20);
        myEngine = new GameEngine(myBoard, listener);
        
        // 게임 화면 생성
        gameScene = new NetworkGameScene(
            manager, 
            settings, 
            myEngine,
            isHost ? "You (Host)" : "You (Client)",
            isHost ? "Opponent (Client)" : "Opponent (Host)"
        );
        
        // 키 입력 핸들러
        gameScene.getScene().setOnKeyPressed(this::handleKeyPress);
        
        // 화면 전환
        Platform.runLater(() -> {
            manager.changeScene(gameScene.getScene());
            gameScene.requestFocus();
            gameScene.setConnected(true);
            
            // 게임 시작
            myEngine.startNewGame();
            
            // 게임 루프 시작 (60 FPS로 화면 업데이트 + 보드 전송)
            startGameLoop();
        });
    }
    
    // 게임 루프 (화면 업데이트 + 네트워크 전송)
    private java.util.Timer gameLoopTimer;
    private void startGameLoop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.cancel();
        }
        
        gameLoopTimer = new java.util.Timer();
        gameLoopTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            private int frameCount = 0;
            
            @Override
            public void run() {
                if (!gameStarted || myEngine == null) {
                    return;
                }
                
                Platform.runLater(() -> {
                    // 화면 업데이트 (매 프레임)
                    if (gameScene != null) {
                        gameScene.updateLocalGrid();
                    }
                });
                
                // 네트워크 전송 (10 FPS - 매 6프레임마다)
                frameCount++;
                if (frameCount % 6 == 0) {
                    sendMyBoardState();
                }
            }
        }, 0, 16); // 약 60 FPS (16ms)
    }
    
    // 게임 루프 중지
    private void stopGameLoop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.cancel();
            gameLoopTimer = null;
        }
    }
    
    
    // 키 입력 처리
    private void handleKeyPress(KeyEvent event) {
        if (!gameStarted || myEngine == null) return;
        
        String leftKey = settings.getKeyLeft();
        String rightKey = settings.getKeyRight();
        String downKey = settings.getKeyDown();
        String rotateKey = settings.getKeyRotate();
        String dropKey = settings.getKeyDrop();
        String pauseKey = settings.getPause();
        
        String keyText = event.getText();
        
        if (keyText.equals(leftKey)) {
            myEngine.moveLeft();
        } else if (keyText.equals(rightKey)) {
            myEngine.moveRight();
        } else if (keyText.equals(downKey)) {
            myEngine.softDrop();
        } else if (keyText.equals(rotateKey)) {
            myEngine.rotateCW();
        } else if (keyText.equals(dropKey)) {
            myEngine.hardDrop();
        } else if (keyText.equals(pauseKey)) {
            // TODO: 일시정지
        }
    }
    
    
    // 내 보드 상태 전송
    private void sendMyBoardState() {
        if (myEngine == null) return;
        
        // P2P에서는 보드 상태 + 현재/다음 블록 + incoming blocks 전송
        int[][] boardState = myEngine.getBoard().snapshot();
        int score = myEngine.getScore();
        int pieceX = myEngine.getPieceX();
        int pieceY = myEngine.getPieceY();
        
        // 현재 블록 정보
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
        
        // Incoming blocks queue (공격받을 블록들)
        java.util.Queue<int[][]> incomingBlocks;
        synchronized (incomingAttacks) {
            incomingBlocks = convertAttacksToBlocks(new LinkedList<>(incomingAttacks));
        }
        
        BoardUpdateMessage msg = new BoardUpdateMessage(
            myPlayerId,
            boardState,
            pieceX,
            pieceY,
            pieceType,
            pieceRotation,
            nextPieceType,
            incomingBlocks,
            score,
            myEngine.getTotalLinesCleared(),
            0   // level - 사용하지 않음
        );
        
        if (isHost && server != null) {
            server.broadcastMessage(msg);
        } else if (!isHost && client != null) {
            client.sendMessage(msg);
        }
    }
    
    // 공격 숫자를 블록 패턴으로 변환
    private Queue<int[][]> convertAttacksToBlocks(Queue<Integer> attacks) {
        Queue<int[][]> blocks = new LinkedList<>();
        for (Integer lines : attacks) {
            if (lines > 0) {
                int[][] pattern = createAttackPattern(lines);
                blocks.add(pattern);
            }
        }
        return blocks;
    }
    
    // 공격 패턴 생성 (회색 블록)
    private int[][] createAttackPattern(int lines) {
        int[][] pattern = new int[lines][10];
        for (int y = 0; y < lines; y++) {
            for (int x = 0; x < 10; x++) {
                pattern[y][x] = 1000; // 1000 = 회색 공격 블록
            }
        }
        return pattern;
    }
    
    
    // 공격 전송
    private void sendAttack(int lines) {
        if (opponentPlayerId == null) return;
        
        AttackMessage msg = AttackMessage.createStandardAttack(
            myPlayerId, 
            opponentPlayerId, 
            lines
        );
        
        if (isHost && server != null) {
            server.broadcastMessage(msg);
        } else if (!isHost && client != null) {
            client.sendMessage(msg);
        }
    }
    
    
    // 게임 오버 처리
    private void handleGameOver(String reason) {
        gameStarted = false;
        if (myEngine != null) {
            myEngine.stopAutoDrop();
        }
        stopGameLoop();
        
        Platform.runLater(() -> {
            // TODO: 게임 오버 화면 표시
            manager.showMainMenu(settings);
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
        // 자기 자신의 PLAYER_READY 메시지는 무시
        if (playerId.equals(myPlayerId)) {
            return;
        }
        
        Platform.runLater(() -> {
            lobbyScene.setOpponentReady(true);
            opponentPlayerId = playerId;
            
            // 서버(호스트)인 경우에만 양쪽 준비 상태 확인하여 게임 시작
            //if (isHost && lobbyScene.areBothReady()) {
            //    // 서버가 GAME_START 메시지 전송하여 동시 시작
            //    startSynchronizedGame();
            //}
        });
    }
    
    @Override
    public void onGameStart() {
        // 서버로부터 GAME_START 메시지를 받으면 동시에 게임 시작
        Platform.runLater(this::startGame);
    }
    
    @Override
    public void onGameOver(String reason) {
        handleGameOver(reason);
    }
    
    @Override
    public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
        if (gameScene != null) {
            Platform.runLater(() -> {
                gameScene.updateRemoteBoardState(
                    boardUpdate.getBoardState(),
                    boardUpdate.getCurrentPieceX(),
                    boardUpdate.getCurrentPieceY(),
                    boardUpdate.getCurrentPieceType(),
                    boardUpdate.getCurrentPieceRotation(),
                    boardUpdate.getNextPieceType(),
                    boardUpdate.getIncomingBlocks(),
                    boardUpdate.getScore(),
                    boardUpdate.getLinesCleared()
                );
            });
        }
    }
    
    @Override
    public void onAttackReceived(AttackMessage attackMessage) {
        int lines = attackMessage.getAttackLines();
        synchronized (incomingAttacks) {
            incomingAttacks.add(lines);
        }
        
        // 공격 라인 추가 (즉시 적용)
        Platform.runLater(() -> {
            if (myEngine != null) {
                synchronized (incomingAttacks) {
                    while (!incomingAttacks.isEmpty()) {
                        int attackLines = incomingAttacks.poll();
                        for (int i = 0; i < attackLines; i++) {
                            addGarbageLine();
                        }
                    }
                }
                // 화면 업데이트
                if (gameScene != null) {
                    gameScene.updateLocalGrid();
                }
            }
        });
    }
    
    /**
     * 공격 라인 추가 (회색 블록)
     */
    private void addGarbageLine() {
        Board board = myEngine.getBoard();
        int width = board.getWidth();
        int height = board.getHeight();
        
        // 모든 줄을 위로 이동
        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width; x++) {
                board.setCell(x, y, board.getCell(x, y + 1));
            }
        }
        
        // 맨 아래에 회색 블록 추가 (구멍 1개)
        int holePosition = (int) (Math.random() * width);
        for (int x = 0; x < width; x++) {
            if (x != holePosition) {
                board.setCell(x, height - 1, 1000);  // 1000 = 회색 공격 블록
            }
        }
    }
    
    @Override
    public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
        this.itemMode = (gameMode == GameModeMessage.GameMode.ITEM);
        Platform.runLater(() -> {
            lobbyScene.setGameMode(itemMode ? "Item Mode" : "Normal Mode");
        });
    }
    
    @Override
    public void onGamePaused() {
        // TODO: 일시정지 구현
    }
    
    @Override
    public void onGameResumed() {
        // TODO: 재개 구현
    }
    
    @Override
    public void onError(String error) {
        System.err.println("Network error: " + error);
    }
    
    // ===== ServerMessageListener 구현 (호스트용) =====
    
    @Override
    public void onClientConnected(String clientId) {
        Platform.runLater(() -> {
            lobbyScene.setStatusText("Client connected!");
            opponentPlayerId = clientId;
        });
    }
    
    @Override
    public void onClientDisconnected(String clientId) {
        Platform.runLater(() -> {
            lobbyScene.setStatusText("Client disconnected");
        });
        handleGameOver("Opponent disconnected");
    }
}

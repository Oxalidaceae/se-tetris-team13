package team13.tetris.game.controller;

import java.io.IOException;
import java.util.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.input.KeyInputHandler;
import team13.tetris.network.client.TetrisClient;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;
import team13.tetris.network.server.TetrisSquadServer;
import team13.tetris.scenes.SquadGameScene;
import team13.tetris.scenes.SquadNetworkLobbyScene;
import team13.tetris.scenes.SquadResultScene;

/**
 * Squad PVP Controller (3 players: 1 host + 2 clients)
 *
 * <p>Manages 3-player game with: - Squad network lobby - Random attack distribution - Elimination
 * order tracking - Final rankings
 */
public class SquadGameController implements ClientMessageListener, ServerMessageListener {
    private final SceneManager manager;
    private final Settings settings;
    private final boolean isHost;
    private final String serverIP;

    // Network
    private TetrisSquadServer server; // Host only
    private TetrisClient client; // Client only
    private String myPlayerId;
    private final Map<Integer, String> playerIds = new HashMap<>(); // index -> playerId
    private final List<String> otherPlayerConnectionOrder =
            new ArrayList<>(); // For clients: track other players' connection order

    // Scenes
    private SquadNetworkLobbyScene lobbyScene;
    private SquadGameScene gameScene;

    // Game
    private GameEngine myEngine;
    private KeyInputHandler keyInputHandler;

    // State
    private boolean gameStarted = false;
    private boolean myReady = false;
    private boolean isAlive = true; // Track if local player is still alive

    // Squad-specific: Player management
    private boolean hostReady = false;
    private boolean client1Ready = false;
    private boolean client2Ready = false;
    private boolean client1Connected = false;
    private boolean client2Connected = false;

    private final Set<String> alivePlayers = new HashSet<>();
    private final List<String> eliminationOrder = new ArrayList<>();

    // Incoming attacks queue
    private final Queue<int[][]> myIncomingBlocks = new LinkedList<>();

    public SquadGameController(
            SceneManager manager, Settings settings, boolean isHost, String serverIP) {
        this.manager = manager;
        this.settings = settings;
        this.isHost = isHost;
        this.serverIP = serverIP;
    }

    public void initializeLobby() {
        lobbyScene = new SquadNetworkLobbyScene(manager, settings, isHost);

        if (isHost) {
            myPlayerId = "Host";
            playerIds.put(0, myPlayerId);
            alivePlayers.add(myPlayerId);

            try {
                server = new TetrisSquadServer(myPlayerId);
                server.setHostMessageListener(this);
                server.start();
                lobbyScene.setStatusText(
                        "Server started. Waiting for 2 clients...\nYour IP: "
                                + TetrisSquadServer.getServerIP());
            } catch (IOException e) {
                showError("Server Error", "Failed to start server: " + e.getMessage());
                manager.showMainMenu(settings);
                return;
            }

            // Poll server for client connections
            new Thread(this::pollServerConnections).start();
        } else {
            // Generate unique client ID
            myPlayerId = "Client-" + System.currentTimeMillis();
            connectToServer();
        }

        lobbyScene.getReadyButton().setOnAction(e -> handleReadyButton());
        lobbyScene.setOnCancelCallback(this::disconnect);
        manager.changeScene(lobbyScene.getScene());
    }

    private void pollServerConnections() {
        while (!gameStarted && server != null) {
            try {
                Thread.sleep(500);
                Platform.runLater(
                        () -> {
                            // Check connected clients count
                            // TODO: TetrisSquadServer needs to expose connected clients
                        });
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void connectToServer() {
        client = new TetrisClient(myPlayerId, serverIP, 12346); // Squad server port
        client.setMessageListener(this);

        Platform.runLater(() -> lobbyScene.setStatusText("Connecting..."));

        new Thread(
                        () -> {
                            if (client.connect()) {
                                settings.setRecentIP(serverIP);
                                Platform.runLater(
                                        () ->
                                                lobbyScene.setStatusText(
                                                        "Connected to Squad server!"));
                            } else {
                                Platform.runLater(
                                        () -> {
                                            showError(
                                                    "Connection Failed",
                                                    "Could not connect to Squad server.");
                                            manager.showMultiModeSelection(settings);
                                        });
                            }
                        })
                .start();
    }

    private void handleReadyButton() {
        if (gameStarted) {
            System.out.println("[SquadGameController] Ready button ignored - game already started");
            return; // 게임이 시작되면 Ready 버튼 무시
        }

        myReady = !myReady;
        System.out.println(
                "[SquadGameController] handleReadyButton - isHost: "
                        + isHost
                        + ", myReady: "
                        + myReady);

        if (isHost) {
            hostReady = myReady;
            if (lobbyScene != null) {
                lobbyScene.setHostReady(hostReady);
            }
            server.setHostReady(hostReady);
            System.out.println("[SquadGameController] Host setHostReady called: " + hostReady);

            if (myReady && lobbyScene != null) {
                lobbyScene.setModeSelectionDisabled(true);
                GameModeMessage.GameMode mode = lobbyScene.getSelectedGameMode();
                server.selectGameMode(mode);
            } else if (lobbyScene != null) {
                lobbyScene.setModeSelectionDisabled(false);
            }

            checkAllReady();
        } else {
            // Client: send ready message to server
            System.out.println("[SquadGameController] Client sending ready message: " + myReady);
            if (myReady) {
                client.sendMessage(ConnectionMessage.createPlayerReady(myPlayerId));
            } else {
                client.sendMessage(ConnectionMessage.createPlayerUnready(myPlayerId));
            }
        }
    }

    private void checkAllReady() {
        if (isHost && server != null && !gameStarted) {
            if (server.areAllPlayersReady()) {
                if (lobbyScene != null) {
                    lobbyScene.setStatusText("All players ready! Starting game...");
                    lobbyScene.setControlsDisabled(true);
                }
                server.startGame();
                startGame();
            }
        }
    }

    private void startGame() {
        if (gameStarted) {
            System.out.println("[SquadGameController] startGame() called but game already started");
            return;
        }

        System.out.println("[SquadGameController] startGame() - Starting game...");
        System.out.println(
                "[SquadGameController] Starting game - myPlayerId: "
                        + myPlayerId
                        + ", isHost: "
                        + isHost);
        System.out.println("[SquadGameController] Starting game - playerIds: " + playerIds);

        gameStarted = true; // 먼저 플래그 설정
        lobbyScene = null; // 그 다음 로비 씬 참조 제거

        Platform.runLater(
                () -> {
                    System.out.println(
                            "[SquadGameController] Platform.runLater - Creating game engine...");
                    // Create game engine with listener
                    Board board = new Board(10, 20);

                    // Game state listener
                    GameStateListener listener =
                            new GameStateListener() {
                                @Override
                                public void onBoardUpdated(Board board) {
                                    if (gameScene != null) {
                                        gameScene.updateLocalGrid();
                                    }
                                }

                                @Override
                                public void onPieceSpawned(Tetromino tetromino, int px, int py) {
                                    // Apply all incoming attacks when new piece spawns
                                    while (!myIncomingBlocks.isEmpty()) {
                                        int[][] attackPattern = myIncomingBlocks.poll();
                                        addIncomingBlockToBoard(myEngine, attackPattern);
                                    }

                                    // Update UI after applying attacks
                                    if (gameScene != null) {
                                        gameScene.updateLocalIncomingGrid(myIncomingBlocks);
                                        gameScene.updateLocalGrid();
                                    }

                                    // Send board state
                                    sendBoardUpdate();
                                }

                                @Override
                                public void onLinesCleared(int linesCleared) {
                                    if (linesCleared >= 2) {
                                        // Create attack pattern (same as NetworkGameController)
                                        int[][] attackPattern =
                                                createAttackPattern(linesCleared, myEngine);
                                        sendAttackPattern(attackPattern);
                                    }
                                }

                                @Override
                                public void onGameOver() {
                                    isAlive = false;
                                    myEngine.stopAutoDrop(); // Stop the engine
                                    handleLocalGameOver();
                                }

                                @Override
                                public void onNextPiece(Tetromino next) {
                                    if (gameScene != null && next != null) {
                                        gameScene.updateLocalPreview(next.getId());
                                    }
                                }

                                @Override
                                public void onScoreChanged(int score) {
                                    // Update score display if needed
                                }
                            };

                    myEngine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);

                    // Create game scene
                    gameScene = new SquadGameScene(manager, settings, myEngine, myPlayerId);

                    // Setup key input
                    Scene scene = gameScene.getScene();
                    scene.setOnKeyPressed(this::handleKeyPress);
                    scene.setOnKeyReleased(this::handleKeyRelease);

                    manager.changeScene(scene);

                    // 화면 가로 크기를 일시적으로 1.4배 늘리기
                    Stage stage = (Stage) scene.getWindow();
                    if (stage != null) {
                        double currentWidth = stage.getWidth();
                        stage.setWidth(currentWidth * 1.3);
                    }

                    myEngine.startNewGame();

                    gameScene.requestFocus();

                    // Start sending board updates
                    startBoardUpdateThread();
                });
    }

    private void startBoardUpdateThread() {
        new Thread(
                        () -> {
                            while (gameStarted) {
                                try {
                                    Thread.sleep(50); // Send updates 20 times per second
                                    sendBoardUpdate();
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                        })
                .start();
    }

    private void sendBoardUpdate() {
        if (myEngine == null) return;

        Board board = myEngine.getBoard();
        BoardUpdateMessage msg =
                new BoardUpdateMessage(
                        myPlayerId,
                        board.snapshot(),
                        0,
                        0,
                        0,
                        0,
                        false,
                        null,
                        -1,
                        0,
                        false,
                        null,
                        -1,
                        null,
                        myEngine.getScore(),
                        0,
                        0);

        if (isHost) {
            server.broadcast(msg);
        } else {
            client.sendMessage(msg);
        }
    }

    private void sendGameOver() {
        // Game over handling moved to handleLocalGameOver and onGameEnd
        // Don't call showSquadGameOver here - wait for final rankings
    }

    private int[][] createAttackPattern(int lines, GameEngine engine) {
        Board board = engine.getBoard();
        int width = board.getWidth();

        // Check if line clear was caused by gravity/split blocks
        boolean isGravityOrSplitClear = engine.isLastClearByGravityOrSplit();

        // Create pattern filled with gray blocks
        int[][] pattern = new int[lines][width];

        // Fill all with gray blocks first
        for (int r = 0; r < lines; r++) {
            for (int c = 0; c < width; c++) {
                pattern[r][c] = 1000; // Gray block
            }
        }

        if (isGravityOrSplitClear) {
            // Gravity/Split: Random hole in each line
            Random random = new Random();
            for (int r = 0; r < lines; r++) {
                int randomCol = random.nextInt(width);
                pattern[r][randomCol] = 0; // Empty space
            }
        } else {
            // Normal blocks: Exclude exact positions of last locked piece
            java.util.List<int[]> lockedCells = engine.getLastLockedCells();
            java.util.List<Integer> clearedLineIndices = engine.getClearedLineIndices();

            // If last block was in cleared lines, empty those positions
            if (clearedLineIndices != null) {
                for (int[] cell : lockedCells) {
                    int cellX = cell[0];
                    int cellY = cell[1];

                    // Check if this cell is in a cleared line
                    for (int i = 0; i < clearedLineIndices.size(); i++) {
                        if (clearedLineIndices.get(i) == cellY) {
                            // Find which line in the pattern and mark as empty
                            pattern[i][cellX] = 0; // Empty space
                            break;
                        }
                    }
                }
            }
        }

        return pattern;
    }

    private void addIncomingBlockToBoard(GameEngine engine, int[][] pattern) {
        Board board = engine.getBoard();
        int width = board.getWidth();
        int height = board.getHeight();
        int lines = pattern.length;

        // Move existing blocks up
        for (int y = 0; y < height - lines; y++) {
            for (int x = 0; x < width; x++) {
                board.setCell(x, y, board.getCell(x, y + lines));
            }
        }

        // Add incoming blocks at the bottom
        for (int i = 0; i < lines; i++) {
            int targetRow = height - lines + i;

            for (int x = 0; x < width; x++) {
                board.setCell(x, targetRow, pattern[i][x]);
            }
        }
    }

    private void sendAttackPattern(int[][] attackPattern) {
        int linesCleared = attackPattern.length;
        AttackMessage attackMsg =
                new AttackMessage(myPlayerId, linesCleared, linesCleared, attackPattern);
        if (isHost) {
            server.distributeHostAttack(attackMsg);
        } else {
            client.sendMessage(attackMsg);
        }
    }

    private void handleLocalGameOver() {
        if (isHost && server != null) {
            server.recordElimination(myPlayerId);
        }
        // Send game over message
        ConnectionMessage gameOverMsg =
                ConnectionMessage.createGameOver(myPlayerId, "Player eliminated");
        if (isHost) {
            server.broadcast(gameOverMsg);
        } else {
            client.sendMessage(gameOverMsg);
        }

        // Enter spectator mode - gray out board, disable input
        Platform.runLater(
                () -> {
                    if (gameScene != null) {
                        gameScene.setLocalBoardGrayedOut(true);
                        gameScene.showGameOverLabel(true);
                    }
                });

        // Don't show game over dialog yet - wait for final rankings
    }

    private void showSquadGameOver(List<String> rankings) {
        Platform.runLater(
                () -> {
                    SquadResultScene resultScene =
                            new SquadResultScene(manager, settings, rankings, playerIds);
                    manager.changeScene(resultScene.getScene());
                });
    }

    private void handleKeyPress(KeyEvent e) {
        if (!gameStarted || myEngine == null) return;

        KeyCode code = e.getCode();
        String keyString = code.toString();

        String leftKey = settings.getKeyLeft();
        String rightKey = settings.getKeyRight();
        String downKey = settings.getKeyDown();
        String rotateKey = settings.getKeyRotate();
        String dropKey = settings.getKeyDrop();
        String pauseKey = settings.getPause();

        // Player 1 controls for my board
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
            // If player is the last one alive (winner), end game and show rankings
            if (isHost && server != null && server.getAlivePlayers().size() == 1 && isAlive) {
                server.endGame(); // Trigger game end with rankings
            } else {
                disconnect();
                manager.showMainMenu(settings);
            }
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        // Handle key release if needed
    }

    private void disconnect() {
        gameStarted = false;
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.disconnect();
        }
    }

    private void showError(String title, String message) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                });
    }

    // ===== ClientMessageListener Implementation =====

    @Override
    public void onConnectionAccepted() {
        Platform.runLater(
                () -> {
                    lobbyScene.setStatusText("Connected to server!");
                    // 클라이언트는 자신의 ID를 0번 슬롯에 등록
                    playerIds.put(0, myPlayerId);
                });
    }

    @Override
    public void onConnectionRejected(String reason) {
        Platform.runLater(
                () -> {
                    showError("Connection Rejected", reason);
                    manager.showMultiModeSelection(settings);
                });
    }

    @Override
    public void onServerDisconnected(String reason) {
        Platform.runLater(
                () -> {
                    showError("Server Disconnected", reason);
                    disconnect();
                    manager.showMainMenu(settings);
                });
    }

    @Override
    public void onPlayerReady(String playerId) {
        // Deprecated: Now handled by onLobbyStateUpdate()
        // This method is kept for interface compatibility but does nothing
    }

    @Override
    public void onPlayerUnready(String playerId) {
        // Deprecated: Now handled by onLobbyStateUpdate()
        // This method is kept for interface compatibility but does nothing
    }

    private int getClientIndex(String playerId) {
        for (Map.Entry<Integer, String> entry : playerIds.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                return entry.getKey();
            }
        }
        return -1; // Unknown player
    }

    @Override
    public void onGameStart() {
        startGame();
    }

    @Override
    public void onGameOver(String reason) {
        // This is called when we receive GAME_OVER message
        // For now, wait for GameEndMessage with rankings
    }

    public void onGameEnd(List<String> rankings) {
        // Called when game ends with final rankings
        showSquadGameOver(rankings);
    }

    @Override
    public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
        if (gameScene == null) return;

        String senderId = boardUpdate.getPlayerId();
        if (senderId.equals(myPlayerId)) return; // 자기 자신의 보드는 무시

        Platform.runLater(
                () -> {
                    // 모든 플레이어 ID 수집 (자신 제외)
                    List<String> allOpponents = new ArrayList<>();

                    if (isHost) {
                        // 호스트 시점: 클라이언트 1과 클라이언트 2가 상대방
                        for (Map.Entry<Integer, String> entry : playerIds.entrySet()) {
                            int idx = entry.getKey();
                            String pid = entry.getValue();
                            if (idx != 0) { // 0번은 호스트 자신
                                allOpponents.add(pid);
                            }
                        }
                    } else {
                        // 클라이언트 시점: 호스트와 다른 클라이언트가 상대방
                        for (Map.Entry<Integer, String> entry : playerIds.entrySet()) {
                            String pid = entry.getValue();
                            if (!pid.equals(myPlayerId)) {
                                allOpponents.add(pid);
                            }
                        }
                    }

                    // 일관된 순서 보장
                    Collections.sort(allOpponents);

                    int opponentSlot = allOpponents.indexOf(senderId);

                    if (opponentSlot == 0) {
                        // 첫 번째 상대방 -> opponent1
                        gameScene.updateOpponent1(
                                boardUpdate.getBoardState(),
                                boardUpdate.getCurrentPieceX(),
                                boardUpdate.getCurrentPieceY(),
                                boardUpdate.getCurrentPieceType(),
                                boardUpdate.getCurrentPieceRotation(),
                                boardUpdate.getNextPieceType(),
                                null,
                                boardUpdate.getScore());
                    } else if (opponentSlot == 1) {
                        // 두 번째 상대방 -> opponent2
                        gameScene.updateOpponent2(
                                boardUpdate.getBoardState(),
                                boardUpdate.getCurrentPieceX(),
                                boardUpdate.getCurrentPieceY(),
                                boardUpdate.getCurrentPieceType(),
                                boardUpdate.getCurrentPieceRotation(),
                                boardUpdate.getNextPieceType(),
                                null,
                                boardUpdate.getScore());
                    }
                });
    }

    @Override
    public void onAttackReceived(AttackMessage attackMessage) {
        if (attackMessage.getSenderId().equals(myPlayerId)) {
            return; // 자신의 공격은 무시
        }

        int[][] pattern = attackMessage.getAttackPattern();
        if (pattern != null && pattern.length > 0) {
            // 공격 패턴을 큐에 추가
            myIncomingBlocks.offer(pattern);

            // UI 업데이트
            Platform.runLater(
                    () -> {
                        if (gameScene != null) {
                            gameScene.updateLocalIncomingGrid(myIncomingBlocks);
                        }
                    });
        }
    }

    @Override
    public void onGamePaused() {
        // TODO
    }

    @Override
    public void onGameResumed() {
        // TODO
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> showError("Error", error));
    }

    @Override
    public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
        Platform.runLater(
                () -> {
                    if (!isHost) {
                        lobbyScene.setGameMode(gameMode.toString());
                    }
                });
    }

    // ===== ServerMessageListener Implementation =====

    @Override
    public void onClientConnected(String clientId) {
        Platform.runLater(
                () -> {
                    // Update client connection indicators
                    if (!client1Connected) {
                        client1Connected = true;
                        playerIds.put(1, clientId);
                        lobbyScene.setClient1Connected(true);
                    } else if (!client2Connected) {
                        client2Connected = true;
                        playerIds.put(2, clientId);
                        lobbyScene.setClient2Connected(true);
                    }

                    int connectedCount = connectedClients();
                    lobbyScene.setStatusText(
                            "Server started. "
                                    + connectedCount
                                    + "/2 clients connected\nYour IP: "
                                    + TetrisSquadServer.getServerIP());
                });
    }

    @Override
    public void onLobbyStateUpdate(List<LobbyStateMessage.PlayerState> playerStates) {
        Platform.runLater(
                () -> {
                    // 호스트 또는 클라이언트 모두 동일하게 처리
                    for (LobbyStateMessage.PlayerState state : playerStates) {
                        String playerId = state.getPlayerId();
                        int order = state.getOrder();
                        boolean ready = state.isReady();

                        if (isHost) {
                            // 호스트: order로 직접 매핑
                            if (order == 0) {
                                // 호스트 자신
                                hostReady = ready;
                                if (lobbyScene != null) {
                                    lobbyScene.setHostReady(ready);
                                }
                            } else if (order == 1) {
                                // 첫 번째 클라이언트
                                if (!playerIds.containsKey(1)
                                        || !playerIds.get(1).equals(playerId)) {
                                    playerIds.put(1, playerId);
                                    client1Connected = true;
                                    if (lobbyScene != null) {
                                        lobbyScene.setClient1Connected(true);
                                    }
                                }
                                client1Ready = ready;
                                if (lobbyScene != null) {
                                    lobbyScene.setClient1Ready(ready);
                                }
                            } else if (order == 2) {
                                // 두 번째 클라이언트
                                if (!playerIds.containsKey(2)
                                        || !playerIds.get(2).equals(playerId)) {
                                    playerIds.put(2, playerId);
                                    client2Connected = true;
                                    if (lobbyScene != null) {
                                        lobbyScene.setClient2Connected(true);
                                    }
                                }
                                client2Ready = ready;
                                if (lobbyScene != null) {
                                    lobbyScene.setClient2Ready(ready);
                                }
                            }
                        } else {
                            // 클라이언트: 자신 제외하고 서버의 order를 기준으로 매핑
                            if (playerId.equals(myPlayerId)) {
                                // 자신의 준비 상태 - 버튼과 상태 라벨 모두 업데이트
                                myReady = ready;

                                // 자신의 playerIds 맵에 자신을 추가
                                if (!playerIds.containsKey(order)) {
                                    playerIds.put(order, myPlayerId);
                                    System.out.println(
                                            "[SquadGameController] Client added self to playerIds: index="
                                                    + order
                                                    + ", id="
                                                    + myPlayerId);
                                }

                                if (lobbyScene != null) {
                                    lobbyScene
                                            .getReadyButton()
                                            .setText(ready ? "Unready" : "Ready");

                                    // 자신의 order에 따라 적절한 라벨 업데이트
                                    if (order == 1) {
                                        if (!client1Connected) {
                                            client1Connected = true;
                                            lobbyScene.setClient1Connected(true);
                                        }
                                        lobbyScene.setClient1Ready(ready);
                                    } else if (order == 2) {
                                        if (!client2Connected) {
                                            client2Connected = true;
                                            lobbyScene.setClient2Connected(true);
                                        }
                                        lobbyScene.setClient2Ready(ready);
                                    }
                                }
                            } else {
                                // 다른 플레이어: 서버가 보낸 order를 기준으로 표시
                                // order 0 = Host, order 1 = Client1, order 2 = Client2

                                if (!otherPlayerConnectionOrder.contains(playerId)) {
                                    otherPlayerConnectionOrder.add(playerId);
                                }

                                if (!playerIds.containsValue(playerId)) {
                                    playerIds.put(order, playerId);
                                }

                                // order가 0이면 Host
                                if (order == 0) {
                                    hostReady = ready;
                                    if (lobbyScene != null) {
                                        lobbyScene.setHostReady(ready);
                                    }
                                }
                                // order가 1이면 첫 번째 클라이언트
                                else if (order == 1) {
                                    if (!client1Connected) {
                                        client1Connected = true;
                                        if (lobbyScene != null) {
                                            lobbyScene.setClient1Connected(true);
                                        }
                                    }
                                    client1Ready = ready;
                                    if (lobbyScene != null) {
                                        lobbyScene.setClient1Ready(ready);
                                    }
                                }
                                // order가 2이면 두 번째 클라이언트
                                else if (order == 2) {
                                    if (!client2Connected) {
                                        client2Connected = true;
                                        if (lobbyScene != null) {
                                            lobbyScene.setClient2Connected(true);
                                        }
                                    }
                                    client2Ready = ready;
                                    if (lobbyScene != null) {
                                        lobbyScene.setClient2Ready(ready);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onClientDisconnected(String clientId) {
        Platform.runLater(
                () -> {
                    // Determine which client disconnected
                    int clientIndex = getClientIndex(clientId);
                    if (clientIndex == 1) {
                        client1Connected = false;
                        client1Ready = false;
                        lobbyScene.setClient1Connected(false);
                        lobbyScene.setClient1Ready(false);
                    } else if (clientIndex == 2) {
                        client2Connected = false;
                        client2Ready = false;
                        lobbyScene.setClient2Connected(false);
                        lobbyScene.setClient2Ready(false);
                    }

                    int connectedCount = connectedClients();
                    lobbyScene.setStatusText(
                            "Client disconnected. " + connectedCount + "/2 clients connected");

                    if (gameStarted) {
                        showError("Player Disconnected", "A player has left the game.");
                        disconnect();
                        manager.showMainMenu(settings);
                    }
                });
    }

    private int connectedClients() {
        int count = 0;
        if (client1Connected) count++;
        if (client2Connected) count++;
        return count;
    }

    // Note: onPlayerReady/Unready already implemented for ClientMessageListener
    // They work for both client and host modes

    @Override
    public void onCountdownStart() {
        // TODO: Implement countdown if needed
    }
}

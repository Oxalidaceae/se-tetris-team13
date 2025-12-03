package team13.tetris.game.controller;

import java.io.IOException;
import java.util.*;
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
import team13.tetris.audio.SoundManager;
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
    private long lastReadyChangeTime = 0; // Track when ready state was last changed
    private boolean isAlive = true; // Track if local player is still alive
    private boolean disconnectionHandled = false; // Prevent duplicate disconnect popups
    private boolean paused = false;
    private boolean pauseInitiatedByMe = false;

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

    // 카운트다운
    private Timeline countdownTimeline;
    private final IntegerProperty countdownSeconds = new SimpleIntegerProperty();

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
            // 고유한 임시 클라이언트 ID 생성
            myPlayerId = "TempClient-" + System.currentTimeMillis();
            connectToServer();
        }

        lobbyScene.getReadyButton().setOnAction(e -> handleReadyButton());
        lobbyScene.setOnCancelCallback(this::disconnect);
        lobbyScene.setOnSendChatCallback(this::handleSendChat);
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

    // 채팅 메시지 전송 처리
    private void handleSendChat() {
        String message = lobbyScene.getChatInput().trim();
        if (message.isEmpty()) {
            return;
        }
        
        if (isHost && server != null) {
            server.sendChatMessage(message);
        } else if (!isHost && client != null) {
            client.sendChatMessage(message);
        }
        
        lobbyScene.clearChatInput();
    }

    private void handleReadyButton() {
        if (gameStarted) {
            System.out.println("[SquadGameController] Ready button ignored - game already started");
            return; // 게임이 시작되면 Ready 버튼 무시
        }

        myReady = !myReady;
        lastReadyChangeTime = System.currentTimeMillis(); // Track when ready state changed
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
            // Client: send ready message to server and update UI immediately
            System.out.println("[SquadGameController] Client sending ready message: " + myReady);
            
            // Update button text and style immediately for responsive UI
            if (lobbyScene != null) {
                lobbyScene.getReadyButton().setText(myReady ? "Cancel Ready" : "Ready");
                
                // Apply or remove selected style
                if (myReady) {
                    lobbyScene.getReadyButton().getStyleClass().add("selected");
                } else {
                    lobbyScene.getReadyButton().getStyleClass().remove("selected");
                }
            }
            
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
                    lobbyScene.setStatusText("All players ready! Starting countdown...");
                }
                // Server will broadcast countdown and then start game after 5 seconds
                server.checkAllReady();
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

        // Play game BGM when starting the squad game
        SoundManager.getInstance().playGameBGM();

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
                    // Set callback to return to lobby instead of disconnecting
                    resultScene.setOnReturnToLobbyCallback(this::returnToLobby);
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
                togglePause();
            }
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        // Handle key release if needed
    }

    private void returnToLobby() {
        // Reset game state without disconnecting network
        gameStarted = false;
        gameScene = null;
        myEngine = null;
        isAlive = true;
        myIncomingBlocks.clear();

        // Don't reset ready states here - they were already reset by server's resetGameState()
        // which was called in endGame() and broadcasted via onLobbyStateUpdate()
        // Do NOT call resetGameState() again here - it would reset states of players who
        // returned to lobby earlier and already pressed ready
        
        // Client: don't send unready message - server already reset all states in endGame()

        // Recreate lobby scene WITHOUT reconnecting
        Platform.runLater(
                () -> {
                    recreateLobbyScene();
                });
    }

    private void recreateLobbyScene() {
        // Create new lobby scene but keep existing network connections
        lobbyScene = new SquadNetworkLobbyScene(manager, settings, isHost);

        if (isHost) {
            // Update lobby UI with current connection status
            int connectedCount = connectedClients();
            lobbyScene.setStatusText(
                    "Server started. "
                            + connectedCount
                            + "/2 clients connected\nYour IP: "
                            + TetrisSquadServer.getServerIP());

            // Restore client connection indicators only (not ready states - they're reset)
            if (client1Connected) {
                lobbyScene.setClient1Connected(true);
            }
            if (client2Connected) {
                lobbyScene.setClient2Connected(true);
            }

            // Ready states are all false after reset - no need to set
        } else {
            // Client: show connected status
            lobbyScene.setStatusText("Connected to Squad server!");

            // Restore connection indicators for clients (not ready states)
            if (client1Connected) {
                lobbyScene.setClient1Connected(true);
            }
            if (client2Connected) {
                lobbyScene.setClient2Connected(true);
            }

            // Ready states will be updated via onLobbyStateUpdate from server
        }

        lobbyScene.getReadyButton().setOnAction(e -> handleReadyButton());
        lobbyScene.setOnCancelCallback(this::disconnect);

        // Set initial button text
        // Host always shows "Start", clients show "Ready" initially (will be updated by onLobbyStateUpdate)
        if (isHost) {
            lobbyScene.getReadyButton().setText("Start");
            
            // Request server to broadcast current lobby state so returning players see current ready states
            if (server != null) {
                server.broadcastLobbyState();
            }
        } else {
            // Always start with "Ready" - server will send updated state via onLobbyStateUpdate
            lobbyScene.getReadyButton().setText("Ready");
            
            // Request current lobby state from server by sending unready then ready if needed
            // This triggers server to broadcast current state to all players
            if (client != null) {
                // Send a dummy unready to trigger server's broadcastLobbyState
                // Server will respond with current state of all players
                client.sendMessage(ConnectionMessage.createPlayerUnready(myPlayerId));
            }
        }

        manager.changeScene(lobbyScene.getScene());
    }

    private void disconnect() {
        gameStarted = false;
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.disconnect();
        }
        disconnectionHandled = false; // Reset for next game
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

    // ===== Pause/Resume Methods (from NetworkGameController) =====

    private void togglePause() {
        if (!gameStarted || myEngine == null) return;

        if (!paused) {
            // 내가 퍼즈 시작
            pauseInitiatedByMe = true;
            applyLocalPause();
            sendPauseToNetwork();
        } else {
            applyLocalResume();
            sendResumeToNetwork();
        }
    }

    private void sendPauseToNetwork() {
        ConnectionMessage pauseMsg =
                new ConnectionMessage(
                        MessageType.PAUSE, myPlayerId, "Game paused by " + myPlayerId);
        if (isHost && server != null) {
            server.broadcast(pauseMsg);
        } else if (!isHost && client != null) {
            client.sendMessage(pauseMsg);
        }
    }

    private void sendResumeToNetwork() {
        ConnectionMessage resumeMsg =
                new ConnectionMessage(
                        MessageType.RESUME, myPlayerId, "Game resumed by " + myPlayerId);
        if (isHost && server != null) {
            server.broadcast(resumeMsg);
        } else if (!isHost && client != null) {
            client.sendMessage(resumeMsg);
        }
    }

    private Stage pauseDialog = null;
    private Stage remotePauseDialog = null;

    private void applyLocalPause() {
        if (paused) return;
        paused = true;
        if (myEngine != null) {
            myEngine.stopAutoDrop();
        }
        // 내가 퍼즈를 건 경우 전체 메뉴, 아니면 단순 안내
        if (pauseInitiatedByMe) {
            showPauseWindow();
        } else {
            showRemotePauseWindow();
        }
    }

    private void applyRemotePause() {
        if (paused) return;
        pauseInitiatedByMe = false;
        applyLocalPause(); // 내부에서 pauseInitiatedByMe에 따라 창 선택
    }

    private void applyLocalResume() {
        if (!paused) return;
        paused = false;

        // Pause 창이 열려있다면 닫기
        if (pauseDialog != null && pauseDialog.isShowing()) {
            pauseDialog.close();
            pauseDialog = null;
        }
        if (remotePauseDialog != null && remotePauseDialog.isShowing()) {
            remotePauseDialog.close();
            remotePauseDialog = null;
        }

        if (myEngine != null) {
            myEngine.startAutoDrop();
        }
        pauseInitiatedByMe = false;
    }

    private void showPauseWindow() {
        Platform.runLater(
                () -> {
                    pauseDialog = new Stage();
                    pauseDialog.initModality(Modality.APPLICATION_MODAL);
                    pauseDialog.initOwner(gameScene.getScene().getWindow());

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
                    final int[] selected = new int[] {0}; // 기본 Resume 선택
                    applySelection(resume, mainMenu, quit, selected[0]);

                    dialogScene.setOnKeyPressed(
                            ev -> {
                                if (ev.getCode() == KeyCode.UP) {
                                    selected[0] = (selected[0] == 0) ? 0 : selected[0] - 1;
                                    applySelection(resume, mainMenu, quit, selected[0]);
                                } else if (ev.getCode() == KeyCode.DOWN) {
                                    selected[0] = (selected[0] == 2) ? 2 : selected[0] + 1;
                                    applySelection(resume, mainMenu, quit, selected[0]);
                                } else if (ev.getCode() == KeyCode.ENTER) {
                                    pauseDialog.close();
                                    pauseDialog = null;

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
                                                });
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
                                                });
                                    }
                                } else if (ev.getCode() == KeyCode.ESCAPE) {
                                    // ESC로 Resume
                                    pauseDialog.close();
                                    pauseDialog = null;
                                    applyLocalResume();
                                    sendResumeToNetwork();
                                }
                            });

                    // 창이 닫힐 때 참조 정리
                    pauseDialog.setOnCloseRequest(e -> pauseDialog = null);

                    pauseDialog.setScene(dialogScene);
                    pauseDialog.setTitle("Paused");
                    pauseDialog.setWidth(220);
                    pauseDialog.setHeight(150);
                    pauseDialog.showAndWait();
                });
    }

    private void showRemotePauseWindow() {
        Platform.runLater(
                () -> {
                    if (remotePauseDialog != null && remotePauseDialog.isShowing()) return;
                    remotePauseDialog = new Stage();
                    remotePauseDialog.initModality(Modality.NONE);
                    remotePauseDialog.initOwner(gameScene.getScene().getWindow());
                    Label pausedLabel = new Label("Paused");
                    pausedLabel.getStyleClass().add("pause-option");
                    VBox box = new VBox(12, pausedLabel);
                    box.setAlignment(Pos.CENTER);
                    box.getStyleClass().add("pause-box");
                    Scene dialogScene = new Scene(box, 160, 80);
                    dialogScene.getStylesheets().addAll(gameScene.getScene().getStylesheets());
                    remotePauseDialog.setScene(dialogScene);
                    remotePauseDialog.setTitle("Paused");
                    remotePauseDialog.setResizable(false);
                    remotePauseDialog.setOnCloseRequest(e -> remotePauseDialog = null);
                    remotePauseDialog.show();
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

    // ===== ClientMessageListener Implementation =====

    @Override
    public void onConnectionAccepted(String assignedClientId) {
        Platform.runLater(
                () -> {
                    // 서버가 할당한 새 ID로 업데이트
                    System.out.println("[SquadGameController] Updating myPlayerId from " + myPlayerId + " to " + assignedClientId);
                    myPlayerId = assignedClientId;
                    
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
        if (disconnectionHandled) return;
        disconnectionHandled = true;
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
        Platform.runLater(() -> {
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            startGame();
        });
    }

    @Override
    public void onGameOver(String reason) {
        // 다른 플레이어의 연결이 끊어진 경우만 처리
        if (reason != null && reason.contains("disconnected") && !reason.contains("Server")) {
            if (disconnectionHandled) return;
            disconnectionHandled = true;
            if (gameStarted) {
                Platform.runLater(
                        () -> {
                            showError("Player Disconnected", "A player has left the game.");
                            disconnect();
                            manager.showMainMenu(settings);
                        });
            }
        }
        // 일반적인 게임 오버의 경우: GameEndMessage를 기다림
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
        // 상대가 퍼즈: 단순 안내만 표시
        Platform.runLater(this::applyRemotePause);
    }

    @Override
    public void onGameResumed() {
        Platform.runLater(this::applyLocalResume);
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
                                myReady = ready;
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
                            // 클라이언트: playerId로 자신인지 명확하게 판단
                            if (playerId.equals(myPlayerId)) {
                                // 자신의 playerIds 맵에 자신을 추가
                                if (!playerIds.containsKey(order)) {
                                    playerIds.put(order, myPlayerId);
                                    System.out.println(
                                            "[SquadGameController] Client added self to playerIds: index="
                                                    + order
                                                    + ", id="
                                                    + myPlayerId);
                                }

                                // 자신의 준비 상태 - 최근 변경이 없었다면 서버 상태로 동기화
                                long timeSinceLastChange = System.currentTimeMillis() - lastReadyChangeTime;
                                if (timeSinceLastChange > 500) {
                                    // 500ms 이상 지났으면 서버 상태를 신뢰
                                    System.out.println(
                                            "[SquadGameController] onLobbyStateUpdate - Updating myReady from "
                                                    + myReady
                                                    + " to "
                                                    + ready);
                                    myReady = ready;

                                    if (lobbyScene != null) {
                                        lobbyScene
                                                .getReadyButton()
                                                .setText(ready ? "Cancel Ready" : "Ready");
                                        
                                        // Apply or remove selected style
                                        if (ready) {
                                            lobbyScene.getReadyButton().getStyleClass().add("selected");
                                        } else {
                                            lobbyScene.getReadyButton().getStyleClass().remove("selected");
                                        }
                                    }
                                } else {
                                    System.out.println(
                                            "[SquadGameController] onLobbyStateUpdate - Ignoring stale update (timeSince="
                                                    + timeSinceLastChange
                                                    + "ms)");
                                }

                                // 자신의 order에 따라 적절한 라벨 업데이트 (항상 수행)
                                if (lobbyScene != null) {
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
                    // 게임이 시작된 경우 즉시 대전 종료
                    if (gameStarted) {
                        showError("Player Disconnected", "A player has left the game.");
                        disconnect();
                        manager.showMainMenu(settings);
                        return;
                    }

                    // 로비 중인 경우: Determine which client disconnected
                    int clientIndex = getClientIndex(clientId);
                    if (clientIndex == 1) {
                        client1Connected = false;
                        client1Ready = false;
                        if (lobbyScene != null) {
                            lobbyScene.setClient1Connected(false);
                            lobbyScene.setClient1Ready(false);
                        }
                    } else if (clientIndex == 2) {
                        client2Connected = false;
                        client2Ready = false;
                        if (lobbyScene != null) {
                            lobbyScene.setClient2Connected(false);
                            lobbyScene.setClient2Ready(false);
                        }
                    }

                    int connectedCount = connectedClients();
                    if (lobbyScene != null) {
                        lobbyScene.setStatusText(
                                "Client disconnected. " + connectedCount + "/2 clients connected");
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
        Platform.runLater(() -> {
            if (lobbyScene != null) {
                // 기존 타임라인이 실행 중이면 중지
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                    countdownTimeline = null;
                }
                
                lobbyScene.setControlsDisabled(true);
                lobbyScene.setStatusText("Game starting in...");
                
                // 카운트다운 전에 기존 바인딩 해제 및 스타일 정리
                lobbyScene.getReadyButton().textProperty().unbind();
                lobbyScene.getReadyButton().getStyleClass().remove("selected");
                
                countdownSeconds.set(5);
                
                // 준비 버튼 텍스트를 카운트다운에 바인딩
                lobbyScene.getReadyButton().textProperty().bind(countdownSeconds.asString());
                
                countdownTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> {
                        int current = countdownSeconds.get();
                        if (current > 0) {
                            countdownSeconds.set(current - 1);
                        }
                    })
                );
                countdownTimeline.setCycleCount(6); // 5, 4, 3, 2, 1, 0까지 총 6번
                countdownTimeline.setOnFinished(e -> {
                    lobbyScene.getReadyButton().textProperty().unbind();
                    lobbyScene.getReadyButton().setText("Starting...");
                });
                countdownTimeline.play();
            }
        });
    }

    @Override
    public void onChatMessageReceived(String senderId, String message) {
        System.out.println("[SquadGameController] Chat message received from " + senderId + ": " + message);
        if (lobbyScene != null) {
            lobbyScene.appendChatMessage(senderId, message);
        }
    }
}

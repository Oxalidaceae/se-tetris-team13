package team13.tetris.network.server;

import team13.tetris.network.protocol.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TetrisServer {
    private static final int DEFAULT_PORT = 12345;
    private static final int MAX_PLAYERS = 1;  // 서버 자신(호스트) + 클라이언트 1명
    
    private final int port;
    private final String hostPlayerId;  
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> connectedClients;
    private final ExecutorService threadPool;
    private volatile boolean isRunning = false;
    
    // 게임 상태
    private volatile boolean gameInProgress = false;
    private final Object gameLock = new Object();
    
    // P2P 상태 관리
    private GameModeMessage.GameMode selectedGameMode = null;
    private final Map<String, Boolean> playerReadyStates = new ConcurrentHashMap<>();
    
    // 호스트 플레이어 메시지 리스너
    public interface HostMessageListener {
        void onClientConnected(String clientId);
        void onClientDisconnected(String clientId);
        void onPlayerReady(String playerId);
        void onGameStart();
        void onGameOver(String reason);
        void onInputReceived(InputMessage inputMessage);
        void onBoardUpdate(BoardUpdateMessage boardUpdate);
        void onAttackReceived(AttackMessage attackMessage);
        void onLinesClearedReceived(LinesClearedMessage linesClearedMessage);
        void onGamePaused();
        void onGameResumed();
    }
    
    private HostMessageListener hostMessageListener;
    
    public TetrisServer(String hostPlayerId, int port) {
        this.hostPlayerId = hostPlayerId;
        this.port = port;
        this.connectedClients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }
    
    public TetrisServer(String hostPlayerId) {
        this(hostPlayerId, DEFAULT_PORT);
    }
    
    // 호스트 메시지 리스너 설정
    public void setHostMessageListener(HostMessageListener listener) {
        this.hostMessageListener = listener;
    }
    
    // 호스트 플레이어 ID 반환
    public String getHostPlayerId() {
        return hostPlayerId;
    }
    
    // 서버 시작
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        
        System.out.println("Tetris Server started on port " + port);
        System.out.println("aiting for players to connect...");
        
        // 클라이언트 접속 대기 스레드
        threadPool.submit(this::acceptClients);
    }
    
    
    // 클라이언트 접속
    private void acceptClients() {
        while (isRunning && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                synchronized (connectedClients) {
                    if (connectedClients.size() >= MAX_PLAYERS) {
                        rejectConnection(clientSocket, "Server is full");
                        continue;
                    }
                }
                
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // 클라이언트 핸들러 생성 및 시작
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.submit(handler);
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }
    
    
    // 연결을 거절
    private void rejectConnection(Socket clientSocket, String reason) {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            ConnectionMessage rejection = ConnectionMessage.createConnectionRejected("server", reason);
            out.writeObject(rejection);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send rejection message: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // 무시
            }
        }
    }
    
    
    // 클라이언트 연결을 등록
    public synchronized boolean registerClient(String playerId, ClientHandler handler) {
        if (connectedClients.size() >= MAX_PLAYERS) {
            return false;
        }
        
        connectedClients.put(playerId, handler);
        System.out.println("Player registered: " + playerId + " (" + connectedClients.size() + "/" + MAX_PLAYERS + ")");
        
        // 호스트에게 클라이언트 연결 알림
        if (hostMessageListener != null) {
            hostMessageListener.onClientConnected(playerId);
        }
        
        // 클라이언트가 접속하면 대기 상태 (양쪽이 ready해야 게임 시작)
        if (connectedClients.size() == MAX_PLAYERS) {
            System.out.println("Client connected! Waiting for both players to be ready...");
        }
        
        return true;
    }
    
    
    // 클라이언트 연결을 해제합    
    public synchronized void unregisterClient(String playerId) {
        ClientHandler removed = connectedClients.remove(playerId);
        if (removed != null) {
            System.out.println("Player disconnected: " + playerId + " (" + connectedClients.size() + "/" + MAX_PLAYERS + ")");
            
            // 호스트에게 클라이언트 연결 해제 알림
            if (hostMessageListener != null) {
                hostMessageListener.onClientDisconnected(playerId);
            }
            
            // 게임 중이면 게임 종료
            if (gameInProgress) {
                endGame("Player " + playerId + " disconnected");
            }
        }
    }
    
    // 게임모드 선택
    public void selectGameMode(GameModeMessage.GameMode gameMode) {
        this.selectedGameMode = gameMode;
        
        GameModeMessage gameModeMsg = new GameModeMessage("server", gameMode);
        broadcastMessage(gameModeMsg);
        
        System.out.println("Game mode selected: " + gameMode);
    }
    
    // 플레이어 준비 상태를 설정 (확장 가능)
    public void setPlayerReady(String playerId, boolean ready) {
        if (ready) {
            playerReadyStates.put(playerId, true);
            System.out.println("Player " + playerId + " is ready!");
        } else {
            playerReadyStates.put(playerId, false);
            System.out.println("Player " + playerId + " is not ready!");
        }
        
        // 모든 플레이어가 준비되면 게임 시작
        checkAllReady();
    }
    
    // 서버(호스트) 준비 상태를 설정
    public void setServerReady(boolean ready) {
        setPlayerReady(hostPlayerId, ready);
    }
    
    // 클라이언트 준비 상태를 설정 (하위 호환성)
    public void setClientReady(boolean ready) {
        // 첫 번째 클라이언트의 준비 상태 설정
        if (!connectedClients.isEmpty()) {
            String firstClientId = connectedClients.keySet().iterator().next();
            setPlayerReady(firstClientId, ready);
        }
    }
    
    // 모든 플레이어가 준비되었는지 확인
    private void checkAllReady() {
        // 게임모드가 선택되지 않았으면 시작 불가
        if (selectedGameMode == null) {
            return;
        }
        
        // 호스트가 준비되지 않았으면 시작 불가
        if (!playerReadyStates.getOrDefault(hostPlayerId, false)) {
            return;
        }
        
        // 모든 접속한 클라이언트가 준비되었는지 확인
        for (String clientId : connectedClients.keySet()) {
            if (!playerReadyStates.getOrDefault(clientId, false)) {
                return;
            }
        }
        
        // 모든 조건 만족 시 게임 시작
        System.out.println("All players ready! Starting game...");
        startGame();
    }
    
    // 현재 서버의 IP 주소 반환
    public String getServerIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }
    
    // 선택된 게임모드 반환
    public GameModeMessage.GameMode getSelectedGameMode() {
        return selectedGameMode;
    }
    
    
    // 연결된 클라이언트 수 반환
    public int getClientCount() {
        return connectedClients.size();
    }
    
    
    // 서버(호스트) 준비 상태 반환
    public boolean isServerReady() {
        return playerReadyStates.getOrDefault(hostPlayerId, false);
    }
    
    
    // 클라이언트 준비 상태 반환 (첫 번째 클라이언트).
    public boolean isClientReady() {
        if (connectedClients.isEmpty()) {
            return false;
        }
        String firstClientId = connectedClients.keySet().iterator().next();
        return playerReadyStates.getOrDefault(firstClientId, false);
    }
    
    
    // 특정 플레이어의 준비 상태 반환
    public boolean isPlayerReady(String playerId) {
        return playerReadyStates.getOrDefault(playerId, false);
    }
    
    
    // P2P 준비 상태 초기화
    public void resetReadyStates() {
        playerReadyStates.clear();
        selectedGameMode = null;
    }
    
    // 게임 시작
    private void startGame() {
        synchronized (gameLock) {
            if (gameInProgress) {
                return;
            }
            gameInProgress = true;
        }
        
        System.out.println("Game starting!");
        
        // 클라이언트에게 게임 시작 메시지 전송
        ConnectionMessage gameStart = ConnectionMessage.createGameStart(hostPlayerId);
        broadcastMessage(gameStart);
        
        // 호스트에게 게임 시작 알림
        if (hostMessageListener != null) {
            hostMessageListener.onGameStart();
        }
    }
    
    // 게임 종료
    private void endGame(String reason) {
        synchronized (gameLock) {
            if (!gameInProgress) {
                return;
            }
            gameInProgress = false;
        }
        
        System.out.println("Game ended: " + reason);
        
        // 클라이언트에게 게임 종료 메시지 전송
        ConnectionMessage gameOver = ConnectionMessage.createGameOver(hostPlayerId, reason);
        broadcastMessage(gameOver);
        
        // 호스트에게 게임 종료 알림
        if (hostMessageListener != null) {
            hostMessageListener.onGameOver(reason);
        }
    }
    
    
    // 모든 클라이언트에게 메시지 전송
    public void broadcastMessage(NetworkMessage message) {
        List<ClientHandler> clients = new ArrayList<>(connectedClients.values());
        
        for (ClientHandler client : clients) {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to send message to client: " + e.getMessage());
                // 연결이 끊어진 클라이언트는 제거
                unregisterClient(client.getPlayerId());
            }
        }
    }
    
    
    // 특정 플레이어에게 메시지 전송    
    public void sendMessageToPlayer(String playerId, NetworkMessage message) {
        ClientHandler client = connectedClients.get(playerId);
        if (client != null) {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to send message to player " + playerId + ": " + e.getMessage());
                unregisterClient(playerId);
            }
        }
    }
    
    
    // 다른 플레이어들에게 메시지 전송 (발신자 제외)
    public void broadcastToOthers(String senderPlayerId, NetworkMessage message) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(senderPlayerId)) {
                try {
                    entry.getValue().sendMessage(message);
                } catch (IOException e) {
                    System.err.println("Failed to broadcast to player " + entry.getKey() + ": " + e.getMessage());
                    unregisterClient(entry.getKey());
                }
            }
        }
    }
    
    // 모든 클라이언트와 호스트에게 메시지 전송
    public void broadcastToAll(NetworkMessage message) {
        // 호스트에게 전송 (호스트가 TetrisClient를 사용하는 경우)
        if (hostMessageListener != null && message instanceof ConnectionMessage connMsg) {
            if (connMsg.getType() == MessageType.PLAYER_READY) {
                // 호스트에게 플레이어 준비 알림
                hostMessageListener.onPlayerReady(connMsg.getSenderId());
            }
        }
        
        // 모든 클라이언트에게 전송
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            try {
                entry.getValue().sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to broadcast to player " + entry.getKey() + ": " + e.getMessage());
                unregisterClient(entry.getKey());
            }
        }
    }
    
    // 클라이언트 입력을 호스트에게 알림
    public void notifyHostInput(InputMessage inputMessage) {
        if (hostMessageListener != null) {
            hostMessageListener.onInputReceived(inputMessage);
        }
    }
    
    // 클라이언트 보드 업데이트를 호스트에게 알림
    public void notifyHostBoardUpdate(BoardUpdateMessage boardUpdate) {
        if (hostMessageListener != null) {
            hostMessageListener.onBoardUpdate(boardUpdate);
        }
    }
    
    // 클라이언트 공격을 호스트에게 알림
    public void notifyHostAttack(AttackMessage attackMessage) {
        if (hostMessageListener != null) {
            hostMessageListener.onAttackReceived(attackMessage);
        }
    }
    
    // 클라이언트 줄 삭제를 호스트에게 알림
    public void notifyHostLinesCleared(LinesClearedMessage linesClearedMessage) {
        if (hostMessageListener != null) {
            hostMessageListener.onLinesClearedReceived(linesClearedMessage);
        }
    }
    
    // 클라이언트 일시정지를 호스트에게 알림
    public void notifyHostPause() {
        if (hostMessageListener != null) {
            hostMessageListener.onGamePaused();
        }
    }
    
    // 클라이언트 재개를 호스트에게 알림
    public void notifyHostResume() {
        if (hostMessageListener != null) {
            hostMessageListener.onGameResumed();
        }
    }
    
    // 클라이언트 게임 오버를 호스트에게 알림
    public void notifyHostGameOver(String reason) {
        if (hostMessageListener != null) {
            hostMessageListener.onGameOver(reason);
        }
    }
    
    // 호스트의 입력을 클라이언트에게 전송
    public boolean sendHostInput(MessageType inputType) {
        if (!gameInProgress) {
            return false;
        }
        
        InputMessage inputMsg = new InputMessage(inputType, hostPlayerId);
        broadcastMessage(inputMsg);
        return true;
    }
    
    // 호스트의 보드 상태를 클라이언트에게 전송
    public boolean sendHostBoardUpdate(int[][] board, int score, int lines, int level) {
        if (!gameInProgress) {
            return false;
        }
        
        BoardUpdateMessage boardMsg = new BoardUpdateMessage(hostPlayerId, board, score, lines, level);
        broadcastMessage(boardMsg);
        return true;
    }
    
    // 호스트의 보드 상태를 클라이언트에게 전송 (상세한 블록 정보 포함)
    public boolean sendHostBoardUpdateWithPiece(int[][] board, int pieceX, int pieceY,
                                               int pieceType, int pieceRotation,
                                               int score, int lines, int level) {
        if (!gameInProgress) {
            return false;
        }
        
        BoardUpdateMessage boardMsg = new BoardUpdateMessage(hostPlayerId, board, pieceX, pieceY,
                                                            pieceType, pieceRotation, score, lines, level);
        broadcastMessage(boardMsg);
        return true;
    }
    
    // 호스트의 공격을 클라이언트에게 전송
    public boolean sendHostAttack(String targetPlayerId, int clearedLines) {
        if (!gameInProgress) {
            return false;
        }
        
        AttackMessage attackMsg = AttackMessage.createStandardAttack(hostPlayerId, targetPlayerId, clearedLines);
        sendMessageToPlayer(targetPlayerId, attackMsg);
        return true;
    }
    
    // 호스트의 줄 삭제 정보를 클라이언트에게 전송
    public boolean sendHostLinesCleared(int linesCleared) {
        if (!gameInProgress) {
            return false;
        }
        
        LinesClearedMessage linesClearedMsg = new LinesClearedMessage(hostPlayerId, linesCleared);
        broadcastMessage(linesClearedMsg);
        return true;
    }
    
    // 호스트가 게임 일시정지
    public boolean pauseGameAsHost() {
        ConnectionMessage pauseMsg = new ConnectionMessage(MessageType.PAUSE, hostPlayerId, "Game paused by host");
        broadcastMessage(pauseMsg);
        return true;
    }
    
    // 호스트가 게임 재개
    public boolean resumeGameAsHost() {
        ConnectionMessage resumeMsg = new ConnectionMessage(MessageType.RESUME, hostPlayerId, "Game resumed by host");
        broadcastMessage(resumeMsg);
        return true;
    }
    
    // 호스트가 준비 완료
    public void setHostReady() {
        System.out.println("Host " + hostPlayerId + " is ready!");
        setPlayerReady(hostPlayerId, true);
        
        // 모든 클라이언트에게 호스트 준비 알림
        ConnectionMessage readyNotification = ConnectionMessage.createPlayerReady(hostPlayerId);
        broadcastToAll(readyNotification);
    }
    
    public boolean sendHostMoveLeft() {
        return sendHostInput(MessageType.MOVE_LEFT);
    }
    
    public boolean sendHostMoveRight() {
        return sendHostInput(MessageType.MOVE_RIGHT);
    }
    
    public boolean sendHostRotate() {
        return sendHostInput(MessageType.ROTATE);
    }
    
    public boolean sendHostHardDrop() {
        return sendHostInput(MessageType.HARD_DROP);
    }
    
    public boolean sendHostSoftDrop() {
        return sendHostInput(MessageType.SOFT_DROP);
    }
    
    // 서버 중지
    public void stop() {
        isRunning = false;
        
        // 모든 클라이언트에게 서버 종료 알림
        if (!connectedClients.isEmpty()) {
            ConnectionMessage serverShutdown = ConnectionMessage.createGameOver("server", "Server shutdown");
            broadcastMessage(serverShutdown);
        }
        
        // 모든 클라이언트 연결 종료
        for (ClientHandler client : connectedClients.values()) {
            client.close();
        }
        connectedClients.clear();
        
        // 서버 소켓 종료
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // 스레드 풀 종료
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Tetris Server stopped");
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isGameInProgress() {
        return gameInProgress;
    }
    
    public int getConnectedPlayerCount() {
        return connectedClients.size();
    }
    
    public Set<String> getConnectedPlayerIds() {
        return new HashSet<>(connectedClients.keySet());
    }
    
    
    // 서버 메인 메서드 - 독립 실행용
    public static void main(String[] args) {
        String hostPlayerId = "HostPlayer";
        int port = DEFAULT_PORT;
        
        // 명령행 인수 처리: [hostPlayerId] [port]
        if (args.length > 0) {
            hostPlayerId = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[1]);
                System.err.println("Using default port: " + DEFAULT_PORT);
            }
        }
        
        TetrisServer server = new TetrisServer(hostPlayerId, port);
        
        // 호스트 메시지 리스너 설정 (간단한 콘솔 출력용)
        server.setHostMessageListener(new HostMessageListener() {
            @Override
            public void onClientConnected(String clientId) {
                System.out.println("Client connected: " + clientId);
            }
            
            @Override
            public void onClientDisconnected(String clientId) {
                System.out.println("Client disconnected: " + clientId);
            }
            
            @Override
            public void onPlayerReady(String playerId) {
                System.out.println(playerId + " is ready!");
            }
            
            @Override
            public void onGameStart() {
                System.out.println("Game started! You can now play as host.");
            }
            
            @Override
            public void onGameOver(String reason) {
                System.out.println("Game over: " + reason);
            }
            
            @Override
            public void onInputReceived(InputMessage inputMessage) {
                System.out.println("Client input: " + inputMessage.getInputType());
            }
            
            @Override
            public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
                System.out.println("Client board updated - Score: " + boardUpdate.getScore());
            }
            
            @Override
            public void onAttackReceived(AttackMessage attackMessage) {
                System.out.println("Attack received from client: " + attackMessage.getAttackLines() + " lines!");
            }
            
            @Override
            public void onLinesClearedReceived(LinesClearedMessage linesClearedMessage) {
                System.out.println("Client cleared " + linesClearedMessage.getLinesCleared() + " lines!");
            }
            
            @Override
            public void onGamePaused() {
                System.out.println("⏸Client paused the game");
            }
            
            @Override
            public void onGameResumed() {
                System.out.println("▶Client resumed the game");
            }
        });
        
        // 서버 시작
        try {
            server.start();
            
            System.out.println("P2P Tetris Server Started (Host Mode)!");
            System.out.println("Host Player: " + hostPlayerId);
            System.out.println("Server IP: " + server.getServerIP());
            System.out.println("Port: " + port);
            System.out.println("\n Available Commands:");
            System.out.println("  'mode normal' - Select normal game mode");
            System.out.println("  'mode item'   - Select item game mode");
            System.out.println("  'ready'       - Set host ready");
            System.out.println("  'reset'       - Reset ready states");
            System.out.println("  'status'      - Show current status");
            System.out.println("  'move L/R'    - Send move left/right as host");
            System.out.println("  'rotate'      - Send rotate as host");
            System.out.println("  'drop'        - Send hard drop as host");
            System.out.println("  'pause'       - Pause game as host");
            System.out.println("  'resume'      - Resume game as host");
            System.out.println("  'quit'        - Stop server");
            System.out.println("----------------------------------------");
            
            // Ctrl+C로 서버 종료할 수 있도록 셧다운 훅 추가
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
            // 인터랙티브 명령 처리
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            while (server.isRunning()) {
                System.out.print("Server> ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    
                    if (input.equalsIgnoreCase("quit")) {
                        System.out.println("Shutting down server...");
                        server.stop();
                        break;
                    } else if (input.equalsIgnoreCase("ready")) {
                        server.setHostReady();
                        System.out.println("Host ready signal sent!");
                    } else if (input.equalsIgnoreCase("reset")) {
                        server.resetReadyStates();
                        System.out.println("Ready states reset");
                    } else if (input.equalsIgnoreCase("status")) {
                        System.out.println("Server Status:");
                        System.out.println("  - IP: " + server.getServerIP());
                        System.out.println("  - Connected clients: " + server.getClientCount());
                        System.out.println("  - Game mode: " + (server.getSelectedGameMode() != null ? server.getSelectedGameMode() : "Not selected"));
                        System.out.println("  - Server ready: " + (server.isServerReady() ? "✅" : "❌"));
                        System.out.println("  - Client ready: " + (server.isClientReady() ? "✅" : "❌"));
                    } else if (input.startsWith("mode ")) {
                        String mode = input.substring(5).trim().toUpperCase();
                        try {
                            GameModeMessage.GameMode gameMode = GameModeMessage.GameMode.valueOf(mode);
                            server.selectGameMode(gameMode);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid game mode. Use 'normal' or 'item'");
                        }
                    } else if (input.startsWith("move ")) {
                        String direction = input.substring(5).trim().toUpperCase();
                        if (direction.equals("L") || direction.equals("LEFT")) {
                            if (server.sendHostMoveLeft()) {
                                System.out.println("Host move left sent");
                            } else {
                                System.out.println("Game not in progress");
                            }
                        } else if (direction.equals("R") || direction.equals("RIGHT")) {
                            if (server.sendHostMoveRight()) {
                                System.out.println("Host move right sent");
                            } else {
                                System.out.println("Game not in progress");
                            }
                        } else {
                            System.out.println("Invalid direction. Use 'L' or 'R'");
                        }
                    } else if (input.equalsIgnoreCase("rotate")) {
                        if (server.sendHostRotate()) {
                            System.out.println("Host rotate sent");
                        } else {
                            System.out.println("Game not in progress");
                        }
                    } else if (input.equalsIgnoreCase("drop")) {
                        if (server.sendHostHardDrop()) {
                            System.out.println("Host hard drop sent");
                        } else {
                            System.out.println("Game not in progress");
                        }
                    } else if (input.equalsIgnoreCase("pause")) {
                        if (server.pauseGameAsHost()) {
                            System.out.println("⏸Game paused by host");
                        } else {
                            System.out.println("Failed to pause game");
                        }
                    } else if (input.equalsIgnoreCase("resume")) {
                        if (server.resumeGameAsHost()) {
                            System.out.println("▶Game resumed by host");
                        } else {
                            System.out.println("Failed to resume game");
                        }
                    } else if (!input.isEmpty()) {
                        System.out.println("Unknown command: " + input);
                    }
                } else {
                    Thread.sleep(100);
                }
            }
            
            scanner.close();
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Server interrupted");
        }
    }
}

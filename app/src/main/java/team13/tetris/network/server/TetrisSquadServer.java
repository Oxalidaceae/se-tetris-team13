package team13.tetris.network.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;

/**
 * Squad PVP Server supporting 3 players (1 host + 2 clients)
 *
 * <p>Differences from TetrisServer: - MAX_PLAYERS = 2 (allowing 2 clients + host = 3 total) -
 * Tracks 3 players' states and elimination order - Randomly distributes attacks to alive opponents
 */
public class TetrisSquadServer {
    private static final int MAX_PLAYERS = 2; // Host + 2 clients = 3 total
    private static final int DEFAULT_PORT = 12346; // Different port from regular server

    private final String hostPlayerId;
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> connectedClients;
    private final Map<String, PlayerInfo> players; // All 3 players including host
    private final List<String> clientConnectionOrder = new ArrayList<>(); // Track connection order
    private final Map<String, String> clientIdMapping = new HashMap<>(); // originalId -> friendlyId
    private final ExecutorService threadPool;
    private volatile boolean isRunning = false;

    // Game state
    private volatile boolean gameInProgress = false;
    private GameModeMessage.GameMode selectedGameMode = null;

    // Squad-specific: Track elimination order
    private final List<String> eliminationOrder = new ArrayList<>();
    private final Set<String> alivePlayers = new HashSet<>();

    // Host message listener
    private ServerMessageListener hostMessageListener;

    // Player info
    private static class PlayerInfo {
        private final String playerId;
        private volatile boolean ready;

        PlayerInfo(String playerId) {
            this.playerId = playerId;
            this.ready = false;
        }

        public String getPlayerId() {
            return playerId;
        }

        public boolean isReady() {
            return ready;
        }

        public void setReady(boolean ready) {
            this.ready = ready;
        }
    }

    public TetrisSquadServer(String hostPlayerId, int port) {
        this.hostPlayerId = hostPlayerId.trim();
        this.port = port;
        this.connectedClients = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        // Register host
        PlayerInfo hostInfo = new PlayerInfo(hostPlayerId);
        players.put(hostPlayerId, hostInfo);
        alivePlayers.add(hostPlayerId);
    }

    public TetrisSquadServer(String hostPlayerId) {
        this(hostPlayerId, DEFAULT_PORT);
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        System.out.println("Squad Server started on port " + port);
        threadPool.submit(this::acceptClients);
    }

    private void acceptClients() {
        while (isRunning && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                if (connectedClients.size() >= MAX_PLAYERS) {
                    // Reject: server full
                    try {
                        ObjectOutputStream out =
                                new ObjectOutputStream(clientSocket.getOutputStream());
                        out.writeObject(
                                ConnectionMessage.createConnectionRejected(
                                        "Server", "Server is full (max 3 players)"));
                        out.flush();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ClientHandler handler = new ClientHandler(clientSocket);
                    threadPool.submit(handler);
                }
            } catch (IOException e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcast(Object message) {
        for (ClientHandler client : connectedClients.values()) {
            client.send(message);
        }
    }

    // 발신자를 제외한 다른 플레이어들에게 메시지 전송
    public void broadcastToOthers(String senderPlayerId, Object message) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(senderPlayerId)) {
                entry.getValue().send(message);
            }
        }
    }

    public void sendToClient(String clientId, Object message) {
        ClientHandler client = connectedClients.get(clientId);
        if (client != null) {
            client.send(message);
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ClientHandler client : connectedClients.values()) {
            client.disconnect();
        }
        threadPool.shutdownNow();
    }

    public void setHostReady(boolean ready) {
        PlayerInfo hostInfo = players.get(hostPlayerId);
        if (hostInfo != null) {
            hostInfo.setReady(ready);
            broadcastLobbyState(); // 전체 상태 브로드캐스트
            if (ready) {
                checkAllReady();
            }
        }
    }

    public void setClientReady(String clientId, boolean ready) {
        System.out.println(
                "[TetrisSquadServer] setClientReady - clientId: " + clientId + ", ready: " + ready);
        PlayerInfo clientInfo = players.get(clientId);
        if (clientInfo != null) {
            clientInfo.setReady(ready);
            broadcastLobbyState(); // 전체 상태 브로드캐스트
            if (ready) {
                checkAllReady();
            }
        }
    }

    public void setHostMessageListener(ServerMessageListener listener) {
        this.hostMessageListener = listener;
    }

    public void broadcastLobbyState() {
        List<LobbyStateMessage.PlayerState> playerStates = new ArrayList<>();

        // Host \ucd94\uac00 (order = 0)
        PlayerInfo hostInfo = players.get(hostPlayerId);
        if (hostInfo != null) {
            playerStates.add(
                    new LobbyStateMessage.PlayerState(hostPlayerId, hostInfo.isReady(), 0));
        }

        // Clients \ucd94\uac00 (\uc5f0\uacb0 \uc21c\uc11c\ub300\ub85c)
        for (int i = 0; i < clientConnectionOrder.size(); i++) {
            String clientId = clientConnectionOrder.get(i);
            PlayerInfo clientInfo = players.get(clientId);
            if (clientInfo != null) {
                playerStates.add(
                        new LobbyStateMessage.PlayerState(clientId, clientInfo.isReady(), i + 1));
            }
        }

        LobbyStateMessage lobbyStateMsg = new LobbyStateMessage(hostPlayerId, playerStates);

        // \ud638\uc2a4\ud2b8\uc5d0\uac8c \uc54c\ub9bc
        if (hostMessageListener != null) {
            hostMessageListener.onLobbyStateUpdate(playerStates);
        }

        // \ubaa8\ub4e0 \ud074\ub77c\uc774\uc5b8\ud2b8\uc5d0\uac8c
        // \ube0c\ub85c\ub4dc\uce90\uc2a4\ud2b8
        broadcast(lobbyStateMsg);
    }

    public boolean areAllPlayersReady() {
        if (connectedClients.size() < MAX_PLAYERS) {
            return false; // Need all 2 clients connected
        }
        for (PlayerInfo player : players.values()) {
            if (!player.isReady()) {
                return false;
            }
        }
        return true;
    }

    public void checkAllReady() {
        if (!areAllPlayersReady()) {
            return;
        }

        // All players ready - start countdown
        startCountdown();
    }

    public void selectGameMode(GameModeMessage.GameMode mode) {
        this.selectedGameMode = mode;
        broadcast(new GameModeMessage(hostPlayerId, mode));
    }

    private void startCountdown() {
        // 카운트다운 시작 메시지 브로드캐스트
        ConnectionMessage countdownMsg = ConnectionMessage.createCountdownStart(hostPlayerId);
        broadcast(countdownMsg);
        
        // 호스트에게도 알림
        if (hostMessageListener != null) {
            hostMessageListener.onCountdownStart();
        }
        
        // 5초 후 게임 시작
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                startGame();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startGame() {
        gameInProgress = true;
        
        // 호스트에게 게임 시작 알림
        if (hostMessageListener != null) {
            hostMessageListener.onGameStart();
        }
        
        // 모든 클라이언트에게 게임 시작 메시지 전송
        broadcast(ConnectionMessage.createGameStart(hostPlayerId));
    }

    public void recordElimination(String playerId) {
        if (alivePlayers.contains(playerId)) {
            alivePlayers.remove(playerId);
            eliminationOrder.add(playerId);
            System.out.println(
                    "Player eliminated: " + playerId + " (Place: " + eliminationOrder.size() + ")");

            // Check if only 1 player remains - automatically end game
            if (alivePlayers.size() == 1) {
                String winner = alivePlayers.iterator().next();
                System.out.println("Only 1 player remains: " + winner + " - Ending game!");
                // Add delay to ensure all clients receive game over messages first
                new Thread(
                                () -> {
                                    try {
                                        Thread.sleep(1000); // 1 second delay
                                        endGame();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                })
                        .start();
            }
        }
    }

    // 채팅 메시지 전송 (호스트용)
    public void sendChatMessage(String message) {
        ChatMessage chatMsg = new ChatMessage(hostPlayerId, message);
        broadcastToOthers(hostPlayerId, chatMsg);

        // 호스트에게도 자신의 메시지를 알림 (에코)
        if (hostMessageListener != null) {
            hostMessageListener.onChatMessageReceived("ME", message);
        }
    }

    /** Manually end the game and show final rankings */
    public void endGame() {
        // Add remaining alive players as winners
        List<String> finalRankings = new ArrayList<>(eliminationOrder);
        finalRankings.addAll(alivePlayers); // Add winner(s) at the end

        System.out.println("Game ended! Final rankings: " + finalRankings);

        // Broadcast final rankings
        GameEndMessage endMsg = new GameEndMessage(hostPlayerId, finalRankings);
        broadcast(endMsg);

        // Send to host too
        if (hostMessageListener != null) {
            hostMessageListener.onGameEnd(finalRankings);
        }

        // Reset game state immediately after sending rankings
        // This ensures all players who return to lobby will have reset ready states
        resetGameState();
    }

    public List<String> getEliminationOrder() {
        return new ArrayList<>(eliminationOrder);
    }

    public Set<String> getAlivePlayers() {
        return new HashSet<>(alivePlayers);
    }

    /** Reset game state for a new game while keeping network connections */
    public void resetGameState() {
        gameInProgress = false;
        eliminationOrder.clear();
        alivePlayers.clear();

        // Re-add all connected players as alive
        alivePlayers.add(hostPlayerId);
        for (String clientId : connectedClients.keySet()) {
            alivePlayers.add(clientId);
        }

        // Reset all ready states
        PlayerInfo hostInfo = players.get(hostPlayerId);
        if (hostInfo != null) {
            hostInfo.setReady(false);
        }
        for (String clientId : connectedClients.keySet()) {
            PlayerInfo clientInfo = players.get(clientId);
            if (clientInfo != null) {
                clientInfo.setReady(false);
            }
        }

        System.out.println("Game state reset. Alive players: " + alivePlayers);

        // Broadcast updated lobby state to all clients
        broadcastLobbyState();
    }

    // 호스트의 공격을 랜덤하게 분배
    public void distributeHostAttack(AttackMessage attack) {
        // Get alive opponents (exclude host)
        List<String> targets = new ArrayList<>(alivePlayers);
        targets.remove(hostPlayerId);

        if (!targets.isEmpty()) {
            // Random selection
            String target = targets.get(new Random().nextInt(targets.size()));
            sendToClient(target, attack);
        }
    }

    public static String getServerIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    // Client handler
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());

                // Read connection request
                Object firstMessage = input.readObject();
                if (!(firstMessage instanceof ConnectionMessage)) {
                    disconnect();
                    return;
                }

                // 원래 클라이언트 ID 저장
                ConnectionMessage connMsg = (ConnectionMessage) firstMessage;
                String originalClientId = connMsg.getSenderId();
                
                // 연결 순서에 따라 친근한 클라이언트 이름 부여 (동기화)
                synchronized (clientConnectionOrder) {
                    int connectionIndex = clientConnectionOrder.size();
                    clientId = "Client " + (connectionIndex + 1);
                    
                    // 원래 ID와 새로운 ID 간 매핑 저장
                    clientIdMapping.put(originalClientId, clientId);

                    // Accept connection and track connection order
                    connectedClients.put(clientId, this);
                    clientConnectionOrder.add(clientId);
                    PlayerInfo playerInfo = new PlayerInfo(clientId);
                    players.put(clientId, playerInfo);
                    alivePlayers.add(clientId);
                }

                output.writeObject(
                        ConnectionMessage.createConnectionAccepted(hostPlayerId, clientId));
                output.flush();

                System.out.println(
                        "Client connected: "
                                + clientId
                                + " (connection order: "
                                + clientConnectionOrder.indexOf(clientId)
                                + ")");

                // Notify host of client connection
                if (hostMessageListener != null) {
                    hostMessageListener.onClientConnected(clientId);
                }

                // 새 클라이언트가 연결되면 전체 로비 상태 브로드캐스트
                broadcastLobbyState();

                // Notify all other clients about this new client with connection order info
                for (ClientHandler otherClient : connectedClients.values()) {
                    if (!otherClient.clientId.equals(clientId)) {
                        // Tell other clients about the new client
                        ConnectionMessage clientConnectedMsg =
                                ConnectionMessage.createConnectionAccepted(clientId, "NewClient");
                        otherClient.send(clientConnectedMsg);
                    }
                }

                // Tell the new client about all existing clients with their order
                for (String existingClientId : connectedClients.keySet()) {
                    if (!existingClientId.equals(clientId)) {
                        ConnectionMessage existingClientMsg =
                                ConnectionMessage.createConnectionAccepted(
                                        existingClientId, "ExistingClient");
                        output.writeObject(existingClientMsg);
                        output.flush();
                    }
                }

                // Message loop
                while (isRunning && !socket.isClosed()) {
                    Object message = input.readObject();
                    handleMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                // Connection closed
            } finally {
                disconnect();
            }
        }

        private void handleMessage(Object message) {
            if (message instanceof ConnectionMessage) {
                ConnectionMessage connMsg = (ConnectionMessage) message;
                if (connMsg.getType() == MessageType.PLAYER_READY) {
                    setClientReady(clientId, true);
                } else if (connMsg.getType() == MessageType.PLAYER_UNREADY) {
                    setClientReady(clientId, false);
                } else if (connMsg.getType() == MessageType.GAME_OVER) {
                    handleGameOver(clientId);
                } else if (connMsg.getType() == MessageType.PAUSE) {
                    // 클라이언트의 일시정지 요청을 다른 모든 플레이어에게 브로드캐스트
                    broadcast(message);
                    // 호스트에게도 알림
                    if (hostMessageListener != null) {
                        hostMessageListener.onGamePaused();
                    }
                } else if (connMsg.getType() == MessageType.RESUME) {
                    // 클라이언트의 일시정지 해제 요청을 다른 모든 플레이어에게 브로드캐스트
                    broadcast(message);
                    // 호스트에게도 알림
                    if (hostMessageListener != null) {
                        hostMessageListener.onGameResumed();
                    }
                }
            } else if (message instanceof BoardUpdateMessage) {
                // 다른 모든 플레이어에게 브로드캐스트 (클라이언트들 + 호스트)
                broadcast(message);
                // 호스트에게도 전달
                if (hostMessageListener != null && !clientId.equals(hostPlayerId)) {
                    hostMessageListener.onBoardUpdate((BoardUpdateMessage) message);
                }
            } else if (message instanceof AttackMessage) {
                distributeAttackRandomly((AttackMessage) message);
            } else if (message instanceof ChatMessage) {
                // 채팅 메시지를 발신자를 제외한 다른 플레이어들에게 브로드캐스트
                ChatMessage chatMsg = (ChatMessage) message;
                // 친근한 클라이언트 ID로 새로운 채팅 메시지 생성
                ChatMessage friendlyChatMsg = new ChatMessage(clientId, chatMsg.getMessage());
                broadcastToOthers(clientId, friendlyChatMsg);
                // 호스트에게도 전달
                if (hostMessageListener != null) {
                    hostMessageListener.onChatMessageReceived(clientId, chatMsg.getMessage());
                }
            }
        }

        private void distributeAttackRandomly(AttackMessage attack) {
            // Get alive opponents (exclude sender)
            List<String> targets = new ArrayList<>(alivePlayers);
            targets.remove(clientId);

            if (!targets.isEmpty()) {
                // Random selection
                String target = targets.get(new Random().nextInt(targets.size()));

                // 타겟이 호스트인 경우
                if (target.equals(hostPlayerId)) {
                    if (hostMessageListener != null) {
                        hostMessageListener.onAttackReceived(attack);
                    }
                } else {
                    // 타겟이 클라이언트인 경우
                    sendToClient(target, attack);
                }
            }
        }

        private void handleGameOver(String playerId) {
            recordElimination(playerId);
            broadcast(ConnectionMessage.createGameOver(playerId, "Player eliminated"));
            // recordElimination will automatically call endGame() when only 1 player remains
        }

        public void send(Object message) {
            try {
                synchronized (output) {
                    output.writeObject(message);
                    output.flush();
                }
            } catch (IOException e) {
                disconnect();
            }
        }

        public void disconnect() {
            try {
                if (clientId != null) {
                    connectedClients.remove(clientId);
                    players.remove(clientId);
                    alivePlayers.remove(clientId);
                    System.out.println("Client disconnected: " + clientId);

                    // 호스트에게 클라이언트 연결 끊김 알리기
                    if (hostMessageListener != null) {
                        hostMessageListener.onClientDisconnected(clientId);
                    }

                    // 다른 클라이언트들에게 이 클라이언트의 연결 끊김 알리기
                    ConnectionMessage disconnectMsg =
                            new ConnectionMessage(
                                    MessageType.GAME_OVER, clientId, "Player disconnected");
                    for (ClientHandler otherClient : connectedClients.values()) {
                        otherClient.send(disconnectMsg);
                    }
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

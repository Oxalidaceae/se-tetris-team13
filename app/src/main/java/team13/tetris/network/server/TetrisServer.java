package team13.tetris.network.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.TimerTask;
import java.util.concurrent.*;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;

public class TetrisServer {
    private static final int MAX_PLAYERS = 1; // 서버 자신(호스트) + 클라이언트 1명
    private static final int DEFAULT_PORT = 12345;

    private final String hostPlayerId;
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> connectedClients;
    private final Map<String, PlayerInfo> players; // 플레이어 상태 관리(host 포함)
    private final ExecutorService threadPool;
    private Future<?> acceptClientsFuture; // acceptClients 작업 추적용
    private volatile boolean isRunning = false;

    // 게임 상태
    private volatile boolean gameInProgress = false;
    private final Object gameLock = new Object();

    // P2P 상태 관리
    private GameModeMessage.GameMode selectedGameMode = null;
    private final Map<String, Boolean> playerReadyStates = new ConcurrentHashMap<>();
    private Timer countdownTimer = null; // 카운트다운 타이머 저장
    private volatile long currentCountdownId = 0; // 현재 카운트다운 ID

    private ServerMessageListener hostMessageListener;

    // 플레이어 상태를 관리하기 위한 내부 클래스
    @SuppressWarnings("unused")
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

    public TetrisServer(String hostPlayerId, int port) {
        this.hostPlayerId = hostPlayerId.trim();
        this.port = port;
        this.connectedClients = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        // 서버 생성 시 Host를 플레이어 목록에 등록
        PlayerInfo hostInfo = new PlayerInfo(hostPlayerId);
        players.put(hostPlayerId, hostInfo);
    }

    public TetrisServer(String hostPlayerId) {
        this(hostPlayerId, DEFAULT_PORT);
    }

    // 호스트 메시지 리스너 설정
    public void setHostMessageListener(ServerMessageListener listener) {
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

        System.out.println("Tetris Server started");
        System.out.println("waiting for players to connect...");

        // 클라이언트 접속 대기 스레드 (Future 저장하여 나중에 취소 가능)
        acceptClientsFuture = threadPool.submit(this::acceptClients);
    }

    // 클라이언트 접속
    private void acceptClients() {
        while (isRunning && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();

                if (connectedClients.size() >= MAX_PLAYERS) {
                    rejectConnection(clientSocket, "Server is full");
                    continue;
                }

                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // 클라이언트 핸들러 생성 및 시작
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.submit(handler);

            } catch (SocketException e) {
                // 서버 소켓이 닫힌 경우 (정상 종료)
                if (isRunning) {
                    System.err.println("Server socket closed unexpectedly: " + e.getMessage());
                } else {
                    System.out.println("Server socket closed - stopping accept loop");
                }
                break;
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
        System.out.println("acceptClients loop ended");
    }

    // 연결을 거절
    private void rejectConnection(Socket clientSocket, String reason) {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            ConnectionMessage rejection =
                    ConnectionMessage.createConnectionRejected("server", reason);
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

        players.computeIfAbsent(playerId, PlayerInfo::new);

        System.out.println(
                "Player registered: "
                        + playerId
                        + " ("
                        + connectedClients.size()
                        + "/"
                        + MAX_PLAYERS
                        + ")");

        // 호스트에게 클라이언트 연결 알림
        if (hostMessageListener != null) {
            hostMessageListener.onClientConnected(playerId);
        }

        // 클라이언트가 접속하면 대기 상태 (양쪽이 ready해야 게임 시작)
        System.out.println("Client connected! Waiting for players to be ready...");

        return true;
    }

    // 클라이언트에게 현재 서버 상태 전송 (CONNECTION_ACCEPTED 이후 호출)
    public void sendInitialStateToClient(String playerId) {
        ClientHandler handler = connectedClients.get(playerId);
        if (handler == null) return;

        try {
            // 게임 모드가 선택되어 있으면 전송
            if (selectedGameMode != null) {
                GameModeMessage gameModeMsg = new GameModeMessage("server", selectedGameMode);
                handler.sendMessage(gameModeMsg);
            }

            // 호스트가 이미 Ready 상태면 알림
            if (playerReadyStates.getOrDefault(hostPlayerId, false)) {
                ConnectionMessage hostReadyMsg = ConnectionMessage.createPlayerReady(hostPlayerId);
                handler.sendMessage(hostReadyMsg);
            }
        } catch (IOException e) {
            System.err.println("Failed to send initial state to client: " + e.getMessage());
        }
    }

    // 클라이언트 연결 해제
    public synchronized void unregisterClient(String playerId) {
        ClientHandler removed = connectedClients.remove(playerId);
        if (removed != null) {
            System.out.println(
                    "Player disconnected: "
                            + playerId
                            + " ("
                            + connectedClients.size()
                            + "/"
                            + MAX_PLAYERS
                            + ")");

            // ready 상태 제거
            playerReadyStates.remove(playerId);
            PlayerInfo info = players.get(playerId);
            if (info != null) {
                info.setReady(false);
            }

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

    // 현재 활성 플레이어 ID 집합 반환 (호스트 + 접속한 클라이언트)
    private Set<String> getActivePlayerIds() {
        Set<String> ids = new HashSet<>();
        ids.add(hostPlayerId); // Host
        ids.addAll(connectedClients.keySet()); // 현재 접속 중인 모든 클라이언트
        return ids;
    }

    // 플레이어 준비 상태를 설정 (내부 전용 - ClientHandler에서만 호출)
    void setPlayerReady(String playerId, boolean ready) {
        playerReadyStates.put(playerId, ready);

        PlayerInfo info = players.computeIfAbsent(playerId, PlayerInfo::new);
        info.setReady(ready);

        if (ready) {
            System.out.println(playerId + " is ready!");
        } else {
            System.out.println(playerId + " is not ready!");
        }
    }

    // 모든 플레이어가 준비되었는지 확인
    public void checkAllReady() {

        // 게임모드가 선택되지 않았으면 시작 불가
        if (selectedGameMode == null) {
            return;
        }

        // 최소 플레이어 수 확인 (호스트 + 클라이언트 최소 1명 = 2명)
        Set<String> activePlayerIds = getActivePlayerIds();

        if (activePlayerIds.size() < 2) {
            return;
        }

        // 현재 게임에 참여하는 Host + 클라이언트 기준으로 체크
        for (String playerId : activePlayerIds) {
            boolean ready = playerReadyStates.getOrDefault(playerId, false);
            if (!ready) {
                return;
            }
        }

        // 모든 조건 만족 시 카운트다운 시작 메시지 전송 후 5초 뒤 게임 시작
        // 기존 카운트다운 타이머가 있다면 취소
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        // 새로운 카운트다운 ID 생성
        currentCountdownId++;
        final long countdownId = currentCountdownId;

        broadcastMessage(ConnectionMessage.createCountdownStart("server"));
        if (hostMessageListener != null) {
            hostMessageListener.onCountdownStart();
        }

        countdownTimer = new Timer();
        countdownTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        startGame(countdownId);
                    }
                },
                5000);
    }

    // 현재 서버의 IP 주소 반환
    public static String getServerIP() {
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

    // 클라이언트 준비 상태 반환
    public boolean isClientReady(String clientId) {
        if (clientId == null) {
            return false;
        }
        return playerReadyStates.getOrDefault(clientId, false);
    }

    // P2P 준비 상태 초기화
    public void resetReadyStates() {

        // 진행 중인 카운트다운 타이머 취소 및 ID 무효화
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
        // 카운트다운 ID 무효화 (이전 카운트다운이 실행되어도 무시됨)
        currentCountdownId++;

        playerReadyStates.clear();
        selectedGameMode = null;
        gameInProgress = false;
    }

    // 게임 시작 (카운트다운 ID 검증 포함)
    private void startGame(long countdownId) {

        // 카운트다운 ID가 현재 ID와 일치하는지 확인
        if (countdownId != currentCountdownId) {
            return;
        }

        synchronized (gameLock) {
            if (gameInProgress) {
                return;
            }
            gameInProgress = true;
        }

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

        // 다음 게임을 위해 ready 상태 초기화
        resetReadyStates();
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
                System.err.println(
                        "Failed to send message to player " + playerId + ": " + e.getMessage());
                unregisterClient(playerId);
            }
        }
    }

    // 발신자를 제외한 다른 플레이어들에게 메시지 전송
    public void broadcastToOthers(String senderPlayerId, NetworkMessage message) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(senderPlayerId)) {
                try {
                    entry.getValue().sendMessage(message);
                } catch (IOException e) {
                    System.err.println(
                            "Failed to broadcast to player "
                                    + entry.getKey()
                                    + ": "
                                    + e.getMessage());
                    unregisterClient(entry.getKey());
                }
            }
        }
    }

    // 모든 플레이어에게 READY 이벤트 전달 (호스트 포함, 발신자 제외)
    public void broadcastPlayerReady(String playerId) {
        ConnectionMessage msg = ConnectionMessage.createPlayerReady(playerId);

        // Host에도 전달 (호스트가 발신자가 아닌 경우만)
        if (hostMessageListener != null && !playerId.equals(hostPlayerId)) {
            hostMessageListener.onPlayerReady(playerId);
        }

        // 발신자 제외 클라이언트들에게 전달
        broadcastToOthers(playerId, msg);
    }

    // 모든 플레이어에게 UNREADY 이벤트 전달 (호스트 포함, 발신자 제외)
    public void broadcastPlayerUnready(String playerId) {
        ConnectionMessage msg = ConnectionMessage.createPlayerUnready(playerId);

        // Host에도 전달 (호스트가 발신자가 아닌 경우만)
        if (hostMessageListener != null && !playerId.equals(hostPlayerId)) {
            hostMessageListener.onPlayerUnready(playerId);
        }

        // 발신자 제외 클라이언트들에게 전달
        broadcastToOthers(playerId, msg);
    }

    // 발신자를 제외한 모든 플레이어에게 보드 업데이트 전달
    public void broadcastBoardUpdateToOthers(String senderId, BoardUpdateMessage msg) {
        broadcastToOthers(senderId, msg);
    }

    // 발신자를 제외한 모든 플레이어에게 공격 전달
    public void broadcastAttackToOthers(String senderId, AttackMessage msg) {
        broadcastToOthers(senderId, msg);
    }

    // 발신자를 제외한 모든 플레이어에게 Pause 전달
    public void broadcastPauseToOthers(String senderId) {
        ConnectionMessage pause =
                new ConnectionMessage(MessageType.PAUSE, senderId, "Game paused by " + senderId);
        broadcastToOthers(senderId, pause);
    }

    // 발신자를 제외한 모든 플레이어에게 Resume 전달
    public void broadcastResumeToOthers(String senderId) {
        ConnectionMessage resume =
                new ConnectionMessage(MessageType.RESUME, senderId, "Game resumed by " + senderId);
        broadcastToOthers(senderId, resume);
    }

    // 발신자를 제외한 모든 플레이어에게 GameOver 전달
    public void broadcastGameOverToOthers(String senderId, String reason) {
        ConnectionMessage msg = ConnectionMessage.createGameOver(senderId, reason);
        broadcastToOthers(senderId, msg);
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

    // 클라이언트 채팅 메시지를 호스트에게 알림
    public void notifyHostChatMessage(String senderId, String message) {
        if (hostMessageListener != null) {
            hostMessageListener.onChatMessageReceived(senderId, message);
        }
    }

    // 호스트의 보드 상태를 클라이언트에게 전송 (다음 블록, incoming blocks 포함)
    public boolean sendHostBoardUpdate(
            int[][] board,
            int pieceX,
            int pieceY,
            int pieceType,
            int pieceRotation,
            boolean pieceIsItem,
            String pieceItemType,
            int pieceItemBlockIndex,
            int nextPieceType,
            boolean nextIsItem,
            String nextItemType,
            int nextItemBlockIndex,
            java.util.Queue<int[][]> incomingBlocks,
            int score,
            int lines,
            int level) {
        if (!gameInProgress) {
            return false;
        }

        BoardUpdateMessage boardMsg =
                new BoardUpdateMessage(
                        hostPlayerId,
                        board,
                        pieceX,
                        pieceY,
                        pieceType,
                        pieceRotation,
                        pieceIsItem,
                        pieceItemType,
                        pieceItemBlockIndex,
                        nextPieceType,
                        nextIsItem,
                        nextItemType,
                        nextItemBlockIndex,
                        incomingBlocks,
                        score,
                        lines,
                        level);
        broadcastMessage(boardMsg);
        return true;
    }

    // 호스트의 공격을 클라이언트에게 전송
    public boolean sendHostAttack(int clearedLines) {
        if (!gameInProgress) {
            return false;
        }

        AttackMessage attackMsg = AttackMessage.createStandardAttack(hostPlayerId, clearedLines);
        broadcastToOthers(hostPlayerId, attackMsg);
        notifyHostAttack(attackMsg);

        return true;
    }

    // 호스트가 게임 일시정지
    public boolean pauseGameAsHost() {
        ConnectionMessage pauseMsg =
                new ConnectionMessage(MessageType.PAUSE, hostPlayerId, "Game paused by host");
        broadcastMessage(pauseMsg);
        return true;
    }

    // 호스트가 게임 재개
    public boolean resumeGameAsHost() {
        ConnectionMessage resumeMsg =
                new ConnectionMessage(MessageType.RESUME, hostPlayerId, "Game resumed by host");
        broadcastMessage(resumeMsg);
        return true;
    }

    // 호스트가 채팅 메시지 전송
    public void sendChatMessage(String message) {
        ChatMessage chatMsg = new ChatMessage(hostPlayerId, message);
        broadcastMessage(chatMsg);

        // 호스트에게도 자신의 메시지를 알림 (에코)
        if (hostMessageListener != null) {
            hostMessageListener.onChatMessageReceived(hostPlayerId, message);
        }
    }

    // 호스트가 준비 완료
    public void setHostReady() {
        // 먼저 준비 상태 설정
        setPlayerReady(hostPlayerId, true);

        // 모든 클라이언트에게 호스트 준비 알림
        broadcastPlayerReady(hostPlayerId);

        // 모든 플레이어가 준비되었는지 확인 (게임 시작)
        checkAllReady();
    }

    public void setHostUnready() {
        setPlayerReady(hostPlayerId, false);
        broadcastPlayerUnready(hostPlayerId);
    }

    // 서버 중지
    public void stop() {
        isRunning = false;

        // 0. 카운트다운 타이머 정리 (Timer는 non-daemon 스레드를 생성함)
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }

        // 1. acceptClients 작업 취소 (가장 먼저 수행)
        if (acceptClientsFuture != null && !acceptClientsFuture.isDone()) {
            acceptClientsFuture.cancel(true); // 인터럽트 발생
        }

        // 2. 서버 소켓 종료 (accept() 블로킹 해제)
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        // 3. 모든 클라이언트에게 서버 종료 알림
        if (gameInProgress) {
            endGame("Server shutdown");
        } else if (!connectedClients.isEmpty()) {
            ConnectionMessage serverShutdown =
                    ConnectionMessage.createGameOver("server", "Server shutdown");
            broadcastMessage(serverShutdown);
        }

        // 4. 모든 클라이언트 연결 종료
        for (ClientHandler client : connectedClients.values()) {
            client.close();
        }
        connectedClients.clear();

        // 5. 스레드 풀 종료 (타임아웃 2초로 단축)
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                // 한 번 더 대기
                if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {}
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
}

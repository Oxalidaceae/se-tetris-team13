package team13.tetris.network.client;

import team13.tetris.network.protocol.*;
import team13.tetris.network.listener.ClientMessageListener;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// Socket을 통해 서버에 접속하여 게임 플레이
public class TetrisClient {
    private static final int DEFAULT_PORT = 12345;
    
    private final String serverHost;
    private final int serverPort;
    private final String playerId;
    
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ExecutorService messageHandler;
    private Future<?> messageLoopFuture;  // messageLoop 작업 추적용
    
    private volatile boolean isConnected = false;
    private volatile boolean gameStarted = false;
    
    private ClientMessageListener messageListener;
    
    public TetrisClient(String playerId, String serverHost, int serverPort) {
        this.playerId = playerId;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.messageHandler = Executors.newSingleThreadExecutor();
    }
    
    public TetrisClient(String playerId, String serverHost) {
        this(playerId, serverHost, DEFAULT_PORT);
    }
    
    // 메시지 리스너 설정
    public void setMessageListener(ClientMessageListener listener) {
        this.messageListener = listener;
    }
    
    // 서버에 접속
    public boolean connect() {
        try {
            System.out.println("Connecting to server " + serverHost);
            
            // 서버에 소켓 연결 (10초 타임아웃)
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, serverPort), 10000);
            
            // 입출력 스트림 설정
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // 연결 요청 메시지 전송
            ConnectionMessage connectionRequest = ConnectionMessage.createConnectionRequest(playerId, playerId);
            
            // 직접 전송 (sendMessage를 사용하지 않음)
            synchronized (output) {
                output.writeObject(connectionRequest);
                output.flush();
            }
            System.out.println("Sending connection request...");
            
            // 연결 응답 대기
            Object response = input.readObject();
            if (!(response instanceof ConnectionMessage msg)) {
                notifyError("Invalid connection response");
                return false;
            }

            if (msg.getType() == MessageType.CONNECTION_ACCEPTED) {
                isConnected = true;
                System.out.println("Connected successfully!");
                if (messageListener != null) messageListener.onConnectionAccepted();

                // 메시지 수신 루프 스레드 시작 (Future 저장하여 나중에 취소 가능)
                messageLoopFuture = messageHandler.submit(this::messageLoop);

                return true;
            }

            // REJECTED
            notifyError(msg.getMessage());
            if (messageListener != null) {
                messageListener.onConnectionRejected(msg.getMessage());
            }
            return false;
        } catch (SocketTimeoutException e) {
            notifyError("Connection timed out. Please check the server IP.");
            return false;
        } catch (Exception e) {
            notifyError("Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    // 메시지 수신 루프
    private void messageLoop() {
        while (isConnected && !socket.isClosed()) {
            try {
                Object obj = input.readObject();
                
                if (obj instanceof NetworkMessage message) {
                    handleReceivedMessage(message);
                }
            } catch (IOException e) {
                if (isConnected) {
                    notifyError("Connection lost: " + e.getMessage());
                    // 서버 연결 종료 알림
                    if (messageListener != null) {
                        messageListener.onServerDisconnected("Server disconnected");
                    }
                }
                break;
            } catch (ClassNotFoundException e) {
                notifyError("Unknown object from server");
            }
        }
        
        // 연결 종료 처리
        isConnected = false;
        gameStarted = false;
    }
    
    // 수신한 메시지 처리
    private void handleReceivedMessage(NetworkMessage message) {
        System.out.println("Received from server: " + message.getType());
        
        if (messageListener == null) {
            return;
        }
        
        switch (message.getType()) {
            case PLAYER_READY -> {
                // 누군가 준비 완료
                if (message instanceof ConnectionMessage connMsg) {
                    messageListener.onPlayerReady(connMsg.getSenderId());
                }
            }

            case PLAYER_UNREADY -> {
                if (message instanceof ConnectionMessage connMsg) {
                    messageListener.onPlayerUnready(connMsg.getSenderId());
                }
            }
            
            case GAME_START -> {
                gameStarted = true;
                messageListener.onGameStart();
            }

            case COUNTDOWN_START -> {
                messageListener.onCountdownStart();
            }
            
            case GAME_OVER -> {
                gameStarted = false;
                String reason = (message instanceof ConnectionMessage connMsg) ? connMsg.getMessage() : "";
                messageListener.onGameOver(reason);
            }
            
            case BOARD_UPDATE -> {
                // 상대방의 보드 상태 업데이트
                if (message instanceof BoardUpdateMessage boardMsg) {
                    messageListener.onBoardUpdate(boardMsg);
                }
            }
            
            case ATTACK_SENT -> {
                // 공격 메시지 수신
                if (message instanceof AttackMessage attackMsg) {
                    messageListener.onAttackReceived(attackMsg);
                }
            }
            
            case PAUSE -> {
                messageListener.onGamePaused();
            }
            
            case RESUME -> {
                messageListener.onGameResumed();
            }
            
            case GAME_MODE_SELECTED -> {
                // 서버가 게임모드를 선택함
                if (message instanceof GameModeMessage gameModeMsg) {
                    messageListener.onGameModeSelected(gameModeMsg.getGameMode());
                }
            }
            
            case ERROR -> {
                // 에러 메시지 수신
                if (message instanceof SystemMessage sysMsg) {
                    messageListener.onError("Server error: " + sysMsg.getMessage());
                }
            }
            
            default -> {
                System.err.println("Unhandled message type: " + message.getType());
            }
        }
    }
    
    // 서버에 메시지 전송
    public boolean sendMessage(NetworkMessage message) {        
        if (!isConnected || socket == null || socket.isClosed()) {
            System.err.println("Cannot send message: not connected to server");
            return false;
        }
        
        try {
            synchronized (output) {
                output.writeObject(message);
                output.flush();
            }
            return true;
            
        } catch (IOException e) {
            notifyError("Send error: " + e.getMessage());
            return false;
        }
    }

    public boolean requestReady() {
        boolean result = sendMessage(ConnectionMessage.createPlayerReady(playerId));
        return result;
    }

    public boolean requestUnready() {
        return sendMessage(ConnectionMessage.createPlayerUnready(playerId));
    }
    
    // 보드 상태 업데이트 전송
    public boolean sendBoardUpdate(int[][] board, int pieceX, int pieceY,
                                  int pieceType, int pieceRotation, int nextPieceType,
                                  java.util.Queue<int[][]> incomingBlocks,
                                  int score, int lines, int level) {
        if (!gameStarted) {
            return false;
        }

        BoardUpdateMessage boardMsg = new BoardUpdateMessage(playerId, board, pieceX, pieceY,
                                                            pieceType, pieceRotation, nextPieceType,
                                                            incomingBlocks, score, lines, level);
        return sendMessage(boardMsg);
    }
    
    // 공격 전송
    public boolean sendAttack(String targetPlayerId, int clearedLines) {
        if (!gameStarted) {
            return false;
        }
        
        AttackMessage attackMsg = AttackMessage.createStandardAttack(playerId, clearedLines);
        return sendMessage(attackMsg);
    }
    
    // 게임 일시정지
    public boolean pauseGame() {
        ConnectionMessage pauseMsg = new ConnectionMessage(MessageType.PAUSE, playerId, "Game paused by " + playerId);
        return sendMessage(pauseMsg);
    }
    
    
    // 게임 재개
    public boolean resumeGame() {
        ConnectionMessage resumeMsg = new ConnectionMessage(MessageType.RESUME, playerId, "Game resumed by " + playerId);
        return sendMessage(resumeMsg);
    }
    
    // 연결 해제
    public void disconnect() {
        if (isConnected) {
            // 연결 해제 메시지 전송
            ConnectionMessage disconnectMsg = new ConnectionMessage(MessageType.DISCONNECT, playerId, "Player disconnected");
            sendMessage(disconnectMsg);
        }
        cleanup();
    }
    
    // 리소스 정리
    private void cleanup() {
        System.out.println("Starting cleanup...");
        isConnected = false;
        gameStarted = false;
        
        // 1. messageLoop 작업 취소
        if (messageLoopFuture != null && !messageLoopFuture.isDone()) {
            messageLoopFuture.cancel(true);  // 인터럽트 발생
        }
        
        // 2. 스트림 및 소켓 정리 (readObject() 블로킹 해제)
        try { if (input != null) input.close(); } catch (IOException ignore) {}
        try { if (output != null) output.close(); } catch (IOException ignore) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignore) {}

        System.out.println("Client disconnected");

        // 3. 메시지 핸들러 종료 (타임아웃 2초)
        if (messageHandler != null && !messageHandler.isShutdown()) {
            messageHandler.shutdown();
            try {
                if (!messageHandler.awaitTermination(2, TimeUnit.SECONDS)) {
                    messageHandler.shutdownNow();
                    // 한 번 더 대기
                    if (!messageHandler.awaitTermination(1, TimeUnit.SECONDS)) {
                        System.err.println("Message handler did not terminate after shutdownNow");
                    }
                }
            } catch (InterruptedException e) {
                messageHandler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Client cleanup completed");
    }

    private void notifyError(String msg) {
        if (messageListener != null) messageListener.onError(msg);
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getServerAddress() {
        return serverHost + ":" + serverPort;
    }
}

package team13.tetris.network.client;

import team13.tetris.network.protocol.*;
import team13.tetris.network.listener.ClientMessageListener;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// Socket을 통해 서버에 접속하여 게임 플레이
public class TetrisClient {
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_HOST = "localhost";
    
    private final String serverHost;
    private final int serverPort;
    private final String playerId;
    
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ExecutorService messageHandler;
    
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
    
    public TetrisClient(String playerId) {
        this(playerId, DEFAULT_HOST, DEFAULT_PORT);
    }
    
    
    // 메시지 리스너 설정
    public void setMessageListener(ClientMessageListener listener) {
        this.messageListener = listener;
    }
    
    // 서버에 접속
    public boolean connect() {
        try {
            System.out.println("Connecting to server " + serverHost + ":" + serverPort + "...");
            
            // 서버에 소켓 연결
            socket = new Socket(serverHost, serverPort);
            
            // 입출력 스트림 설정
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // 연결 요청 메시지 전송
            ConnectionMessage connectionRequest = ConnectionMessage.createConnectionRequest(playerId, playerId);
            System.out.println("Sending connection request...");
            output.writeObject(connectionRequest);
            output.flush();
            System.out.println("Connection request sent!");
            
            // 연결 응답 대기
            Object response = input.readObject();
            
            if (response instanceof ConnectionMessage connMsg) {
                if (connMsg.getType() == MessageType.CONNECTION_ACCEPTED) {
                    isConnected = true;
                    System.out.println("Connected to server successfully!");
                    
                    // 메시지 수신 스레드 시작
                    messageHandler.submit(this::messageLoop);
                    
                    if (messageListener != null) {
                        messageListener.onConnectionAccepted();
                    }
                    
                    return true;
                    
                } else if (connMsg.getType() == MessageType.CONNECTION_REJECTED) {
                    System.err.println("Connection rejected: " + connMsg.getMessage());
                    
                    if (messageListener != null) {
                        messageListener.onConnectionRejected(connMsg.getMessage());
                    }
                    
                    return false;
                }
            }
            
            System.err.println("Invalid connection response from server");
            return false;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            
            if (messageListener != null) {
                messageListener.onError("Connection failed: " + e.getMessage());
            }
            
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
                } else {
                    System.err.println("Received non-NetworkMessage object from server");
                }
                
            } catch (IOException e) {
                if (isConnected) {
                    System.err.println("Connection lost with server: " + e.getMessage());
                    
                    if (messageListener != null) {
                        messageListener.onError("Connection lost: " + e.getMessage());
                    }
                }
                break;
                
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown message class from server: " + e.getMessage());
                
                if (messageListener != null) {
                    messageListener.onError("Unknown message: " + e.getMessage());
                }
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
            
            case GAME_START -> {
                gameStarted = true;
                messageListener.onGameStart();
            }
            
            case GAME_OVER -> {
                gameStarted = false;
                String reason = "";
                if (message instanceof ConnectionMessage connMsg) {
                    reason = connMsg.getMessage();
                }
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
        if (!isConnected || socket.isClosed()) {
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
            System.err.println("Failed to send message: " + e.getMessage());
            
            if (messageListener != null) {
                messageListener.onError("Send failed: " + e.getMessage());
            }
            
            return false;
        }
    }
    
    // 보드 상태 업데이트 전송ming blocks 포함)
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
        
        AttackMessage attackMsg = AttackMessage.createStandardAttack(playerId, targetPlayerId, clearedLines);
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
    
    // 게임 시작 요청
    public boolean requestGameStart() {
        ConnectionMessage playerReadyMsg = ConnectionMessage.createPlayerReady(playerId);
        return sendMessage(playerReadyMsg);
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
        isConnected = false;
        gameStarted = false;
        
        // 메시지 핸들러 종료
        if (messageHandler != null && !messageHandler.isShutdown()) {
            messageHandler.shutdown();
            try {
                if (!messageHandler.awaitTermination(2, TimeUnit.SECONDS)) {
                    messageHandler.shutdownNow();
                }
            } catch (InterruptedException e) {
                messageHandler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 스트림 정리
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // 무시
        }
        
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            // 무시
        }
        
        // 소켓 정리
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // 무시
        }
        
        System.out.println("Client cleanup completed");
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

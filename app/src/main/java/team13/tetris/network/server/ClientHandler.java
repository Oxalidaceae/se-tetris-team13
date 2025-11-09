package team13.tetris.network.server;

import team13.tetris.network.protocol.*;
import java.io.*;
import java.net.*;


// 서버에서 각 클라이언트 연결을 처리하는 핸들러(각 클라이언트마다 별도의 스레드에서 실행)
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final TetrisServer server;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String playerId;
    private volatile boolean isRunning = true;
    
    public ClientHandler(Socket clientSocket, TetrisServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            setupStreams();
            handleConnection();
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    // 입출력 스트림 설정
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(clientSocket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(clientSocket.getInputStream());
    }
    
    // 클라이언트 연결 처리
    private void handleConnection() throws IOException {
        // 연결 요청 대기
        ConnectionMessage connectionRequest = waitForConnectionRequest();
        if (connectionRequest == null) {
            return;
        }
        
        // 플레이어 ID 추출 (메시지에서 플레이어 이름 파싱)
        playerId = connectionRequest.getSenderId();
        
        // 서버에 클라이언트 등록
        boolean registered = server.registerClient(playerId, this);
        
        if (registered) {
            // 연결 승인 메시지 전송
            ConnectionMessage accepted = ConnectionMessage.createConnectionAccepted("server", playerId);
            sendMessage(accepted);
            
            // 메시지 처리 루프 시작
            messageLoop();
            
        } else {
            // 연결 거절 메시지 전송
            ConnectionMessage rejected = ConnectionMessage.createConnectionRejected("server", "Server is full");
            sendMessage(rejected);
        }
    }
    
    
    // 연결 요청 메시지 대기
    private ConnectionMessage waitForConnectionRequest() {
        System.out.println("Waiting for connection request...");

        try {
            Object obj = input.readObject();
            
            if (obj instanceof ConnectionMessage connMsg && connMsg.isConnectionRequest()) {
                return connMsg;
            } else {
                System.err.println("Invalid connection request from client");
                return null;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to read connection request: " + e.getMessage());
            return null;
        }
    }
    
    
    // 메시지 처리 루프
    private void messageLoop() {
        while (isRunning && !clientSocket.isClosed()) {
            try {
                Object obj = input.readObject();
                
                if (obj instanceof NetworkMessage message) {
                    handleMessage(message);
                } else {
                    System.err.println("Received non-NetworkMessage object from " + playerId);
                }
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Connection lost with " + playerId + ": " + e.getMessage());
                }
                break;
                
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown message class from " + playerId + ": " + e.getMessage());
            }
        }
    }
    
    
    // 수신한 메시지 처리
    private void handleMessage(NetworkMessage message) {
        System.out.println("Received from " + playerId + ": " + message.getType());
        
        switch (message.getType()) {
            case MOVE_LEFT, MOVE_RIGHT, ROTATE, SOFT_DROP, HARD_DROP -> {
                if (message instanceof InputMessage inputMsg) {
                    server.notifyHostInput(inputMsg);
                    server.broadcastToOthers(playerId, inputMsg);
                }
            }
            
            case BOARD_UPDATE -> {
                if (message instanceof BoardUpdateMessage boardMsg) {
                    server.notifyHostBoardUpdate(boardMsg);
                    server.broadcastToOthers(playerId, boardMsg);
                }
            }
            
            case ATTACK_SENT -> {
                if (message instanceof AttackMessage attackMsg) {
                    server.notifyHostAttack(attackMsg);
                    server.broadcastToOthers(playerId, attackMsg);
                }
            }
            
            case PAUSE -> {
                server.notifyHostPause();
                server.broadcastToOthers(playerId, new ConnectionMessage(MessageType.PAUSE, playerId, "Game paused by " + playerId));
            }
            
            case RESUME -> {
                server.notifyHostResume();
                server.broadcastToOthers(playerId, new ConnectionMessage(MessageType.RESUME, playerId, "Game resumed by " + playerId));
            }
            
            case GAME_OVER -> {
                if (message instanceof ConnectionMessage connMsg) {
                    server.notifyHostGameOver(connMsg.getMessage());
                    server.broadcastToOthers(playerId, connMsg);
                }
            }
            
            case PLAYER_READY -> {
                System.out.println(playerId + " is ready to start game");
                server.setPlayerReady(playerId, true);
                
                ConnectionMessage readyNotification = ConnectionMessage.createPlayerReady(playerId);
                server.broadcastToAll(readyNotification);
            }
            
            case GAME_START -> {
                // 더 이상 사용하지 않음 (PLAYER_READY로 대체)
                // 하지만 하위 호환성을 위해 남겨둠
                System.out.println("🎮 " + playerId + " is ready to start game (legacy)");
                server.setPlayerReady(playerId, true);
            }
            
            case LINES_CLEARED -> {
                if (message instanceof LinesClearedMessage linesClearedMsg) {
                    server.notifyHostLinesCleared(linesClearedMsg);
                    server.broadcastToOthers(playerId, linesClearedMsg);
                }
            }
            
            case HEARTBEAT -> {
                if (message instanceof SystemMessage) {
                    try {
                        sendMessage(SystemMessage.createHeartbeat("server"));
                    } catch (IOException e) {
                        System.err.println("Failed to send heartbeat response: " + e.getMessage());
                    }
                }
            }
            
            case ERROR -> {
                if (message instanceof SystemMessage sysMsg) {
                    System.err.println("Error from " + playerId + ": " + sysMsg.getMessage());
                }
            }
            
            case DISCONNECT -> {
                System.out.println(playerId + " requested disconnect");
                close();
            }
            
            default -> {
                System.err.println("Unhandled message type: " + message.getType() + " from " + playerId);
            }
        }
    }
    
    
    // 클라이언트에게 메시지 전송
    public void sendMessage(NetworkMessage message) throws IOException {
        if (output != null && !clientSocket.isClosed()) {
            synchronized (output) {
                output.writeObject(message);
                output.flush();
            }
        }
    }
    
    // 연결 종료
    public void close() {
        isRunning = false;
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
    
    // 리소스 정리
    private void cleanup() {
        // 서버에서 클라이언트 등록 해제
        if (playerId != null) {
            server.unregisterClient(playerId);
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
        close();
        
        System.out.println("Cleaned up client handler for " + playerId);
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return isRunning && clientSocket != null && !clientSocket.isClosed();
    }
    
    public String getClientAddress() {
        return clientSocket != null ? clientSocket.getInetAddress().toString() : "unknown";
    }
}

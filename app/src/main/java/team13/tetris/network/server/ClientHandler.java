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
    private volatile boolean running = true;
    
    public ClientHandler(Socket clientSocket, TetrisServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            setupStreams();
            if (!handleConnectionRequest()) {
                cleanup();
                return;
            }
            messageLoop();
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
    private boolean  handleConnectionRequest() throws IOException {
        try {
            Object obj = input.readObject();
            if (!(obj instanceof ConnectionMessage req) || !req.isConnectionRequest()) {
                System.err.println("[ClientHandler] Invalid connection request.");
                return false;
            }

            this.playerId = req.getSenderId();

            boolean registered = server.registerClient(playerId, this);
            if (!registered) {
                sendMessage(ConnectionMessage.createConnectionRejected("server", "Server is full"));
                return false;
            }

            // Accept 메시지 전송
            sendMessage(ConnectionMessage.createConnectionAccepted("server", playerId));
            
            // 클라이언트에게 현재 서버 상태 전송
            server.sendInitialStateToClient(playerId);
            
            return true;

        } catch (Exception e) {
            System.err.println("[ClientHandler] Failed to read connection request: " + e.getMessage());
            return false;
        }
    }    
    
    // 메시지 처리 루프
    private void messageLoop() {
        while (running && !clientSocket.isClosed()) {
            try {
                Object obj = input.readObject();
                
                if (obj instanceof NetworkMessage message) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                System.out.println("[ClientHandler] Disconnected: " + playerId);
                running = false;
                break;
                
            } catch (ClassNotFoundException e) {
                System.err.println("[ClientHandler] Unknown message from " + playerId);
            }
        }
    }
    
    
    // 수신한 메시지 처리(메시지를 서버로 위임)
    private void handleMessage(NetworkMessage message) {      
        System.out.println("Received from client: " + message.getType());
        switch (message.getType()) {
            case PLAYER_READY -> {
                server.setPlayerReady(playerId, true);
                server.broadcastPlayerReady(playerId);

                // 모든 플레이어가 준비되었는지 확인 (게임 시작)
                server.checkAllReady();
            }
            case BOARD_UPDATE -> {
                if (message instanceof BoardUpdateMessage boardMsg) {
                    server.notifyHostBoardUpdate(boardMsg);
                    server.broadcastBoardUpdateToOthers(playerId, boardMsg);
                }
            }
            
            case ATTACK_SENT -> {
                if (message instanceof AttackMessage attackMsg) {
                    server.notifyHostAttack(attackMsg);
                    server.broadcastAttackToOthers(playerId, attackMsg);
                }
            }
            
            case PAUSE -> {
                server.notifyHostPause();
                server.broadcastPauseToOthers(playerId);
            }
            
            case RESUME -> {
                server.notifyHostResume();
                server.broadcastResumeToOthers(playerId);
            }
            
            case GAME_OVER -> {
                String reason = (message instanceof ConnectionMessage connMsg) ? connMsg.getMessage() : "Game over";
                server.notifyHostGameOver(reason);
                server.broadcastGameOverToOthers(playerId, reason);
            }
            
            case DISCONNECT -> {
                close();
            }     
            
            default -> {
                System.err.println("[ClientHandler] Unhandled message: " + message.getType());
            }
        }
    }
    
    // 서버 -> 클라이언트 메시지 전송
    public void sendMessage(NetworkMessage msg) throws IOException {
        synchronized (output) {
            output.writeObject(msg);
            output.flush();
        }
    }

    // 연결 종료
    public void close() {
        running = false;
        
        try { clientSocket.close(); } catch (IOException ignored) {}
    }
    
    // 리소스 정리
    private void cleanup() {
        // 서버에서 클라이언트 등록 해제
        server.unregisterClient(playerId);
        
        // 스트림 정리
        try { input.close(); } catch (Exception ignored) {}
        try { output.close(); } catch (Exception ignored) {}
        try { clientSocket.close(); } catch (Exception ignored) {}

        System.out.println("Cleaned up client handler for " + playerId);
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return running && clientSocket != null && !clientSocket.isClosed();
    }
    
}

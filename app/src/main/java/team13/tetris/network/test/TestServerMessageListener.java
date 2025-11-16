package team13.tetris.network.test;

import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;


// CLI 테스트용 서버 메시지 리스너
// 콘솔에 메시지를 출력하여 네트워크 통신을 확인
public class TestServerMessageListener implements ServerMessageListener {
    
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
    public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
        if (boardUpdate != null) {
            System.out.println("Client board updated - Score: " + boardUpdate.getScore());
        } else {
            System.out.println("Client board updated - Score: null");
        }
    }
    
    @Override
    public void onAttackReceived(AttackMessage attackMessage) {
        if (attackMessage != null) {
            System.out.println("Attack received: " + attackMessage.getAttackLines() + " lines!");
        } else {
            System.out.println("Attack received: null lines!");
        }
    }
    
    @Override
    public void onGamePaused() {
        System.out.println("[PAUSE]Client paused the game");
    }
    
    @Override
    public void onGameResumed() {
        System.out.println("[PLAY]Client resumed the game");
    }
}

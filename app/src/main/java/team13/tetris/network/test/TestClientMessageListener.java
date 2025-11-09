package team13.tetris.network.test;

import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.protocol.*;

/**
 * CLI 테스트용 클라이언트 메시지 리스너
 * 콘솔에 메시지를 출력하여 네트워크 통신을 확인
 */
public class TestClientMessageListener implements ClientMessageListener {
    
    @Override
    public void onConnectionAccepted() {
        System.out.println("✅ Successfully connected to server!");
    }
    
    @Override
    public void onConnectionRejected(String reason) {
        System.out.println("❌ Connection rejected: " + reason);
    }
    
    @Override
    public void onPlayerReady(String playerId) {
        System.out.println("👤 " + playerId + " is ready!");
    }
    
    @Override
    public void onGameStart() {
        System.out.println("🎮 Game started! You can now play.");
    }
    
    @Override
    public void onGameOver(String reason) {
        System.out.println("🏁 Game over: " + reason);
    }
    
    @Override
    public void onInputReceived(InputMessage inputMessage) {
        System.out.println("🎮 Opponent input: " + inputMessage.getInputType());
    }
    
    @Override
    public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
        System.out.println("📊 Opponent board updated - Score: " + boardUpdate.getScore());
    }
    
    @Override
    public void onAttackReceived(AttackMessage attackMessage) {
        System.out.println("💥 Attack received: " + attackMessage.getAttackLines() + " lines!");
    }
    
    @Override
    public void onLinesClearedReceived(LinesClearedMessage linesClearedMessage) {
        System.out.println("📊 Opponent cleared " + linesClearedMessage.getLinesCleared() + " lines!");
    }
    
    @Override
    public void onGamePaused() {
        System.out.println("⏸️ Game paused");
    }
    
    @Override
    public void onGameResumed() {
        System.out.println("▶️ Game resumed");
    }
    
    @Override
    public void onError(String error) {
        System.err.println("❌ Error: " + error);
    }
    
    @Override
    public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
        System.out.println("🎯 Server selected game mode: " + gameMode);
    }
}

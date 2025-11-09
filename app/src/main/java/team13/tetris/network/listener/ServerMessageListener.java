package team13.tetris.network.listener;

import team13.tetris.network.protocol.*;

// 서버(호스트)가 클라이언트로부터 받은 메시지를 처리하는 리스너 인터페이스
public interface ServerMessageListener {
    
    // 클라이언트가 연결되었을 때 호출
    void onClientConnected(String clientId);
    
    // 클라이언트가 연결 해제되었을 때 호출
    void onClientDisconnected(String clientId);
    
    // 플레이어가 준비 완료했을 때 호출
    void onPlayerReady(String playerId);
    
    // 게임이 시작되었을 때 호출
    void onGameStart();
    
    // 게임이 종료되었을 때 호출
    void onGameOver(String reason);
    
    // 클라이언트의 입력을 받았을 때 호출
    void onInputReceived(InputMessage inputMessage);
    
    // 클라이언트의 보드 상태 업데이트를 받았을 때 호출
    void onBoardUpdate(BoardUpdateMessage boardUpdate);
    
    // 공격을 받았을 때 호출
    void onAttackReceived(AttackMessage attackMessage);
    
    // 클라이언트가 줄을 삭제했을 때 호출
    void onLinesClearedReceived(LinesClearedMessage linesClearedMessage);
    
    // 게임이 일시정지되었을 때 호출
    void onGamePaused();
    
    // 게임이 재개되었을 때 호출
    void onGameResumed();
}

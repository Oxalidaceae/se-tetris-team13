package team13.tetris.network.listener;

import team13.tetris.network.protocol.*;


// 클라이언트가 서버로부터 받은 메시지를 처리하는 리스너 인터페이스
public interface ClientMessageListener {
    
    // 서버 연결이 승인되었을 때 호출
    void onConnectionAccepted();
    
    // 서버 연결이 거절되었을 때 호출
    void onConnectionRejected(String reason);
    
    // 서버 연결이 끊겼을 때 호출
    void onServerDisconnected(String reason);
    
    // 플레이어(호스트 또는 다른 클라이언트)가 준비 완료했을 때 호출
    void onPlayerReady(String playerId);
    
    // 게임이 시작되었을 때 호출
    void onGameStart();
    
    // 게임이 종료되었을 때 호출
    void onGameOver(String reason);
    
    // 상대방의 보드 상태 업데이트를 받았을 때 호출
    void onBoardUpdate(BoardUpdateMessage boardUpdate);
    
    // 공격을 받았을 때 호출
    void onAttackReceived(AttackMessage attackMessage);
    
    // 게임이 일시정지되었을 때 호출
    void onGamePaused();
    
    // 게임이 재개되었을 때 호출
    void onGameResumed();
    
    // 에러가 발생했을 때 호출
    void onError(String error);
    
    // 게임 모드가 선택되었을 때 호출
    void onGameModeSelected(GameModeMessage.GameMode gameMode);
}

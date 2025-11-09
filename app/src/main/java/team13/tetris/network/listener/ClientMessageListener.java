package team13.tetris.network.listener;

import team13.tetris.network.protocol.*;

/**
 * 클라이언트가 서버로부터 받은 메시지를 처리하는 리스너 인터페이스
 * GUI 또는 CLI 구현체에서 이 인터페이스를 구현하여 사용
 */
public interface ClientMessageListener {
    /**
     * 서버 연결이 승인되었을 때 호출
     */
    void onConnectionAccepted();
    
    /**
     * 서버 연결이 거절되었을 때 호출
     * @param reason 거절 사유
     */
    void onConnectionRejected(String reason);
    
    /**
     * 플레이어(호스트 또는 다른 클라이언트)가 준비 완료했을 때 호출
     * @param playerId 준비 완료한 플레이어 ID
     */
    void onPlayerReady(String playerId);
    
    /**
     * 게임이 시작되었을 때 호출
     */
    void onGameStart();
    
    /**
     * 게임이 종료되었을 때 호출
     * @param reason 종료 사유
     */
    void onGameOver(String reason);
    
    /**
     * 상대방의 입력을 받았을 때 호출
     * @param inputMessage 입력 메시지
     */
    void onInputReceived(InputMessage inputMessage);
    
    /**
     * 상대방의 보드 상태 업데이트를 받았을 때 호출
     * @param boardUpdate 보드 업데이트 메시지
     */
    void onBoardUpdate(BoardUpdateMessage boardUpdate);
    
    /**
     * 공격을 받았을 때 호출
     * @param attackMessage 공격 메시지
     */
    void onAttackReceived(AttackMessage attackMessage);
    
    /**
     * 상대방이 줄을 삭제했을 때 호출
     * @param linesClearedMessage 줄 삭제 메시지
     */
    void onLinesClearedReceived(LinesClearedMessage linesClearedMessage);
    
    /**
     * 게임이 일시정지되었을 때 호출
     */
    void onGamePaused();
    
    /**
     * 게임이 재개되었을 때 호출
     */
    void onGameResumed();
    
    /**
     * 에러가 발생했을 때 호출
     * @param error 에러 메시지
     */
    void onError(String error);
    
    /**
     * 게임 모드가 선택되었을 때 호출
     * @param gameMode 선택된 게임 모드
     */
    void onGameModeSelected(GameModeMessage.GameMode gameMode);
}

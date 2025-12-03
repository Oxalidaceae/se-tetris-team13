package team13.tetris.network.listener;

import java.util.List;
import team13.tetris.network.protocol.*;

// 클라이언트가 서버로부터 받은 메시지를 처리하는 리스너 인터페이스
public interface ClientMessageListener {

    // 서버 연결이 승인되었을 때 호출
    void onConnectionAccepted(String assignedClientId);

    // 서버 연결이 거절되었을 때 호출
    void onConnectionRejected(String reason);

    // 서버 연결이 끊겼을 때 호출
    void onServerDisconnected(String reason);

    // 로비 상태 업데이트 (서버로부터 전체 플레이어 상태를 받을 때)
    void onLobbyStateUpdate(List<LobbyStateMessage.PlayerState> playerStates);

    // 플레이어(호스트 또는 다른 클라이언트)가 준비 완료했을 때 호출
    void onPlayerReady(String playerId);

    // 플레이어가 준비를 취소했을 때 호출
    void onPlayerUnready(String playerId);

    // 게임이 시작되었을 때 호출
    void onGameStart();

    // 카운트다운이 시작되었을 때 호출
    void onCountdownStart();

    // 게임이 종료되었을 때 호출
    void onGameOver(String reason);

    // 게임이 끝나고 최종 순위가 결정되었을 때 호출 (Squad PVP)
    void onGameEnd(List<String> rankings);

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

    // 채팅 메시지를 받았을 때 호출
    void onChatMessageReceived(String senderId, String message);
}

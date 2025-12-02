package team13.tetris.network.protocol;

// 네트워크 메시지 타입을 정의하는 열거형(사용되는 모든 메시지 타입을 포함)
public enum MessageType {
    // 연결 관련
    CONNECTION_REQUEST, // 클라이언트가 서버에 연결 요청
    CONNECTION_ACCEPTED, // 서버가 연결 승인
    CONNECTION_REJECTED, // 서버가 연결 거부 (서버 가득참 등)
    DISCONNECT, // 연결 해제

    // 게임 준비 및 시작
    GAME_MODE_SELECTED, // 서버가 게임모드 선택 (일반/아이템)
    PLAYER_READY, // 플레이어가 준비 완료
    PLAYER_UNREADY, // 플레이어가 준비 취소
    LOBBY_STATE_UPDATE, // 호스트가 전체 로비 상태 브로드캐스트 (플레이어 목록 + 준비 상태)
    GAME_START, // 게임 시작 신호 (모든 플레이어 준비 완료)
    COUNTDOWN_START, // 게임 시작 전 카운트다운 시작 신호

    // 게임 상태 동기화
    BOARD_UPDATE, // 보드 상태 업데이트 (다음 블록 포함)

    // 공격/방어 시스템
    ATTACK_SENT, // 공격 라인 전송

    // 게임 제어
    PAUSE, // 게임 일시정지
    RESUME, // 게임 재개
    GAME_OVER, // 게임 오버

    // 시스템 메시지
    ERROR // 오류 메시지
}

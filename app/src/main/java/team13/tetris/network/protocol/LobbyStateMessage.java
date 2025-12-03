package team13.tetris.network.protocol;

import java.util.ArrayList;
import java.util.List;

/** 로비 상태 메시지 - 호스트가 모든 플레이어의 상태를 브로드캐스트 */
public class LobbyStateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private final List<PlayerState> players; // 순서대로 정렬된 플레이어 목록

    public LobbyStateMessage(String senderId, List<PlayerState> players) {
        super(MessageType.LOBBY_STATE_UPDATE, senderId);
        this.players = new ArrayList<>(players);
    }

    public List<PlayerState> getPlayers() {
        return new ArrayList<>(players);
    }

    /** 플레이어 상태 정보 */
    public static class PlayerState implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final String playerId;
        private final boolean ready;
        private final int order; // 연결 순서 (Host=0, Client1=1, Client2=2)

        public PlayerState(String playerId, boolean ready, int order) {
            this.playerId = playerId;
            this.ready = ready;
            this.order = order;
        }

        public String getPlayerId() {
            return playerId;
        }

        public boolean isReady() {
            return ready;
        }

        public int getOrder() {
            return order;
        }
    }
}

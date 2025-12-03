package team13.tetris.network.protocol;

import java.util.List;

/**
 * Message sent when the game ends with final rankings.
 *
 * <p>Contains the elimination order (last eliminated = winner).
 */
public class GameEndMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    private final List<String> rankings; // Order: 3rd, 2nd, 1st (elimination order)

    public GameEndMessage(String senderId, List<String> rankings) {
        super(MessageType.GAME_OVER, senderId);
        this.rankings = rankings;
    }

    public List<String> getRankings() {
        return rankings;
    }
}

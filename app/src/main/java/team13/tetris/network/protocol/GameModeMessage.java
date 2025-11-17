package team13.tetris.network.protocol;

/**
 * 서버가 게임모드를 선택했을 때 클라이언트에게 알리는 메시지
 */
public class GameModeMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public enum GameMode {
        NORMAL,     
        ITEM,
        TIMER
    }
    
    private final GameMode gameMode;
    
    public GameModeMessage(String serverId, GameMode gameMode) {
        super(MessageType.GAME_MODE_SELECTED, serverId);
        this.gameMode = gameMode;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public boolean isNormalMode() {
        return gameMode == GameMode.NORMAL;
    }

    public boolean isItemMode() {
        return gameMode == GameMode.ITEM;
    }

    public boolean isTimerMode() {
        return gameMode == GameMode.TIMER;
    }
    
    @Override
    public String toString() {
        return "GameModeMessage{" +
               "gameMode=" + gameMode +
               ", sender='" + getSenderId() + '\'' +
               ", timestamp=" + getTimestamp() +
               '}';
    }
}

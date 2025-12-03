package team13.tetris.config;

public class Settings {
    private boolean colorBlindMode = false;
    private String windowSize = "MEDIUM";
    private String keyLeft = "A";
    private String keyRight = "D";
    private String keyDown = "S";
    private String keyRotate = "W";
    private String keyDrop = "SPACE";
    private String pause = "ESCAPE";

    // Player 2 키 설정 (충돌 방지를 위해 다른 키 사용)
    private String keyLeftP2 = "LEFT";
    private String keyRightP2 = "RIGHT";
    private String keyDownP2 = "DOWN";
    private String keyRotateP2 = "UP";
    private String keyDropP2 = "SLASH";

    private String recentIP = "";

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode = colorBlindMode;
    }

    public String getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
    }

    public String getKeyLeft() {
        return keyLeft;
    }

    public void setKeyLeft(String keyLeft) {
        this.keyLeft = keyLeft;
    }

    public String getKeyRight() {
        return keyRight;
    }

    public void setKeyRight(String keyRight) {
        this.keyRight = keyRight;
    }

    public String getKeyDown() {
        return keyDown;
    }

    public void setKeyDown(String keyDown) {
        this.keyDown = keyDown;
    }

    public String getKeyRotate() {
        return keyRotate;
    }

    public void setKeyRotate(String keyRotate) {
        this.keyRotate = keyRotate;
    }

    public String getKeyDrop() {
        return keyDrop;
    }

    public void setKeyDrop(String keyDrop) {
        this.keyDrop = keyDrop;
    }

    public String getPause() {
        return pause;
    }

    public void setPause(String pause) {
        this.pause = pause;
    }

    // Player 2 키 설정 getter/setter 메서드들
    public String getKeyLeftP2() {
        return keyLeftP2;
    }

    public void setKeyLeftP2(String keyLeftP2) {
        this.keyLeftP2 = keyLeftP2;
    }

    public String getKeyRightP2() {
        return keyRightP2;
    }

    public void setKeyRightP2(String keyRightP2) {
        this.keyRightP2 = keyRightP2;
    }

    public String getKeyDownP2() {
        return keyDownP2;
    }

    public void setKeyDownP2(String keyDownP2) {
        this.keyDownP2 = keyDownP2;
    }

    public String getKeyRotateP2() {
        return keyRotateP2;
    }

    public void setKeyRotateP2(String keyRotateP2) {
        this.keyRotateP2 = keyRotateP2;
    }

    public String getKeyDropP2() {
        return keyDropP2;
    }

    public void setKeyDropP2(String keyDropP2) {
        this.keyDropP2 = keyDropP2;
    }

    public String getRecentIP() {
        return recentIP;
    }

    public void setRecentIP(String recentIP) {
        this.recentIP = recentIP;
    }

    public boolean isKeyAlreadyUsed(String key) {
        if (key == null || key.trim().isEmpty()) return false;

        key = key.toUpperCase();

        return key.equals(keyLeft.toUpperCase())
                || key.equals(keyRight.toUpperCase())
                || key.equals(keyDown.toUpperCase())
                || key.equals(keyRotate.toUpperCase())
                || key.equals(keyDrop.toUpperCase())
                || key.equals(pause.toUpperCase())
                || key.equals(keyLeftP2.toUpperCase())
                || key.equals(keyRightP2.toUpperCase())
                || key.equals(keyDownP2.toUpperCase())
                || key.equals(keyRotateP2.toUpperCase())
                || key.equals(keyDropP2.toUpperCase());
    }

    public void restoreDefaultKeys() {
        setKeyLeft("A");
        setKeyRight("D");
        setKeyDown("S");
        setKeyRotate("W");
        setKeyDrop("SPACE");
        setPause("ESCAPE");

        // Player 2 키도 기본값으로 복원
        setKeyLeftP2("LEFT");
        setKeyRightP2("RIGHT");
        setKeyDownP2("DOWN");
        setKeyRotateP2("UP");
        setKeyDropP2("SLASH");
    }
}

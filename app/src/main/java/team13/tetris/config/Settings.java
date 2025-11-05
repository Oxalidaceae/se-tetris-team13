package team13.tetris.config;

public class Settings {
    private boolean colorBlindMode = false;
    private String windowSize = "MEDIUM";

    private String keyLeft = "LEFT";
    private String keyRight = "RIGHT";
    private String keyDown = "DOWN";
    private String keyRotate = "Z";
    private String keyDrop = "X";
    private String pause = "P";

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

    public boolean isKeyAlreadyUsed(String key) {
        if (key == null || key.trim().isEmpty()) return false;

        key = key.toUpperCase();
        return key.equals(keyLeft.toUpperCase()) ||
                key.equals(keyRight.toUpperCase()) ||
                key.equals(keyDown.toUpperCase()) ||
                key.equals(keyRotate.toUpperCase()) ||
                key.equals(keyDrop.toUpperCase()) ||
                key.equals(pause.toUpperCase());
    }

    public void restoreDefaultKeys() {
        setKeyLeft("LEFT");
        setKeyRight("RIGHT");
        setKeyDown("DOWN");
        setKeyRotate("Z");
        setKeyDrop("X");
        setPause("P");
    }
}

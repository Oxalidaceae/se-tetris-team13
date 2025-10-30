package team13.tetris.game;

public class Timer {
    
    private double elapsedTime;
    private double speedFactor;
    private static final double BASE_INTERVAL = 1000.0;
    private static final double SPEED_INCREMENT = 0.1;
    private static final double MAX_SPEED_FACTOR = 10.0;
    
    public Timer() {
        this.elapsedTime = 0.0;
        this.speedFactor = 1.0;
    }
    
    public void tick(double deltaTime) {
        elapsedTime += deltaTime;
    }
    
    public void increaseSpeed() {
        increaseSpeed(1.0);
    }
    
    public void increaseSpeed(double multiplier) {
        if (speedFactor < MAX_SPEED_FACTOR) {
            speedFactor += SPEED_INCREMENT * multiplier;
            if (speedFactor > MAX_SPEED_FACTOR) {
                speedFactor = MAX_SPEED_FACTOR;
            }
        }
    }
    
    public double getInterval() {
        return BASE_INTERVAL / speedFactor;
    }
    
    public double getElapsedTime() {
        return elapsedTime;
    }
    
    public double getSpeedFactor() {
        return speedFactor;
    }
    
    public void reset() {
        elapsedTime = 0.0;
        speedFactor = 1.0;
    }
    
    public void setSpeedFactor(double speedFactor) {
        if (speedFactor > 0 && speedFactor <= MAX_SPEED_FACTOR) {
            this.speedFactor = speedFactor;
        }
    }
    
    public String getFormattedTime() {
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public int getSpeedLevel() {
        return (int) speedFactor;
    }
    
    public int calculateDropScore(int dropDistance) {
        return (int) (10 * dropDistance * speedFactor);
    }
    
    public int getSoftDropScore() {
        return calculateDropScore(1);
    }
    
    public int getHardDropScore(int hardDropDistance) {
        return calculateDropScore(hardDropDistance);
    }
}

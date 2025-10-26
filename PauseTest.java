import team13.tetris.game.GameManager;

public class PauseTest {
    public static void main(String[] args) throws InterruptedException {
        GameManager gameManager = new GameManager();
        gameManager.startGame();
        
        System.out.println("\n=== 게임 시작 ===");
        Thread.sleep(500); // 0.5초 대기
        
        System.out.println("\n=== 일시정지 ===");
        gameManager.togglePause();
        Thread.sleep(2000); // 2초 일시정지
        
        System.out.println("\n=== 게임 재개 ===");
        gameManager.togglePause();
        Thread.sleep(1000); // 1초 더 대기
        
        System.out.println("\n=== 게임 종료 ===");
        gameManager.endGame();
    }
}
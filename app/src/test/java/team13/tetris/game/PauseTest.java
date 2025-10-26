package team13.tetris.game;

public class PauseTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 일시정지/재개 테스트 시작 ===");
        GameManager gameManager = new GameManager();
        gameManager.startGame();
        
        System.out.println("\n--- 게임 시작, 3초 대기 ---");
        Thread.sleep(3000); // 3초 대기 (이때 몇 번의 오토드랍이 발생해야 함)
        
        System.out.println("\n--- 일시정지 ---");
        gameManager.togglePause();
        
        System.out.println("\n--- 2초 일시정지 상태 유지 ---");
        Thread.sleep(2000); // 2초 일시정지
        
        System.out.println("\n--- 게임 재개 ---");
        gameManager.togglePause();
        
        System.out.println("\n--- 재개 후 3초 대기 (오토드랍 확인) ---");
        Thread.sleep(3000); // 3초 더 대기 (재개 후 오토드랍이 정상 작동하는지 확인)
        
        System.out.println("\n--- 게임 종료 ---");
        gameManager.endGame();
        System.out.println("=== 테스트 완료 ===");
    }
}
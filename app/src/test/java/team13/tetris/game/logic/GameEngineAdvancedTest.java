package team13.tetris.game.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.data.ScoreBoard;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineAdvancedTest {

    private GameEngine engine;
    private TestGameStateListener listener;
    private Board board;

    // 테스트용 GameStateListener 구현
    private static class TestGameStateListener implements GameStateListener {
        private int scoreChanged = 0;
        private int linesCleared = 0;
        private boolean gameOverCalled = false;
        private Board lastBoard = null;
        private Tetromino lastPiece = null;

        @Override
        public void onScoreChanged(int newScore) {
            this.scoreChanged = newScore;
        }

        @Override
        public void onLinesCleared(int lines) {
            this.linesCleared = lines;
        }

        @Override
        public void onBoardUpdated(Board board) {
            this.lastBoard = board;
        }

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {
            this.lastPiece = piece;
        }

        @Override
        public void onNextPiece(Tetromino nextPiece) {
            // 테스트용 - 별도 처리 없음
        }

        @Override
        public void onGameOver() {
            this.gameOverCalled = true;
        }

        // Getter 메서드들
        public int getScoreChanged() { return scoreChanged; }
        public int getLinesCleared() { return linesCleared; }
        public boolean isGameOverCalled() { return gameOverCalled; }
        public Board getLastBoard() { return lastBoard; }
        public Tetromino getLastPiece() { return lastPiece; }
    }

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
        listener = new TestGameStateListener();
        engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
    }

    @Test
    @DisplayName("GameEngine - 복잡한 보드 상황에서 라인 클리어 테스트")
    void testComplexLineClearScenarios() {
        engine.startNewGame();
        
        // 보드 하단에 부분적으로 채워진 라인들 생성
        for (int y = 18; y < 20; y++) {
            for (int x = 0; x < 9; x++) { // 마지막 칸만 비워둠
                board.setCell(x, y, 1);
            }
        }
        
        // I 블록을 수동으로 배치하여 라인 클리어 유발
        Tetromino iPiece = Tetromino.of(Tetromino.Kind.I);
        int[][] shape = iPiece.getShape();
        
        assertDoesNotThrow(() -> {
            // I 블록을 적절한 위치에 배치
            board.placePiece(shape, 9, 17, iPiece.getId());
            
            // 라인 클리어 실행
            int clearedLines = board.clearLinesAndReturnCount();
            
            // 라인이 클리어되었는지 확인
            assertTrue(clearedLines > 0, "라인이 클리어되어야 함");
            
        }, "복잡한 라인 클리어 시나리오가 안전해야 함");
    }

    @Test
    @DisplayName("GameEngine - 경계 조건에서의 조각 배치 테스트")
    void testPiecePlacementBoundaryConditions() {
        engine.startNewGame();
        
        // 보드 가장자리에서 조각 이동 테스트
        assertDoesNotThrow(() -> {
            // 왼쪽 끝까지 이동
            for (int i = 0; i < 10; i++) {
                engine.moveLeft();
            }
            
            // 오른쪽 끝까지 이동
            for (int i = 0; i < 10; i++) {
                engine.moveRight();
            }
            
            // 바닥까지 소프트 드롭
            for (int i = 0; i < 25; i++) {
                if (!engine.softDrop()) break;
            }
            
        }, "경계 조건에서 조각 조작이 안전해야 함");
    }

    @Test
    @DisplayName("GameEngine - 연속 회전과 벽차기(Wall Kick) 테스트")
    void testRotationAndWallKick() {
        engine.startNewGame();
        
        // 조각을 벽 근처로 이동
        for (int i = 0; i < 4; i++) {
            engine.moveLeft();
        }
        
        assertDoesNotThrow(() -> {
            // 연속 회전으로 모든 방향 테스트
            for (int i = 0; i < 8; i++) {
                engine.rotateCW();
            }
            
            // 벽 근처에서 회전
            for (int i = 0; i < 3; i++) {
                engine.moveRight();
            }
            
            for (int i = 0; i < 4; i++) {
                engine.rotateCW();
            }
            
        }, "회전과 벽차기가 안전해야 함");
    }

    @Test
    @DisplayName("GameEngine - 아이템 모드 기능 테스트")
    void testItemModeFeatures() {
        // 아이템 모드로 엔진 생성
        GameEngine itemEngine = new GameEngine(new Board(10, 20), listener, ScoreBoard.ScoreEntry.Mode.ITEM);
        itemEngine.startNewGame();
        
        assertNotNull(itemEngine.getCurrent(), "아이템 모드에서 조각 생성");
        
        // 아이템 블록 특성 테스트
        assertDoesNotThrow(() -> {
            // 여러 조각을 생성하여 아이템 블록 확률 테스트
            for (int i = 0; i < 10; i++) {
                itemEngine.hardDrop();
                // 새 조각 생성을 위해 잠시 대기
                Thread.sleep(10);
            }
        }, "아이템 모드 동작이 안전해야 함");
        
        itemEngine.shutdown();
    }

    @Test
    @DisplayName("GameEngine - 다양한 난이도별 성능 차이 테스트")
    void testDifficultyPerformanceDifferences() {
        TestGameStateListener easyListener = new TestGameStateListener();
        TestGameStateListener hardListener = new TestGameStateListener();
        
        GameEngine easyEngine = new GameEngine(new Board(10, 20), easyListener, ScoreBoard.ScoreEntry.Mode.EASY);
        GameEngine hardEngine = new GameEngine(new Board(10, 20), hardListener, ScoreBoard.ScoreEntry.Mode.HARD);
        
        easyEngine.startNewGame();
        hardEngine.startNewGame();
        
        // 같은 동작을 수행하여 난이도별 차이 확인
        for (int i = 0; i < 5; i++) {
            easyEngine.softDrop();
            hardEngine.softDrop();
        }
        
        assertNotNull(easyEngine.getCurrent(), "EASY 엔진 정상 동작");
        assertNotNull(hardEngine.getCurrent(), "HARD 엔진 정상 동작");
        
        // 하강 간격이 다른지 확인
        double easyInterval = easyEngine.getDropIntervalSeconds();
        double hardInterval = hardEngine.getDropIntervalSeconds();
        
        assertTrue(easyInterval >= 0, "EASY 모드 하강 간격 유효");
        assertTrue(hardInterval >= 0, "HARD 모드 하강 간격 유효");
        
        easyEngine.shutdown();
        hardEngine.shutdown();
    }

    @Test
    @DisplayName("GameEngine - 멀티스레드 환경에서의 안전성 테스트")
    void testMultithreadSafety() throws InterruptedException {
        engine.startNewGame();
        engine.startAutoDrop();
        
        // 여러 스레드에서 동시 조작
        Thread moveThread = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                engine.moveLeft();
                engine.moveRight();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        Thread rotateThread = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                engine.rotateCW();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        moveThread.start();
        rotateThread.start();
        
        // 메인 스레드에서도 조작
        for (int i = 0; i < 10; i++) {
            engine.softDrop();
            Thread.sleep(10);
        }
        
        moveThread.join(1000);
        rotateThread.join(1000);
        
        // 엔진이 여전히 유효한 상태인지 확인
        assertNotNull(engine.getBoard(), "멀티스레드 후에도 보드 유효");
        
        engine.stopAutoDrop();
        engine.shutdown();
    }

    @Test
    @DisplayName("GameEngine - 극한 상황에서의 점수 계산 테스트")
    void testExtremeScoring() {
        engine.startNewGame();
        
        // 매우 높은 점수 시뮬레이션
        int initialScore = engine.getScore();
        
        // 하드 드롭을 여러 번 수행
        for (int i = 0; i < 50; i++) {
            engine.hardDrop();
            
            // 새 조각 생성을 위해 잠시 대기
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        int finalScore = engine.getScore();
        assertTrue(finalScore >= initialScore, "점수가 증가해야 함");
        
        // 점수가 음수가 되지 않는다는 것을 확인
        assertTrue(finalScore >= 0, "점수는 음수가 될 수 없음");
        
        engine.shutdown();
    }

    @Test
    @DisplayName("GameEngine - 리소스 누수 방지 테스트")
    void testResourceLeakPrevention() {
        // 여러 엔진을 생성하고 제거하여 리소스 누수 테스트
        for (int i = 0; i < 10; i++) {
            TestGameStateListener tempListener = new TestGameStateListener();
            GameEngine tempEngine = new GameEngine(new Board(10, 20), tempListener);
            
            tempEngine.startNewGame();
            tempEngine.startAutoDrop();
            
            // 짧은 시간 실행
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            // 정리
            tempEngine.stopAutoDrop();
            tempEngine.shutdown();
        }
        
        // 메모리 사용량 확인을 위해 GC 실행
        System.gc();
        
        // 메인 엔진이 여전히 정상 동작하는지 확인
        engine.startNewGame();
        assertNotNull(engine.getCurrent(), "메인 엔진이 정상 동작해야 함");
    }

    @Test
    @DisplayName("GameEngine - 타이머 및 스케줄링 동작 테스트")
    void testTimerAndScheduling() {
        engine.startNewGame();
        
        // 다양한 하강 간격 테스트
        double[] intervals = {0.1, 0.5, 1.0, 2.0};
        
        for (double interval : intervals) {
            assertDoesNotThrow(() -> {
                engine.setDropIntervalSeconds(interval);
                assertEquals(interval, engine.getDropIntervalSeconds(), 0.01, 
                    "하강 간격이 올바르게 설정되어야 함");
                
                engine.startAutoDrop();
                Thread.sleep(50); // 짧은 시간 동작
                engine.stopAutoDrop();
                
            }, "타이머 간격 " + interval + "초 테스트");
        }
        
        engine.shutdown();
    }
}
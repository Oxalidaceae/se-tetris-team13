package team13.tetris.game.logic;

import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.game.Timer;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 최소한의 게임 엔진으로, 조각 스폰, 이동, 회전, 라인 제거 기능을 제공합니다.
 * 전체 실시간 루프를 구현하지는 않으며, UI 루프나 스케줄러와 함께 사용하도록 설계되었습니다.
 */
public class GameEngine {
    private final Board board;
    private final GameStateListener listener;
    private Tetromino current;
    private Tetromino next;
    private int px, py;
    private final Random rnd = new Random();
    private int score = 0;
    private final Timer gameTimer; // 점수 계산을 위한 타이머
    private final ScoreBoard.ScoreEntry.Mode difficulty; // 난이도 정보

    // 자동 하강 간격(초) - 설정 가능
    private volatile double dropIntervalSeconds = 1.0;

    // 자동 하강을 위한 스케줄러
    private final Object schedulerLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autoDropFuture;
    
    // 일시정지 관련 타이밍
    private long lastDropTime = 0;
    private long pauseStartTime = 0;
    
    // 아이템 모드 관련
    private int totalLinesCleared = 0; // 총 삭제된 라인 수
    private boolean itemModeEnabled = false; // 아이템 모드 활성화 여부
    private Tetromino nextItemPiece = null; // 다음 아이템 피스
    
    // 무게추 충돌 상태 추적
    private boolean weightCollisionDetected = false; // 무게추가 충돌을 감지했는지 여부
    private boolean isHardDrop = false; // 하드드롭 여부를 나타내는 플래그

    public GameEngine(Board board, GameStateListener listener) {
        this(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
    }

    public GameEngine(Board board, GameStateListener listener, ScoreBoard.ScoreEntry.Mode difficulty) {
        this.board = board;
        this.listener = listener;
        this.difficulty = difficulty;
        this.gameTimer = new Timer(); // 점수 계산용 타이머 초기화
        this.itemModeEnabled = (difficulty == ScoreBoard.ScoreEntry.Mode.ITEM);
    }

    public void startNewGame() {
        board.clear();
        // prepare next bag
        next = randomPiece();
        spawnNext();
        listener.onScoreChanged(score);
        // restart automatic dropping when a new game starts
        stopAutoDrop();
        startAutoDrop();
    }

    /**
     * Roulette Wheel Selection을 사용하여 난이도별 가중치로 블록을 선택합니다.
     * EASY: I블록 12, 나머지 10 (I블록이 20% 더 자주)
     * NORMAL: 모두 10 (균등 분포)
     * HARD: I블록 8, 나머지 10 (I블록이 20% 덜 자주)
     * ITEM: 아이템 미노가 설정되어 있으면 우선적으로 반환
     */
    private Tetromino randomPiece() {
        // 아이템 모드에서 다음 아이템 피스가 설정되어 있으면 우선 반환
        if (itemModeEnabled && nextItemPiece != null) {
            Tetromino itemPiece = nextItemPiece;
            nextItemPiece = null; // 사용 후 초기화
            return itemPiece;
        }
        
        // 난이도별 가중치 배열 [I, O, T, S, Z, J, L]
        int[] weights = getWeightsByDifficulty();

        // 총 가중치 계산
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        // Roulette Wheel Selection
        int randomValue = rnd.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return getPieceByIndex(i);
            }
        }

        // Fallback (should never reach here)
        return Tetromino.of(Tetromino.Kind.I);
    }

    /**
     * 난이도에 따른 블록 가중치 배열 반환
     * @return [I, O, T, S, Z, J, L] 순서의 가중치 배열
     */
    private int[] getWeightsByDifficulty() {
        switch (difficulty) {
            case EASY:
                // I블록 12, 나머지 10 (I블록이 20% 더 자주 등장)
                return new int[] { 12, 10, 10, 10, 10, 10, 10 };
            case HARD:
                // I블록 8, 나머지 10 (I블록이 20% 덜 등장)
                return new int[] { 8, 10, 10, 10, 10, 10, 10 };
            case ITEM:
                // ITEM 모드는 NORMAL과 동일한 생성 확률
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
            case NORMAL:
            default:
                // 모두 동일 가중치
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
        }
    }

    /**
     * 인덱스에 해당하는 Tetromino 반환
     * @param index 0:I, 1:O, 2:T, 3:S, 4:Z, 5:J, 6:L
     */
    private Tetromino getPieceByIndex(int index) {
        switch (index) {
            case 0:
                return Tetromino.of(Tetromino.Kind.I);
            case 1:
                return Tetromino.of(Tetromino.Kind.O);
            case 2:
                return Tetromino.of(Tetromino.Kind.T);
            case 3:
                return Tetromino.of(Tetromino.Kind.S);
            case 4:
                return Tetromino.of(Tetromino.Kind.Z);
            case 5:
                return Tetromino.of(Tetromino.Kind.J);
            case 6:
                return Tetromino.of(Tetromino.Kind.L);
            default:
                return Tetromino.of(Tetromino.Kind.I);
        }
    }
    
    /**
     * 미노 복사 아이템을 생성합니다.
     * 기본 미노와 같은 형태이지만 랜덤한 블록 하나가 'C' 표시됩니다.
     */
    private Tetromino createItemPiece(Tetromino.Kind itemKind, Tetromino.Kind targetKind) {
        if (itemKind == Tetromino.Kind.COPY) {
            // 복사 아이템: 대상 미노의 블록 개수 확인 (4개의 블록 중 랜덤 선택)
            int copyBlockIndex = (int)(Math.random() * 4);
            Tetromino result = new Tetromino(targetKind, 0, copyBlockIndex);
            return result;
        } else if (itemKind == Tetromino.Kind.WEIGHT) {
            // 무게추 아이템: copyBlockIndex는 사용하지 않음
            return new Tetromino(itemKind, 0, 0);
        } else if (itemKind == Tetromino.Kind.GRAVITY) {
            // 중력 아이템: copyBlockIndex는 사용하지 않음
            return new Tetromino(itemKind, 0, 0);
        } else if (itemKind == Tetromino.Kind.SPLIT) {
            // 분할 아이템: copyBlockIndex는 사용하지 않음
            return new Tetromino(itemKind, 0, 0);
        } else {
            // 다른 아이템들 (향후 구현)
            return new Tetromino(itemKind, 0, 0);
        }
    }
    
    /**
     * 10줄이 삭제될 때마다 호출되어 다음 미노를 아이템 미노로 설정합니다.
     * 현재 구현된 5가지 아이템 중 하나를 20% 확률로 선택합니다.
     */
    private void generateItemPiece() {
        if (!itemModeEnabled) return;
        
        // 5가지 아이템 타입 중 하나를 선택 (각 20% 확률)
        int itemChoice = rnd.nextInt(5);
        
        if (itemChoice == 0) {
            // COPY 아이템: 랜덤한 기본 미노 종류 선택
            Tetromino.Kind[] kinds = {
                Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createItemPiece(Tetromino.Kind.COPY, targetKind);
        } else if (itemChoice == 1) {
            // WEIGHT 아이템
            nextItemPiece = createItemPiece(Tetromino.Kind.WEIGHT, null);
        } else if (itemChoice == 2) {
            // GRAVITY 아이템
            nextItemPiece = createItemPiece(Tetromino.Kind.GRAVITY, null);
        } else if (itemChoice == 3) {
            // SPLIT 아이템
            nextItemPiece = createItemPiece(Tetromino.Kind.SPLIT, null);
        } else {
            // LINE_CLEAR 아이템: COPY와 같은 방식으로 생성하되 타입만 다름
            Tetromino.Kind[] kinds = {
                Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createLineClearItemPiece(targetKind);
        }
    }
    
    /**
     * LINE_CLEAR 아이템 미노를 생성합니다.
     * COPY 아이템의 코드를 복사해서 만든 독립적인 구현입니다.
     */
    private Tetromino createLineClearItemPiece(Tetromino.Kind targetKind) {
        // 라인클리어 아이템: 대상 미노의 블록 개수 확인 (4개의 블록 중 랜덤 선택)
        int lineClearBlockIndex = (int)(Math.random() * 4);
        Tetromino result = new Tetromino(targetKind, 0, lineClearBlockIndex, Tetromino.ItemType.LINE_CLEAR);
        return result;
    }

    /**
     * 아이템 미노의 특수 효과를 처리합니다.
     * COPY: 특정 미노 타입으로 다음 미노를 복사합니다.
     * WEIGHT: 무게추 효과로 착지 지점 아래의 블록들을 파괴합니다.
     * GRAVITY: 전체 보드에 중력을 적용하여 빈 공간을 메웁니다.
     * SPLIT: 3개의 열로 분할되어 각각 독립적으로 떨어집니다.
     */
    private void processItemEffect(Tetromino.ItemType itemType, Tetromino.Kind targetKind) {
        if (!itemModeEnabled) {
            return;
        }
        
        if (itemType == Tetromino.ItemType.COPY) {
            // 복사 효과: 지정된 미노와 같은 종류의 다음 미노 생성
            if (targetKind != null) {
                // 현재 next를 current로 이동시키고, 복사된 미노를 새로운 next로 설정
                current = next != null ? next : randomPiece();
                next = new Tetromino(targetKind, 0);
                
                // 새로운 current 위치 설정
                px = (board.getWidth() - current.getWidth()) / 2;
                py = 0;
                
                
                // 리스너에게 알림
                listener.onPieceSpawned(current, px, py);
                listener.onNextPiece(next);
                listener.onBoardUpdated(board);
            }
        } else if (itemType == Tetromino.ItemType.GRAVITY) {
            // 중력 효과: 전체 보드에 중력을 적용하여 빈 공간을 메움
            processGravityEffect();
        } else if (itemType == Tetromino.ItemType.SPLIT) {
            // 분할 효과: 3개의 열로 분할되어 각각 독립적으로 떨어짐
            processSplitEffect();
        } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
            // 라인 클리어 효과: 해당 블록이 있는 줄 전체를 즉시 제거
            processLineClearEffect();
        }
    }
    
    /**
     * 무게추 아이템 효과를 처리합니다.
     * 착지 지점 아래의 블록들을 한 줄씩 차례대로 파괴합니다.
     */
    private void processWeightEffect() {
        if (current == null || !current.isItemPiece()) {
            return;
        }
        
        // 무게추가 차지하는 모든 열과 가장 아래쪽 행을 찾기
        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();
        
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;
                    
                    // 보드 범위 내에 있는 경우만
                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }
        
        // 파괴할 행들을 아래부터 위로 순서대로 찾기
        java.util.List<Integer> rowsToDestroy = new java.util.ArrayList<>();
        for (int y = bottomRow + 1; y < board.getHeight(); y++) {
            boolean hasBlocksInRow = false;
            for (int col : occupiedColumns) {
                if (board.getCell(col, y) != 0) {
                    hasBlocksInRow = true;
                    break;
                }
            }
            if (hasBlocksInRow) {
                rowsToDestroy.add(y);
            }
        }
        
        // 한 줄씩 차례대로 파괴 (100ms 간격)
        destroyRowsSequentially(occupiedColumns, rowsToDestroy, 0);
    }
    
    /**
     * 행들을 순차적으로 파괴하는 메서드
     */
    private void destroyRowsSequentially(java.util.Set<Integer> columns, java.util.List<Integer> rows, int currentIndex) {
        if (currentIndex >= rows.size()) {
            // 모든 행 파괴 완료 후 중력 적용
            board.applyGravity();
            listener.onBoardUpdated(board);
            return;
        }
        
        int currentRow = rows.get(currentIndex);
        
        // 현재 행의 지정된 열들의 블록 파괴
        for (int col : columns) {
            if (board.getCell(col, currentRow) != 0) {
                board.setCell(col, currentRow, 0);
            }
        }
        
        listener.onBoardUpdated(board);
        
        // 100ms 후 다음 행 파괴
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                destroyRowsSequentially(columns, rows, currentIndex + 1);
                timer.cancel();
            }
        }, 100);
    }
    
    /**
     * 무게추 바로 아래 한 줄을 파괴합니다.
     */
    private void destroyLineDirectlyBelow() {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) {
            return;
        }
        
        // 무게추가 차지하는 모든 열과 가장 아래쪽 행을 찾기
        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();
        
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;
                    
                    // 보드 범위 내에 있는 경우만
                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }
        
        // 무게추 바로 아래 한 줄만 파괴 (중력 적용 안함)
        int targetRow = bottomRow + 1;
        if (targetRow < board.getHeight()) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, targetRow) != 0) {
                    board.setCell(col, targetRow, 0);
                }
            }
            // 무게추는 파괴만 하고 중력은 적용하지 않음
        }
    }
    
    /**
     * 하드드롭 시 무게추 아래의 모든 블록을 즉시 파괴합니다.
     */
    private void destroyAllBlocksBelow() {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) {
            return;
        }
        
        // 무게추가 차지하는 모든 열과 가장 아래쪽 행을 찾기
        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();
        
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;
                    
                    // 보드 범위 내에 있는 경우만
                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }
        
        // 무게추 아래의 모든 블록을 즉시 파괴
        for (int y = bottomRow + 1; y < board.getHeight(); y++) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, y) != 0) {
                    board.setCell(col, y, 0);
                }
            }
        }
        // 하드드롭이므로 애니메이션 없이 즉시 파괴하고 중력 적용 안함
    }
    
    /**
     * 중력 아이템 효과를 처리합니다.
     * 전체 보드에 중력을 적용하여 빈 공간을 메웁니다.
     */
    private void processGravityEffect() {
        // 전체 보드에 중력 적용
        board.applyGravity();
    }
    
    /**
     * 분할 아이템 효과를 처리합니다.
     * SPLIT 블록이 차지하는 3개의 열을 각각 독립적으로 떨어뜨립니다.
     */
    private void processSplitEffect() {
        if (current == null || !current.isItemPiece()) {
            System.out.println("[SPLIT 디버그] current가 null이거나 아이템 미노가 아님");
            return;
        }
        
        // SPLIT 블록이 차지하는 열들을 찾기
        int[][] shape = current.getShape();
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();
        
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int worldCol = px + c;
                    if (worldCol >= 0 && worldCol < board.getWidth()) {
                        occupiedColumns.add(worldCol);
                    }
                }
            }
        }
        
        
        // 각 열에 대해 독립적으로 중력 적용
        for (int col : occupiedColumns) {
            applySingleColumnGravity(col);
        }
        
    }
    
    /**
     * 특정 열에서 스플릿 블록들에만 하드드롭을 적용합니다.
     */
    private void applySingleColumnGravity(int col) {
        if (col < 0 || col >= board.getWidth()) return;
        
        int height = board.getHeight();
        
        // 해당 열에서 스플릿 블록들만 추출 (위에서 아래 순서로)
        java.util.List<Integer> splitBlocks = new java.util.ArrayList<>();
        
        for (int y = 0; y < height; y++) {
            int val = board.getCell(col, y);
            if (val >= 500 && val < 600) { // SPLIT 블록들 (500번대)
                splitBlocks.add(val);
                board.setCell(col, y, 0); // 원래 위치에서 제거
            }
        }
        
        // 각 스플릿 블록을 위에서부터 순차적으로 하드드롭
        for (int blockValue : splitBlocks) {
            // 맨 위부터 시작해서 해당 블록이 떨어질 수 있는 위치 찾기
            int dropPosition = 0;
            
            // 위에서부터 아래로 내려가면서 떨어질 수 있는 가장 아래 위치 찾기
            while (dropPosition < height - 1 && board.getCell(col, dropPosition + 1) == 0) {
                dropPosition++;
            }
            
            // 해당 위치에 블록 배치
            board.setCell(col, dropPosition, blockValue);
        }
    }
    
    /**
     * LINE_CLEAR 아이템 효과를 처리합니다.
     * L 블록이 있는 행을 라인클리어하고 점수를 적용합니다.
     */
    private void processLineClearEffect() {
        if (current == null || !current.isItemPiece()) {
            return;
        }
        
        // L 블록(lineClearBlockIndex)이 있는 행을 찾기
        int[][] shape = current.getShape();
        int lineClearBlockIndex = current.getLineClearBlockIndex();
        int blockCount = 0;
        int targetRow = -1;
        
        
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    if (blockCount == lineClearBlockIndex) {
                        targetRow = py + r; // 월드 좌표에서의 행
                        break;
                    }
                    blockCount++;
                }
            }
            if (targetRow != -1) break;
        }
        
        // 유효한 행인지 확인
        if (targetRow >= 0 && targetRow < board.getHeight()) {
            
            // 해당 행을 직접 제거하고 위의 행들을 아래로 이동
            // 위쪽 행들을 한 줄씩 아래로 복사
            for (int r = targetRow; r > 0; r--) {
                for (int c = 0; c < board.getWidth(); c++) {
                    board.setCell(c, r, board.getCell(c, r - 1));
                }
            }
            
            // 맨 위 행을 빈 공간으로 설정
            for (int c = 0; c < board.getWidth(); c++) {
                board.setCell(c, 0, 0);
            }
            
            totalLinesCleared += 1;
            
            // 점수 적산 (일반 라인클리어와 동일)
            addScoreForClearedLines(1);
            updateSpeedForLinesCleared(1, totalLinesCleared);
            
            
            // 보드 업데이트
            listener.onBoardUpdated(board);
        }
    }

    /**
     * 미노 ID로부터 Kind를 찾습니다.
     */
    private Tetromino.Kind getKindFromId(int id) {
        for (Tetromino.Kind kind : Tetromino.Kind.values()) {
            if (kind.getId() == id) {
                return kind;
            }
        }
        return null;
    }

    private void spawnNext() {
        // 새로운 미노 생성 시 무게추 충돌 상태 리셋
        weightCollisionDetected = false;
        
        current = next != null ? next : randomPiece();
        
        // 아이템 모드에서 nextItemPiece가 설정되어 있으면 그것을 next로 사용
        if (itemModeEnabled && nextItemPiece != null) {
            next = nextItemPiece;
            nextItemPiece = null; // 한 번 사용 후 초기화
        } else {
            next = randomPiece();
        }
        
        px = (board.getWidth() - current.getWidth()) / 2;
        py = 0;
        if (!board.fits(current.getShape(), px, py)) {
            // 게임 오버: 알리고 자동 하강을 중지합니다
            listener.onGameOver();
            stopAutoDrop();
            return;
        }
        listener.onPieceSpawned(current, px, py);
        listener.onNextPiece(next);
        listener.onBoardUpdated(board);
    }

    /**
     * 현재 설정된 하강 간격을 사용하여 자동 하강 스케줄러를 시작합니다.
     * 여러 번 호출해도 안전하며, 필요 시 스케줄러를 생성합니다.
     */
    public void startAutoDrop() {
        synchronized (schedulerLock) {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "GameEngine-AutoDrop");
                    t.setDaemon(true);
                    return t;
                });
            }
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) {
                // already running
                return;
            }
            
            long periodMillis = Math.max(1L, (long) (dropIntervalSeconds * 1000.0));
            long initialDelay = periodMillis;
            long currentTime = System.currentTimeMillis();
            
            // 일시정지 후 재개하는 경우 - pauseStartTime > 0이면 일시정지된 상태였음
            if (pauseStartTime > 0) {
                if (lastDropTime > 0) {
                    // 이전 드랍이 있었던 경우: 일시정지된 시간을 보상
                    long pauseDuration = currentTime - pauseStartTime;
                    lastDropTime += pauseDuration;
                    long timeSinceLastDrop = currentTime - lastDropTime;
                    long remainingTime = periodMillis - (timeSinceLastDrop % periodMillis);
                    initialDelay = Math.max(1L, remainingTime);
                } else {
                    // 이전 드랍이 없었던 경우: 바로 시작
                    lastDropTime = currentTime;
                    initialDelay = periodMillis;
                }
                pauseStartTime = 0; // 리셋
            } else {
                // 새 게임 시작
                lastDropTime = currentTime;
            }
            
            autoDropFuture = scheduler.scheduleAtFixedRate(() -> {
                try {
                    lastDropTime = System.currentTimeMillis();
                    softDrop();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, initialDelay, periodMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 자동 하강 스케줄러를 중지하고 예약된 작업을 취소합니다.
     */
    public void stopAutoDrop() {
        synchronized (schedulerLock) {
            pauseStartTime = System.currentTimeMillis(); // 일시정지 시간 기록
            
            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
            // 스케줄러는 종료하지 않고 유지 (재사용을 위해)
        }
    }
    
    /**
     * 게임 종료 시 스케줄러를 완전히 종료합니다.
     */
    public void shutdown() {
        synchronized (schedulerLock) {
            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                try {
                    scheduler.shutdownNow();
                } catch (Throwable ignored) {}
                scheduler = null;
            }
        }
    }

    public double getDropIntervalSeconds() {
        return dropIntervalSeconds;
    }

    /**
     * 자동 하강 간격(초)을 설정합니다. 자동 하강이 동작 중이면 새로운 간격으로 재스케줄링됩니다.
     */
    public void setDropIntervalSeconds(double seconds) {
        if (seconds <= 0)
            throw new IllegalArgumentException("drop interval must be > 0");
        synchronized (schedulerLock) {
            this.dropIntervalSeconds = seconds;
            // if running, restart with new interval
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) {
                autoDropFuture.cancel(false);
                long periodMillis = Math.max(1L, (long) (dropIntervalSeconds * 1000.0));
                autoDropFuture = scheduler.scheduleAtFixedRate(() -> {
                    try {
                        softDrop();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void moveLeft() {
        if (current == null)
            return;
            
        // 무게추 충돌 상태에서는 좌우 이동 제한
        if (weightCollisionDetected) {
            return;
        }
            
        if (board.fits(current.getShape(), px - 1, py)) {
            px--;
            listener.onBoardUpdated(board);
        }
    }

    public void moveRight() {
        if (current == null)
            return;
            
        // 무게추 충돌 상태에서는 좌우 이동 제한
        if (weightCollisionDetected) {
            return;
        }
            
        if (board.fits(current.getShape(), px + 1, py)) {
            px++;
            listener.onBoardUpdated(board);
        }
    }
    
    public void rotateCW() {
        if (current == null)
            return;
            
        // 무게추 아이템은 회전할 수 없음
        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) {
            return;
        }
            
        Tetromino rotated = current.rotateClockwise();
        // try wall-kick offsets: prefer no-offset, then left/right small kicks, then
        // larger kicks, then upward kick
        int[][] offsets = new int[][] { { 0, 0 }, { -1, 0 }, { 1, 0 }, { -2, 0 }, { 2, 0 }, { 0, -1 } };
        for (int[] off : offsets) {
            int nx = px + off[0];
            int ny = py + off[1];
            if (board.fits(rotated.getShape(), nx, ny)) {
                current = rotated;
                px = nx;
                py = ny;
                listener.onBoardUpdated(board);
                return;
            }
        }
    }

    public boolean softDrop() {
        if (current == null)
            return false;
        if (board.fits(current.getShape(), px, py + 1)) {
            py++;
            
            // 무게추 아이템의 경우 한 칸 떨어질 때마다 바로 아래 한 줄 파괴
            if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT && weightCollisionDetected) {
                destroyLineDirectlyBelow();
            }
            
            // 소프트 드롭 점수 추가 (한 칸 하강)
            addDropScore(1);
            listener.onBoardUpdated(board);
            return true;
        } else {
            // 무게추 아이템의 경우 첫 번째 충돌 감지 시점에서 상태 변경
            if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT && !weightCollisionDetected) {
                weightCollisionDetected = true;
                // 첫 충돌 시에도 바로 아래 한 줄 파괴
                destroyLineDirectlyBelow();
                // 무게추는 충돌 감지 후에는 바로 착지시키지 않고 계속 진행
                return false;
            }
            
            // place - 아이템 미노인지 확인하여 적절한 메서드 사용
            if (current.isItemPiece()) {
                // 모든 아이템은 특별한 아이템 블록으로 배치
                String itemTypeStr = current.getItemType().name();
                int itemBlockIndex = -1;
                
                if (current.getItemType() == Tetromino.ItemType.COPY) {
                    itemBlockIndex = current.getCopyBlockIndex();
                } else if (current.getItemType() == Tetromino.ItemType.LINE_CLEAR) {
                    itemBlockIndex = current.getLineClearBlockIndex();
                } else {
                    itemBlockIndex = 0; // 다른 아이템은 첫 번째 블록을 아이템 블록으로 사용
                }
                
                board.placeItemPiece(current.getShape(), px, py, current.getId(), itemBlockIndex, itemTypeStr);
            } else {
                // 일반 미노는 일반 블록으로 배치
                board.placePiece(current.getShape(), px, py, current.getId());
            }
            handleLockedPiece();
            return false;
        }
    }

    public void hardDrop() {
        if (current == null)
            return;
        
        // 하드드롭 플래그 설정
        isHardDrop = true;
        
        int startY = py; // 시작 위치 기록
        
        // 무게추 아이템의 경우 특별 처리
        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) {
            weightCollisionDetected = true;
            
            // 먼저 최대한 아래로 내려가기
            while (board.fits(current.getShape(), px, py + 1))
                py++;
            
            // 아래 모든 블록 파괴
            destroyAllBlocksBelow();
            
            // 블록 파괴 후 다시 최대한 아래로 내려가기
            while (board.fits(current.getShape(), px, py + 1))
                py++;
        } else {
            // 일반 미노나 다른 아이템의 경우
            while (board.fits(current.getShape(), px, py + 1))
                py++;
        }
        
        int dropDistance = py - startY; // 떨어진 거리 계산

        // 하드 드롭 점수 추가 (거리 > 0일 때만)
        if (dropDistance > 0) {
            addHardDropScore(dropDistance);
        }

        // 아이템 미노인지 확인하여 적절한 메서드 사용
        if (current.isItemPiece()) {
            // 모든 아이템은 특별한 아이템 블록으로 배치
            String itemTypeStr = current.getItemType().name();
            int itemBlockIndex = -1;
            
            if (current.getItemType() == Tetromino.ItemType.COPY) {
                itemBlockIndex = current.getCopyBlockIndex();
            } else if (current.getItemType() == Tetromino.ItemType.LINE_CLEAR) {
                itemBlockIndex = current.getLineClearBlockIndex();
            } else {
                itemBlockIndex = 0; // 다른 아이템은 첫 번째 블록을 아이템 블록으로 사용
            }
            
            board.placeItemPiece(current.getShape(), px, py, current.getId(), itemBlockIndex, itemTypeStr);
        } else {
            // 일반 미노는 일반 블록으로 배치
            board.placePiece(current.getShape(), px, py, current.getId());
        }
        handleLockedPiece();
        
        // 하드드롭 플래그 해제
        isHardDrop = false;
    }

    // Handles animation + scoring after the falling piece is fixed to the board.
    private void handleLockedPiece() {
        // 아이템이 착지한 경우 즉시 효과 발동 (current를 null로 만들기 전에)
        if (itemModeEnabled && current != null && current.isItemPiece()) {
            Tetromino.Kind kind = current.getKind();
            Tetromino.ItemType itemType = current.getItemType();
            
            // 무게추는 softDrop에서 이미 처리되므로 여기서는 제외
            if (kind == Tetromino.Kind.GRAVITY) {
                processGravityEffect();
            } else if (kind == Tetromino.Kind.SPLIT) {
                processSplitEffect();
            } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
                processLineClearEffect();
            }
        }
        
        java.util.List<Integer> fullLines = board.getFullLineIndices();
        
        current = null; // 잠시 조작을 막고, 보드에는 고정된 조각만 남김

        if (fullLines.isEmpty()) {
            spawnNext();
            return;
        }

        // 아이템 모드: fillLineWith로 덮어쓰기 전에 아이템 블록 확인
        boolean hasItemBlockInFullLines = false;
        Tetromino.Kind itemPieceKind = null; // 아이템 블록의 원래 미노 타입
        Tetromino.ItemType detectedItemType = null; // 감지된 아이템 타입
        
        if (itemModeEnabled) {
            int[][] snapshot = board.snapshot();
            for (int row : fullLines) {
                for (int c = 0; c < snapshot[row].length; c++) {
                    if (snapshot[row][c] != 0) {
                    }
                    // 아이템 블록 범위 확인 (100-599)
                    if (snapshot[row][c] >= 100 && snapshot[row][c] < 600) {
                        hasItemBlockInFullLines = true;
                        int originalId = snapshot[row][c] % 100; // 원래 미노 ID 추출
                        itemPieceKind = getKindFromId(originalId); // ID로부터 Kind 추출
                        
                        // 아이템 타입 구분
                        if (snapshot[row][c] >= 100 && snapshot[row][c] < 200) {
                            detectedItemType = Tetromino.ItemType.COPY;
                        } else if (snapshot[row][c] >= 200 && snapshot[row][c] < 300) {
                            detectedItemType = Tetromino.ItemType.LINE_CLEAR;
                        } else if (snapshot[row][c] >= 300 && snapshot[row][c] < 400) {
                            detectedItemType = Tetromino.ItemType.WEIGHT;
                        } else if (snapshot[row][c] >= 400 && snapshot[row][c] < 500) {
                            detectedItemType = Tetromino.ItemType.GRAVITY;
                        } else if (snapshot[row][c] >= 500 && snapshot[row][c] < 600) {
                            detectedItemType = Tetromino.ItemType.SPLIT;
                        }
                        
                        break;
                    }
                }
                if (hasItemBlockInFullLines) break;
            }
        }

        for (int row : fullLines) {
            board.fillLineWith(row, -1); // 흰색 플래시 마커
        }
        listener.onBoardUpdated(board);

        // 잠깐(150ms) 보여준 뒤에 실제로 라인을 제거하고 다음 조각을 소환
        final boolean finalHasItemBlock = hasItemBlockInFullLines;
        final Tetromino.Kind finalItemPieceKind = itemPieceKind;
        final Tetromino.ItemType finalDetectedItemType = detectedItemType;
        
        // Timer를 사용하여 150ms 후 실행
        java.util.Timer delayTimer = new java.util.Timer();
        delayTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean copyEffectProcessed = false;
                
                // 아이템 효과를 먼저 처리 (clearFullLines 전에)
                if (itemModeEnabled && finalHasItemBlock && finalDetectedItemType != null) {
                    if (finalDetectedItemType == Tetromino.ItemType.COPY) {
                        copyEffectProcessed = true;
                    }
                    processItemEffect(finalDetectedItemType, finalItemPieceKind);
                }
                
                // 아이템 효과를 처리하는 콜백 정의 (더미)
                Runnable itemEffectCallback = () -> {
                };
                
                int cleared = board.clearFullLines(itemEffectCallback);
                if (cleared > 0)
                    listener.onLinesCleared(cleared);
                if (cleared > 0) {
                    addScoreForClearedLines(cleared);
                    listener.onScoreChanged(score);
                }
                listener.onBoardUpdated(board);
                
                // COPY 효과가 처리된 경우 spawnNext를 호출하지 않음
                if (!copyEffectProcessed) {
                    spawnNext();
                }
                delayTimer.cancel(); // Timer 정리
            }
        }, 150); // 150ms 지연
    }

    /**
     * 제거된 라인 수에 따른 점수 추가:
     * 1 -> 100, 2 -> 250, 3 -> 500, 4 -> 1000
     */
    public void addScoreForClearedLines(int cleared) {
        switch (cleared) {
            case 1:
                score += 100;
                break;
            case 2:
                score += 250;
                break;
            case 3:
                score += 500;
                break;
            case 4:
                score += 1000;
                break;
            default:
                if (cleared > 4)
                    score += 1000 + (cleared - 4) * 250;
                break; // graceful handling
        }
        
        // 아이템 모드에서 라인 클리어 처리
        if (itemModeEnabled && cleared > 0) {
            totalLinesCleared += cleared;
            
            // 10줄마다 아이템 미노 생성 (10, 20, 30, ... 의 배수마다)
            int beforeClear = totalLinesCleared - cleared;
            int currentGroup = totalLinesCleared / 10;
            int previousGroup = beforeClear / 10;
            
            if (currentGroup > previousGroup) {
                generateItemPiece();
            }
        }
    }

    /**
     * 블록 하강에 따른 점수 추가 (10점 × 거리 × 속도 계수)
     * @param dropDistance 하강한 칸 수
     */
    public void addDropScore(int dropDistance) {
        int dropPoints = gameTimer.calculateDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
        // 소프트 드롭과 자동 드롭은 너무 빈번하므로 로그 비활성화
        // System.out.println("Drop score: " + dropPoints + " (Distance: " +
        // dropDistance + ", Speed: " + String.format("%.1f",
        // gameTimer.getSpeedFactor()) + "x)");
    }

    /**
     * 하드 드롭에 따른 점수 추가
     * @param dropDistance 하강한 칸 수
     */
    public void addHardDropScore(int dropDistance) {
        int dropPoints = gameTimer.getHardDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
    }

    /**
     * 게임 타이머 접근자 (속도 조정용)
     * @return 게임 타이머 인스턴스
     */
    public Timer getGameTimer() {
        return gameTimer;
    }

    /**
     * 라인 클리어 시 게임 속도 증가 (10줄마다)
     * Timer의 속도와 GameEngine의 드롭 간격을 동기화합니다.
     * 난이도에 따라 속도 증가율이 달라집니다:
     * - EASY: 20% 덜 증가 (0.8배)
     * - NORMAL: 기본 증가 (1.0배)
     * - HARD: 20% 더 증가 (1.2배)
     * @param clearedLines 이번에 클리어된 라인 수
     * @param totalLinesCleared 총 클리어된 라인 수
     */
    public void updateSpeedForLinesCleared(int clearedLines, int totalLinesCleared) {
        // 10줄마다 속도 증가
        int newSpeedLevel = totalLinesCleared / 10;
        if (newSpeedLevel > (totalLinesCleared - clearedLines) / 10) {
            // 난이도에 따른 속도 증가 배율 적용
            double speedMultiplier = getSpeedIncreaseMultiplier();
            gameTimer.increaseSpeed(speedMultiplier);
            // Timer의 새로운 속도로 드롭 간격 업데이트
            double newInterval = gameTimer.getInterval() / 1000.0; // milliseconds to seconds
            setDropIntervalSeconds(newInterval);
        }
    }

    /**
     * 난이도에 따른 속도 증가 배율을 반환합니다.
     * @return EASY: 0.8, NORMAL: 1.0, HARD: 1.2
     */
    private double getSpeedIncreaseMultiplier() {
        switch (difficulty) {
            case EASY:
                return 0.8; // 20% 덜 증가
            case HARD:
                return 1.2; // 20% 더 증가
            case NORMAL:
            default:
                return 1.0; // 기본 증가율
        }
    }

    public Tetromino getNext() {
        return next;
    }

    public int getScore() {
        return score;
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrent() {
        return current;
    }

    public int getPieceX() {
        return px;
    }

    public int getPieceY() {
        return py;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    /**
     * 테스트용: 난이도 기반 랜덤 피스를 생성합니다.
     * 이 메서드는 Roulette Wheel Selection 알고리즘을 테스트하기 위해 사용됩니다.
     */
    public Tetromino generateTestPiece() {
        return randomPiece();
    }
}

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

// 최소한의 게임 엔진으로, 조각 스폰, 이동, 회전, 라인 제거 기능을 제공합니다.
// 전체 실시간 루프를 구현하지는 않으며, UI 루프나 스케줄러와 함께 사용하도록 설계되었습니다.
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
    private int speedPerClearLines = 3;

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
    
    // 마지막으로 고정된 블록의 위치 정보 (대전 모드용)
    private java.util.Set<Integer> lastLockedColumns = new java.util.HashSet<>();
    private java.util.Set<Integer> tempLockedColumnsForEvent = null; // onLinesCleared 이벤트용 임시 저장
    
    // 마지막으로 고정된 블록의 실제 셀 위치들 (x, y 좌표)
    private java.util.List<int[]> lastLockedCells = new java.util.ArrayList<>();
    private java.util.List<int[]> tempLockedCellsForEvent = null; // onLinesCleared 이벤트용 임시 저장
    
    private int[][] boardSnapshotBeforeClear = null; // 라인 클리어 전 보드 상태 저장
    private java.util.List<Integer> clearedLineIndices = null; // 삭제된 줄 인덱스 저장
    
    // 라인클리어를 유발한 아이템 타입 추적 (대전 모드 공격 패턴용)
    private boolean lastClearWasByGravityOrSplit = false;

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
        totalLinesCleared = 0; // 아이템 생성 카운터 초기화
        nextItemPiece = null; // 대기 중인 아이템 피스 초기화
        next = randomPiece(); // 다음 피스 생성
        spawnNext();
        listener.onScoreChanged(score);
        stopAutoDrop(); // 오토 드랍 재시작
        startAutoDrop();
    }

    // Roulette Wheel Selection을 사용하여 난이도별 가중치로 블록을 선택합니다.
    // EASY: I블록 12, 나머지 10 (I블록이 20% 더 자주)
    // NORMAL: 모두 10 (균등 분포)
    // HARD: I블록 8, 나머지 10 (I블록이 20% 덜 자주)
    // ITEM: 아이템 미노가 설정되어 있으면 우선적으로 반환
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
            if (randomValue < cumulativeWeight) 
                return getPieceByIndex(i);
        }

        // Fallback (should never reach here)
        return Tetromino.of(Tetromino.Kind.I);
    }

    // 난이도에 따른 블록 가중치 배열 반환
    // @return [I, O, T, S, Z, J, L] 순서의 가중치 배열
    private int[] getWeightsByDifficulty() {
        switch (difficulty) {
            case EASY: // I블록 12, 나머지 10 (I블록이 20% 더 자주 등장)
                return new int[] { 12, 10, 10, 10, 10, 10, 10 };
            case HARD: // I블록 8, 나머지 10 (I블록이 20% 덜 등장)
                return new int[] { 8, 10, 10, 10, 10, 10, 10 };
            case ITEM: // ITEM 모드는 NORMAL과 동일한 생성 확률
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
            case NORMAL:
            default: // 모두 동일 가중치
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
        }
    }

    // 인덱스에 해당하는 Tetromino 반환
    // @param index 0:I, 1:O, 2:T, 3:S, 4:Z, 5:J, 6:L
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

    // 미노 복사 아이템을 생성
    // 기본 미노와 같은 형태이지만 랜덤한 블록 하나가 'C' 표시
    private Tetromino createItemPiece(Tetromino.Kind itemKind, Tetromino.Kind targetKind) {
        // COPY 아이템 → targetKind 미노에서 copyBlockIndex 랜덤 선택
        if (itemKind == Tetromino.Kind.COPY) {
            int copyBlockIndex = (int) (Math.random() * 4);  // 4개의 블록 중 하나
            return Tetromino.item(targetKind, 0, Tetromino.ItemType.COPY, copyBlockIndex);
        }

        // WEIGHT 아이템
        if (itemKind == Tetromino.Kind.WEIGHT) {
            return Tetromino.item(
                Tetromino.Kind.WEIGHT,
                0,
                Tetromino.ItemType.WEIGHT,
                0 // 인덱스는 의미 없음
            );
        }

        // GRAVITY 아이템
        if (itemKind == Tetromino.Kind.GRAVITY) {
            return Tetromino.item(
                Tetromino.Kind.GRAVITY,
                0,
                Tetromino.ItemType.GRAVITY,
                0
            );
        }

        // SPLIT 아이템
        if (itemKind == Tetromino.Kind.SPLIT) {
            return Tetromino.item(
                Tetromino.Kind.SPLIT,
                0,
                Tetromino.ItemType.SPLIT,
                0
            );
        }

        // fallback: COPY와 동일 처리
        return Tetromino.item(targetKind, 0, Tetromino.ItemType.COPY, 0);
    }

    // 10줄이 삭제될 때마다 호출되어 다음 미노를 아이템 미노로 설정합니다.
    // 현재 구현된 5가지 아이템 중 하나를 20% 확률로 선택합니다.
    private void generateItemPiece() {
        int itemChoice = rnd.nextInt(5);

        if (itemChoice == 0) {// COPY 아이템: 랜덤한 기본 미노 종류 선택
            Tetromino.Kind[] kinds = {
                    Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                    Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createItemPiece(Tetromino.Kind.COPY, targetKind);
        } else if (itemChoice == 1) {// WEIGHT 아이템
            nextItemPiece = createItemPiece(Tetromino.Kind.WEIGHT, null);
        } else if (itemChoice == 2) {// GRAVITY 아이템
            nextItemPiece = createItemPiece(Tetromino.Kind.GRAVITY, null);
        } else if (itemChoice == 3) {// SPLIT 아이템
            nextItemPiece = createItemPiece(Tetromino.Kind.SPLIT, null);
        } else {// LINE_CLEAR 아이템
            Tetromino.Kind[] kinds = {
                    Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                    Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createLineClearItemPiece(targetKind);
        }
    }

    // LINE_CLEAR 아이템 미노를 생성
    // COPY 아이템의 코드를 복사해서 만든 독립적인 구현
    private Tetromino createLineClearItemPiece(Tetromino.Kind targetKind) {
        // LINE_CLEAR 마크는 블록 4개 중 랜덤 선택
        int lineClearBlockIndex = (int) (Math.random() * 4);

        return Tetromino.lineClearItem(
            targetKind,           // LINE_CLEAR는 copy가 아닌 "타겟 미노 모양" 기반
            0,           // 초기 회전값
            lineClearBlockIndex   // L 마크 블록 위치
        );
    }
    
    // 아이템 미노의 특수 효과를 처리합니다.
    private void processItemEffect(Tetromino.ItemType itemType, Tetromino.Kind targetKind) {
        if (!itemModeEnabled) return;

        if (itemType == Tetromino.ItemType.COPY) {
            if (targetKind != null) {
                // 현재 next를 current로 이동시키고, 복사된 미노를 새로운 next로 설정
                current = next != null ? next : randomPiece();
                next = new Tetromino(targetKind, 0);

                // 새로운 current 위치 설정
                px = (board.getWidth() - current.getWidth()) / 2;
                py = 0;

                // COPY 효과에서도 게임오버 체크
                if (!board.fits(current.getShape(), px, py)) {
                    current = null;
                    javafx.application.Platform.runLater(() -> {
                        listener.onGameOver();
                    });
                    stopAutoDrop();
                    return;
                }

                // 리스너에게 알림
                listener.onPieceSpawned(current, px, py);
                listener.onNextPiece(next);
                listener.onBoardUpdated(board);
            }
        } else if (itemType == Tetromino.ItemType.GRAVITY) {
            processGravityEffect();
        } else if (itemType == Tetromino.ItemType.SPLIT) {
            processSplitEffect();
        } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
            processLineClearEffect();
        }
    }

    
    // 무게추 아래의 블록들을 파괴합니다. allLines가 true면 모든 줄, false면 바로 아래 한 줄만 파괴
    private void destroyBlocksBelow(boolean allLines) {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) return;

        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;
                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }

        int startRow = bottomRow + 1;
        int endRow = allLines ? board.getHeight() : Math.min(startRow + 1, board.getHeight());
        
        for (int y = startRow; y < endRow; y++) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, y) != 0) {
                    board.setCell(col, y, 0);
                }
            }
        }
    }
    
    private void destroyLineDirectlyBelow() { destroyBlocksBelow(false); }
    private void destroyAllBlocksBelow() { destroyBlocksBelow(true); }

    
    // 중력 아이템 효과를 처리합니다.
    private void processGravityEffect() {
        board.applyGravity();
    }
    
    // 분할 아이템 효과를 처리합니다.
    private void processSplitEffect() {
        if (current == null || !current.isItemPiece()) return;

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

    // 특정 열에서 스플릿 블록들에만 하드드롭을 적용합니다.
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
            // 맨 위부터 시작해서 해당 블록이 떨어질 수 있는 위치를 찾고 배치
            int dropPosition = 0;

            while (dropPosition < height - 1 && board.getCell(col, dropPosition + 1) == 0) {
                dropPosition++;
            }

            board.setCell(col, dropPosition, blockValue);
        }
    }

    // LINE_CLEAR 아이템 효과를 처리합니다.
    private void processLineClearEffect() {
        if (current == null || !current.isItemPiece()) return;

        // 보드를 스캔하여 LINE_CLEAR 마커(200번대 값)가 있는 행을 찾기
        int targetRow = -1;
        
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int cellValue = board.getCell(x, y);
                if (cellValue >= 200 && cellValue < 300) {
                    targetRow = y;
                    break;
                }
            }
            if (targetRow != -1) break;
        }

        // 유효한 행인지 확인
        if (targetRow >= 0 && targetRow < board.getHeight()) {
            final int finalTargetRow = targetRow;
            
            // 라인 삭제 전 원래 상태를 저장
            final int[] originalRow = new int[board.getWidth()];
            for (int c = 0; c < board.getWidth(); c++) {
                originalRow[c] = board.getCell(c, finalTargetRow);
            }

            // 하얀색으로 변경 (250ms)
            board.fillLineWith(finalTargetRow, -1); // 흰색 플래시 마커
            listener.onBoardUpdated(board);

            // Timer를 사용하여 250ms 후 라인 제거 및 게임 진행
            java.util.Timer delayTimer = new java.util.Timer();
            delayTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        // 흰색 플래시를 원래 상태로 복원
                        for (int c = 0; c < board.getWidth(); c++) {
                            board.setCell(c, finalTargetRow, originalRow[c]);
                        }
                        
                        // 해당 행을 직접 제거하고 위의 행들을 아래로 이동
                        // 위쪽 행들을 한 줄씩 아래로 복사
                        for (int r = finalTargetRow; r > 0; r--) {
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
                        
                        // LINE_CLEAR 효과 후 남아있는 full line이 있는지 체크
                        java.util.List<Integer> remainingFullLines = board.getFullLineIndices();
                        if (!remainingFullLines.isEmpty()) {
                            // 남은 full line이 있으면 일반 라인클리어 처리
                            board.clearFullLines();
                            int cleared = remainingFullLines.size();
                            totalLinesCleared += cleared;
                            addScoreForClearedLines(cleared);
                            updateSpeedForLinesCleared(cleared, totalLinesCleared);
                            listener.onBoardUpdated(board);
                        }
                        spawnNext();// 다음 블록 생성
                    });
                    delayTimer.cancel(); // Timer 정리
                }
            }, 250); // 250ms 지연
        }
    }

    // 현재 블록을 보드에 배치하는 공통 메서드
    private void placeCurrentPiece() {
        if (current == null) return;
        
        if (current.isItemPiece()) {
            // 모든 아이템은 특별한 아이템 블록으로 배치
            String itemTypeStr = current.getItemType().name();
            int itemBlockIndex;

            if (current.getItemType() == Tetromino.ItemType.COPY) {
                itemBlockIndex = current.getCopyBlockIndex();
            } else if (current.getItemType() == Tetromino.ItemType.LINE_CLEAR) {
                itemBlockIndex = current.getLineClearBlockIndex();
            } else {
                itemBlockIndex = 0; // 다른 아이템은 첫 번째 블록을 아이템 블록으로 사용
            }

            board.placeItemPiece(current.getShape(), px, py, current.getId(), itemBlockIndex, itemTypeStr);
        } else { // 일반 미노는 일반 블록으로 배치
            board.placePiece(current.getShape(), px, py, current.getId());
        }
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

        // 게임오버 조건: 새 블록이 생성 위치에 배치될 수 없을 때
        if (!board.fits(current.getShape(), px, py)) {
            // 게임오버 즉시 처리
            stopAutoDrop(); // 자동 하강을 먼저 완전히 중지
            current = null; // current를 null로 설정하여 더 이상의 조작 방지

            // JavaFX Application Thread에서 안전하게 게임오버 처리
            javafx.application.Platform.runLater(() -> {
                listener.onGameOver(); // 게임오버 이벤트 발생
            });
            return;
        }

        // 새 블록이 생성될 때 보드 상태 스냅샷 저장 (블록 배치 전)
        boardSnapshotBeforeClear = board.snapshot();

        // 정상적으로 생성된 경우에만 리스너 호출
        listener.onPieceSpawned(current, px, py);
        listener.onNextPiece(next);
        listener.onBoardUpdated(board);
    }

    
    // 현재 설정된 하강 간격을 사용하여 자동 하강 스케줄러를 시작합니다.
    // 여러 번 호출해도 안전하며, 필요 시 스케줄러를 생성합니다.
    public void startAutoDrop() {
        synchronized (schedulerLock) {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "GameEngine-AutoDrop");
                    t.setDaemon(true);
                    return t;
                });
            }
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) 
                return; // 이미 실행 중

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
            } else {// 새 게임 시작
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

    
    // 자동 하강 스케줄러를 중지하고 예약된 작업을 취소합니다.
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

    // 게임 종료 시 스케줄러를 완전히 종료합니다.
    public void shutdown() {
        synchronized (schedulerLock) {
            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                try {
                    scheduler.shutdownNow();
                } catch (Throwable ignored) {
                }
                scheduler = null;
            }
        }
    }

    public double getDropIntervalSeconds() {return dropIntervalSeconds;}

    // 자동 하강 간격(초)을 설정합니다. 자동 하강이 동작 중이면 새로운 간격으로 재스케줄링됩니다.
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
        if (current == null) return;

        // 무게추 충돌 상태에서는 좌우 이동 제한
        if (weightCollisionDetected) return;

        if (board.fits(current.getShape(), px - 1, py)) {
            px--;
            listener.onBoardUpdated(board);
        }
    }

    public void moveRight() {
        if (current == null) return;

        // 무게추 충돌 상태에서는 좌우 이동 제한
        if (weightCollisionDetected) return;

        if (board.fits(current.getShape(), px + 1, py)) {
            px++;
            listener.onBoardUpdated(board);
        }
    }

    public void rotateCW() {
        if (current == null) return;

        // 무게추 아이템은 회전할 수 없음
        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) return;

        Tetromino rotated = current.rotateClockwise();
        // wall kick 시도
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
        if (current == null) return false;
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

            placeCurrentPiece();// 현재 블록을 보드에 배치
            recordLastLockedColumns();// 마지막으로 고정된 블록의 열 위치 저장
            handleLockedPiece();
            return false;
        }
    }

    public void hardDrop() {
        if (current == null) return;

        int startY = py; // 시작 위치 기록

        // 무게추 아이템의 경우 특별 처리
        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) {
            weightCollisionDetected = true;

            // 먼저 최대한 아래로 내려가기
            while (board.fits(current.getShape(), px, py + 1)) py++;

            // 아래 모든 블록 파괴
            destroyAllBlocksBelow();

            // 블록 파괴 후 다시 최대한 아래로 내려가기
            while (board.fits(current.getShape(), px, py + 1)) py++;
        } else {
            // 일반 미노나 다른 아이템의 경우
            while (board.fits(current.getShape(), px, py + 1)) py++;
        }

        int dropDistance = py - startY; // 떨어진 거리 계산

        // 하드 드롭 점수 추가 (거리 > 0일 때만)
        if (dropDistance > 0) addHardDropScore(dropDistance);
        
        placeCurrentPiece();// 현재 블록을 보드에 배치
        recordLastLockedColumns();// 마지막으로 고정된 블록의 열 위치 저장
        handleLockedPiece();
    }

    // Handles animation + scoring after the falling piece is fixed to the board.
    private void handleLockedPiece() {
        // 이미 게임오버 상태라면 더 이상 처리하지 않음
        if (current == null) return;
        
        // 기본적으로 일반 라인클리어로 간주
        lastClearWasByGravityOrSplit = false;
        
        // 아이템이 착지한 경우 즉시 효과 발동 (current를 null로 만들기 전에)
        if (itemModeEnabled && current != null && current.isItemPiece()) {
            Tetromino.Kind kind = current.getKind();
            Tetromino.ItemType itemType = current.getItemType();

            // 무게추는 softDrop에서 이미 처리되므로 여기서는 제외
            if (kind == Tetromino.Kind.GRAVITY) {
                lastClearWasByGravityOrSplit = true; // 중력 블록으로 라인클리어
                processGravityEffect();
            } else if (kind == Tetromino.Kind.SPLIT) {
                lastClearWasByGravityOrSplit = true; // 스플릿 블록으로 라인클리어
                processSplitEffect();
            } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
                // LINE_CLEAR는 일반 라인 클리어 로직을 실행하지 않음
                processLineClearEffect();
                current = null; // 조작 방지
                return; // 여기서 종료
            }
        }
        
        // 아이템 효과 적용 후 보드 상태 스냅샷 저장 (라인 클리어 전 상태)
        boardSnapshotBeforeClear = board.snapshot();
        
        // lastLockedColumns와 lastLockedCells를 미리 백업 (다른 블록이 떨어지면서 덮어씌워질 수 있으므로)
        tempLockedColumnsForEvent = new java.util.HashSet<>(lastLockedColumns);
        tempLockedCellsForEvent = new java.util.ArrayList<>(lastLockedCells);

        java.util.List<Integer> fullLines = board.getFullLineIndices();
        
        // 삭제될 줄 인덱스 저장
        clearedLineIndices = new java.util.ArrayList<>(fullLines);

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
                    // 아이템 블록 범위 확인 (100-599)
                    if (snapshot[row][c] >= 100 && snapshot[row][c] < 600) {
                        hasItemBlockInFullLines = true;
                        int originalId = snapshot[row][c] % 100; // 원래 미노 ID 추출
                        itemPieceKind = Tetromino.kindForId(originalId); // ID로부터 Kind 추출

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

        // 하얀색으로 변경 (250ms)
        for (int row : fullLines) board.fillLineWith(row, -1); // 흰색 플래시 마커
        listener.onBoardUpdated(board);

        final boolean finalHasItemBlock = hasItemBlockInFullLines;
        final Tetromino.Kind finalItemPieceKind = itemPieceKind;
        final Tetromino.ItemType finalDetectedItemType = detectedItemType;
        final int lineCount = fullLines.size();
        
        // 라인 삭제 이벤트를 즉시 발생 (lastLockedColumns 정보가 유효한 동안)
        if (lineCount > 0) listener.onLinesCleared(lineCount);

        // Timer를 사용하여 250ms 후 라인 제거 및 게임 진행
        java.util.Timer delayTimer = new java.util.Timer();
        delayTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean copyEffectProcessed = false;

                // 흰색 플래시를 원래 상태로 복원 (clearFullLines 전에)
                if (boardSnapshotBeforeClear != null) {
                    for (int row : fullLines) {
                        if (row >= 0 && row < boardSnapshotBeforeClear.length) {
                            for (int c = 0; c < boardSnapshotBeforeClear[row].length; c++) {
                                board.setCell(c, row, boardSnapshotBeforeClear[row][c]);
                            }
                        }
                    }
                }

                // 아이템 효과를 먼저 처리 (clearFullLines 전에)
                // 단, GRAVITY/SPLIT/LINE_CLEAR는 이미 착지 시점에 처리되었으므로 제외
                if (itemModeEnabled && finalHasItemBlock && finalDetectedItemType != null) {
                    if (finalDetectedItemType == Tetromino.ItemType.COPY) {
                        copyEffectProcessed = true;
                        processItemEffect(finalDetectedItemType, finalItemPieceKind);
                    }
                }

                int cleared = board.clearFullLines(null);
                // onLinesCleared는 이미 호출되었으므로 여기서는 점수만 추가
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
        }, 250); // 250ms 지연
    }

    // 제거된 라인 수에 따른 점수 추가 100/250/500/1000
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
                if (cleared > 4) score += 1000 + (cleared - 4) * 250;
                break; // graceful handling
        }

        // 아이템 모드에서 라인 클리어 처리
        if (itemModeEnabled && cleared > 0) {
            totalLinesCleared += cleared;
            
            int beforeClear = totalLinesCleared - cleared;
            int currentGroup = totalLinesCleared / 2;
            int previousGroup = beforeClear / 2;

            if (currentGroup > previousGroup) generateItemPiece();
        }
    }

    // 블록 하강에 따른 점수 추가 (10점 × 거리 × 속도 계수)
    // @param dropDistance 하강한 칸 수
    public void addDropScore(int dropDistance) {
        int dropPoints = gameTimer.calculateDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
    }

    // 하드 드롭에 따른 점수 추가
    // @param dropDistance 하강한 칸 수
    public void addHardDropScore(int dropDistance) {
        int dropPoints = gameTimer.getHardDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
    }

    // 라인 클리어 시 게임 속도 증가 (10줄마다)
    // Timer의 속도와 GameEngine의 드롭 간격을 동기화합니다.
    // 난이도에 따라 속도 증가율이 달라집니다:
    // - EASY: (0.8배) NORMAL: (1.0배) HARD: (1.2배)
    // @param clearedLines 이번에 클리어된 라인 수
    // @param totalLinesCleared 총 클리어된 라인 수
    public void updateSpeedForLinesCleared(int clearedLines, int totalLinesCleared) {
        // 3줄마다 속도 증가
        int newSpeedLevel = totalLinesCleared / speedPerClearLines;
        if (newSpeedLevel > (totalLinesCleared - clearedLines) / speedPerClearLines) {
            // 난이도에 따른 속도 증가 배율 적용
            double speedMultiplier = getSpeedIncreaseMultiplier();
            gameTimer.increaseSpeed(speedMultiplier);
            // Timer의 새로운 속도로 드롭 간격 업데이트
            double newInterval = gameTimer.getInterval() / 1000.0; // milliseconds to seconds
            setDropIntervalSeconds(newInterval);
        }
    }

    // 난이도에 따른 속도 증가 배율을 반환합니다.
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

    // 현재 블록이 하드 드롭했을 때 도달하게 될 Y 위치를 계산합니다.
    // 고스트 블록 표시를 위해 사용됩니다. 
    // @return 고스트 블록의 Y 위치, 현재 블록이 없으면 -1
    public int getGhostY() {
        if (current == null) return -1;
        
        int ghostY = py;
        while (board.fits(current.getShape(), px, ghostY + 1)) {
            ghostY++;
        }
        
        return ghostY;
    }

    // 게임 타이머 접근자 (속도 조정용) @return 게임 타이머 인스턴스
    public Timer getGameTimer() {return gameTimer;}
    public Tetromino getNext() {return next;}
    public int getScore() {return score;}
    public Board getBoard() {return board;}
    public Tetromino getCurrent() {return current;}
    public int getPieceX() {return px;}
    public int getPieceY() {return py;}
    public int getTotalLinesCleared() {return totalLinesCleared;}
    
    public java.util.Set<Integer> getLastLockedColumns() {
        // tempLockedColumnsForEvent가 설정되어 있으면 그것을 반환 (이벤트용 백업)
        if (tempLockedColumnsForEvent != null) {
            return new java.util.HashSet<>(tempLockedColumnsForEvent);
        }
        return new java.util.HashSet<>(lastLockedColumns);
    }
    
    public java.util.List<int[]> getLastLockedCells() {
        // tempLockedCellsForEvent가 설정되어 있으면 그것을 반환 (이벤트용 백업)
        if (tempLockedCellsForEvent != null) {
            return new java.util.ArrayList<>(tempLockedCellsForEvent);
        }
        return new java.util.ArrayList<>(lastLockedCells);
    }
    
    public int[][] getBoardSnapshotBeforeClear() {
        return boardSnapshotBeforeClear;
    }
    
    public java.util.List<Integer> getClearedLineIndices() {
        return clearedLineIndices;
    }
    
    public boolean isLastClearByGravityOrSplit() {
        return lastClearWasByGravityOrSplit;
    }
    
    private void recordLastLockedColumns() {
        java.util.Set<Integer> newLockedColumns = new java.util.HashSet<>();
        java.util.List<int[]> newLockedCells = new java.util.ArrayList<>();
        
        if (current != null) {
            int[][] shape = current.getShape();
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        int boardX = px + c;
                        int boardY = py + r;
                        newLockedColumns.add(boardX);
                        newLockedCells.add(new int[]{boardX, boardY}); // [x, y] 좌표 저장
                    }
                }
            }
        }
        lastLockedColumns = newLockedColumns;
        lastLockedCells = newLockedCells;
    }

    // 테스트용: 난이도 기반 랜덤 피스를 생성합니다.
    // 이 메서드는 Roulette Wheel Selection 알고리즘을 테스트하기 위해 사용됩니다.
    public Tetromino generateTestPiece() {
        return randomPiece();
    }
}

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

// 최소한의 게임 엔진으로, 조각 스폰, 이동, 회전, 라인 제거 기능을 제공합니다
// 전체 렌더링 루프를 구현하지 않기 때문에 UI 루프와 스케줄러와 함께 사용하도록 설계되었습니다
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

    // 자동 낙하 간격(초) - 설정 가능
    private volatile double dropIntervalSeconds = 1.0;

    // 자동 낙하를 위한 스케줄러
    private final Object schedulerLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autoDropFuture;

    // 일시정지 관련 타이밍
    private long lastDropTime = 0;
    private long pauseStartTime = 0;

    // 아이템 모드 관련
    private int totalLinesCleared = 0; // 총 클리어된 라인 수
    private boolean itemModeEnabled = false; // 아이템 모드 활성화 여부
    private Tetromino nextItemPiece = null; // 다음 아이템 피스

    // 무게추 충돌 상태 추적
    private boolean weightCollisionDetected = false; // 무게추가 충돌을 감지했는지 여부
    
    // 마지막으로 고정된 블록의 위치 정보 (공격 모드용)
    private java.util.Set<Integer> lastLockedColumns = new java.util.HashSet<>();
    private java.util.Set<Integer> tempLockedColumnsForEvent = null; // onLinesCleared 이벤트용 임시 저장
    
    // 마지막으로 고정된 블록의 정확한 위치들(x, y 좌표)
    private java.util.List<int[]> lastLockedCells = new java.util.ArrayList<>();
    private java.util.List<int[]> tempLockedCellsForEvent = null; // onLinesCleared 이벤트용 임시 저장
    
    private int[][] boardSnapshotBeforeClear = null; // 라인 클리어 전 보드 상태 저장
    private java.util.List<Integer> clearedLineIndices = null; // 클리어된 라인 인덱스 저장
    
    // 라인클리어가 특발성 아이템에 의해 발생했는지 추적 (공격 모드 공격 패턴용)
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
        nextItemPiece = null; // 대기중인 아이템 피스 초기화
        // prepare next bag
        next = randomPiece();
        spawnNext();
        if (listener != null) {
            if (listener != null) {
                listener.onScoreChanged(score);
            }
        }
        // restart automatic dropping when a new game starts
        stopAutoDrop();
        startAutoDrop();
    }

    // Roulette Wheel Selection을 사용하여 난이도별 가중치로 블록을 선택합니다
    // EASY: I블록 12, 나머지 10 (I블록에 20% 더 유리)
    // NORMAL: 모두 10 (균등 분포)
    // HARD: I블록 8, 나머지 10 (I블록에 20% 더 불리)
    // ITEM: 아이템 테트로미노가 예정되어 있으면 우선적으로 반환
    private Tetromino randomPiece() {
        // 아이템 모드에서 다음 아이템 피스가 예정되어 있으면 우선 반환
        if (itemModeEnabled && nextItemPiece != null) {
            Tetromino itemPiece = nextItemPiece;
            nextItemPiece = null; // 사용 후 초기화
            System.out.println("[DEBUG] Returning item piece: " + itemPiece.getKind());
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

    // 난이도에 따른 블록 가중치 배열 반환
    // @return [I, O, T, S, Z, J, L] 순서의 가중치 배열
    private int[] getWeightsByDifficulty() {
        switch (difficulty) {
            case EASY:
                // I블록 12, 나머지 10 (I블록에 20% 더 유리 제공)
                return new int[] { 12, 10, 10, 10, 10, 10, 10 };
            case HARD:
                // I블록 8, 나머지 10 (I블록에 20% 더 불리)
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

    // 테트로미노 복사 아이템을 생성
    // 기본 테트로미노와 같은 모양이지만 랜덤한 블록 하나가 'C' 표시
    private Tetromino createItemPiece(Tetromino.Kind itemKind, Tetromino.Kind targetKind) {
        // COPY 아이템은 targetKind 테트로미노에서 copyBlockIndex 랜덤 선택
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
                0 // 인덱스는 사용 안함
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

        // fallback: COPY?� ?�일 처리
        return Tetromino.item(targetKind, 0, Tetromino.ItemType.COPY, 0);
    }


    // 10줄이 청소될 때마다 다음 테트로미노를 아이템 테트로미노로 설정합니다
    // 현재 구현은 5가지 아이템 중 하나를 20% 확률로 선택합니다
    private void generateItemPiece() {
        if (!itemModeEnabled) {
            System.out.println("[DEBUG] generateItemPiece called but itemModeEnabled is false!");
            return;
        }

        // 5가지 아이템 중 하나를 선택 (각 20% 확률)
        int itemChoice = rnd.nextInt(5);
        System.out.println("[DEBUG] Item choice: " + itemChoice);

        if (itemChoice == 0) {
            // COPY 아이템: 랜덤한 기본 테트로미노 종류 선택
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
            // LINE_CLEAR 아이템: COPY와 같은 방식으로 생성되지만 이름만 다름
            Tetromino.Kind[] kinds = {
                    Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                    Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createLineClearItemPiece(targetKind);
        }
    }

    // 이전 코드 - 주석 처리
    // // LINE_CLEAR 아이템 테트로미노를 생성합니다
    // // COPY 아이템의 코드를 복사해서 만든 독립적인 구현입니다
    // private Tetromino createLineClearItemPiece(Tetromino.Kind targetKind) {
    //     // 라인클리어 아이템 대상 테트로미노의 블록 개수 확인 (4개의 블록 중 랜덤 선택)
    //     int lineClearBlockIndex = (int) (Math.random() * 4);
    //     Tetromino result = new Tetromino(targetKind, 0, lineClearBlockIndex, Tetromino.ItemType.LINE_CLEAR);
    //     return result;
    // }

    // LINE_CLEAR 아이템 테트로미노를 생성
    // COPY 아이템의 코드를 복사해서 만든 독립적인 구현
    private Tetromino createLineClearItemPiece(Tetromino.Kind targetKind) {
        // LINE_CLEAR 마크가 표시될 블록 4개 중 랜덤 선택
        int lineClearBlockIndex = (int) (Math.random() * 4);

        return Tetromino.lineClearItem(
            targetKind,           // LINE_CLEAR??copy가 ?�닌 "?��?미노 모양" 기반
            0,           // 초기 ?�전�?
            lineClearBlockIndex   // L 마크 블록 ?�치
        );
    }
    
    // ?�이??미노???�수 ?�과�?처리?�니??
    // COPY: ?�정 미노 ?�?�으�??�음 미노�?복사?�니??
    // WEIGHT: 무게�??�과�?착�? 지???�래??블록?�을 ?�괴?�니??
    // GRAVITY: ?�체 보드??중력???�용?�여 �?공간??메웁?�다.
    // SPLIT: 3개의 ?�로 분할?�어 각각 ?�립?�으�??�어집니??
    
    private void processItemEffect(Tetromino.ItemType itemType, Tetromino.Kind targetKind) {
        if (!itemModeEnabled) return;

        if (itemType == Tetromino.ItemType.COPY) {
            // 복사 효과: 지정된 테트로미노와 같은 종류의 다음 테트로미노 생성
            if (targetKind != null) {
                // 기존 next를 current로 이동시키고 복사된 테트로미노를 새로운 next로 설정
                current = next != null ? next : randomPiece();
                next = new Tetromino(targetKind, 0);

                // 새로운 current 위치 설정
                px = (board.getWidth() - current.getWidth()) / 2;
                py = 0;

                // COPY ?�과?�서??게임?�버 체크
                if (!board.fits(current.getShape(), px, py)) {
                    current = null;
                    javafx.application.Platform.runLater(() -> {
                        if (listener != null) {
                            if (listener != null) {
                                listener.onGameOver();
                            }
                        }
                    });
                    stopAutoDrop();
                    return;
                }

                // 리스?�에�??�림
                if (listener != null) {
                    if (listener != null) {
                        listener.onPieceSpawned(current, px, py);
                    }
                    if (listener != null) {
                        listener.onNextPiece(next);
                    }
                    if (listener != null) {
                        listener.onBoardUpdated(board);
                    }
                }
            }
        } else if (itemType == Tetromino.ItemType.GRAVITY) {
            // 중력 ?�과: ?�체 보드??중력???�용?�여 �?공간??메�?
            processGravityEffect();
        } else if (itemType == Tetromino.ItemType.SPLIT) {
            // 분할 ?�과: 3개의 ?�로 분할?�어 각각 ?�립?�으�??�어�?
            processSplitEffect();
        } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
            // ?�인 ?�리???�과: ?�당 블록???�는 �??�체�?즉시 ?�거
            processLineClearEffect();
        }
    }

    
    // 무게�?바로 ?�래 ??줄을 ?�괴?�니??
    
    private void destroyLineDirectlyBelow() {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) return;

        // 무게추�? 차�??�는 모든 ?�과 가???�래�??�을 찾기
        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;

                    // 보드 범위 ?�에 ?�는 경우�?
                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }

        // 무게�?바로 ?�래 ??줄만 ?�괴 (중력 ?�용 ?�함)
        int targetRow = bottomRow + 1;
        if (targetRow < board.getHeight()) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, targetRow) != 0) {
                    board.setCell(col, targetRow, 0);
                }
            }
            // 무게추는 ?�괴�??�고 중력?� ?�용?��? ?�음
        }
    }

    
    // ?�드?�롭 ??무게�??�래??모든 블록??즉시 ?�괴?�니??
    
    private void destroyAllBlocksBelow() {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) return;

        // 무게추�? 차�??�는 모든 ?�과 가???�래�??�을 찾기
        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;

                    // 보드 범위 ?�에 ?�는 경우�?
                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }

        // 무게�??�래??모든 블록??즉시 ?�괴
        for (int y = bottomRow + 1; y < board.getHeight(); y++) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, y) != 0) {
                    board.setCell(col, y, 0);
                }
            }
        }
        // ?�드?�롭?��?�??�니메이???�이 즉시 ?�괴?�고 중력 ?�용 ?�함
    }

    
    // 중력 ?�이???�과�?처리?�니??
    // ?�체 보드??중력???�용?�여 �?공간??메웁?�다.
    
    private void processGravityEffect() {
        // ?�체 보드??중력 ?�용
        board.applyGravity();
    }

    
    // 분할 ?�이???�과�?처리?�니??
    // SPLIT 블록??차�??�는 3개의 ?�을 각각 ?�립?�으�??�어?�립?�다.
     
    private void processSplitEffect() {
        if (current == null || !current.isItemPiece()) return;

        // SPLIT 블록??차�??�는 ?�들??찾기
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

        // �??�에 ?�???�립?�으�?중력 ?�용
        for (int col : occupiedColumns) {
            applySingleColumnGravity(col);
        }

    }

    
    // ?�정 ?�에???�플�?블록?�에�??�드?�롭???�용?�니??
     
    private void applySingleColumnGravity(int col) {
        if (col < 0 || col >= board.getWidth())
            return;

        int height = board.getHeight();

        // ?�당 ?�에???�플�?블록?�만 추출 (?�에???�래 ?�서�?
        java.util.List<Integer> splitBlocks = new java.util.ArrayList<>();

        for (int y = 0; y < height; y++) {
            int val = board.getCell(col, y);
            if (val >= 500 && val < 600) { // SPLIT 블록??(500번�?)
                splitBlocks.add(val);
                board.setCell(col, y, 0); // ?�래 ?�치?�서 ?�거
            }
        }

        // �??�플�?블록???�에?��????�차?�으�??�드?�롭
        for (int blockValue : splitBlocks) {
            // �??��????�작?�서 ?�당 블록???�어�????�는 ?�치 찾기
            int dropPosition = 0;

            // ?�에?��????�래�??�려가면서 ?�어�????�는 가???�래 ?�치 찾기
            while (dropPosition < height - 1 && board.getCell(col, dropPosition + 1) == 0) {
                dropPosition++;
            }

            // ?�당 ?�치??블록 배치
            board.setCell(col, dropPosition, blockValue);
        }
    }

    
    // LINE_CLEAR ?�이???�과�?처리?�니??
    // L 블록???�는 ?�을 ?�인?�리?�하�??�수�??�용?�니??
     
    private void processLineClearEffect() {
        if (current == null || !current.isItemPiece()) {
            return;
        }

        // 보드�??�캔?�여 LINE_CLEAR 마커(200번�? �?가 ?�는 ?�을 찾기
        int targetRow = -1;
        
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int cellValue = board.getCell(x, y);
                // LINE_CLEAR 마커??200번�? (200-299)
                if (cellValue >= 200 && cellValue < 300) {
                    targetRow = y;
                    break;
                }
            }
            if (targetRow != -1) {
                break;
            }
        }

        // ?�효???�인지 ?�인
        if (targetRow >= 0 && targetRow < board.getHeight()) {
            final int finalTargetRow = targetRow;
            
            // ?�인 ??�� ???�래 ?�태�??�??
            final int[] originalRow = new int[board.getWidth()];
            for (int c = 0; c < board.getWidth(); c++) {
                originalRow[c] = board.getCell(c, finalTargetRow);
            }

            // ?��??�으�?변�?(250ms)
            board.fillLineWith(finalTargetRow, -1); // ?�색 ?�래??마커
            if (listener != null) {
                if (listener != null) {
                    listener.onBoardUpdated(board);
                }
            }

            // Timer�??�용?�여 250ms ???�인 ?�거 �?게임 진행
            java.util.Timer delayTimer = new java.util.Timer();
            delayTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        // ?�색 ?�래?��? ?�래 ?�태�?복원
                        for (int c = 0; c < board.getWidth(); c++) {
                            board.setCell(c, finalTargetRow, originalRow[c]);
                        }
                        
                        // ?�당 ?�을 직접 ?�거?�고 ?�의 ?�들???�래�??�동
                        // ?�쪽 ?�들????줄씩 ?�래�?복사
                        for (int r = finalTargetRow; r > 0; r--) {
                            for (int c = 0; c < board.getWidth(); c++) {
                                board.setCell(c, r, board.getCell(c, r - 1));
                            }
                        }

                        // �????�을 �?공간?�로 ?�정
                        for (int c = 0; c < board.getWidth(); c++) {
                            board.setCell(c, 0, 0);
                        }

                        totalLinesCleared += 1;

                        // ?�수 ?�산 (?�반 ?�인?�리?��? ?�일)
                        addScoreForClearedLines(1);
                        updateSpeedForLinesCleared(1, totalLinesCleared);

                        // 보드 ?�데?�트
                        if (listener != null) {
                            if (listener != null) {
                                listener.onBoardUpdated(board);
                            }
                        }
                        
                        // LINE_CLEAR ?�과 ???�아?�는 full line???�는지 체크
                        java.util.List<Integer> remainingFullLines = board.getFullLineIndices();
                        if (!remainingFullLines.isEmpty()) {
                            // ?��? full line???�으�??�반 ?�인?�리??처리
                            board.clearFullLines();
                            int cleared = remainingFullLines.size();
                            totalLinesCleared += cleared;
                            addScoreForClearedLines(cleared);
                            updateSpeedForLinesCleared(cleared, totalLinesCleared);
                            if (listener != null) {
                                if (listener != null) {
                                    listener.onBoardUpdated(board);
                                }
                            }
                        }
                        
                        // ?�음 블록 ?�성
                        spawnNext();
                    });
                    
                    delayTimer.cancel(); // Timer ?�리
                }
            }, 250); // 250ms 지??
        }
    }

    // ?�재 블록??보드??배치?�는 공통 메서??
    private void placeCurrentPiece() {
        if (current == null) return;
        
        if (current.isItemPiece()) {
            // 모든 ?�이?��? ?�별???�이??블록?�로 배치
            String itemTypeStr = current.getItemType().name();
            int itemBlockIndex;

            if (current.getItemType() == Tetromino.ItemType.COPY) {
                itemBlockIndex = current.getCopyBlockIndex();
            } else if (current.getItemType() == Tetromino.ItemType.LINE_CLEAR) {
                itemBlockIndex = current.getLineClearBlockIndex();
            } else {
                itemBlockIndex = 0; // ?�른 ?�이?��? �?번째 블록???�이??블록?�로 ?�용
            }

            board.placeItemPiece(current.getShape(), px, py, current.getId(), itemBlockIndex, itemTypeStr);
        } else {
            // ?�반 미노???�반 블록?�로 배치
            board.placePiece(current.getShape(), px, py, current.getId());
        }
    }

    private void spawnNext() {
        // ?�로??미노 ?�성 ??무게�?충돌 ?�태 리셋
        weightCollisionDetected = false;

        current = next != null ? next : randomPiece();

        // ?�이??모드?�서 nextItemPiece가 ?�정?�어 ?�으�?그것??next�??�용
        if (itemModeEnabled && nextItemPiece != null) {
            next = nextItemPiece;
            nextItemPiece = null; // ??�??�용 ??초기??
        } else {
            next = randomPiece();
        }

        px = (board.getWidth() - current.getWidth()) / 2;
        py = 0;

        // 게임?�버 조건: ??블록???�성 ?�치??배치?????�을 ??
        if (!board.fits(current.getShape(), px, py)) {
            // 게임?�버 즉시 처리
            stopAutoDrop(); // ?�동 ?�강??먼�? ?�전??중�?
            current = null; // current�?null�??�정?�여 ???�상??조작 방�?

            // JavaFX Application Thread?�서 ?�전?�게 게임?�버 처리
            javafx.application.Platform.runLater(() -> {
                if (listener != null) {
                    if (listener != null) {
                        listener.onGameOver();
                    } // 게임?�버 ?�벤??발생
                }
            });

            return;
        }

        // ??블록???�성????보드 ?�태 ?�냅???�??(블록 배치 ??
        boardSnapshotBeforeClear = board.snapshot();

        // ?�상?�으�??�성??경우?�만 리스???�출
        if (listener != null) {
            if (listener != null) {
                listener.onPieceSpawned(current, px, py);
            }
            if (listener != null) {
                listener.onNextPiece(next);
            }
            if (listener != null) {
                listener.onBoardUpdated(board);
            }
        }
    }

    
    // ?�재 ?�정???�강 간격???�용?�여 ?�동 ?�강 ?��?줄러�??�작?�니??
    // ?�러 �??�출?�도 ?�전?�며, ?�요 ???��?줄러�??�성?�니??
    
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

            // ?�시?��? ???�개?�는 경우 - pauseStartTime > 0?�면 ?�시?��????�태?�??
            if (pauseStartTime > 0) {
                if (lastDropTime > 0) {
                    // ?�전 ?�랍???�었??경우: ?�시?��????�간??보상
                    long pauseDuration = currentTime - pauseStartTime;
                    lastDropTime += pauseDuration;
                    long timeSinceLastDrop = currentTime - lastDropTime;
                    long remainingTime = periodMillis - (timeSinceLastDrop % periodMillis);
                    initialDelay = Math.max(1L, remainingTime);
                } else {
                    // ?�전 ?�랍???�었??경우: 바로 ?�작
                    lastDropTime = currentTime;
                    initialDelay = periodMillis;
                }
                pauseStartTime = 0; // 리셋
            } else {
                // ??게임 ?�작
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

    
    // ?�동 ?�강 ?��?줄러�?중�??�고 ?�약???�업??취소?�니??
    public void stopAutoDrop() {
        synchronized (schedulerLock) {
            pauseStartTime = System.currentTimeMillis(); // ?�시?��? ?�간 기록

            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
            // ?��?줄러??종료?��? ?�고 ?��? (?�사?�을 ?�해)
        }
    }

    // 게임 종료 ???��?줄러�??�전??종료?�니??
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

    public double getDropIntervalSeconds() {
        return dropIntervalSeconds;
    }

    
    // ?�동 ?�강 간격(�????�정?�니?? ?�동 ?�강???�작 중이�??�로??간격?�로 ?�스케줄링?�니??
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
        if (current == null)return;

        // 무게�?충돌 ?�태?�서??좌우 ?�동 ?�한
        if (weightCollisionDetected) {
            return;
        }

        if (board.fits(current.getShape(), px - 1, py)) {
            px--;
            if (listener != null) {
                if (listener != null) {
                    listener.onBoardUpdated(board);
                }
            }
        }
    }

    public void moveRight() {
        if (current == null) return;

        // 무게�?충돌 ?�태?�서??좌우 ?�동 ?�한
        if (weightCollisionDetected) return;
        

        if (board.fits(current.getShape(), px + 1, py)) {
            px++;
            if (listener != null) {
                listener.onBoardUpdated(board);
            }
        }
    }

    public void rotateCW() {
        if (current == null)
            return;

        // 무게�??�이?��? ?�전?????�음
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
                if (listener != null) {
                    listener.onBoardUpdated(board);
                }
                return;
            }
        }
    }

    public boolean softDrop() {
        if (current == null) return false;
        if (board.fits(current.getShape(), px, py + 1)) {
            py++;

            // 무게�??�이?�의 경우 ??�??�어�??�마??바로 ?�래 ??�??�괴
            if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT && weightCollisionDetected) {
                destroyLineDirectlyBelow();
            }

            // ?�프???�롭 ?�수 추�? (??�??�강)
            addDropScore(1);
            if (listener != null) {
                listener.onBoardUpdated(board);
            }
            return true;
        } else {
            // 무게�??�이?�의 경우 �?번째 충돌 감�? ?�점?�서 ?�태 변�?
            if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT && !weightCollisionDetected) {
                weightCollisionDetected = true;
                // �?충돌 ?�에??바로 ?�래 ??�??�괴
                destroyLineDirectlyBelow();
                // 무게추는 충돌 감�? ?�에??바로 착�??�키지 ?�고 계속 진행
                return false;
            }

            // ?�재 블록??보드??배치
            placeCurrentPiece();
            
            // 마�?막으�?고정??블록?????�치 ?�??
            recordLastLockedColumns();
            
            handleLockedPiece();
            return false;
        }
    }

    public void hardDrop() {
        if (current == null) return;

        int startY = py; // ?�작 ?�치 기록

        // 무게�??�이?�의 경우 ?�별 처리
        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) {
            weightCollisionDetected = true;

            // 먼�? 최�????�래�??�려가�?
            while (board.fits(current.getShape(), px, py + 1)) py++;

            // ?�래 모든 블록 ?�괴
            destroyAllBlocksBelow();

            // 블록 ?�괴 ???�시 최�????�래�??�려가�?
            while (board.fits(current.getShape(), px, py + 1)) py++;
        } else {
            // ?�반 미노???�른 ?�이?�의 경우
            while (board.fits(current.getShape(), px, py + 1)) py++;
        }

        int dropDistance = py - startY; // ?�어�?거리 계산

        // ?�드 ?�롭 ?�수 추�? (거리 > 0???�만)
        if (dropDistance > 0) addHardDropScore(dropDistance);
        

        // ?�재 블록??보드??배치
        placeCurrentPiece();

        // 마�?막으�?고정??블록?????�치 ?�??
        recordLastLockedColumns();

        handleLockedPiece();
    }

    // Handles animation + scoring after the falling piece is fixed to the board.
    private void handleLockedPiece() {
        // ?��? 게임?�버 ?�태?�면 ???�상 처리?��? ?�음
        if (current == null) return;
        
        // 기본?�으�??�반 ?�인?�리?�로 간주
        lastClearWasByGravityOrSplit = false;
        
        // ?�이?�이 착�???경우 즉시 ?�과 발동 (current�?null�?만들�??�에)
        if (itemModeEnabled && current != null && current.isItemPiece()) {
            Tetromino.Kind kind = current.getKind();
            Tetromino.ItemType itemType = current.getItemType();

            // 무게추는 softDrop?�서 ?��? 처리?��?�??�기?�는 ?�외
            if (kind == Tetromino.Kind.GRAVITY) {
                lastClearWasByGravityOrSplit = true; // 중력 블록?�로 ?�인?�리??
                processGravityEffect();
            } else if (kind == Tetromino.Kind.SPLIT) {
                lastClearWasByGravityOrSplit = true; // ?�플�?블록?�로 ?�인?�리??
                processSplitEffect();
            } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
                // LINE_CLEAR???�체?�으�??�인 ?�거 �??�음 블록 ?�성??처리?��?�?
                // ?�반 ?�인 ?�리??로직???�행?��? ?�음
                processLineClearEffect();
                current = null; // 조작 방�?
                return; // ?�기??종료
            }
        }
        
        // ?�이???�과 ?�용 ??보드 ?�태 ?�냅???�??(?�인 ?�리?????�태)
        boardSnapshotBeforeClear = board.snapshot();
        
        // lastLockedColumns?� lastLockedCells�?미리 백업 (?�른 블록???�어지면서 ??��?�워�????�으므�?
        tempLockedColumnsForEvent = new java.util.HashSet<>(lastLockedColumns);
        tempLockedCellsForEvent = new java.util.ArrayList<>(lastLockedCells);

        java.util.List<Integer> fullLines = board.getFullLineIndices();
        
        // ??��??�??�덱???�??
        clearedLineIndices = new java.util.ArrayList<>(fullLines);

        current = null; // ?�시 조작??막고, 보드?�는 고정??조각�??��?

        if (fullLines.isEmpty()) {
            spawnNext();
            return;
        }

        // ?�이??모드: fillLineWith�???��?�기 ?�에 ?�이??블록 ?�인
        boolean hasItemBlockInFullLines = false;
        Tetromino.Kind itemPieceKind = null; // ?�이??블록???�래 미노 ?�??
        Tetromino.ItemType detectedItemType = null; // 감�????�이???�??

        if (itemModeEnabled) {
            int[][] snapshot = board.snapshot();
            for (int row : fullLines) {
                for (int c = 0; c < snapshot[row].length; c++) {
                    if (snapshot[row][c] != 0) {
                    }
                    // ?�이??블록 범위 ?�인 (100-599)
                    if (snapshot[row][c] >= 100 && snapshot[row][c] < 600) {
                        hasItemBlockInFullLines = true;
                        int originalId = snapshot[row][c] % 100; // ?�래 미노 ID 추출
                        itemPieceKind = Tetromino.kindForId(originalId); // ID로�???Kind 추출

                        // ?�이???�??구분
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

        // ?��??�으�?변�?(250ms)
        for (int row : fullLines) board.fillLineWith(row, -1); // ?�색 ?�래??마커
        if (listener != null) {
            listener.onBoardUpdated(board);
        }

        final boolean finalHasItemBlock = hasItemBlockInFullLines;
        final Tetromino.Kind finalItemPieceKind = itemPieceKind;
        final Tetromino.ItemType finalDetectedItemType = detectedItemType;
        final int lineCount = fullLines.size();
        
        // ?�인 ??�� ?�벤?��? 즉시 발생 (lastLockedColumns ?�보가 ?�효???�안)
        if (lineCount > 0 && listener != null) {
            listener.onLinesCleared(lineCount);
        }

        // Timer�??�용?�여 250ms ???�인 ?�거 �?게임 진행
        java.util.Timer delayTimer = new java.util.Timer();
        delayTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean copyEffectProcessed = false;

                // ?�색 ?�래?��? ?�래 ?�태�?복원 (clearFullLines ?�에)
                if (boardSnapshotBeforeClear != null) {
                    for (int row : fullLines) {
                        if (row >= 0 && row < boardSnapshotBeforeClear.length) {
                            for (int c = 0; c < boardSnapshotBeforeClear[row].length; c++) {
                                board.setCell(c, row, boardSnapshotBeforeClear[row][c]);
                            }
                        }
                    }
                }

                // ?�이???�과�?먼�? 처리 (clearFullLines ?�에)
                // ?? GRAVITY/SPLIT/LINE_CLEAR???��? 착�? ?�점??처리?�었?��?�??�외
                if (itemModeEnabled && finalHasItemBlock && finalDetectedItemType != null) {
                    if (finalDetectedItemType == Tetromino.ItemType.COPY) {
                        copyEffectProcessed = true;
                        processItemEffect(finalDetectedItemType, finalItemPieceKind);
                    }
                    // GRAVITY, SPLIT, LINE_CLEAR??착�? ???��? 처리?�었?��?�??�기?�는 처리?��? ?�음
                }

                // ?�이???�과�?처리?�는 콜백 ?�의 (?��?)
                Runnable itemEffectCallback = () -> {
                };

                int cleared = board.clearFullLines(itemEffectCallback);
                // onLinesCleared???��? ?�출?�었?��?�??�기?�는 ?�수�?추�?
                if (cleared > 0) {
                    addScoreForClearedLines(cleared);
                    if (listener != null) {
                        listener.onScoreChanged(score);
                    }
                }
                if (listener != null) {
                    listener.onBoardUpdated(board);
                }

                // COPY ?�과가 처리??경우 spawnNext�??�출?��? ?�음
                if (!copyEffectProcessed) {
                    spawnNext();
                }
                delayTimer.cancel(); // Timer ?�리
            }
        }, 250); // 250ms 지??
    }

    // ?�거???�인 ?�에 ?�른 ?�수 추�?:
    // 1 -> 100, 2 -> 250, 3 -> 500, 4 -> 1000
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

        // ?�이??모드?�서 ?�인 ?�리??처리
        if (itemModeEnabled && cleared > 0) {
            totalLinesCleared += cleared;
            
            // 
            int beforeClear = totalLinesCleared - cleared;
            int currentGroup = totalLinesCleared / 2;
            int previousGroup = beforeClear / 2;

            if (currentGroup > previousGroup) generateItemPiece();
        }
    }

    // 블록 ?�강???�른 ?�수 추�? (10??× 거리 × ?�도 계수)
    // @param dropDistance ?�강??�???
    public void addDropScore(int dropDistance) {
        int dropPoints = gameTimer.calculateDropScore(dropDistance);
        score += dropPoints;
        if (listener != null) {
            listener.onScoreChanged(score);
        }
    }

    // ?�드 ?�롭???�른 ?�수 추�?
    // @param dropDistance ?�강??�???
    public void addHardDropScore(int dropDistance) {
        int dropPoints = gameTimer.getHardDropScore(dropDistance);
        score += dropPoints;
        if (listener != null) {
            listener.onScoreChanged(score);
        }
    }

    // 게임 ?�?�머 ?�근??(?�도 조정??
    // @return 게임 ?�?�머 ?�스?�스
    public Timer getGameTimer() {
        return gameTimer;
    }

    // ?�인 ?�리????게임 ?�도 증�? (10줄마??
    // Timer???�도?� GameEngine???�롭 간격???�기?�합?�다.
    // ?�이?�에 ?�라 ?�도 증�??�이 ?�라집니??
    // - EASY: 20% ??증�? (0.8�?
    // - NORMAL: 기본 증�? (1.0�?
    // - HARD: 20% ??증�? (1.2�?
    // @param clearedLines      ?�번???�리?�된 ?�인 ??
    // @param totalLinesCleared �??�리?�된 ?�인 ??
    public void updateSpeedForLinesCleared(int clearedLines, int totalLinesCleared) {
        // 3줄마???�도 증�?
        int newSpeedLevel = totalLinesCleared / speedPerClearLines;
        if (newSpeedLevel > (totalLinesCleared - clearedLines) / speedPerClearLines) {
            // ?�이?�에 ?�른 ?�도 증�? 배율 ?�용
            double speedMultiplier = getSpeedIncreaseMultiplier();
            gameTimer.increaseSpeed(speedMultiplier);
            // Timer???�로???�도�??�롭 간격 ?�데?�트
            double newInterval = gameTimer.getInterval() / 1000.0; // milliseconds to seconds
            setDropIntervalSeconds(newInterval);
        }
    }

    // ?�이?�에 ?�른 ?�도 증�? 배율??반환?�니??
    // @return EASY: 0.8, NORMAL: 1.0, HARD: 1.2
    private double getSpeedIncreaseMultiplier() {
        switch (difficulty) {
            case EASY:
                return 0.8; // 20% ??증�?
            case HARD:
                return 1.2; // 20% ??증�?
            case NORMAL:
            default:
                return 1.0; // 기본 증�???
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

    /**
     * ?�재 블록???�드 ?�롭?�을 ???�달?�게 ??Y ?�치�?계산?�니??
     * 고스??블록 ?�시�??�해 ?�용?�니??
     * 
     * @return 고스??블록??Y ?�치, ?�재 블록???�으�?-1
     */
    public int getGhostY() {
        if (current == null) {
            return -1;
        }
        
        int ghostY = py;
        while (board.fits(current.getShape(), px, ghostY + 1)) {
            ghostY++;
        }
        
        return ghostY;
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
    
    public java.util.Set<Integer> getLastLockedColumns() {
        // tempLockedColumnsForEvent가 ?�정?�어 ?�으�?그것??반환 (?�벤?�용 백업)
        if (tempLockedColumnsForEvent != null) {
            return new java.util.HashSet<>(tempLockedColumnsForEvent);
        }
        return new java.util.HashSet<>(lastLockedColumns);
    }
    
    public java.util.List<int[]> getLastLockedCells() {
        // tempLockedCellsForEvent가 ?�정?�어 ?�으�?그것??반환 (?�벤?�용 백업)
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
                        newLockedCells.add(new int[]{boardX, boardY}); // [x, y] 좌표 ?�??
                    }
                }
            }
        }
        lastLockedColumns = newLockedColumns;
        lastLockedCells = newLockedCells;
    }

    // ?�스?�용: ?�이??기반 ?�덤 ?�스�??�성?�니??
    // ??메서?�는 Roulette Wheel Selection ?�고리즘???�스?�하�??�해 ?�용?�니??
    public Tetromino generateTestPiece() {
        return randomPiece();
    }
}

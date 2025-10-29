package team13.tetris.game.model;

import java.util.Arrays;

// 테트로미노 데이터 래퍼 (CSS 스타일 포함)
public class Tetromino {

    public enum ItemType {
        COPY, // 미노 복사 (C 마크)
        WEIGHT, // 무게추 (W 마크)
        GRAVITY, // 중력 (G 마크)
        SPLIT, // 분할 (S 마크)
        LINE_CLEAR // 라인클리어 (L 마크)
    }

    public enum Kind {
        I(new int[][][] {
                { { 0, 0, 0, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 1, 0 }, { 0, 0, 1, 0 }, { 0, 0, 1, 0 }, { 0, 0, 1, 0 } },
                { { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 } }
        }, 1, "block-I", "tetris-i-text"),

        O(new int[][][] {
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } }
        }, 2, "block-O", "tetris-o-text"),

        T(new int[][][] {
                { { 0, 1, 0, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 0, 0 }, { 1, 1, 1, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 1, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } }
        }, 3, "block-T", "tetris-t-text"),

        S(new int[][][] {
                { { 0, 1, 1, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 0, 0 }, { 0, 1, 1, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 } },
                { { 1, 0, 0, 0 }, { 1, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } }
        }, 4, "block-S", "tetris-s-text"),

        Z(new int[][][] {
                { { 1, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 1, 0 }, { 0, 1, 1, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 0, 0 }, { 1, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 0, 0, 0 }, { 0, 0, 0, 0 } }
        }, 5, "block-Z", "tetris-z-text"),

        J(new int[][][] {
                { { 1, 0, 0, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 0, 0 }, { 1, 1, 1, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 } }
        }, 6, "block-J", "tetris-j-text"),

        L(new int[][][] {
                { { 0, 0, 1, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 } },
                { { 0, 0, 0, 0 }, { 1, 1, 1, 0 }, { 1, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 } }
        }, 7, "block-L", "tetris-l-text"),

        WEIGHT(new int[][][] {
                { { 0, 1, 1, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } }
        }, 8, "block-weight", "tetris-weight-text"),

        COPY(new int[][][] {
                { { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } }
        }, 9, "block-copy", "tetris-copy-text"),

        GRAVITY(new int[][][] {
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } },
                { { 0, 1, 1, 0 }, { 0, 1, 1, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } }
        }, 10, "block-gravity", "tetris-gravity-text"),

        SPLIT(new int[][][] {
                { { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 } },
                { { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 1, 1, 1, 0 }, { 0, 0, 0, 0 } }
        }, 11, "block-split", "tetris-split-text");

        private final int[][][] rotations;
        private final int id;
        private final String blockStyleClass; // 블록 CSS 클래스
        private final String textStyleClass; // 텍스트 CSS 클래스

        Kind(int[][][] rotations, int id, String blockStyleClass, String textStyleClass) {
            this.rotations = rotations;
            this.id = id;
            this.blockStyleClass = blockStyleClass;
            this.textStyleClass = textStyleClass;
        }

        public int[][] getRotation(int idx) {
            return rotations[idx % rotations.length];
        }

        public int getId() {
            return id;
        }

        public String getBlockStyleClass() {
            return blockStyleClass;
        }

        public String getTextStyleClass() {
            return textStyleClass;
        }
    }

    private final Kind kind; // raw shape면 null
    private final int rotation; // 회전 인덱스 (0~3)
    private final int[][] shape; // raw shape 저장소
    private final int id; // 고유 id (1~7)

    // 아이템 관련 필드
    private final boolean isItemPiece; // 아이템 여부
    private final ItemType itemType; // 아이템 타입
    private final int copyBlockIndex; // C 표시 인덱스
    private final int copyBlockRow; // C 표시 행
    private final int copyBlockCol; // C 표시 열

    // LINE_CLEAR 전용 필드
    private final int lineClearBlockIndex; // L 표시 인덱스
    private final int lineClearBlockRow; // L 표시 행
    private final int lineClearBlockCol; // L 표시 열

    private final boolean canRotate; // 회전 가능 여부
    private boolean isLocked; // 이동 잠금 상태

    // 미리 정의된 Kind로 생성
    public Tetromino(Kind kind) {
        this(kind, 0);
    }

    public Tetromino(Kind kind, int rotation) {
        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
        this.isItemPiece = false;
        this.itemType = null;
        this.copyBlockIndex = -1;
        this.copyBlockRow = -1;
        this.copyBlockCol = -1;
        // LINE_CLEAR 필드 초기화
        this.lineClearBlockIndex = -1;
        this.lineClearBlockRow = -1;
        this.lineClearBlockCol = -1;
        this.canRotate = true;
        this.isLocked = false;
    }

    // 레거시 호환: id + raw shape로 생성
    public Tetromino(int id, int[][] shape) {
        this.kind = null;
        this.rotation = 0;
        this.id = id;
        this.shape = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            this.shape[i] = Arrays.copyOf(shape[i], shape[i].length);
        }
        this.isItemPiece = false;
        this.itemType = null;
        this.copyBlockIndex = -1;
        this.copyBlockRow = -1;
        this.copyBlockCol = -1;
        // LINE_CLEAR 필드 초기화
        this.lineClearBlockIndex = -1;
        this.lineClearBlockRow = -1;
        this.lineClearBlockCol = -1;
        this.canRotate = true;
        this.isLocked = false;
    }

    // 아이템 미노(미노 복사) 생성자
    public Tetromino(Kind kind, int rotation, int copyBlockIndex) {
        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
        this.isItemPiece = true;
        this.copyBlockIndex = copyBlockIndex;

        // LINE_CLEAR 필드 초기화
        this.lineClearBlockIndex = -1;
        this.lineClearBlockRow = -1;
        this.lineClearBlockCol = -1;

        // 아이템 타입 설정
        if (kind == Kind.WEIGHT) {
            this.itemType = ItemType.WEIGHT;
        } else if (kind == Kind.GRAVITY) {
            this.itemType = ItemType.GRAVITY;
        } else if (kind == Kind.SPLIT) {
            this.itemType = ItemType.SPLIT;
        } else {
            this.itemType = ItemType.COPY;
        }

        // 회전 가능 여부 (무게추/중력/분할 제외)
        this.canRotate = (kind != Kind.WEIGHT && kind != Kind.GRAVITY && kind != Kind.SPLIT);
        this.isLocked = false;

        // copyBlockIndex 기반 위치 계산
        int[][] currentShape = kind.getRotation(rotation % 4);
        int blockCount = 0;
        int foundRow = -1, foundCol = -1;

        for (int r = 0; r < currentShape.length; r++) {
            for (int c = 0; c < currentShape[r].length; c++) {
                if (currentShape[r][c] != 0) {
                    if (blockCount == copyBlockIndex) {
                        foundRow = r;
                        foundCol = c;
                        break;
                    }
                    blockCount++;
                }
            }
            if (foundRow != -1)
                break;
        }

        this.copyBlockRow = foundRow;
        this.copyBlockCol = foundCol;
    }

    // LINE_CLEAR 아이템 생성자
    public Tetromino(Kind kind, int rotation, int lineClearBlockIndex, ItemType itemType) {
        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
        this.isItemPiece = true;
        this.itemType = itemType; // ItemType.LINE_CLEAR
        this.canRotate = true; // 회전 가능
        this.isLocked = false;

        // COPY 필드 미사용
        this.copyBlockIndex = -1;
        this.copyBlockRow = -1;
        this.copyBlockCol = -1;

        // LINE_CLEAR 필드 설정
        this.lineClearBlockIndex = lineClearBlockIndex;

        // lineClearBlockIndex 기반 위치 계산
        int[][] currentShape = kind.getRotation(rotation % 4);
        int blockCount = 0;
        int foundRow = -1, foundCol = -1;

        for (int r = 0; r < currentShape.length; r++) {
            for (int c = 0; c < currentShape[r].length; c++) {
                if (currentShape[r][c] != 0) {
                    if (blockCount == lineClearBlockIndex) {
                        foundRow = r;
                        foundCol = c;
                        break;
                    }
                    blockCount++;
                }
            }
            if (foundRow != -1)
                break;
        }

        this.lineClearBlockRow = foundRow;
        this.lineClearBlockCol = foundCol;
    }

    public int getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public boolean canRotate() {
        return canRotate;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    // LINE_CLEAR 전용 getter
    public int getLineClearBlockIndex() {
        return lineClearBlockIndex;
    }

    public int getLineClearBlockRow() {
        return lineClearBlockRow;
    }

    public int getLineClearBlockCol() {
        return lineClearBlockCol;
    }

    public int[][] getShape() {
        int[][] src = (kind != null) ? kind.getRotation(rotation) : shape;
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return copy;
    }

    public int getHeight() {
        return getShape().length;
    }

    public int getWidth() {
        return getShape()[0].length;
    }

    public Tetromino rotateClockwise() {
        if (kind != null) {
            // 아이템 회전 시 인덱스 갱신
            if (isItemPiece) {
                // LINE_CLEAR는 lineClearBlockIndex 사용
                if (itemType == ItemType.LINE_CLEAR) {
                    int newLineClearBlockIndex = calculateRotatedLineClearBlockIndex(true);
                    return new Tetromino(kind, (rotation + 1) % 4, newLineClearBlockIndex, ItemType.LINE_CLEAR);
                } else {
                    // 기타 아이템은 copyBlockIndex 사용
                    int newCopyBlockIndex = calculateRotatedCopyBlockIndex(true);
                    return new Tetromino(kind, (rotation + 1) % 4, newCopyBlockIndex);
                }
            } else {
                return new Tetromino(kind, (rotation + 1) % 4);
            }
        }
        // raw shape 회전
        int[][] s = getShape();
        int h = s.length, w = s[0].length;
        int[][] out = new int[w][h];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                out[c][h - 1 - r] = s[r][c];
            }
        }
        return new Tetromino(id, out);
    }

    public Tetromino rotateCounter() {
        if (kind != null) {
            // 아이템 회전 시 인덱스 갱신
            if (isItemPiece) {
                // LINE_CLEAR는 lineClearBlockIndex 사용
                if (itemType == ItemType.LINE_CLEAR) {
                    int newLineClearBlockIndex = calculateRotatedLineClearBlockIndex(false);
                    return new Tetromino(kind, (rotation + 3) % 4, newLineClearBlockIndex, ItemType.LINE_CLEAR);
                } else {
                    // 기타 아이템은 copyBlockIndex 사용
                    int newCopyBlockIndex = calculateRotatedCopyBlockIndex(false);
                    return new Tetromino(kind, (rotation + 3) % 4, newCopyBlockIndex);
                }
            } else {
                return new Tetromino(kind, (rotation + 3) % 4);
            }
        }
        int[][] s = getShape();
        int h = s.length, w = s[0].length;
        int[][] out = new int[w][h];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                out[w - 1 - c][r] = s[r][c];
            }
        }
        return new Tetromino(id, out);
    }

    // 편의 팩토리
    public static Tetromino of(Kind k) {
        return new Tetromino(k, 0);
    }

    // Kind 반환 (raw shape면 null)
    public Kind getKind() {
        return kind;
    }

    // 블록 CSS 클래스명
    public String getBlockStyleClass() {
        if (kind != null)
            return kind.getBlockStyleClass();
        return blockClassForId(id);
    }

    // 텍스트 CSS 클래스명
    public String getTextStyleClass() {
        if (kind != null)
            return kind.getTextStyleClass();
        return textClassForId(id);
    }

    // id 기반 Kind 조회
    public static Kind kindForId(int id) {
        for (Kind k : Kind.values()) {
            if (k.getId() == id)
                return k;
        }
        return null;
    }

    // raw 생성자 호환 매핑

    private static String blockClassForId(int id) {
        switch (id) {
            case 1:
                return "block-I";
            case 2:
                return "block-O";
            case 3:
                return "block-T";
            case 4:
                return "block-S";
            case 5:
                return "block-Z";
            case 6:
                return "block-J";
            case 7:
                return "block-L";
            default:
                return "block";
        }
    }

    private static String textClassForId(int id) {
        switch (id) {
            case 1:
                return "tetris-i-text";
            case 2:
                return "tetris-o-text";
            case 3:
                return "tetris-t-text";
            case 4:
                return "tetris-s-text";
            case 5:
                return "tetris-z-text";
            case 6:
                return "tetris-j-text";
            case 7:
                return "tetris-l-text";
            default:
                return "tetris-generic-text";
        }
    }

    // 아이템 관련 메서드
    public boolean isItemPiece() {
        return isItemPiece;
    }

    public int getCopyBlockIndex() {
        return copyBlockIndex;
    }

    // 회전 후 copyBlock 인덱스 계산
    private int calculateRotatedCopyBlockIndex(boolean clockwise) {
        if (!isItemPiece || kind == null) {
            return -1;
        }

        // 현재 copyBlock 위치
        int originalRow = copyBlockRow;
        int originalCol = copyBlockCol;

        // 회전된 블록 위치 계산
        Tetromino rotatedTetromino;
        if (clockwise) {
            rotatedTetromino = new Tetromino(kind, (rotation + 1) % 4);
        } else {
            rotatedTetromino = new Tetromino(kind, (rotation + 3) % 4);
        }

        // 회전 변환 적용
        int newRow, newCol;

        // 종류별 회전 공식
        if (kind == Kind.O) {
            // O미노: 블록 고정, C 이동
            // 실제 블록: (0,1), (0,2), (1,1), (1,2)
            if (clockwise) {
                // 시계방향 C 이동
                if (originalRow == 0 && originalCol == 1) {
                    newRow = 0;
                    newCol = 2;
                } else if (originalRow == 0 && originalCol == 2) {
                    newRow = 1;
                    newCol = 2;
                } else if (originalRow == 1 && originalCol == 2) {
                    newRow = 1;
                    newCol = 1;
                } else if (originalRow == 1 && originalCol == 1) {
                    newRow = 0;
                    newCol = 1;
                } else {
                    // 기본값
                    newRow = 0;
                    newCol = 1;
                }
            } else {
                // 반시계방향 C 이동
                if (originalRow == 0 && originalCol == 1) {
                    newRow = 1;
                    newCol = 1;
                } else if (originalRow == 1 && originalCol == 1) {
                    newRow = 1;
                    newCol = 2;
                } else if (originalRow == 1 && originalCol == 2) {
                    newRow = 0;
                    newCol = 2;
                } else if (originalRow == 0 && originalCol == 2) {
                    newRow = 0;
                    newCol = 1;
                } else {
                    // 기본값
                    newRow = 0;
                    newCol = 1;
                }
            }

            // O미노 블록 고정
            if (newRow == 0 && newCol == 1) {
                return 0; // 첫 번째 블록
            } else if (newRow == 0 && newCol == 2) {
                return 1; // 두 번째 블록
            } else if (newRow == 1 && newCol == 1) {
                return 2; // 세 번째 블록
            } else if (newRow == 1 && newCol == 2) {
                return 3; // 네 번째 블록
            } else {
                return 0; // 기본값
            }
        }

        int[][] rotatedPositions = rotatedTetromino.getBlockPositions();

        if (kind == Kind.I) {
            // I미노 4x4 공식
            if (clockwise) {
                // 시계방향 공식
                newRow = originalCol;
                newCol = 3 - originalRow;
            } else {
                // 반시계방향 공식
                newRow = 3 - originalCol;
                newCol = originalRow;
            }
        } else {
            // 3x3 미노 회전 공식
            if (clockwise) {
                // 시계방향 공식
                newRow = originalCol;
                newCol = 2 - originalRow;
            } else {
                // 반시계방향 공식
                newRow = 2 - originalCol;
                newCol = originalRow;
            }
        }

        // 회전된 위치에서 인덱스 찾기
        for (int i = 0; i < rotatedPositions.length; i++) {
            if (rotatedPositions[i][0] == newRow && rotatedPositions[i][1] == newCol) {
                return i;
            }
        }

        // 없으면 첫 블록
        return 0;
    }

    // 회전 후 lineClearBlock 인덱스 계산
    private int calculateRotatedLineClearBlockIndex(boolean clockwise) {
        if (!isItemPiece || kind == null || itemType != ItemType.LINE_CLEAR) {
            return -1;
        }

        // 현재 lineClear 위치
        int originalRow = lineClearBlockRow;
        int originalCol = lineClearBlockCol;

        // 회전된 블록 위치 계산
        Tetromino rotatedTetromino;
        if (clockwise) {
            rotatedTetromino = new Tetromino(kind, (rotation + 1) % 4);
        } else {
            rotatedTetromino = new Tetromino(kind, (rotation + 3) % 4);
        }

        // 회전 변환 적용
        int newRow, newCol;

        // 종류별 회전 공식
        if (kind == Kind.O) {
            // O미노: 블록 고정, L 이동
            // 실제 블록: (0,1), (0,2), (1,1), (1,2)
            if (clockwise) {
                // 시계방향 L 이동
                if (originalRow == 0 && originalCol == 1) {
                    newRow = 0;
                    newCol = 2;
                } else if (originalRow == 0 && originalCol == 2) {
                    newRow = 1;
                    newCol = 2;
                } else if (originalRow == 1 && originalCol == 2) {
                    newRow = 1;
                    newCol = 1;
                } else if (originalRow == 1 && originalCol == 1) {
                    newRow = 0;
                    newCol = 1;
                } else {
                    // 기본값
                    newRow = 0;
                    newCol = 1;
                }
            } else {
                // 반시계방향 L 이동
                if (originalRow == 0 && originalCol == 1) {
                    newRow = 1;
                    newCol = 1;
                } else if (originalRow == 1 && originalCol == 1) {
                    newRow = 1;
                    newCol = 2;
                } else if (originalRow == 1 && originalCol == 2) {
                    newRow = 0;
                    newCol = 2;
                } else if (originalRow == 0 && originalCol == 2) {
                    newRow = 0;
                    newCol = 1;
                } else {
                    // 기본값
                    newRow = 0;
                    newCol = 1;
                }
            }

            // O미노 블록 고정
            if (newRow == 0 && newCol == 1) {
                return 0; // 첫 번째 블록
            } else if (newRow == 0 && newCol == 2) {
                return 1; // 두 번째 블록
            } else if (newRow == 1 && newCol == 1) {
                return 2; // 세 번째 블록
            } else if (newRow == 1 && newCol == 2) {
                return 3; // 네 번째 블록
            } else {
                return 0; // 기본값
            }
        }

        int[][] rotatedPositions = rotatedTetromino.getBlockPositions();

        if (kind == Kind.I) {
            // I미노 4x4 공식
            if (clockwise) {
                // 시계방향 공식
                newRow = originalCol;
                newCol = 3 - originalRow;
            } else {
                // 반시계방향 공식
                newRow = 3 - originalCol;
                newCol = originalRow;
            }
        } else {
            // 3x3 미노 회전 공식
            if (clockwise) {
                // 시계방향 공식
                newRow = originalCol;
                newCol = 2 - originalRow;
            } else {
                // 반시계방향 공식
                newRow = 2 - originalCol;
                newCol = originalRow;
            }
        }

        // 회전된 위치에서 인덱스 찾기
        for (int i = 0; i < rotatedPositions.length; i++) {
            if (rotatedPositions[i][0] == newRow && rotatedPositions[i][1] == newCol) {
                return i;
            }
        }

        // 없으면 첫 블록
        return 0;
    }

    // 현재 블록 좌표 반환 (아이템 처리용)
    public int[][] getBlockPositions() {
        int[][] currentShape = getShape();
        java.util.List<int[]> positions = new java.util.ArrayList<>();

        for (int r = 0; r < currentShape.length; r++) {
            for (int c = 0; c < currentShape[r].length; c++) {
                if (currentShape[r][c] != 0) {
                    positions.add(new int[] { r, c });
                }
            }
        }

        return positions.toArray(new int[positions.size()][]);
    }
}

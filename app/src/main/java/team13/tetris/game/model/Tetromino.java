package team13.tetris.game.model;

import java.util.Arrays;

// 테트로미노 클래스
// 일반/색맹 테마 전환은 CSS 파일 교체만으로 처리 가능
public class Tetromino {

    // 아이템 타입 정의
    public enum ItemType {
        COPY, // 미노 복사 (C 마크)
        WEIGHT, // 무게추 (W 마크)
        GRAVITY, // 중력 (G 마크)
        SPLIT, // 분할 (S 마크)
        LINE_CLEAR // 라인클리어 (L 마크)
    }

    public enum Kind {
        I(
                new int[][][] {
                    {{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}},
                    {{0, 0, 0, 0}, {0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}}
                },
                1,
                "block-I",
                "tetris-i-text"),

        O(
                new int[][][] {
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
                },
                2,
                "block-O",
                "tetris-o-text"),

        T(
                new int[][][] {
                    {{0, 1, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {0, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}},
                    {{0, 0, 0, 0}, {1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {1, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}}
                },
                3,
                "block-T",
                "tetris-t-text"),

        S(
                new int[][][] {
                    {{0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 0}},
                    {{0, 0, 0, 0}, {0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}},
                    {{1, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}}
                },
                4,
                "block-S",
                "tetris-s-text"),

        Z(
                new int[][][] {
                    {{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 0, 1, 0}, {0, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}},
                    {{0, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0}}
                },
                5,
                "block-Z",
                "tetris-z-text"),

        J(
                new int[][][] {
                    {{1, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}},
                    {{0, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {0, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}}
                },
                6,
                "block-J",
                "tetris-j-text"),

        L(
                new int[][][] {
                    {{0, 0, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}},
                    {{0, 0, 0, 0}, {1, 1, 1, 0}, {1, 0, 0, 0}, {0, 0, 0, 0}},
                    {{1, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}}
                },
                7,
                "block-L",
                "tetris-l-text"),

        WEIGHT(
                new int[][][] {
                    {{0, 1, 1, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}}
                },
                8,
                "block-weight",
                "tetris-weight-text"),

        COPY(
                new int[][][] {
                    {{1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
                },
                9,
                "block-copy",
                "tetris-copy-text"),

        GRAVITY(
                new int[][][] {
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                    {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
                },
                10,
                "block-gravity",
                "tetris-gravity-text"),

        SPLIT(
                new int[][][] {
                    {{1, 1, 1, 0}, {1, 1, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}},
                    {{1, 1, 1, 0}, {1, 1, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}},
                    {{1, 1, 1, 0}, {1, 1, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}},
                    {{1, 1, 1, 0}, {1, 1, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}}
                },
                11,
                "block-split",
                "tetris-split-text");

        private final int[][][] rotations;
        private final int id;
        private final String blockStyleClass; // 도형/영역용 CSS 클래스명
        private final String textStyleClass; // 텍스트(Label 등)용 CSS 클래스명

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

    private final Kind kind; // raw shape로 생성되면 null
    private final int rotation; // kind != null일 때의 회전 인덱스 (0~3)
    private final int[][] shape; // raw shape용 저장소
    private final int id; // 고유 id (1~7)

    // 아이템 관련 필드
    private final boolean isItemPiece; // 아이템 미노인지 여부
    private final ItemType itemType; // 아이템 타입 (COPY, WEIGHT 등)
    private final int copyBlockIndex; // C 표시가 있는 블록의 인덱스 (-1이면 없음)
    private final int copyBlockRow; // C 표시 블록의 상대적 행 위치
    private final int copyBlockCol; // C 표시 블록의 상대적 열 위치

    // LINE_CLEAR 아이템 전용 필드 (COPY와 완전히 분리)
    private final int lineClearBlockIndex; // L 표시가 있는 블록의 인덱스 (-1이면 없음)
    private final int lineClearBlockRow; // L 표시 블록의 상대적 행 위치
    private final int lineClearBlockCol; // L 표시 블록의 상대적 열 위치

    private final boolean canRotate; // 회전 가능 여부 (무게추는 false)
    private boolean isLocked; // 이동 잠금 상태 (무게추가 착지했을 때 true)

    private static final int NO_MARK = -1;

    // 아이템 미노 생성자 (통합)
    public Tetromino(
            Kind kind,
            int rotation,
            boolean isItemPiece,
            ItemType itemType,
            int itemBlockIndex, // COPY/LNIE_CLEAR 공통 마커 인덱스
            boolean canRotate,
            boolean isLocked) {

        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
        this.isItemPiece = isItemPiece;
        this.itemType = itemType;
        this.canRotate = canRotate;
        this.isLocked = isLocked;

        int[][] currentShape = kind.getRotation(this.rotation);

        // itemBlockIndex가 유효하면 그 인덱스에 해당하는 (row, col)를 찾음
        int[] pos =
                (itemBlockIndex >= 0)
                        ? findBlockPositionByIndex(currentShape, itemBlockIndex)
                        : new int[] {NO_MARK, NO_MARK};

        if (isItemPiece && itemType == ItemType.LINE_CLEAR) {
            // LINE_CLEAR 전용 필드 세팅
            this.lineClearBlockIndex = itemBlockIndex;
            this.lineClearBlockRow = pos[0];
            this.lineClearBlockCol = pos[1];

            this.copyBlockIndex = NO_MARK;
            this.copyBlockRow = NO_MARK;
            this.copyBlockCol = NO_MARK;
        } else if (isItemPiece) {
            // COPY/WEIGHT/GRAVITY/SPLIT 전용 필드 세팅
            this.copyBlockIndex = itemBlockIndex;
            this.copyBlockRow = pos[0];
            this.copyBlockCol = pos[1];

            this.lineClearBlockIndex = NO_MARK;
            this.lineClearBlockRow = NO_MARK;
            this.lineClearBlockCol = NO_MARK;
        } else {
            // 일반 미노
            this.copyBlockIndex = NO_MARK;
            this.copyBlockRow = NO_MARK;
            this.copyBlockCol = NO_MARK;
            this.lineClearBlockIndex = NO_MARK;
            this.lineClearBlockRow = NO_MARK;
            this.lineClearBlockCol = NO_MARK;
        }
    }

    // 주어진 모양에서 targetIndex에 해당하는 블록의 (row, col) 위치를 반환
    private static int[] findBlockPositionByIndex(int[][] shape, int targetIndex) {
        int count = 0;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    if (count == targetIndex) return new int[] {r, c};
                    count++;
                }
            }
        }
        return new int[] {NO_MARK, NO_MARK};
    }

    public Tetromino(Kind kind) {
        this(kind, 0);
    }

    // 기본 미노 생성자
    public Tetromino(Kind kind, int rotation) {
        this(kind, rotation, false, null, NO_MARK, true, false);
    }

    // 아이템 미노 생성자 (통합)
    public static Tetromino item(Kind kind, int rotation, ItemType type, int itemBlockIndex) {
        boolean canRotate = (kind != Kind.WEIGHT && kind != Kind.GRAVITY && kind != Kind.SPLIT);
        return new Tetromino(kind, rotation, true, type, itemBlockIndex, canRotate, false);
    }

    // LINE_CLEAR 아이템 미노 생성자
    public static Tetromino lineClearItem(Kind kind, int rotation, int index) {
        return new Tetromino(kind, rotation, true, ItemType.LINE_CLEAR, index, true, false);
    }

    // 레거시 호환(id + raw shape로 생성)
    public Tetromino(int id, int[][] shape) {
        this.kind = null;
        this.rotation = 0;
        this.id = id;
        this.shape = new int[shape.length][];
        for (int i = 0; i < shape.length; i++)
            this.shape[i] = Arrays.copyOf(shape[i], shape[i].length);

        // 아이템 관련은 전부 미사용
        this.isItemPiece = false;
        this.itemType = null;
        this.canRotate = true;
        this.isLocked = false;
        this.copyBlockIndex = NO_MARK;
        this.copyBlockRow = NO_MARK;
        this.copyBlockCol = NO_MARK;
        this.lineClearBlockIndex = NO_MARK;
        this.lineClearBlockRow = NO_MARK;
        this.lineClearBlockCol = NO_MARK;
    }

    public int getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    // 현재 회전 인덱스 반환 (네트워크 전송용)
    public int getRotationIndex() {
        return rotation;
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

    // LINE_CLEAR 아이템 전용 getter 메소드들 (COPY와 분리)
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
        for (int i = 0; i < src.length; i++) copy[i] = Arrays.copyOf(src[i], src[i].length);
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
            int nextRotation = (rotation + 1) % 4;
            // 아이템 미노인 경우 회전 후 새로운 블록 인덱스 계산
            if (isItemPiece) {
                // LINE_CLEAR 아이템인 경우 lineClearBlockIndex를 사용
                if (itemType == ItemType.LINE_CLEAR) {
                    int newLineClearBlockIndex = calculateRotatedLineClearBlockIndex();
                    // 예전 코드 - 삭제 예정
                    // return new Tetromino(kind, (rotation + 1) % 4,
                    //         newLineClearBlockIndex, ItemType.LINE_CLEAR);
                    return Tetromino.lineClearItem(kind, nextRotation, newLineClearBlockIndex);

                } else {
                    // 다른 아이템들은 copyBlockIndex 사용
                    int newCopyBlockIndex = calculateRotatedCopyBlockIndex();
                    // 예전 코드 - 삭제 예정
                    // return new Tetromino(kind, (rotation + 1) % 4, newCopyBlockIndex);
                    return Tetromino.item(kind, nextRotation, itemType, newCopyBlockIndex);
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

    // 편의 팩토리
    public static Tetromino of(Kind k) {
        return new Tetromino(k, 0);
    }

    // 이 테트로미노의 Kind를 반환. raw shape로 생성된 경우 null을 반환할 수 있음.
    public Kind getKind() {
        return kind;
    }

    // 뷰(노드)가 붙일 블록용 CSS 클래스명 (예: "block-L")
    public String getBlockStyleClass() {
        if (kind != null) return kind.getBlockStyleClass();
        return blockClassForId(id);
    }

    // 텍스트 라벨에 붙일 CSS 클래스명 (예: "tetris-l-text")
    public String getTextStyleClass() {
        if (kind != null) return kind.getTextStyleClass();
        return textClassForId(id);
    }

    // id -> Kind 조회 (레거시/raw shape 지원용)
    public static Kind kindForId(int id) {
        for (Kind k : Kind.values()) if (k.getId() == id) return k;
        return null;
    }

    // 내부 유틸: raw 생성자 호환용 매핑
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

    // 아이템 관련 메서드들
    public boolean isItemPiece() {
        return isItemPiece;
    }

    public int getCopyBlockIndex() {
        return copyBlockIndex;
    }

    // 공통: (row, col) → 회전 후 blockIndex
    private int calculateRotatedItemBlockIndex(int originalRow, int originalCol) {
        if (kind == null) return -1;

        int nextRotation = (rotation + 1) % 4;

        // O 미노 특수 케이스
        if (kind == Kind.O) {
            int[] pos = rotateOBlockPositionClockwise(originalRow, originalCol);
            int[][] shapeNext = kind.getRotation(nextRotation);
            return findBlockIndexByPosition(shapeNext, pos[0], pos[1]);
        }

        // 일반 회전 공식
        int newRow, newCol;
        if (kind == Kind.I) {
            // 4x4: (r, c) -> (c, 3-r)
            newRow = originalCol;
            newCol = 3 - originalRow;
        } else {
            // 3x3: (r, c) -> (c, 2-r)
            newRow = originalCol;
            newCol = 2 - originalRow;
        }

        int[][] shapeNext = kind.getRotation(nextRotation);
        return findBlockIndexByPosition(shapeNext, newRow, newCol);
    }

    // O 미노용 회전 사이클
    private int[] rotateOBlockPositionClockwise(int row, int col) {
        int[][] cycle = {
            {0, 1}, // 0
            {0, 2}, // 1
            {1, 2}, // 2
            {1, 1} // 3
        };
        int idx = 0;
        for (int i = 0; i < cycle.length; i++) {
            if (cycle[i][0] == row && cycle[i][1] == col) {
                idx = i;
                break;
            }
        }
        int nextIdx = (idx + 1) % cycle.length;
        return cycle[nextIdx];
    }

    // 공통: (row, col)에 해당하는 blockIndex 찾기
    private static int findBlockIndexByPosition(int[][] shape, int row, int col) {
        int count = 0;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    if (r == row && c == col) return count;
                    count++;
                }
            }
        }
        return 0; // fallback
    }

    // COPY 아이템 전용: 회전 후 copyBlock의 새로운 인덱스를 계산
    private int calculateRotatedCopyBlockIndex() {
        if (!isItemPiece || kind == null) return -1;
        return calculateRotatedItemBlockIndex(copyBlockRow, copyBlockCol);
    }

    // LINE_CLEAR 아이템 전용: 회전 후 lineClearBlock의 새로운 인덱스를 계산
    private int calculateRotatedLineClearBlockIndex() {
        if (!isItemPiece || kind == null || itemType != ItemType.LINE_CLEAR) return -1;
        return calculateRotatedItemBlockIndex(lineClearBlockRow, lineClearBlockCol);
    }

    // 현재 미노의 블록 위치들을 반환 (아이템 효과 처리용)
    // return 값: 블록이 있는 위치들의 배열 [행, 열]
    public int[][] getBlockPositions() {
        int[][] currentShape = getShape();
        java.util.List<int[]> positions = new java.util.ArrayList<>();

        for (int r = 0; r < currentShape.length; r++) {
            for (int c = 0; c < currentShape[r].length; c++) {
                if (currentShape[r][c] != 0) positions.add(new int[] {r, c});
            }
        }

        return positions.toArray(new int[positions.size()][]);
    }
}

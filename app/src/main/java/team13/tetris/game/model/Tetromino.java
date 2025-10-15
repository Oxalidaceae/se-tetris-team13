package team13.tetris.game.model;

import java.util.Arrays;

/**
 * 테트로미노 래퍼 클래스.
 * - 색상은 문자열/인라인 스타일이 아닌 "CSS 클래스명"으로만 노출한다.
 *   * 블록(도형/영역)용: block-I, block-O, ... (예: Rectangle/Panes)
 *   * 텍스트용: tetris-i-text, tetris-o-text, ... (예: Label)
 * - 일반/색맹 테마 전환은 CSS 파일 교체만으로 처리 가능.
 */
public class Tetromino {

    public enum Kind {
        I(new int[][][]{
                {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}},
                {{0,0,1,0},{0,0,1,0},{0,0,1,0},{0,0,1,0}},
                {{0,0,0,0},{0,0,0,0},{1,1,1,1},{0,0,0,0}},
                {{0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0}}
        }, 1, "block-I", "tetris-i-text"),

        O(new int[][][]{
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}
        }, 2, "block-O", "tetris-o-text"),

        T(new int[][][]{
                {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,1,0},{0,1,0,0},{0,0,0,0}},
                {{0,1,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}
        }, 3, "block-T", "tetris-t-text"),

        S(new int[][][]{
                {{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,1,0},{0,0,1,0},{0,0,0,0}},
                {{0,0,0,0},{0,1,1,0},{1,1,0,0},{0,0,0,0}},
                {{1,0,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}
        }, 4, "block-S", "tetris-s-text"),

        Z(new int[][][]{
                {{1,1,0,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,0,1,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,0,0},{0,1,1,0},{0,0,0,0}},
                {{0,1,0,0},{1,1,0,0},{1,0,0,0},{0,0,0,0}}
        }, 5, "block-Z", "tetris-z-text"),

        J(new int[][][]{
                {{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,1,0},{0,0,1,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,0,0},{1,1,0,0},{0,0,0,0}}
        }, 6, "block-J", "tetris-j-text"),

        L(new int[][][]{
                {{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,0,0},{0,1,1,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,1,0},{1,0,0,0},{0,0,0,0}},
                {{1,1,0,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}}
        }, 7, "block-L", "tetris-l-text");

        private final int[][][] rotations;
        private final int id;
        private final String blockStyleClass; // 도형/영역용 CSS 클래스명
        private final String textStyleClass;  // 텍스트(Label 등)용 CSS 클래스명

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

    private final Kind kind;     // raw shape로 생성되면 null
    private final int rotation;  // kind != null일 때의 회전 인덱스 (0~3)
    private final int[][] shape; // raw shape용 저장소
    private final int id;        // 고유 id (1~7)

    // 미리 정의된 Kind로 생성
    public Tetromino(Kind kind) {
        this(kind, 0);
    }

    public Tetromino(Kind kind, int rotation) {
        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
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
    }

    public int getId() {
        return id;
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
            return new Tetromino(kind, (rotation + 1) % 4);
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
            return new Tetromino(kind, (rotation + 3) % 4);
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

    /** 뷰(노드)가 붙일 블록용 CSS 클래스명 (예: "block-L") */
    public String getBlockStyleClass() {
        if (kind != null) return kind.getBlockStyleClass();
        return blockClassForId(id);
    }

    /** 텍스트 라벨에 붙일 CSS 클래스명 (예: "tetris-l-text") */
    public String getTextStyleClass() {
        if (kind != null) return kind.getTextStyleClass();
        return textClassForId(id);
    }

    /** id -> Kind 조회 (레거시/raw shape 지원용) */
    public static Kind kindForId(int id) {
        for (Kind k : Kind.values()) {
            if (k.getId() == id) return k;
        }
        return null;
    }

    /* ===== 내부 유틸: raw 생성자 호환용 매핑 ===== */

    private static String blockClassForId(int id) {
        switch (id) {
            case 1: return "block-I";
            case 2: return "block-O";
            case 3: return "block-T";
            case 4: return "block-S";
            case 5: return "block-Z";
            case 6: return "block-J";
            case 7: return "block-L";
            default: return "block";
        }
    }

    private static String textClassForId(int id) {
        switch (id) {
            case 1: return "tetris-i-text";
            case 2: return "tetris-o-text";
            case 3: return "tetris-t-text";
            case 4: return "tetris-s-text";
            case 5: return "tetris-z-text";
            case 6: return "tetris-j-text";
            case 7: return "tetris-l-text";
            default: return "tetris-generic-text";
        }
    }
}

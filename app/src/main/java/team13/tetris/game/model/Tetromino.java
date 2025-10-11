package team13.tetris.game.model;

import java.util.Arrays;

/**
 * 테트로미노 래퍼 클래스.
 * 미리 정의된 회전 상태를 가진 `Kind`로 생성하거나,
 * 이전 코드와의 호환을 위해 raw id+shape로 생성할 수 있습니다.
 */
public class Tetromino {
    public enum Kind {
    I(new int[][][]{
                {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}},
                {{0,0,1,0},{0,0,1,0},{0,0,1,0},{0,0,1,0}},
                {{0,0,0,0},{0,0,0,0},{1,1,1,1},{0,0,0,0}},
                {{0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0}}
    }, 1, "cyan"),
    O(new int[][][]{
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}
    }, 2, "yellow"),
    T(new int[][][]{
                {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,1,0},{0,1,0,0},{0,0,0,0}},
                {{0,1,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}
    }, 3, "purple"),
    S(new int[][][]{
                {{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,1,0},{0,0,1,0},{0,0,0,0}},
                {{0,0,0,0},{0,1,1,0},{1,1,0,0},{0,0,0,0}},
                {{1,0,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}
    }, 4, "green"),
    Z(new int[][][]{
                {{1,1,0,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,0,1,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,0,0},{0,1,1,0},{0,0,0,0}},
                {{0,1,0,0},{1,1,0,0},{1,0,0,0},{0,0,0,0}}
    }, 5, "red"),
    J(new int[][][]{
                {{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,1,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,1,0},{0,0,1,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,0,0},{1,1,0,0},{0,0,0,0}}
    }, 6, "blue"),
    L(new int[][][]{
                {{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
                {{0,1,0,0},{0,1,0,0},{0,1,1,0},{0,0,0,0}},
                {{0,0,0,0},{1,1,1,0},{1,0,0,0},{0,0,0,0}},
                {{1,1,0,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}}
    }, 7, "orange");

    private final int[][][] rotations;
    private final int id;
    private final String colorCss; // 이 미노 종류에 대한 CSS 색상 문자열

        Kind(int[][][] rotations, int id, String colorCss) {
            this.rotations = rotations;
            this.id = id;
            this.colorCss = colorCss;
        }

        public int[][] getRotation(int idx) {
            return rotations[idx % rotations.length];
        }

        public int getId() { return id; }
        public String getColorCss() { return colorCss; }
    }

    private final Kind kind; // raw shape로 생성된 경우 null
    private final int rotation; // kind != null일 때의 회전 인덱스
    private final int[][] shape; // raw shape를 위한 배열
    private final int id;
    
    // 미리 정의된 Kind로 생성 (회전 인덱스는 0부터 시작)
    public Tetromino(Kind kind) { this(kind, 0); }

    public Tetromino(Kind kind, int rotation) {
        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
    }

    // 레거시 호환용 생성자: id와 raw shape로 생성
    public Tetromino(int id, int[][] shape) {
        this.kind = null;
        this.rotation = 0;
        this.id = id;
        this.shape = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) this.shape[i] = Arrays.copyOf(shape[i], shape[i].length);
    }

    public int getId() { return id; }

    public int[][] getShape() {
        int[][] src = (kind != null) ? kind.getRotation(rotation) : shape;
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) copy[i] = Arrays.copyOf(src[i], src[i].length);
        return copy;
    }

    public int getHeight() { return getShape().length; }
    public int getWidth() { return getShape()[0].length; }

    public Tetromino rotateClockwise() {
        if (kind != null) return new Tetromino(kind, (rotation + 1) % 4);
        // raw shape를 시계 방향으로 회전
        int[][] s = getShape();
        int h = s.length, w = s[0].length;
        int[][] out = new int[w][h];
        for (int r = 0; r < h; r++) for (int c = 0; c < w; c++) out[c][h - 1 - r] = s[r][c];
        return new Tetromino(id, out);
    }

    public Tetromino rotateCounter() {
        if (kind != null) return new Tetromino(kind, (rotation + 3) % 4);
        int[][] s = getShape();
        int h = s.length, w = s[0].length;
        int[][] out = new int[w][h];
        for (int r = 0; r < h; r++) for (int c = 0; c < w; c++) out[w - 1 - c][r] = s[r][c];
        return new Tetromino(id, out);
    }

    // 편의 팩토리 메서드
    public static Tetromino of(Kind k) { return new Tetromino(k, 0); }

    public String getColorCss() {
        if (kind != null) return kind.getColorCss();
        return "white";
    }

    /**
     * id 값을 통해 Kind를 조회합니다. 찾지 못하면 null을 반환합니다.
     */
    public static Kind kindForId(int id) {
        for (Kind k : Kind.values()) {
            if (k.getId() == id) return k;
        }
        return null;
    }
}

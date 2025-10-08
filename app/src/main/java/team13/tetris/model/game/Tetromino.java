package team13.tetris.model.game;

import java.util.Arrays;

/**
 * ?뚰듃濡쒕????섑띁 ?대옒??
 * 誘몃━ ?뺤쓽???뚯쟾 ?곹깭瑜?媛吏?`Kind`濡??앹꽦?섍굅??
 * ?댁쟾 肄붾뱶????명솚???꾪빐 raw id+shape濡??앹꽦?????덉뒿?덈떎.
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
    private final String colorCss; // ??誘몃끂 醫낅쪟?????CSS ?됱긽 臾몄옄??

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

    private final Kind kind; // raw shape濡??앹꽦??寃쎌슦 null
    private final int rotation; // kind != null???뚯쓽 ?뚯쟾 ?몃뜳??
    private final int[][] shape; // raw shape瑜??꾪븳 諛곗뿴
    private final int id;
    
    // 誘몃━ ?뺤쓽??Kind濡??앹꽦 (?뚯쟾 ?몃뜳?ㅻ뒗 0遺???쒖옉)
    public Tetromino(Kind kind) { this(kind, 0); }

    public Tetromino(Kind kind, int rotation) {
        this.kind = kind;
        this.rotation = rotation % 4;
        this.shape = null;
        this.id = kind.getId();
    }

    // ?덇굅???명솚???앹꽦?? id? raw shape濡??앹꽦
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
        // raw shape瑜??쒓퀎 諛⑺뼢?쇰줈 ?뚯쟾
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

    // ?몄쓽 ?⑺넗由?硫붿꽌??
    public static Tetromino of(Kind k) { return new Tetromino(k, 0); }

    public String getColorCss() {
        if (kind != null) return kind.getColorCss();
        return "white";
    }

    /**
     * id 媛믪쓣 ?듯빐 Kind瑜?議고쉶?⑸땲?? 李얠? 紐삵븯硫?null??諛섑솚?⑸땲??
     */
    public static Kind kindForId(int id) {
        for (Kind k : Kind.values()) {
            if (k.getId() == id) return k;
        }
        return null;
    }
}
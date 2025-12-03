package team13.tetris.scenes;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import team13.tetris.config.Settings;

// 테트리스 게임 셀을 표시하는 UI 컴포넌트
// Rectangle, Canvas(패턴용), Label(텍스트용)로 구성됨
public class CellView extends StackPane {
    private final Rectangle rect;
    private final Canvas patternCanvas;
    private final Label label;
    private final Settings settings;
    private String currentPattern = null;

    public CellView(double size, Settings settings) {
        this.settings = settings;

        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);

        setAlignment(Pos.CENTER);
        getStyleClass().add("cell");

        rect = new Rectangle(size, size);
        rect.getStyleClass().add("cell-rect");
        rect.setStrokeWidth(0);
        rect.widthProperty().bind(widthProperty());
        rect.heightProperty().bind(heightProperty());

        patternCanvas = new Canvas(size, size);
        patternCanvas.widthProperty().bind(widthProperty());
        patternCanvas.heightProperty().bind(heightProperty());
        patternCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawPattern());
        patternCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawPattern());

        label = new Label(" ");
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.getStyleClass().add("cell-text");

        getChildren().addAll(rect, patternCanvas, label);

        setEmpty();
    }

    private void redrawPattern() {
        if (currentPattern == null || currentPattern.equals("none")) {
            clearCanvas();
            return;
        }

        double w = patternCanvas.getWidth();
        double h = patternCanvas.getHeight();
        GraphicsContext gc = patternCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setStroke(Color.rgb(0, 0, 0, 0.7));
        gc.setLineWidth(1);

        switch (currentPattern) {
            case "horizontal": // S - 수평 줄무늬
                for (double y = 0; y < h; y += 5) gc.strokeLine(0, y, w, y);
                break;
            case "vertical": // J - 수직 줄무늬
                for (double x = 0; x < w; x += 5) gc.strokeLine(x, 0, x, h);
                break;
            case "diagonal-right": // I - 빗살무늬 ↗
                for (double offset = -h; offset < w + h; offset += 5)
                    gc.strokeLine(offset, h, offset + h, 0);
                break;
            case "diagonal-left": // T - 빗살무늬 ↖
                for (double offset = -h; offset < w + h; offset += 5)
                    gc.strokeLine(offset, 0, offset + h, h);
                break;
            case "diagonal-right-wide": // Z - 빗살무늬 ↗ (넓은 간격)
                for (double offset = -h; offset < w + h; offset += 7)
                    gc.strokeLine(offset, h, offset + h, 0);
                break;
            case "diagonal-left-wide": // L - 빗살무늬 ↖ (넓은 간격)
                for (double offset = -h; offset < w + h; offset += 7)
                    gc.strokeLine(offset, 0, offset + h, h);
                break;
        }
    }

    private void clearCanvas() {
        GraphicsContext gc = patternCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, patternCanvas.getWidth(), patternCanvas.getHeight());
    }

    private void clearDynamicStyles() {
        ObservableList<String> rectClasses = rect.getStyleClass();
        rectClasses.removeIf(
                name ->
                        name.startsWith("block-")
                                || name.startsWith("item-")
                                || name.equals("cell-empty")
                                || name.equals("cell-border"));

        ObservableList<String> labelClasses = label.getStyleClass();
        labelClasses.removeIf(
                name ->
                        name.startsWith("tetris-")
                                || name.startsWith("item-")
                                || name.equals("cell-empty")
                                || name.equals("cell-border"));
    }

    public void setEmpty() {
        clearDynamicStyles();
        currentPattern = null;
        clearCanvas();
        if (!rect.getStyleClass().contains("cell-empty")) rect.getStyleClass().add("cell-empty");
        if (!label.getStyleClass().contains("cell-empty")) label.getStyleClass().add("cell-empty");
        label.setText(" ");
    }

    public void setBorder() {
        clearDynamicStyles();
        currentPattern = null;
        clearCanvas();
        if (!rect.getStyleClass().contains("cell-border")) rect.getStyleClass().add("cell-border");
        if (!label.getStyleClass().contains("cell-border"))
            label.getStyleClass().add("cell-border");
        // label.setText("X");
    }

    public void setBlock(String symbol, String blockClass, String textClass) {
        clearDynamicStyles();
        if (blockClass != null
                && !blockClass.isBlank()
                && !rect.getStyleClass().contains(blockClass)) rect.getStyleClass().add(blockClass);
        if (textClass != null && !textClass.isBlank() && !label.getStyleClass().contains(textClass))
            label.getStyleClass().add(textClass);

        // 색맹 모드에서는 아이템 블록(C, L, W, G, S)만 글자 표시, 일반 블록은 패턴만
        boolean isItemBlock =
                symbol != null
                        && (symbol.equals("C")
                                || symbol.equals("L")
                                || symbol.equals("W")
                                || symbol.equals("G")
                                || symbol.equals("S"));
        boolean isGhostBlock = "block-ghost".equals(blockClass);

        if ((settings.isColorBlindMode() && !isItemBlock) || isGhostBlock) {
            label.setText(" "); // 일반 블록은 색맹모드에서 글자 숨김, 고스트 블록은 항상 글자 숨김
        } else {
            label.setText(symbol == null ? "" : symbol); // 아이템 블록은 글자 표시
        }

        // 색맹 모드에서 패턴 적용
        if (blockClass != null && settings.isColorBlindMode()) {
            applyPattern(blockClass);
        } else {
            currentPattern = null;
            clearCanvas();
        }
    }

    private void applyPattern(String blockClass) {
        switch (blockClass) {
            case "block-I":
                currentPattern = "diagonal-right";
                break;
            case "block-O":
                currentPattern = "none";
                break;
            case "block-T":
                currentPattern = "diagonal-left";
                break;
            case "block-S":
                currentPattern = "horizontal";
                break;
            case "block-Z":
                currentPattern = "diagonal-right-wide";
                break;
            case "block-J":
                currentPattern = "vertical";
                break;
            case "block-L":
                currentPattern = "diagonal-left-wide";
                break;
            default:
                currentPattern = null;
        }
        redrawPattern();
    }
}

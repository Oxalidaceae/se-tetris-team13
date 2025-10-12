package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

/**
 * 테트리스 게임의 메인 화면을 담당하는 View 클래스 (MVC 패턴의 View 계층)
 * 
 * 주요 기능:
 * - 텍스트 기반 테트리스 보드 렌더링 ('X' 테두리, 'O' 블록)
 * - GridPane + Label 구조로 각 셀의 개별 색상 적용
 * - 다음 미노 미리보기 (4x4 GridPane)
 * - 점수 표시 및 게임 오버 화면
 * 
 * 설계 원칙:
 * - 순수한 UI 렌더링만 담당 (비즈니스 로직 제외)
 * - 게임 상태 변화는 GameSceneController에서 처리
 * - Platform.runLater()를 통한 JavaFX 스레드 안전성 보장
 * 
 * @author Team13
 * @version 1.0
 * @since 2025-10-12
 */
public class GameScene {
    private final Settings settings;
    private GameEngine engine;

    private final HBox root;
    private Scene scene;
    private final GridPane boardGrid;
    private final GridPane previewGrid;
    private final Label scoreLabel;

    public GameScene(Settings settings, GameEngine engine) {
        this.settings = settings;
        this.engine = engine;

        root = new HBox(12);

        Board board = engine.getBoard();
        int w = board.getWidth();
        int h = board.getHeight();

        // 플레이 가능한 영역 주위에 1셀 테두리를 만들어 사용자의 요청대로 'X' 문자를 테두리로 표시합니다.
        boardGrid = new GridPane();
        boardGrid.setStyle("-fx-background-color: black; -fx-padding: 6;");

        // 그리드 크기 = (w + 2) x (h + 2)
        for (int gy = 0; gy < h + 2; gy++) {
            for (int gx = 0; gx < w + 2; gx++) {
                Label cell = makeCellLabel();
                // border cells
                if (gx == 0 || gx == w + 1 || gy == 0 || gy == h + 1) {
                    cell.setText("X");
                    cell.setStyle(
                            "-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                }
                boardGrid.add(cell, gx, gy);
            }
        }

        previewGrid = new GridPane();
        previewGrid.setStyle("-fx-background-color: black; -fx-padding: 6;");
        for (int r = 0; r < 4; r++)
            for (int c = 0; c < 4; c++)
                previewGrid.add(makeCellLabel(), c, r);

        scoreLabel = new Label("Score:\n0");
        scoreLabel.setFont(Font.font("Monospaced", 14));
        scoreLabel.setStyle("-fx-text-fill: darkred; -fx-padding: 8;");

        VBox right = new VBox(8, previewGrid, scoreLabel);
        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        root.getChildren().addAll(boardGrid, right);

        // 초기 렌더링
        updateGrid();
    }

    /**
     * 개별 게임 보드 셀을 위한 Label 컴포넌트를 생성합니다.
     * 
     * @return 스타일이 적용된 Label 객체
     */
    private Label makeCellLabel() {
        Label lbl = new Label(" ");
        lbl.setMinSize(20, 16);
        lbl.setPrefSize(20, 16);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle(
                "-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
        return lbl;
    }

    /**
     * JavaFX Scene 객체를 생성하고 반환합니다.
     * 
     * @return 생성된 Scene 객체
     */
    public Scene createScene() {
        this.scene = new Scene(root);
        return scene;
    }

    /**
     * 현재 Scene 객체를 반환합니다.
     * 
     * @return 현재 Scene 객체 (null일 수 있음)
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * GameEngine 참조를 설정합니다.
     * 
     * @param engine 설정할 GameEngine 객체
     */
    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    /**
     * 키보드 입력을 받기 위해 Scene에 포커스를 요청합니다.
     * JavaFX UI 스레드에서 안전하게 실행됩니다.
     */
    public void requestFocus() {
        Platform.runLater(() -> {
            if (scene != null)
                scene.getRoot().requestFocus();
        });
    }

    // ========== UI 렌더링 메서드 ==========
    /**
     * 게임 보드와 UI를 업데이트합니다 (Controller에서 호출)
     */
    public void updateGrid() {
        if (engine == null)
            return; // null 체크 추가

        Board b = engine.getBoard();
        int w = b.getWidth();
        int h = b.getHeight();

        Platform.runLater(() -> {
            // paint static board cells (mapped to internal grid offset by +1,+1 because of
            // border)
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = b.getCell(x, y);
                    Label cell = (Label) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                    if (val == 0) {
                        cell.setText(" ");
                        cell.setStyle(
                                "-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                    } else {
                        Tetromino.Kind kind = Tetromino.kindForId(val);
                        String color = (kind != null) ? kind.getColorCss() : "white";
                        cell.setText("O");
                        // show colored character on black background (no grid lines)
                        cell.setStyle("-fx-background-color: black; -fx-text-fill: " + color
                                + "; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                    }
                }
            }

            // overlay current falling piece on top (drawn as O but not stored in board)
            Tetromino cur = engine.getCurrent();
            if (cur != null) {
                int[][] shape = cur.getShape();
                int px = engine.getPieceX();
                int py = engine.getPieceY();
                String color = cur.getColorCss();
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            int x = px + c;
                            int y = py + r;
                            if (x >= 0 && x < w && y >= 0 && y < h) {
                                // map into grid with +1 offset for border
                                Label cell = (Label) getNodeByRowColumnIndex(y + 1, x + 1, boardGrid);
                                cell.setText("O");
                                // falling piece: colored character (no grid lines)
                                cell.setStyle("-fx-background-color: black; -fx-text-fill: " + color
                                        + "; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                            }
                        }
                    }
                }
            }

            // preview
            for (int r = 0; r < 4; r++)
                for (int c = 0; c < 4; c++) {
                    Label cell = (Label) getNodeByRowColumnIndex(r, c, previewGrid);
                    cell.setText(" ");
                    cell.setStyle(
                            "-fx-background-color: black; -fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                }
            Tetromino next = engine.getNext();
            if (next != null) {
                int[][] s = next.getShape();
                String color = next.getColorCss();
                // center the 4x4 preview: shapes are already in 4x4 but just in case
                for (int r = 0; r < s.length && r < 4; r++)
                    for (int c = 0; c < s[r].length && c < 4; c++) {
                        if (s[r][c] != 0) {
                            Label cell = (Label) getNodeByRowColumnIndex(r, c, previewGrid);
                            cell.setText("O");
                            // preview: colored character on black (no grid lines)
                            cell.setStyle("-fx-background-color: black; -fx-text-fill: " + color
                                    + "; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
                        }
                    }
            }

            scoreLabel.setText("Score:\n" + engine.getScore());
        });
    }

    // helper to fetch node by row/col (GridPane stores col=x, row=y)
    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            int r = rowIndex == null ? 0 : rowIndex;
            int c = colIndex == null ? 0 : colIndex;
            if (r == row && c == column)
                return node;
        }
        return null;
    }

    /**
     * 게임 오버 상태를 화면에 표시합니다 (Controller에서 호출)
     */
    public void showGameOver() {
        Platform.runLater(() -> scoreLabel.setText("GAME OVER\n" + engine.getScore()));
    }
}
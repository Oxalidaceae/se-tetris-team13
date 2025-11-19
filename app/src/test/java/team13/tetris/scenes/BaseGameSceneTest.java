package team13.tetris.scenes;

import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.config.Settings;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for BaseGameScene
public class BaseGameSceneTest {
    
    private Settings mockSettings;
    private BaseGameScene baseGameScene;
    
    @BeforeAll
    static void initToolkit() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        mockSettings = mock(Settings.class);
        when(mockSettings.getWindowSize()).thenReturn("MEDIUM");
        when(mockSettings.isColorBlindMode()).thenReturn(false);
        
        // Concrete implementation for testing (BaseGameScene has no abstract methods)
        baseGameScene = new BaseGameScene(mockSettings) {};
    }
    
    @Test
    void testCreateBoardGrid() {
        Board mockBoard = mock(Board.class);
        when(mockBoard.getWidth()).thenReturn(10);
        when(mockBoard.getHeight()).thenReturn(20);
        
        GridPane grid = baseGameScene.createBoardGrid(mockBoard);
        
        assertNotNull(grid);
        assertEquals(264, grid.getChildren().size()); // (10+2) * (20+2) = 264
    }
    
    @Test
    void testCreatePreviewGrid() {
        GridPane grid = baseGameScene.createPreviewGrid();
        
        assertNotNull(grid);
        assertEquals(16, grid.getChildren().size()); // 4x4 = 16
    }
    
    @Test
    void testGetNodeByRowColumnIndex() {
        Board mockBoard = mock(Board.class);
        when(mockBoard.getWidth()).thenReturn(10);
        when(mockBoard.getHeight()).thenReturn(20);
        
        GridPane grid = baseGameScene.createBoardGrid(mockBoard);
        
        assertNotNull(baseGameScene.getNodeByRowColumnIndex(0, 0, grid));
        assertNotNull(baseGameScene.getNodeByRowColumnIndex(5, 5, grid));
    }
    
    @Test
    void testApplyCellEmpty() {
        CellView cell = new CellView(28.0, mockSettings);
        baseGameScene.applyCellEmpty(cell);
        // Verify no exception thrown
        assertNotNull(cell);
    }
    
    @Test
    void testApplyCellEmptyWithNull() {
        baseGameScene.applyCellEmpty(null);
        // Should not throw exception
    }
    
    @Test
    void testFillCell() {
        CellView cell = new CellView(28.0, mockSettings);
        baseGameScene.fillCell(cell, "O", "block-i", "tetris-i-text");
        assertNotNull(cell);
    }
    
    @Test
    void testFillCellWithNull() {
        baseGameScene.fillCell(null, "O", "block-i", "tetris-i-text");
        // Should not throw exception
    }
    
    @Test
    void testBlockClassForKind() {
        assertEquals("block-I", baseGameScene.blockClassForKind(Tetromino.Kind.I));
        assertEquals("block-O", baseGameScene.blockClassForKind(Tetromino.Kind.O));
        assertEquals("block-T", baseGameScene.blockClassForKind(Tetromino.Kind.T));
        assertEquals("block", baseGameScene.blockClassForKind(null));
    }
    
    @Test
    void testTextClassForKind() {
        assertEquals("tetris-i-text", baseGameScene.textClassForKind(Tetromino.Kind.I));
        assertEquals("tetris-o-text", baseGameScene.textClassForKind(Tetromino.Kind.O));
        assertEquals("tetris-t-text", baseGameScene.textClassForKind(Tetromino.Kind.T));
        assertEquals("tetris-generic-text", baseGameScene.textClassForKind(null));
    }
    
    @Test
    void testMakeCellView() {
        CellView cell = baseGameScene.makeCellView(28.0, false);
        assertNotNull(cell);
        
        CellView previewCell = baseGameScene.makeCellView(22.0, true);
        assertNotNull(previewCell);
    }
    
    @Test
    void testRenderGhostBlock() {
        Board mockBoard = mock(Board.class);
        when(mockBoard.getWidth()).thenReturn(10);
        when(mockBoard.getHeight()).thenReturn(20);
        
        GridPane grid = baseGameScene.createBoardGrid(mockBoard);
        
        int[][] shape = {{1, 1, 1, 1}}; // I-block shape
        
        // ghostY == py, should not render
        baseGameScene.renderGhostBlock(shape, 3, 5, 5, 10, 20, grid);
        
        // ghostY != py, should render
        baseGameScene.renderGhostBlock(shape, 3, 5, 15, 10, 20, grid);
        
        // ghostY == -1, should not render
        baseGameScene.renderGhostBlock(shape, 3, 5, -1, 10, 20, grid);
    }
}

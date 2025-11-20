package team13.tetris.scenes;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.config.Settings;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CellViewTest {
    
    private Settings mockSettings;
    private CellView cellView;
    
    @BeforeAll
    static void initToolkit() {
        // CI/CD 환경에서도 동작하도록 JavaFX 툴킷 초기화
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() throws Exception {
        mockSettings = mock(Settings.class);
        when(mockSettings.isColorBlindMode()).thenReturn(false);
        
        // JavaFX UI 생성은 FX Application Thread에서 실행
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            cellView = new CellView(28.0, mockSettings);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testConstructor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            CellView cell = new CellView(30.0, mockSettings);
            assertNotNull(cell);
            assertEquals(3, cell.getChildren().size()); // rect, canvas, label
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetEmpty() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            cellView.setEmpty();
            assertTrue(cellView.getChildren().get(2) instanceof javafx.scene.control.Label);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetBorder() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            cellView.setBorder();
            // Verify no exception thrown
            assertNotNull(cellView);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetBlockNormalMode() throws Exception {
        when(mockSettings.isColorBlindMode()).thenReturn(false);
        
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            cellView.setBlock("O", "block-I", "tetris-i-text");
            assertNotNull(cellView);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetBlockColorBlindMode() throws Exception {
        when(mockSettings.isColorBlindMode()).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            CellView cell = new CellView(28.0, mockSettings);
            
            // Test all block types with patterns
            cell.setBlock("O", "block-I", "tetris-i-text"); // diagonal-right
            cell.setBlock("O", "block-O", "tetris-o-text"); // none
            cell.setBlock("O", "block-T", "tetris-t-text"); // diagonal-left
            cell.setBlock("O", "block-S", "tetris-s-text"); // horizontal
            cell.setBlock("O", "block-Z", "tetris-z-text"); // diagonal-right-wide
            cell.setBlock("O", "block-J", "tetris-j-text"); // vertical
            cell.setBlock("O", "block-L", "tetris-l-text"); // diagonal-left-wide
            
            assertNotNull(cell);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetBlockWithItemSymbols() throws Exception {
        when(mockSettings.isColorBlindMode()).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            CellView cell = new CellView(28.0, mockSettings);
            
            // Item blocks should show text even in colorblind mode
            cell.setBlock("C", "item-clear", "item-clear-text");
            cell.setBlock("L", "item-line", "item-line-text");
            cell.setBlock("W", "item-weight", "item-weight-text");
            cell.setBlock("G", "item-gravity", "item-gravity-text");
            cell.setBlock("S", "item-split", "item-split-text");
            
            assertNotNull(cell);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetBlockGhostBlock() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            cellView.setBlock("O", "block-ghost", "tetris-ghost-text");
            assertNotNull(cellView);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testSetBlockWithNullValues() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            cellView.setBlock(null, null, null);
            assertNotNull(cellView);
            
            cellView.setBlock("", "", "");
            assertNotNull(cellView);
            
            cellView.setBlock(" ", " ", " ");
            assertNotNull(cellView);
            
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testPatternRedrawOnResize() throws Exception {
        when(mockSettings.isColorBlindMode()).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            CellView cell = new CellView(28.0, mockSettings);
            cell.setBlock("O", "block-I", "tetris-i-text");
            
            // Trigger resize to test redraw
            cell.setPrefSize(35.0, 35.0);
            
            assertNotNull(cell);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testMultipleStateChanges() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            // Test rapid state changes
            cellView.setEmpty();
            cellView.setBorder();
            cellView.setBlock("O", "block-I", "tetris-i-text");
            cellView.setEmpty();
            cellView.setBlock("T", "block-T", "tetris-t-text");
            
            assertNotNull(cellView);
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testApplyPatternCoverage() throws Exception {
        when(mockSettings.isColorBlindMode()).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            CellView cell = new CellView(28.0, mockSettings);
            
            // Test unknown pattern
            cell.setBlock("O", "block-unknown", "tetris-unknown-text");
            
            assertNotNull(cell);
            latch.countDown();
        });
        latch.await();
    }
}

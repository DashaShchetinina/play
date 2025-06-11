package Tests;

import GameMap.MapEditor;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.Map;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

public class MapEditorTest {

    private static final String TEST_MAP_NAME = "test_map";
    private static final Logger logger = Logger.getLogger(MapEditorTest.class.getName());
    private static FileHandler fileHandler;

    @BeforeAll
    public static void setUpLogger() throws Exception {
        fileHandler = new FileHandler("map_editor_test.log", true); // Логи в файл
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
    }

    @AfterAll
    public static void closeLogger() {
        fileHandler.close();
    }

    @AfterEach
    public void cleanup() {
        File file = new File(MapEditor.MAPS_DIR + TEST_MAP_NAME + ".map");
        if (file.exists()) {
            if (file.delete()) {
                logger.log(Level.WARNING, "Удалён файл карты: " + file.getPath());
            }
        }
    }

    @Test
    public void testCreateAndValidateMap() {
        MapEditor editor = new MapEditor(5);
        editor.placeElement(0, 0, 'P');
        editor.placeElement(4, 4, 'C');
        assertTrue(editor.validateMap(), "Карта с P и C должна быть валидна");
        logger.log(Level.INFO, "testCreateAndValidateMap — тест прошёл");
    }

    @Test
    public void testSaveAndLoadMap() throws Exception {
        MapEditor editor = new MapEditor(5);
        editor.placeElement(0, 0, 'P');
        editor.placeElement(4, 4, 'C');
        editor.saveMap(TEST_MAP_NAME);
        logger.log(Level.WARNING, "Файл карты сохранён: " + MapEditor.MAPS_DIR + TEST_MAP_NAME + ".map");
        MapEditor loaded = MapEditor.loadMap(TEST_MAP_NAME);
        logger.log(Level.WARNING, "Файл карты прочитан: " + MapEditor.MAPS_DIR + TEST_MAP_NAME + ".map");
        assertNotNull(loaded);
        assertEquals(5, loaded.getGrid().length);
        assertEquals('P', loaded.getGrid()[0][0]);
        assertEquals('C', loaded.getGrid()[4][4]);
        logger.log(Level.INFO, "testSaveAndLoadMap — тест прошёл");
    }

    @Test
    public void testFillNonMainDiagonalEmptyCells() {
        MapEditor editor = new MapEditor(3);
        editor.placeElement(0, 0, 'P');
        editor.placeElement(2, 2, 'C');
        editor.fillNonMainDiagonalEmptyCells();
        char[][] grid = editor.getGrid();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) continue;
                assertEquals('#', grid[i][j], "Клетка вне диагонали должна быть стеной");
            }
        }
        logger.log(Level.INFO, "testFillNonMainDiagonalEmptyCells — тест прошёл");
    }

    @Test
    public void testListAvailableMaps() throws Exception {
        MapEditor editor = new MapEditor(5);
        editor.placeElement(0, 0, 'P');
        editor.placeElement(4, 4, 'C');
        editor.saveMap(TEST_MAP_NAME);
        logger.log(Level.WARNING, "Файл карты сохранён: " + MapEditor.MAPS_DIR + TEST_MAP_NAME + ".map");
        Map<String, String> maps = MapEditor.listAvailableMaps();
        assertTrue(maps.containsKey(TEST_MAP_NAME), "Сохраненная карта должна быть в списке");
        logger.log(Level.INFO, "testListAvailableMaps — тест прошёл");
    }

    @Test
    public void testValidateMapFailsWithoutCastles() {
        MapEditor editor = new MapEditor(5);
        assertFalse(editor.validateMap(), "Карта без замков невалидна");
        editor.placeElement(0, 0, 'P');
        assertFalse(editor.validateMap(), "Карта только с P невалидна");
        editor = new MapEditor(5);
        editor.placeElement(0, 0, 'C');
        assertFalse(editor.validateMap(), "Карта только с C невалидна");
        logger.log(Level.INFO, "testValidateMapFailsWithoutCastles — тест прошёл");
    }
}

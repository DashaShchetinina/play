package GameMap;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MapEditor {
    public static final String MAPS_DIR = "game_data/maps/";
    private char[][] grid;
    private int size;
    private String currentMapName;
    // Карта: координата входа -> файл пещеры
    private Map<String, String> caveEntrances = new HashMap<>();

    public MapEditor(int size) {
        this.size = size;
        this.grid = new char[size][size];
        initializeEmptyGrid();
    }

    private void initializeEmptyGrid() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = ' ';
            }
        }
    }

    public void placeElement(int x, int y, char symbol) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            grid[x][y] = symbol;
            if (symbol == 'E') { // 'E' — вход в пещеру
                Scanner scanner = new Scanner(System.in);
                System.out.println("Добавление входа в пещеру по координате (" + x + "," + y + ")");
                System.out.print("Имя файла пещеры будет сгенерировано автоматически.\n");
                // Формируем уникальное имя для файла пещеры:
                String caveFileName = (currentMapName == null ? "custommap" : currentMapName) + "_cave_" + x + "_" + y + ".cave";
                CaveEditor caveEditor = new CaveEditor(caveFileName);
                caveEditor.edit();
                caveEntrances.put(x + "_" + y, caveFileName);
                System.out.println("Пещера создана и связана с координатой (" + x + "," + y + ")");
            }
        } else {
            throw new IllegalArgumentException("Invalid coordinates!");
        }
    }

    public void fillNonMainDiagonalEmptyCells() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == ' ' && i != j) {
                    grid[i][j] = '#';
                }
            }
        }
    }

    public void saveMap(String mapName) throws IOException {
        if (!validateMap()) {
            throw new IllegalArgumentException("Map validation failed!");
        }
        this.currentMapName = mapName;
        File dir = new File(MAPS_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Сохраняем основную карту
        try (FileWriter fw = new FileWriter(MAPS_DIR + mapName + ".map")) {
            for (int i = 0; i < size; i++) {
                fw.write(grid[i]);
                fw.write(System.lineSeparator());
            }
        }
        // Сохраняем связи с пещерами
        if (!caveEntrances.isEmpty()) {
            try (FileWriter fw = new FileWriter(MAPS_DIR + mapName + ".caves")) {
                for (Map.Entry<String, String> entry : caveEntrances.entrySet()) {
                    fw.write(entry.getKey() + ":" + entry.getValue() + System.lineSeparator());
                }
            }
        }
    }

    public static MapEditor loadMap(String mapName) throws IOException {
        File file = new File(MAPS_DIR + mapName + ".map");
        if (!file.exists()) throw new FileNotFoundException("Map file not found: " + file.getAbsolutePath());

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int size = 0;
        // Сначала узнаём размер карты по числу символов в первой строке
        file = new File(MAPS_DIR + mapName + ".map");
        try (BufferedReader sizeBr = new BufferedReader(new FileReader(file))) {
            String firstLine = sizeBr.readLine();
            if (firstLine == null) throw new IOException("Empty map file: " + mapName);
            size = firstLine.length();
        }

        MapEditor editor = new MapEditor(size);
        int row = 0;
        while ((line = br.readLine()) != null && row < size) {
            for (int col = 0; col < size && col < line.length(); col++) {
                editor.grid[row][col] = line.charAt(col);
            }
            row++;
        }
        br.close();
        editor.currentMapName = mapName;

        // --- Восстанавливаем связи с пещерами ---
        File cavesFile = new File(MAPS_DIR + mapName + ".caves");
        if (cavesFile.exists()) {
            try (BufferedReader cavesBr = new BufferedReader(new FileReader(cavesFile))) {
                String cavesLine;
                while ((cavesLine = cavesBr.readLine()) != null) {
                    String[] parts = cavesLine.split(":");
                    if (parts.length == 2) {
                        editor.caveEntrances.put(parts[0], parts[1]);
                    }
                }
            }
        }
        return editor;
    }

    public void printMap() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public char[][] getGrid() {
        return this.grid;
    }

    public boolean isCaveEntrance(int x, int y) {
        return caveEntrances.containsKey(x + "_" + y);
    }

    public String getCaveFile(int x, int y) {
        return caveEntrances.get(x + "_" + y);
    }

    public static Map<String, String> listAvailableMaps() {
        Map<String, String> maps = new HashMap<>();
        File dir = new File(MAPS_DIR);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".map")) {
                    String name = file.getName().replace(".map", "");
                    maps.put(name, file.getPath());
                }
            }
        }
        return maps;
    }

    public boolean validateMap() {
        boolean hasPlayerCastle = false;
        boolean hasComputerCastle = false;
        String allowed = "PC#+ .E"; // Добавь свои символы (допустимые на карте)

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                char c = grid[i][j];
                if (c == 'P') hasPlayerCastle = true;
                if (c == 'C') hasComputerCastle = true;
                if (allowed.indexOf(c) == -1) {
                    System.out.println("Недопустимый символ '" + c + "' на (" + i + "," + j + ")");
                    return false;
                }
            }
        }
        return hasPlayerCastle && hasComputerCastle;
    }

    public void printPreview() {
        System.out.println("\nMap Preview (" + size + "x" + size + "):");
        int scale = size > 15 ? 2 : 1;
        for (int i = 0; i < size; i += scale) {
            for (int j = 0; j < size; j += scale) {
                char c = grid[i][j];
                System.out.print(c == ' ' ? '.' : c);
            }
            System.out.println();
        }
        System.out.println("Key: P=Your Castle, C=Enemy Castle, #=Wall, E=Entrance, .=Empty");
    }
}

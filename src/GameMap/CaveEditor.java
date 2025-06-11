package GameMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CaveEditor {
    public static final int SIZE = 3;
    private final char[][] grid;
    private final String caveFileName;

    public static final char WALL = '#';
    public static final char FLOOR = '.';
    public static final char MINE = 'M';
    public static final char TREASURE = 'T';
    public static final char EXIT = 'X';

    public CaveEditor(String caveFileName) {
        this.caveFileName = caveFileName;
        this.grid = new char[SIZE][SIZE];
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = FLOOR;
        grid[0][0] = EXIT; // один выход по умолчанию
    }

    public void edit() {
        Scanner scanner = new Scanner(System.in);
        boolean editing = true;
        while (editing) {
            printCave();
            System.out.println("Редактор пещеры: команды —");
            System.out.println("[1] Стена (#)   [2] Проход (.)   [3] Мина (M)");
            System.out.println("[4] Сокровище (T)   [5] Выход (X)   [0] Сохранить и выйти");
            System.out.print("Выберите команду: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1", "2", "3", "4", "5" -> {
                    System.out.print("Координаты (x y): ");
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    scanner.nextLine();
                    if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
                        System.out.println("Некорректные координаты!");
                        continue;
                    }
                    char symbol = switch (input) {
                        case "1" -> WALL;
                        case "2" -> FLOOR;
                        case "3" -> MINE;
                        case "4" -> TREASURE;
                        case "5" -> EXIT;
                        default -> FLOOR;
                    };
                    grid[x][y] = symbol;
                }
                case "0" -> {
                    if (validateCave()) {
                        try {
                            saveCave();
                            System.out.println("Пещера сохранена как " + caveFileName);
                            editing = false;
                        } catch (IOException e) {
                            System.out.println("Ошибка при сохранении: " + e.getMessage());
                        }
                    } else {
                        System.out.println("В пещере должен быть хотя бы один выход (X)!");
                    }
                }
                default -> System.out.println("Неизвестная команда.");
            }
        }
    }

    public void printCave() {
        System.out.println("== Пещера (" + SIZE + "x" + SIZE + ") ==");
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Ключ: #=стена, .=проход, M=мина, T=сокровище, X=выход");
    }

    public boolean validateCave() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (grid[i][j] == EXIT)
                    return true;
        return false;
    }

    public void saveCave() throws IOException {
        File file = new File(MapEditor.MAPS_DIR + caveFileName);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileWriter fw = new FileWriter(file)) {
            for (int i = 0; i < SIZE; i++) {
                fw.write(grid[i]);
                fw.write(System.lineSeparator());
            }
        }
    }

    // Для чтения пещеры из файла
    public static char[][] loadCave(String caveFileName) throws IOException {
        char[][] cave = new char[SIZE][SIZE];
        File file = new File(MapEditor.MAPS_DIR + caveFileName);
        try (Scanner scanner = new Scanner(file)) {
            for (int i = 0; i < SIZE; i++) {
                String line = scanner.nextLine();
                for (int j = 0; j < SIZE; j++) {
                    cave[i][j] = line.charAt(j);
                }
            }
        }
        return cave;
    }
}

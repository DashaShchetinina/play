package GameLogic;

import java.io.*;
import java.util.*;

import Buildings.*;
import Entities.*;
import jline.console.ConsoleReader;

import GameMap.GameMap;
import GameMap.MapEditor;
import saves.GameSaveState;
import saves.HeroState;
import GameMap.CaveEditor;

public class GameLogic {
    private int turnCount = 0;
    public GameMap map;
    public GameMap GameMap;//Объект карты
    public Castle playerCastle; //Объект замка/фракции игрока
    public Castle computerCastle; //Объект замка/фракции компьютера
    public BattleMap battleMap; //Объект карты сражения
    public boolean isPlayerTurn = true;
    public int choosenHero = 0;
    ConsoleReader console;
    public BotAI bot; //Объект действий компьютера
    public boolean isBattle = false;
    int gameEndCountdown = 2;
    public Castle winner = null;
    private String playerName;
    private static final String SAVES_DIR = "game_data/saves/";
    private boolean buildingPurchased;
    private boolean heroHired;
    private boolean battleEnded = false;
    public Hero playerGold;
    private Hero playerHero;
    private int unitsKilledByPlayer = 0;
    private String mapNameUsed = "default";
    public ServiceBuilding hotel, cafe, barbershop;  // Здания
    public Building[] buildings;
    GameTime gameTime;
    private List<TownNPC> npcs = new ArrayList<>();
    private volatile boolean gameRunning = true;


    public GameLogic() {
        gameTime = new GameTime();
        // Инициализация зданий с использованием BuildingType
        hotel = new ServiceBuilding(BuildingType.HOTEL, 5, Arrays.asList(
                new Service("Короткий отдых", 24, "health", 2),
                new Service("Длинный отдых", 3 * 24, "health", 3)
        ));
        cafe = new ServiceBuilding(BuildingType.CAFE, 12, Arrays.asList(
                new Service("Просто перекус", 15, "move", 2),
                new Service("Плотный обед", 30, "move", 3)
        ));
        barbershop = new ServiceBuilding(BuildingType.BARBERSHOP, 2, Arrays.asList(
                new Service("Просто стрижка", 10, "none", 0),
                new Service("Модная стрижка", 30, "capture_speed", 1)
        ));
        playerCastle = new Castle(5000, 'P', '1', 0, 0); // Замок с координатами (0,0)
        computerCastle = new Castle(5000, 'C', '2', 10, 10); // Замок противника

        // Создаем массив зданий
        buildings = new ServiceBuilding[] {hotel, cafe, barbershop};
        gameTime = new GameTime();
    }

    public Castle startGame() throws IOException, ClassNotFoundException {
        // Инициализация консольного ридера
        console = new ConsoleReader();

        // Главное меню
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. New Game");
            System.out.println("2. Load Game");
            System.out.println("3. Map Editor");
            System.out.println("4. View Highscores");
            System.out.println("5. Exit");
            System.out.print("Choose option: ");

            int choice;
            try {
                choice = Integer.parseInt(console.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: // Новая игра
                    return startNewGame();

                case 2: // Загрузка игры
                    Castle result = loadGame();
                    if (result != null) {
                        return runGameLoop(); // Продолжить с места сохранения
                    }
                    break;

                case 3: // Редактор карт
                    startMapEditor();
                    break;
                case 4: // View Highscores
                    showHighScores();
                    break;

                case 5: // Выход
                    quitGame();
                    break;

                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    public void showMenu(Hero hero) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("=== Меню ===");
            System.out.println("1. Посетить здание");
            System.out.println("2. Просмотр статистики зданий");
            System.out.println("3. Выход");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    visitBuilding(hero, scanner);  // Метод для посещения зданий
                    break;
                case 2:
                    showBuildingStatus((ServiceBuilding[]) buildings);  // Передаем массив зданий
                    break;
                case 3:
                    System.out.println("Выход из игры...");
                    return;
                default:
                    System.out.println("Некорректный выбор.");
            }
        }
    }

    private void visitBuilding(Hero hero, Scanner scanner) {
        System.out.println("Выберите здание для посещения:");
        for (int i = 0; i < buildings.length; i++) {
            System.out.println((i + 1) + ". " + buildings[i].getBuildingName());
        }

        int buildingChoice = scanner.nextInt() - 1;

        if (buildingChoice >= 0 && buildingChoice < buildings.length) {
            ServiceBuilding serviceBuilding = (ServiceBuilding) buildings[buildingChoice];

            System.out.println("Выберите услугу:");
            for (int i = 0; i < serviceBuilding.getServices().size(); i++) {
                Service service = serviceBuilding.getServices().get(i);
                System.out.println((i + 1) + ". " + service.getName() + " (Бонус: " + service.getBonusType() + ")");
            }

            int serviceChoice = scanner.nextInt() - 1;
            if (serviceChoice >= 0 && serviceChoice < serviceBuilding.getServices().size()) {
                Service chosenService = serviceBuilding.getServices().get(serviceChoice);
                long finishTime = gameTime.getCurrentGameTime() + gameTime.convertToMilliseconds(chosenService.getDurationMinutes());
                if (serviceBuilding.tryEnter(hero.getSymbol() + "", finishTime)) {
                    logActionToFile(hero.getSymbol() + " зашел в " + serviceBuilding.getBuildingName() + " на услугу: " + chosenService.getName());
                    System.out.println(hero.getSymbol() + " зашел в " + serviceBuilding.getBuildingName() + " на услугу: " + chosenService.getName());

                    logActionToFile(hero.getSymbol() + " ждет в очереди в " + serviceBuilding.getBuildingName());
                    System.out.println(hero.getSymbol() + " ждет в очереди в " + serviceBuilding.getBuildingName());

                    serviceBuilding.applyBonus(hero, chosenService);

                    // Применяем бонус
                    serviceBuilding.applyBonus(hero, chosenService);

// Грабеж ночью с вероятностью 90%
                    int currentHour = gameTime.getCurrentHour();
                    if (currentHour >= 22 || currentHour < 6) {
                        if (Math.random() < 0.9) {
                            hero.rob(currentHour);
                        } else {
                            System.out.println("В этот раз вам повезло — вас не ограбили!");
                        }
                    } else {
                        System.out.println("Сейчас не ночь, вы в безопасности.");
                    }


                    if ("health".equals(chosenService.getBonusType())) {
                        hero.increaseHealthAll(chosenService.getBonusValue());
                    } else if ("move".equals(chosenService.getBonusType())) {
                        hero.increaseMoveAll(chosenService.getBonusValue());
                    } else if ("capture_speed".equals(chosenService.getBonusType())) {
                        hero.setCastleCaptureTime(chosenService.getBonusValue());
                    }

                    // Добавленная задержка в 2 секунды
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Задержка была прервана");
                    }

                    serviceBuilding.release(hero.getSymbol() + "");
                    logActionToFile(hero.getSymbol() + " покинул " + serviceBuilding.getBuildingName());
                    System.out.println(hero.getSymbol() + " покинул " + serviceBuilding.getBuildingName() + " и получил бонус.");
                } else {
                    logActionToFile(hero.getSymbol() + " не может попасть в " + serviceBuilding.getBuildingName());
                    System.out.println(hero.getSymbol() + " не может попасть в " + serviceBuilding.getBuildingName());
                }
            }
        } else {
            System.out.println("Некорректный выбор здания.");
        }
    }






    // Метод для записи действий в файл
    private void logActionToFile(String actionLog) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("npc_actions.log", true))) {
            writer.write(actionLog);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл: " + e.getMessage());
        }
    }




    public void showBuildingStatus(ServiceBuilding[] buildings) {
        for (ServiceBuilding building : buildings) {
            System.out.println("Building: " + building.getBuildingName());
            System.out.println("Level: " + building.getLevel());
            System.out.println("Cost: " + building.getCost());

            // Показываем текущих посетителей и время освобождения
            Map<String, Long> busyUntil = building.getBusyUntil();
            if (busyUntil.isEmpty()) {
                System.out.println("Здание пусто, никто не обслуживается.");
            } else {
                System.out.println("Занятые посетители:");
                busyUntil.forEach((visitor, timeLeft) ->
                        System.out.println(visitor + " до времени освобождения: " + timeLeft + " мин"));
            }
            System.out.println(); // Пустая строка для разделения зданий
        }
    }


    public Castle startNewGame() throws IOException, ClassNotFoundException {
        // Запрос имени игрока
        if (console == null)
            console = new jline.console.ConsoleReader();
        System.out.print("\nEnter your name: ");
        playerName = console.readLine().trim();

        // Выбор карты
        System.out.println("\n=== Select Map ===");
        System.out.println("1. Default Map (11x11)");
        System.out.println("2. Load Custom Map");
        System.out.print("Choose option: ");

        int mapChoice;
        try {
            mapChoice = Integer.parseInt(console.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Using default map.");
            mapChoice = 1;
        }
        // Преобразуем Building[] в ServiceBuilding[] при условии, что все элементы - ServiceBuilding
        ServiceBuilding[] serviceBuildings = Arrays.copyOf(buildings, buildings.length, ServiceBuilding[].class);
        for (int i = 0; i < 10; i++) {
            TownNPC npc = new TownNPC("NPC-" + (i + 1), serviceBuildings, gameTime, playerCastle);
            npcs.add(npc);
            npc.start();
        }
        // Инициализация карты
        if (mapChoice == 1) {
            // --- Дефолтная карта
            map = new GameMap(11);
            playerCastle = new Castle(5000, 'P', '1', 0, 0);
            computerCastle = new Castle(5000, 'C', '2', 10, 10);
            mapNameUsed = "default";
        } else {
            // --- Пользовательская карта
            File[] customMaps = new File(MapEditor.MAPS_DIR).listFiles();
            if (customMaps == null || customMaps.length == 0) {
                System.out.println("No custom maps found! Using default map.");
                map = new GameMap(11);
                playerCastle = new Castle(5000, 'P', '1', 0, 0);
                computerCastle = new Castle(5000, 'C', '2', 10, 10);
                mapNameUsed = "default";
            } else {
                System.out.println("\nAvailable Maps:");
                for (int i = 0; i < customMaps.length; i++) {
                    System.out.println((i + 1) + ". " + customMaps[i].getName().replace(".map", ""));
                }
                System.out.print("Select map (number): ");

                try {
                    int selectedMap = Integer.parseInt(console.readLine());
                    if (selectedMap < 1 || selectedMap > customMaps.length) {
                        throw new NumberFormatException();
                    }
                    String chosenMapName = customMaps[selectedMap - 1].getName().replace(".map", "");
                    File file = new File(MapEditor.MAPS_DIR + chosenMapName + ".map");
                    if (!file.exists()) {
                        throw new IOException("Map file does not exist: " + file.getPath());
                    }
                    MapEditor editor = MapEditor.loadMap(chosenMapName);
                    if (editor == null) {
                        throw new IOException("MapEditor.loadMap returned null!");
                    }
                    map = new GameMap(editor.getGrid());
                    mapNameUsed = chosenMapName;

                    // Поиск замков на карте ТОЛЬКО здесь, если editor не null!
                    int[] playerPos = findSymbolPosition(editor.getGrid(), 'P');
                    int[] computerPos = findSymbolPosition(editor.getGrid(), 'C');
                    playerCastle = new Castle(5000, 'P', '1',
                            playerPos != null ? playerPos[0] : 0,
                            playerPos != null ? playerPos[1] : 0);

                    computerCastle = new Castle(5000, 'C', '2',
                            computerPos != null ? computerPos[0] : map.getSize() - 1,
                            computerPos != null ? computerPos[1] : map.getSize() - 1);

                } catch (Exception e) {
                    System.out.println("Error loading map! Using default map.");
                    map = new GameMap(11);
                    playerCastle = new Castle(5000, 'P', '1', 0, 0);
                    computerCastle = new Castle(5000, 'C', '2', 10, 10);
                    mapNameUsed = "default";
                    // editor нигде вне try не используем!
                }
            }
        }

        // Инициализация бота
        bot = new BotAI(map, computerCastle, this);

        // Первое автосохранение
        autoSave();

        // Запуск игрового цикла
        return runGameLoop();
    }
    public void updateGame() {
        int currentHour = gameTime.getCurrentHour();
        long currentGameTime = gameTime.getCurrentGameTime(); // Текущее время в игровых миллисекундах

        // Для всех героев игрока:
        for (Hero hero : playerCastle.getHeroes()) {

            // 1. Проверяем: если герой находится в каком-либо сервисном здании ночью — шанс быть ограбленным
            for (Building b : buildings) {
                if (b instanceof ServiceBuilding sb) {
                    if (sb.getType() == BuildingType.HOTEL
                            || sb.getType() == BuildingType.CAFE
                            || sb.getType() == BuildingType.BARBERSHOP) {
                        if (sb.isHeroInside(hero)) {
                            // Грабят только ночью (22:00 - 5:59)
                            if ((currentHour >= 22 || currentHour < 6) && !hero.isRobbed()) {
                                if (Math.random() < 0.9) { // 90% шанс быть ограбленным ночью
                                    hero.rob(currentHour);
                                } else {
                                    System.out.println("В этот раз вам повезло — вас не ограбили!");
                                }
                            }
                        }
                    }
                }
            }

            // 2. Проверяем — пора ли вернуть награбленное? (через 3 игровых часа после вызова полиции)
            if (hero.shouldCompensate(currentGameTime)) {
                hero.tryCompensate();
            }
        }

        // 3. Обновление состояния зданий (по текущему игровому времени)
        for (Building building : playerCastle.getBuildings()) {
            if (building instanceof ServiceBuilding) {
                ((ServiceBuilding) building).update(currentGameTime);
            }
        }
    }


    // Простой способ — если у тебя есть список текущих посетителей

    public Castle runGameLoop() throws IOException, ClassNotFoundException {
        while (!isGameOver()) {
            if (isPlayerTurn) {
                playerTurn();  // Ход игрока

                updateGame();  // Обновление игрового времени, NPC и состояния зданий

                // Автосохранение после хода игрока
                if (turnCount % 3 == 0) { // Каждые 3 хода
                    autoSave();
                }
            } else {
                computerTurn();  // Ход компьютера

                updateGame();  // Обновление игрового времени, NPC и состояния зданий
            }

            // Переключаем ход
            isPlayerTurn = !isPlayerTurn;
            turnCount++;
        }

        // Проверка победителя и сохранение
        if (winner == playerCastle) {
            int score = unitsKilledByPlayer * 5 + playerCastle.getBuildings().size() * 10 + playerCastle.getGold() - turnCount;
            highscores.HighScoreEntry entry = new highscores.HighScoreEntry(
                    playerName,
                    mapNameUsed,
                    score,
                    unitsKilledByPlayer,
                    turnCount,
                    new java.util.Date()
            );
            highscores.HighScoreTable table = new highscores.HighScoreTable();
            table.addOrUpdateEntry(entry);
        }

        // Сохранение при завершении игры
        saveGame(false);
        return determineWinner();
    }

    private void showHighScores() {
        highscores.HighScoreTable table = new highscores.HighScoreTable();
        System.out.println("\n=== TOP 5 HIGHSCORES ===");
        int place = 1;
        for (highscores.HighScoreEntry entry : table.getTopEntries()) {
            System.out.printf("%d. %s | %d pts | Map: %s | Kills: %d | Turns: %d | Date: %s\n",
                    place++, entry.getPlayerName(), entry.getScore(), entry.getMapName(),
                    entry.getUnitsKilled(), entry.getTurnsTaken(), entry.getDate().toString());
        }
        if (place == 1) {
            System.out.println("No highscores yet!");
        }
        System.out.println("Press Enter to return to menu...");
        try { System.in.read(); } catch (Exception e) {}
    }
    private int[] findSymbolPosition(char[][] grid, char symbol) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == symbol) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    public Castle startMapEditor() throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Запрос размера карты с валидацией
        int size = 0;
        while (size < 5 || size > 20) {
            System.out.print("Enter map size (5-20): ");
            try {
                size = scanner.nextInt();
                if (size < 5 || size > 20) {
                    System.out.println("Size must be between 5 and 20!");
                }
            } catch (Exception e) {
                System.out.println("Please enter a valid number!");
                scanner.next();
            }
        }

        MapEditor editor = new MapEditor(size);
        boolean editing = true;

        while (editing) {
            System.out.println("\n".repeat(50)); // Очистка консоли
            editor.printMap();

            System.out.println("\n=== Map Editor ===");
            System.out.println("1. Add Multiple Elements");
            System.out.println("2. Clear Area");
            System.out.println("3. Preview Map");
            System.out.println("4. Save Map (Auto-fill empty cells)");
            System.out.println("5. Exit Without Saving");
            System.out.print("Choose option: ");

            try {
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1: // Добавление нескольких элементов
                        boolean addingElements = true;
                        while (addingElements) {
                            System.out.println("\nCurrent Map:");
                            editor.printMap();
                            System.out.println("\nAdd elements (one per line) in format: X Y Symbol");
                            System.out.println("Examples:");
                            System.out.println("1 1 P  - Your castle at (1,1)");
                            System.out.println("8 8 C  - Enemy castle at (8,8)");
                            System.out.println("5 5 #  - Wall at (5,5)");
                            System.out.println("Type 'done' when finished");

                            scanner.nextLine(); // Очистка буфера
                            while (true) {
                                System.out.print("> ");
                                String input = scanner.nextLine().trim();

                                if (input.equalsIgnoreCase("done")) {
                                    addingElements = false;
                                    break;
                                }

                                try {
                                    String[] parts = input.split(" ");
                                    if (parts.length != 3) {
                                        System.out.println("Invalid format! Use: X Y Symbol");
                                        continue;
                                    }

                                    int x = Integer.parseInt(parts[0]);
                                    int y = Integer.parseInt(parts[1]);
                                    char symbol = parts[2].charAt(0);

                                    editor.placeElement(x, y, symbol);
                                    System.out.println("Added: " + symbol + " at (" + x + "," + y + ")");

                                    System.out.println("\nUpdated Map:");
                                    editor.printMap();

                                } catch (Exception e) {
                                    System.out.println("Error: " + e.getMessage());
                                }
                            }
                        }
                        break;

                    case 2: // Очистка области
                        System.out.println("\nClear area (enter top-left and bottom-right coordinates):");
                        System.out.print("Start X: ");
                        int startX = scanner.nextInt();
                        System.out.print("Start Y: ");
                        int startY = scanner.nextInt();
                        System.out.print("End X: ");
                        int endX = scanner.nextInt();
                        System.out.print("End Y: ");
                        int endY = scanner.nextInt();

                        if (startX > endX) { int temp = startX; startX = endX; endX = temp; }
                        if (startY > endY) { int temp = startY; startY = endY; endY = temp; }

                        for (int x = startX; x <= endX; x++) {
                            for (int y = startY; y <= endY; y++) {
                                try {
                                    editor.placeElement(x, y, ' ');
                                } catch (IllegalArgumentException ignored) {}
                            }
                        }
                        System.out.println("Area cleared!");
                        break;

                    case 3: // Предпросмотр
                        System.out.println("\n=== Map Preview ===");
                        editor.printPreview();
                        System.out.println("\nPress Enter to continue...");
                        System.in.read();
                        break;

                    case 4: // Сохранение с автозаполнением
                        editor.fillNonMainDiagonalEmptyCells();
                        System.out.println("\nMap after auto-fill:");
                        editor.printMap();

                        System.out.println("\nEnter map name:");
                        String mapName = scanner.next();

                        try {
                            if (editor.validateMap()) {
                                editor.saveMap(mapName);
                                System.out.println("Map saved successfully!");
                            } else {
                                System.out.println("Cannot save - map is invalid!");
                                System.out.println("Map must contain both P (player castle) and C (computer castle)");
                            }
                            System.out.println("Press Enter to continue...");
                            System.in.read();
                        } catch (IOException e) {
                            System.out.println("Error saving map: " + e.getMessage());
                        }
                        break;

                    case 5: // Выход
                        System.out.print("Are you sure you want to exit without saving? (y/n): ");
                        if (scanner.next().equalsIgnoreCase("y")) {
                            editing = false;
                        }
                        break;
                    default:
                        System.out.println("Invalid option! Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: Invalid input! Please try again.");
                scanner.next();
            }
        }
        return null;
    }

    private void saveGame(boolean isAutosave) {
        try {
            // Создаем папку для сохранений, если её нет
            File dir = new File("game_data/saves/");
            if (!dir.exists()) dir.mkdirs();

            // Формируем имя файла
            String filename = "game_data/saves/" + playerName + "_" +
                    (isAutosave ? "autosave" : System.currentTimeMillis()) + ".sav";
            if (playerHero == null) {
                System.out.println("");
            }
            HeroState heroState = (playerHero != null) ? new HeroState(playerHero) : null; // Преобразуем playerHero в HeroState

            GameSaveState saveState = new GameSaveState(
                    playerName,
                    map,
                    playerCastle,
                    computerCastle,
                    isPlayerTurn,
                    playerGold,
                    heroState // Передаем HeroState
            );

            // Сохраняем
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(saveState);
                System.out.println("Game " + (isAutosave ? "autosaved" : "saved") + " successfully!");
            }
        } catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    // Метод для автосохранения после важных событий
    private void autoSave() {
        saveGame(true);
    }

    public Castle loadGame() throws IOException, ClassNotFoundException {
        File savesDir = new File("game_data/saves/");
        File[] saves = savesDir.listFiles((dir, name) ->
                name.startsWith(playerName + "_") && name.endsWith(".sav"));

        if (saves == null || saves.length == 0) {
            System.out.println("No saves found for player: " + playerName);
            return null;
        }

        // Показываем список сохранений
        System.out.println("\nAvailable saves:");
        for (int i = 0; i < saves.length; i++) {
            System.out.println((i+1) + ". " + saves[i].getName());
        }

        // Выбор сохранения
        System.out.print("Select save to load (0 to cancel): ");
        int choice = Integer.parseInt(console.readLine());

        if (choice == 0) return null;

        // Загрузка сохранения
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(saves[choice-1]))) {

            GameSaveState saveState = (GameSaveState) ois.readObject();

            // Восстанавливаем состояние игры
            map = new GameMap(saveState.getMapGrid());
            playerCastle = Castle.fromState(saveState.getPlayerCastle());
            computerCastle = Castle.fromState(saveState.getComputerCastle());
            isPlayerTurn = saveState.isPlayerTurn();

            // Восстанавливаем героя
            HeroState heroState = saveState.getPlayerHero();
            if (heroState != null) {
                playerHero = heroState.toHero(playerCastle);
                playerCastle.getHeroes().add(playerHero); // Добавляем героя в замок
            }

            System.out.println("Game loaded successfully!");
            return runGameLoop();
        }
    }
    private String selectMap() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Select Map ===");
        System.out.println("1. Default Map (11x11)");
        System.out.println("2. Load Custom Map");
        System.out.print("Choose option: ");

        int choice = scanner.nextInt();

        if (choice == 1) {
            // Показываем превью стандартной карты
            System.out.println("\nDefault Map Preview:");
            System.out.println("+-----------+");
            System.out.println("|P         #|");
            System.out.println("| # + + + # |");
            System.out.println("|  # # # #  |");
            System.out.println("|   + + +   |");
            System.out.println("|    ###    |");
            System.out.println("|   + + +   |");
            System.out.println("|  # # # #  |");
            System.out.println("| # + + + # |");
            System.out.println("|#         C|");
            System.out.println("+-----------+");
            return "default";
        } else if (choice == 2) {
            Map<String, String> availableMaps = MapEditor.listAvailableMaps();
            if (availableMaps.isEmpty()) {
                System.out.println("\nNo custom maps found! Using default map.");
                return "default";
            }

            System.out.println("\nAvailable Maps:");
            int i = 1;
            for (String mapName : availableMaps.keySet()) {
                System.out.print(i++ + ". " + mapName);

                // Загружаем и показываем превью для каждой карты
                try {
                    MapEditor editor = MapEditor.loadMap(mapName);
                    editor.printPreview();
                } catch (IOException e) {
                    System.out.println(" (Error loading preview)");
                }
            }

            System.out.print("\nSelect map (number) or 0 to cancel: ");
            int mapChoice = scanner.nextInt();

            if (mapChoice == 0) {
                return selectMap(); // Возврат в меню выбора
            } else if (mapChoice > 0 && mapChoice <= availableMaps.size()) {
                return (String) availableMaps.keySet().toArray()[mapChoice-1];
            } else {
                System.out.println("Invalid choice! Using default map.");
                return "default";
            }
        } else {
            System.out.println("Invalid choice! Using default map.");
            return "default";
        }
    }
    private void playerTurn() throws IOException, ClassNotFoundException {
        String input;
        // Обработка действий игрока
        do {
            if(isBattle) { //Битва или нет
                battleMap.startFight();
                Hero loser = battleMap.getLoser();
                loser.getHomeCastle().getHeroes().remove(loser); //Удаляем проигравшего героя из замка
                isBattle = false;
                map.updateMap(playerCastle, computerCastle);
                map.printMap();
            }
            map.updateMap(playerCastle, computerCastle);
            map.printMap();
            List<Hero> heroes = playerCastle.getHeroes();
            List<Hero> enemies = computerCastle.getHeroes();

            console.println("Current Heroes: ");
            for (Hero hero : heroes) { //Выводим на экран героев игрока
                console.print(hero.getSymbol() + " ");
            }
            console.println();

            console.println("Enemy Heroes: ");
            for (Hero hero : enemies) { //Выводим на экран героев компьютера
                console.print(hero.getSymbol() + " ");
            }
            console.println();
            if (shouldAutoSave()) {
                autoSave();
            }
            if(!heroes.isEmpty()) {
                console.print("Selected hero: " + heroes.get(choosenHero).getSymbol());
                console.println();
                console.println(heroes.get(choosenHero).toString());
                if(!enemies.isEmpty()) {
                    console.println(enemies.get(0).toString());
                }
                Hero activeHero = heroes.get(choosenHero);
                int currentHour = (int) ((gameTime.getCurrentGameTimeInMinutes() / 60) % 24);
                if (activeHero.canCallPoliceNow(currentHour)) {
                    console.println("Доступно действие: Позвонить в полицию (call_police)");
                }
                input = console.readLine(
                    """
                            (Q - next turn)
                            (R - next hero)
                            (W - up)
                            (A - left)
                            (S - down)
                            (D - right)
                            (WD - up right)
                            (WA - up left)
                            (SA - down left)
                            (SD - down right)
                            (B - buy building)
                            (U - buy hero)
                            (Y - hire army)
                            (O - electrowall actions)
                            (* - quit game)
                            (call_police)
                            (save)
                            (load)
                            action>""");
                chooseAction(heroes.get(choosenHero), input); //Выбор действия
            }
            else {
                console.println("No heroes! Buy Tavern and hire Entities.Hero!");
                input = console.readLine(
                    """
                            Choose your action:
                            (Q - next turn)
                            (B - buy building)
                            (U - buy hero)
                            (` - quit game)
                            action>""");
                chooseAction(null, input); //Выбор действия
            }

            map.updateMap(playerCastle, computerCastle);
            map.printMap();

        } while(!input.equals("q"));
        playerCastle.newTurn();
    }

    private void chooseAction(Hero hero, String input) throws IOException, ClassNotFoundException {
        int size = map.getSize();

        if (hero != null) {
            int x = hero.getxCoord();
            int y = hero.getyCoord();
            Hero enemy = null;
            boolean moved = false;

            switch (input) {
                case "w" -> {
                    map.setSymbol(x, y, hero.getPrevSymbol());
                    if (x - 1 >= 0) {
                        if ((enemy = isBattle(computerCastle, x - 1, y)) == null) {
                            hero.move(Direction.UP, map.getSymbol(x - 1, y));
                            moved = true;
                        } else {
                            isBattle = true;
                            battleMap = new BattleMap(9, hero, enemy);
                        }
                    }
                }
                case "a" -> {
                    map.setSymbol(x, y, hero.getPrevSymbol());
                    if (y - 1 >= 0) {
                        if ((enemy = isBattle(computerCastle, x, y - 1)) == null) {
                            hero.move(Direction.LEFT, map.getSymbol(x, y - 1));
                            moved = true;
                        } else {
                            isBattle = true;
                            battleMap = new BattleMap(9, hero, enemy);
                        }
                    }
                }
                case "s" -> {
                    map.setSymbol(x, y, hero.getPrevSymbol());
                    if (x + 1 < size) {
                        if ((enemy = isBattle(computerCastle, x + 1, y)) == null) {
                            hero.move(Direction.DOWN, map.getSymbol(x + 1, y));
                            moved = true;
                        } else {
                            isBattle = true;
                            battleMap = new BattleMap(9, hero, enemy);
                        }
                    }
                }
                case "d" -> {
                    map.setSymbol(x, y, hero.getPrevSymbol());
                    if (y + 1 < size) {
                        if ((enemy = isBattle(computerCastle, x, y + 1)) == null) {
                            hero.move(Direction.RIGHT, map.getSymbol(x, y + 1));
                            moved = true;
                        } else {
                            isBattle = true;
                            battleMap = new BattleMap(9, hero, enemy);
                        }
                    }
                }
                case "sd" -> {
                    map.setSymbol(x, y, hero.getPrevSymbol());
                    if (y + 1 < size) {
                        if ((enemy = isBattle(computerCastle, x+1, y+1)) == null) {
                            hero.move(Direction.DOWNRIGHT, map.getSymbol(x+1, y + 1));
                            moved = true;
                        } else {
                            isBattle = true;
                            battleMap = new BattleMap(9, hero, enemy);
                        }
                    }
                }
                case "b" -> {
                    BuildingType b;
                    if ((b = chooseBuilding()) != null) {
                        playerCastle.buyBuilding(b);
                    }
                }
                case "u" -> {
                    if (playerCastle.canHireHero(computerCastle)) {
                        String name = console.readLine("Name your hero: ");
                        playerCastle.hireHero(name.charAt(0));
                    }
                }
                case "r" -> {
                    choosenHero++;
                    if (choosenHero >= playerCastle.getHeroes().size())
                        choosenHero = 0;
                }
                case "y" -> {
                    if (playerCastle.isHeroInCastle(hero)) {
                        hireArmy(playerCastle, hero);
                    }
                }
                case "o" -> {
                    chooseElectroWallAction();
                }
                case "m" -> {
                    // Взаимодействие с меню и зданиями
                    showMenu(hero);  // Теперь вызываем меню для выбора действий с зданиями
                }
                case "*" -> {
                    quitGame();
                }
                case "save" -> {
                    saveGame(false);
                    return;
                }
                case "load" -> {
                    if (!isBattle) {
                        Castle loadedGame = loadGame();
                        if (loadedGame != null) {
                            return;
                        }
                    } else {
                        System.out.println("Cannot load during battle!");
                    }
                    return;
                }
                case "call_police" -> {
                    int currentHour = gameTime.getCurrentHour();
                    if (hero.callPolice(gameTime.getCurrentGameTime())) {
                        console.println("Вы вызвали полицию! Ожидайте возврата HP и золота через 3 часа игрового времени.");
                    } else {
                        console.println("Вы не можете вызвать полицию.");
                    }
                }

                default -> { /* ничего не делаем */ }
            }

            // === Проверка входа в пещеру после ДВИЖЕНИЯ ===
            if (moved && mapNameUsed != null && !"default".equals(mapNameUsed)) {
                try {
                    MapEditor editor = MapEditor.loadMap(mapNameUsed);
                    int heroX = hero.getxCoord();
                    int heroY = hero.getyCoord();
                    if (editor.isCaveEntrance(heroX, heroY)) {
                        String caveFile = editor.getCaveFile(heroX, heroY);
                        int[] exitPos = playCave(hero, caveFile, heroX, heroY); // предполагаем, что playCave возвращает [x, y]
                        if (exitPos != null && exitPos.length == 2) {
                            hero.setxCoord(exitPos[0]);
                            hero.setyCoord(exitPos[1]);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка загрузки пещеры: " + e.getMessage());
                }
            }
        } else {
            switch (input) {
                case "b" -> {
                    BuildingType b;
                    if ((b = chooseBuilding()) != null) {
                        playerCastle.buyBuilding(b);
                    }
                }
                case "u" -> {
                    if (playerCastle.canHireHero(computerCastle)) {
                        String name = console.readLine("Name your hero: ");
                        playerCastle.hireHero(name.charAt(0));
                    }
                }
                case "*" -> {
                    quitGame();
                }
                default -> { /* ничего не делаем */ }
            }
        }
    }


    // @param onExit    — callback, который вызовется с координатами выхода на основную карту
    private int[] playCave(Hero hero, String caveFile, int mapX, int mapY) throws IOException {
        char[][] cave = CaveEditor.loadCave(caveFile);
        int x = 0, y = 0; // Начальная позиция в пещере (можно расширить если надо)
        Scanner scanner = new Scanner(System.in);

        // Найти первую проходимую клетку, если (0,0) не проходима
        if (cave[x][y] == CaveEditor.WALL) {
            outer: for (int i = 0; i < cave.length; i++) {
                for (int j = 0; j < cave[0].length; j++) {
                    if (cave[i][j] == CaveEditor.FLOOR || cave[i][j] == CaveEditor.EXIT) {
                        x = i; y = j;
                        break outer;
                    }
                }
            }
        }

        while (true) {
            // Печать пещеры с героем
            for (int i = 0; i < cave.length; i++) {
                for (int j = 0; j < cave[0].length; j++) {
                    if (i == x && j == y) System.out.print('@');
                    else System.out.print(cave[i][j]);
                    System.out.print(' ');
                }
                System.out.println();
            }
            System.out.print("Пещера (w/a/s/d — движение, e — выйти через выход X): ");
            String input = scanner.nextLine().trim().toLowerCase();
            int newX = x, newY = y;
            switch (input) {
                case "w" -> newX--;
                case "a" -> newY--;
                case "s" -> newX++;
                case "d" -> newY++;
                case "e" -> {
                    // Можно выйти только если стоим на X
                    if (cave[x][y] == CaveEditor.EXIT) {
                        System.out.println("Вы выбрались из пещеры! Возвращаемся на основную карту.");
                        return new int[] { mapX, mapY };
                    } else {
                        System.out.println("Выходить можно только с клетки 'X' (выход)!");
                        continue;
                    }
                }
                default -> { continue; }
            }
            // Проверка на границы и стены
            if (newX < 0 || newY < 0 || newX >= cave.length || newY >= cave[0].length)
                continue;
            if (cave[newX][newY] == CaveEditor.WALL)
                continue;

            x = newX; y = newY;

            // Механика мины
            if (cave[x][y] == CaveEditor.MINE) {
                System.out.println("Вы наступили на мину! Потери в армии.");
                // Пример: снимаем 1 юнита первого типа, если есть
                if (!hero.getArmy().isEmpty()) {
                    Unit firstUnit = hero.getArmy().get(0);
                    if (firstUnit.getAmount() > 1) {
                        firstUnit.setAmount(firstUnit.getAmount() - 1);
                        System.out.println("Один юнит потерян.");
                    } else {
                        hero.getArmy().remove(0);
                        System.out.println("Отряд уничтожен.");
                    }
                } else {
                    System.out.println("Армия пуста, но выживший герой!");
                }
                cave[x][y] = CaveEditor.FLOOR;
            }
            // Механика сокровища
            if (cave[x][y] == CaveEditor.TREASURE) {
                System.out.println("Вы нашли сокровище! +100 золота.");
                playerCastle.addGold(100);
                cave[x][y] = CaveEditor.FLOOR;
            }
        }
    }



    private boolean shouldAutoSave() {
        // Автосохранение будет происходить:
        // 1. После каждого 5-го хода
        // 2. После покупки здания
        // 3. После найма героя
        // 4. После завершения битвы
        return turnCount % 5 == 0 || buildingPurchased || heroHired || battleEnded;
    }

    private BuildingType chooseBuilding() throws IOException { //Выбираем что построить
        console.clearScreen();
        console.flush();
        console.println("You have " + playerCastle.getGold() + " gold!");
        List<Building> buildings = playerCastle.getBuildings();
        console.println("Already built: ");
        for (Building building : buildings) {
            console.println(building.getName() + "(" + building.getLevel() + ")");
        }
        String input = console.readLine("""
                (0 - TAVERN)
                (1 - STABLE)
                (2 - BARRACKS)
                (3 - RANGE)
                (4 - SMITH)
                (5 - ACADEMY)
                (6 - CHURCH)
                (7 - ELECTROHOUSE)
                (anykey - back)
                action>""");
        int index;
        return switch (input) {
            case "0", "1", "2", "3", "4", "5", "6", "7" -> {
                index = Integer.parseInt(input); //Заменяем целвм числом
                yield BuildingType.values()[index]; //Достаём нужный тип здания
            }
            default -> null;
        };
    }

    private boolean hireArmy(Castle castle, Hero hero) throws IOException {
        console.clearScreen();
        console.flush();
        console.println("You have " + playerCastle.getGold() + " gold!");
        List<Unit> army = hero.getArmy();
        console.println("Current Army: ");
        for (Unit unit : army) {
            console.println(unit.getType().toString() + ": " + unit.getAmount());
        }
        String input = console.readLine("(0 - SPEARMAN|cost: " + new Unit(hero, UnitType.SPEARMAN, 1).getPrice() + ")\n" +
                                               "(1 - CROSSBOWMAN|cost: " + new Unit(hero, UnitType.CROSSBOWMAN, 1).getPrice() + ")\n" +
                                               "(2 - SWORDSMAN|cost: " + new Unit(hero, UnitType.SWORDSMAN, 1).getPrice() + ")\n" +
                                               "(3 - CAVALRYMAN|cost: " + new Unit(hero, UnitType.CAVALRYMAN, 1).getPrice() + ")\n" +
                                               "(4 - PALADIN|cost: " + new Unit(hero, UnitType.PALADIN, 1).getPrice() + ")\n" +
                                               "(anykey - back)\n" +
                                               "action>");
        int index;
        UnitType type = switch (input) {
            case "0", "1", "2", "3", "4" -> {
                index = Integer.parseInt(input);
                yield UnitType.values()[index];
            }
            default -> null;
        };

        if (type == null)
            return false;

        if (castle.getBuildings().stream().noneMatch(b -> b.getUnitType() == type)) { //В замке нету нужного здания выбранного юнита
            return false;
        }

        String input_amount = console.readLine("Type amount(Max amount: " + castle.getGold() / (new Unit(hero, type, 1).getPrice()) + "):");
        int amount = Integer.parseInt(input_amount);
        hero.buyUnit(type, amount);

        return true;
    }

    public void printDefaultMapPreview() {
        System.out.println("""
        \nDefault Map Preview:
        +-----------+
        |P         #|
        | # + + + # |
        |  # # # #  |
        |   + + +   |
        |    ###    |
        |   + + +   |
        |  # # # #  |
        | # + + + # |
        |#         C|
        +-----------+
        Key: P=Your Castle, C=Enemy Castle, #=Wall, +=Road
        """);
    }

    private void computerTurn() throws IOException {
        // Логика бота: атака -> движение -> найм юнитов
        bot.performMapAction();

        if(isBattle) {
            battleMap.startFight();
            Hero loser = battleMap.getLoser();
            loser.getHomeCastle().getHeroes().remove(loser);
            isBattle = false;
            map.updateMap(playerCastle, computerCastle);
            map.printMap();
        }

        map.updateMap(playerCastle, computerCastle);
        map.printMap();
    }
    public void addUnitsKilled(int killed) {
        unitsKilledByPlayer += killed;
    }
    public void setMapNameUsed(String mapName) {
        this.mapNameUsed = mapName;
    }

    public boolean isGameOver() {
        if (gameEndCountdown <= 0)
            return true;

        boolean heroInEnemyCastle = false;

        for (Hero hero : computerCastle.getHeroes()) {
            if (playerCastle.isHeroInCastle(hero)) {
                winner = computerCastle;
                gameEndCountdown--;
                return false;
            }
        }
        for (Hero hero : playerCastle.getHeroes()) {
            if (computerCastle.isHeroInCastle(hero)) {
                winner = playerCastle;
                gameEndCountdown--;
                return false;
            }
        }

        if (winner != null) {
            // отсчёт уже идёт, уменьшаем
            gameEndCountdown--;
        }

        return false;
    }


    public Hero isBattle(Castle enemy, int x, int y) {
        for(Hero enemyHero:enemy.getHeroes())
        {
            if(enemyHero.getxCoord() == x && enemyHero.getyCoord() == y)
                return enemyHero;
        }
        return null;
    }


    public void initializeGame() throws IOException {
        String selectedMap = selectMap();
        if (playerName == null) {
            System.out.print("Enter your name: ");
            playerName = console.readLine();
        }
        if ("default".equals(selectedMap)) {
            // Стандартная инициализация
            map = new GameMap(11);
        } else {
            // Загрузка пользовательской карты
            MapEditor editor = MapEditor.loadMap(selectedMap);
            map = new GameMap(editor.getGrid());
        }

        console = new ConsoleReader();
        playerCastle = new Castle(5000, 'P', '1', 0, 0);
        computerCastle = new Castle(5000, 'C', '2', map.getSize()-1, map.getSize()-1);

        map.updateMap(playerCastle, computerCastle);
        map.printMap();

        bot = new BotAI(map, computerCastle, this);
    }

    public Castle getPlayerCastle() {
        if (playerCastle == null) {
            throw new IllegalStateException("GameLogic not initialized. Call initializeGame() first.");
        }
        return playerCastle;
    }

    public Castle getComputerCastle() {
        if (computerCastle == null) {
            throw new IllegalStateException("GameLogic not initialized. Call initializeGame() first.");
        }
        return computerCastle;
    }

    public void setBattleMap(BattleMap map) {
        battleMap = map;
        isBattle = true;
    }

    private void chooseElectroWallAction() throws IOException {
        String input = "";
        do {
            map.updateMap(playerCastle, computerCastle);
            map.printMap();

            ElectroWall wall = playerCastle.getElectroWall();

            if(wall != null) {
                int x = wall.getxCoord();
                int y = wall.getyCoord();
                int size = map.getSize();

                input = console.readLine(
                        """
                                (W - up)
                                (A - left)
                                (S - down)
                                (D - right)
                                (Q - back to map)
                                action>""");
                switch (input) {
                    case "w" -> {
                        if (x - 1 >= 0)
                            wall.move(Direction.UP);

                    }
                    case "a" -> {
                        if (y - 1 >= 0)
                            wall.move(Direction.LEFT);
                    }
                    case "s" -> {
                        if (x + 1 < size)
                            wall.move(Direction.DOWN);
                    }
                    case "d" -> {
                        if (y + 1 + 5 < size)
                            wall.move(Direction.RIGHT);
                    }
                    case "q" -> {
                        break;
                    }
                }
            } else {
                input = console.readLine(
                        """
                                (B - place wall)
                                (Q - back to map)
                                action>""");
                switch (input) {
                    case "b" -> {
                        if (playerCastle.getElectroWall() == null
                                && playerCastle.getElectroWallCooldown() <= 0
                                && playerCastle.getBuildingByType(BuildingType.ELECTROHOUSE) != null)
                            playerCastle.placeWall(map.getSize() / 2, map.getSize() / 2 - 2);
                        map.updateMap(playerCastle, computerCastle);
                        map.printMap();
                    }
                    case "q" -> {
                        break;
                    }
                }
            }
            map.updateMap(playerCastle, computerCastle);
            map.printMap();
        } while(!input.equals("q"));
    }

    private void quitGame() throws IOException {
        console.clearScreen();
        console.flush();
        String input = console.readLine("Are you sure you want to quit? (y/n)");
        if (input.equals("y")) {
            System.exit(0);
        } else {
            return;
        }
        gameTime.stop();
    }
    public GameMap getGameMap() {
        return GameMap;
    }
    public BotAI getBot() {
        return bot;
    }
    public Castle determineWinner() {
        if (playerCastle.isEnemyHeroInCastle(computerCastle)) {
            return computerCastle;
        }
        if (computerCastle.isEnemyHeroInCastle(playerCastle)) {
            return playerCastle;
        }
        return null;
    }

}

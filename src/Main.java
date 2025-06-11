import Buildings.Castle;
import GameLogic.GameLogic;
import jline.console.ConsoleReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ConsoleReader console = new ConsoleReader();
        try {
            GameLogic game = new GameLogic();

            // Главный игровой цикл
            while (true) {
                console.clearScreen();
                console.println("=== ГЛАВНОЕ МЕНЮ ===");
                console.println("1. Начать игру");
                console.println("2. Редактор карт");
                console.println("3. Выход");

                String choice = console.readLine("Выберите вариант: ").trim();

                switch (choice) {
                    case "1":
                        startGameSession(game, console);
                        break;
                    case "2":
                        game.startMapEditor();
                        break;
                    case "3":
                        return;
                    default:
                        console.println("Неверный ввод!");
                }
            }
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void startGameSession(GameLogic game, ConsoleReader console)
            throws IOException, InterruptedException, ClassNotFoundException {
        Castle winner = game.startGame();
        console.clearScreen();
        if (winner != null) {
            if (winner.getSymbol() == 'P') {
                console.println("=== ВЫ ПОБЕДИЛИ! ===");
            } else {
                console.println("=== ВЫ ПРОИГРАЛИ ===");
            }
        }
        console.readLine("Нажмите Enter для продолжения...");
    }
}
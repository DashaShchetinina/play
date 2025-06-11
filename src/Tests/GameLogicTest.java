package Tests;

import Buildings.Castle;
import Entities.Hero;
import GameMap.GameMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import GameLogic.GameLogic;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.IOException;
import java.util.logging.*;

public class GameLogicTest {
    private GameLogic game;
    private Castle playerCastle;
    private Castle computerCastle;

    // --- Логгер ---
    private static final Logger logger = Logger.getLogger(GameLogicTest.class.getName());
    static {
        try {
            Handler fileHandler = new FileHandler("game_logic_test.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Ошибка инициализации логгера: " + e.getMessage());
        }
    }
    // --------------

    @BeforeEach
    public void setUp() throws Exception {
        game = new GameLogic();

        // SEVERE — доступ к приватным полям через reflection (явное нарушение инкапсуляции)
        logger.severe("Доступ к приватным полям класса GameLogic через reflection — нарушение инкапсуляции!");

        // Инициализируем необходимые компоненты без полного запуска игры
        Field mapField = GameLogic.class.getDeclaredField("map");
        mapField.setAccessible(true);
        mapField.set(game, new GameMap(11));

        playerCastle = new Castle(5000, 'P', '1', 0, 0);
        computerCastle = new Castle(5000, 'C', '2', 10, 10);

        Field playerCastleField = GameLogic.class.getDeclaredField("playerCastle");
        playerCastleField.setAccessible(true);
        playerCastleField.set(game, playerCastle);

        Field computerCastleField = GameLogic.class.getDeclaredField("computerCastle");
        computerCastleField.setAccessible(true);
        computerCastleField.set(game, computerCastle);
    }

    @Test
    public void testPlayerVictoryWhenHeroInEnemyCastle() throws Exception {
        // Получаем героя игрока
        Hero playerHero = playerCastle.getHeroes().get(0);

        // INFO — обычное логгирование, если нужно просто информировать
        logger.info("Устанавливаем координаты героя для проверки условия победы.");

        playerHero.setxCoord(computerCastle.getxCoord());
        playerHero.setyCoord(computerCastle.getyCoord());

        assertTrue(computerCastle.isHeroInCastle(playerHero),
                "Hero should be inside enemy castle for victory condition");

        // SEVERE — повторно логируем нарушение инкапсуляции при использовании reflection
        logger.severe("Изменение приватного поля gameEndCountdown через reflection — нарушение инкапсуляции!");

        Field gameEndCountdownField = GameLogic.class.getDeclaredField("gameEndCountdown");
        gameEndCountdownField.setAccessible(true);
        gameEndCountdownField.set(game, 1);

        Method isGameOver = GameLogic.class.getDeclaredMethod("isGameOver");
        isGameOver.setAccessible(true);
        isGameOver.invoke(game);

        Field winnerField = GameLogic.class.getDeclaredField("winner");
        winnerField.setAccessible(true);
        Castle winner = (Castle) winnerField.get(game);

        // SEVERE — если тест провален, пишем в логи
        if (!playerCastle.equals(winner)) {
            logger.severe("Внимание! Нарушение логики игры: победитель не определён корректно.");
        }

        assertEquals(playerCastle, winner,
                "Player should win when their hero is in enemy castle");

        // Альтернативная проверка через determineWinner()
        Castle determinedWinner = game.determineWinner();
        assertEquals(playerCastle, determinedWinner);

        // INFO — тест успешно завершён
        logger.info("Тест testPlayerVictoryWhenHeroInEnemyCastle успешно пройден.");
    }
}

package Tests;

import Entities.*;
import GameLogic.BattleMap;
import Buildings.Castle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.logging.*;

public class AttackExecutionTest {
    // Инициализация логгера
    private static final Logger logger = Logger.getLogger(AttackExecutionTest.class.getName()); //
    static {
        try {
            // Логи будут писаться в файл attack_execution_test.log, handler то как и куда пишем,
            Handler fileHandler = new FileHandler("attack_execution_test.log", true); //записываем в один
            fileHandler.setFormatter(new SimpleFormatter()); //формать вывода
            logger.addHandler(fileHandler); //в файл add обработчик
            logger.setUseParentHandlers(false); // Не выводим в консоль
        } catch (IOException e) {
            System.err.println("Ошибка инициализации логгера: " + e.getMessage());
        }
    }

    // Механика атаки в бою между юнитами
    @Test
    public void testCorrectAttackExecution() throws Exception {
        // WARNING — доступ к файловой системе (логгер пишет лог в файл)
        logger.warning("Доступ к файловой системе для логирования результатов теста.");

        // Создаем замок и двух героев
        Castle dummyCastle = new Castle(1000, 'P', 'H', 0, 0);
        Hero hero1 = dummyCastle.getHeroes().get(0);
        Hero hero2 = new Hero(dummyCastle, 'E');

        // Создаем атакующего: PALADIN (урон = 10), 2 юнита
        Unit paladin = new Unit(hero1, UnitType.PALADIN, 2);
        // Создаем жертву: SPEARMAN (здоровье = 3), 5 юнитов
        Unit spearman = new Unit(hero2, UnitType.SPEARMAN, 5);

        paladin.setxCoord(5); paladin.setyCoord(5);
        spearman.setxCoord(5); spearman.setyCoord(6);

        BattleMap battleMap = new BattleMap(9, hero1, hero2);

        int goldReward = battleMap.attack(paladin, spearman);

        // Проверка, что юниты уничтожены
        assertTrue(spearman.isDead(), "Юниты должны быть мертвы после атаки");
        assertEquals(0, spearman.getAmount(), "Ни один юнит не должен остаться");
        assertEquals(0, spearman.getCurrentHealth(), "Текущее здоровье должно быть ноль");

        // Проверка награды за убийство
        int expectedGold = 5 * Math.max(spearman.getPrice() / 2, 1);
        assertEquals(expectedGold, goldReward, "Награда за убийство юнитов должна быть корректной");

        // INFO — тест успешно пройден
        logger.info("Тест testCorrectAttackExecution успешно пройден.");
    }
}

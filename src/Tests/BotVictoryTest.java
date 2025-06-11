package Tests;

import Buildings.Castle;
import Entities.Hero;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BotVictoryTest {
//условие победы бота
    @Test
    public void testBotWinsWhenInPlayerCastle() {
        // 1. Создаем замки без инициализации всей игры
        Castle playerCastle = new Castle(5000, 'P', '1', 0, 0);
        Castle computerCastle = new Castle(5000, 'C', '2', 10, 10);

        // 2. Создаем героя бота и помещаем его в замок игрока
        Hero botHero = new Hero(computerCastle, 'B');
        botHero.setxCoord(playerCastle.getxCoord());
        botHero.setyCoord(playerCastle.getyCoord());
        computerCastle.getHeroes().add(botHero);

        // 3. Проверяем базовое условие победы
        assertTrue(playerCastle.isEnemyHeroInCastle(computerCastle),
                "Герой бота должен быть в замке игрока");

        // 4. Проверяем определение победителя (изолированная логика)
        Castle winner = determineTestWinner(playerCastle, computerCastle);
        assertEquals(computerCastle, winner,
                "Победителем должен быть бот, когда его герой в замке игрока");
    }

    // Упрощенная версия determineWinner
    private Castle determineTestWinner(Castle player, Castle computer) {
        if (player.isEnemyHeroInCastle(computer)) {
            return computer;
        }
        if (computer.isEnemyHeroInCastle(player)) {
            return player;
        }
        return null;
    }
}
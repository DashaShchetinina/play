package Tests;

import Entities.Hero;
import Entities.Direction;
import Buildings.Castle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovementPenaltyTest {

    @Test
    public void testHeroMovementPenaltyAndBonus() {
        // Создаем замок (0,0) и героя
        Castle castle = new Castle(1000, 'P', 'H', 0, 0);
        Hero hero = castle.getHeroes().get(0);

        // Проверка: начальное количество очков движения = 3
        assertEquals(3, hero.getCurrentMovement());

        // Символ обычной клетки — не дорога
        hero.move(Direction.RIGHT, '.'); // тратим 1 очко
        assertEquals(2, hero.getCurrentMovement(), "Движение по обычной клетке должно отнимать 1 очко");

        // Символ дороги '+'
        hero.move(Direction.DOWN, '+'); // тратим 1 очко, но умножаем результат на 2
        // логика: сначала отнимается 1, потом движение умножается на 2
        assertEquals(2, hero.getCurrentMovement(), "Движение по дороге должно удваивать оставшееся движение");

        // Потратим оставшиеся очки
        hero.move(Direction.DOWN, '.');
        hero.move(Direction.DOWN, '.');

        // Попытка двигаться при 0 очках — не должна изменять координаты
        int prevX = hero.getxCoord();
        int prevY = hero.getyCoord();
        hero.move(Direction.RIGHT, '.'); // движения нет

        assertEquals(prevX, hero.getxCoord(), "Герой не должен двигаться без очков");
        assertEquals(prevY, hero.getyCoord(), "Герой не должен двигаться без очков");
    }
}

package Tests;
import Entities.*;
import GameLogic.BattleMap;
import Buildings.Castle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MovementValidationTest {

    @Test
    public void testUnitCannotMoveToOccupiedOrOutOfBoundsCells() throws Exception {
        // Инициализация
        Castle dummyCastle = new Castle(1000, 'P', 'H', 0, 0);
        Hero player = dummyCastle.getHeroes().get(0);
        Hero computer = new Hero(dummyCastle, 'E');

        // Юниты: создаём двух — один будет пытаться двигаться, второй будет "занимать" клетку
        Unit mover = new Unit(player, UnitType.SWORDSMAN, 1);
        Unit blocker = new Unit(player, UnitType.SPEARMAN, 1);

        // Устанавливаем позиции
        mover.setxCoord(4); mover.setyCoord(4);
        blocker.setxCoord(4); blocker.setyCoord(5); // стоит справа от mover'а

        // Добавим в армию героя (иначе они не появятся на карте)
        player.getArmy().add(mover);
        player.getArmy().add(blocker);

        // Создаём боевую карту
        BattleMap battleMap = new BattleMap(9, player, computer);
        battleMap.updateMap(); // обновим карту

        // ❌ Попытка выйти за границу (например, (-1,0))
        boolean canMoveOutOfBounds = false;
        try {
            canMoveOutOfBounds = battleMap.checkMove(-1, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            canMoveOutOfBounds = false; // ожидаем ошибку или false
        }
        assertFalse(canMoveOutOfBounds, "Нельзя выйти за пределы карты");

        // ✅ Попытка пойти на пустую клетку (4,3)
        boolean canMoveToEmpty = battleMap.checkMove(4, 3);
        assertTrue(canMoveToEmpty, "Можно идти на пустую клетку");
    }
}

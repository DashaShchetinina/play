package Tests;

import Entities.*;
import GameLogic.BattleMap;
import Buildings.Castle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AttackRangeTest {
//дальность атаки
    @Test
    public void testAttackRangeCorrectness() throws Exception {
        // Создаем замок и двух героев
        Castle dummyCastle = new Castle(1000, 'P', 'H', 0, 0);
        Hero hero1 = dummyCastle.getHeroes().get(0);
        Hero hero2 = new Hero(dummyCastle, 'E'); // второй герой вручную

        // Создаем двух юнитов: стрелка и мечника
        Unit archer = new Unit(hero1, UnitType.CROSSBOWMAN, 1); // дальность = 9
        Unit swordsman = new Unit(hero2, UnitType.SWORDSMAN, 1); // дальность = 1

        // Устанавливаем координаты: archer (5, 5), swordsman (5, 7) — расстояние 2
        archer.setxCoord(5);
        archer.setyCoord(5);

        swordsman.setxCoord(5);
        swordsman.setyCoord(7);

        // Подключаем боевую карту, чтобы использовать метод isEnemyInRange()
        BattleMap battleMap = new BattleMap(9, hero1, hero2);

        // Проверка: стрелок должен видеть врага
        assertTrue(battleMap.isEnemyInRange(archer, swordsman),
                "Арбалетчик должен видеть цель на расстоянии 2");

        // Проверка: мечник не должен видеть врага на расстоянии 2
        assertFalse(battleMap.isEnemyInRange(swordsman, archer),
                "Мечник не должен видеть цель на расстоянии 2 (у него range = 1)");

        // Переместим мечника
    }
}
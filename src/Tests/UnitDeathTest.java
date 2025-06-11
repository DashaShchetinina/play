package Tests;

import Entities.*;
import Buildings.Castle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
//смерть юнита
public class UnitDeathTest {

    @Test
    public void testUnitDiesWhenHealthReachesZero() {
        // Создаем героя и юнита
        Castle dummyCastle = new Castle(1000, 'P', 'H', 0, 0);
        Hero hero = dummyCastle.getHeroes().get(0);
        Unit unit = new Unit(hero, UnitType.SWORDSMAN, 1); // мечник

        // Проверяем, что юнит жив
        assertFalse(unit.isDead(), "Юнит должен быть жив изначально");

        // Наносим смертельный урон
        unit.acceptDamage(100); // намного больше его здоровья

        // Проверяем, что юнит мертв
        assertTrue(unit.isDead(), "Юнит должен быть мертв после получения смертельного урона");
        assertEquals(0, unit.getAmount(), "Количество юнитов должно быть 0 после смерти");
    }
}
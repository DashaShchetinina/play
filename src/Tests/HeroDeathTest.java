package Tests;

import Entities.*;
import Buildings.Castle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
// смерть героя
public class HeroDeathTest {

    @Test
    public void testHeroDiesWhenNoUnitsRemain() {
        // Создаем героя и одного юнита
        Castle dummyCastle = new Castle(1000, 'P', 'H', 0, 0);
        Hero hero = dummyCastle.getHeroes().get(0);
        Unit unit = new Unit(hero, UnitType.SWORDSMAN, 1);

        // Добавляем юнита в армию героя
        hero.getArmy().clear(); // на всякий случай очищаем
        hero.getArmy().add(unit);

        // Проверяем, что армия непуста
        assertFalse(hero.getArmy().isEmpty(), "Армия героя должна содержать хотя бы одного юнита");

        // Убиваем юнита
        unit.acceptDamage(1000); // наносим очень большой урон
        hero.getArmy().removeIf(Unit::isDead);

        // Проверяем, что армия теперь пуста
        assertTrue(hero.getArmy().isEmpty(), "Армия героя должна быть пуста после смерти всех юнитов");
    }
}


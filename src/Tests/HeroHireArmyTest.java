package Tests;

import Entities.*;
import Buildings.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HeroHireArmyTest {
// покупка в замке и вне замка
    @Test
    public void testHeroCanHireArmyOnlyInCastle() {
        // Создаем замок и героя
        Castle castle = new Castle(5000, 'P', 'H', 0, 0);
        Hero hero = castle.getHeroes().get(0);

        // Строим казарму (BARRACKS), чтобы нанимать копейщиков (SPEARMAN)
        castle.buyBuilding(BuildingType.BARRACKS);
        castle.newTurn(); // сбрасываем запрет на строительство в этом ходу

        // Проверяем, что герой сейчас в замке
        assertTrue(castle.isHeroInCastle(hero), "Герой должен быть в замке");

        // Пробуем нанять юнитов (в замке) — должно работать
        int goldBefore = castle.getGold();
        hero.buyUnit(UnitType.SPEARMAN, 5); // покупаем 5 копейщиков

        assertFalse(hero.getArmy().isEmpty(), "Армия героя должна пополниться после найма");
        assertTrue(hero.getArmy().stream().anyMatch(u -> u.getType() == UnitType.SPEARMAN),
                "Армия должна содержать копейщиков");

        assertTrue(castle.getGold() < goldBefore, "Золото должно уменьшиться после найма армии");

        // Теперь перемещаем героя ВНЕ замка
        hero.setxCoord(2);
        hero.setyCoord(2);

        // Пытаемся ещё раз купить юнитов
        int goldBeforeSecondAttempt = castle.getGold();
        hero.buyUnit(UnitType.SPEARMAN, 5);

        // Армия не должна измениться, золото не должно уменьшиться
        int spearmanCount = (int) hero.getArmy().stream()
                .filter(u -> u.getType() == UnitType.SPEARMAN)
                .count();
        assertEquals(1, spearmanCount, "Новых юнитов нельзя нанять вне замка");

        assertEquals(goldBeforeSecondAttempt, castle.getGold(), "Золото не должно уменьшаться вне замка при найме");
    }
}

package Tests;
import Entities.*;
import Buildings.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
// передвижение героя
public class HeroCastleLogicTest {

    @Test
    public void testHeroActionsDependOnCastlePresence() {
        // Создаем замок и героя
        Castle castle = new Castle(5000, 'P', 'H', 0, 0);
        Hero hero = castle.getHeroes().get(0);

        // Проверяем, что герой изначально в замке
        assertTrue(castle.isHeroInCastle(hero), "Герой должен находиться в замке при создании");

        // Имитируем выход героя из замка
        hero.setxCoord(1);
        hero.setyCoord(1);

        // Проверяем, что теперь герой НЕ в замке
        assertFalse(castle.isHeroInCastle(hero), "Герой не должен находиться в замке после выхода");

        // Возвращаем героя обратно
        hero.setxCoord(castle.getxCoord());
        hero.setyCoord(castle.getyCoord());

        // Проверка наличия бонуса к перемещению после постройки конюшни
        castle.buyBuilding(BuildingType.STABLE);
        int baseMovement = hero.getCurrentMovement();

        hero.newTurn(); // обновляем ход, бонус должен примениться

        assertTrue(hero.getCurrentMovement() > baseMovement,
                "Бонус конюшни должен увеличивать очки движения, если герой в замке");

        // Проверяем, что без нахождения в замке бонус не применяется
        hero.setxCoord(2);
        hero.setyCoord(2);

        hero.newTurn();
        assertEquals(hero.getCurrentMovement(), hero.getHomeCastle().getHeroes().get(0).getCurrentMovement(),
                "Герой не должен получать бонус к движению вне замка");
    }
}
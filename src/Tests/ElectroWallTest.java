package Tests;

import GameMap.GameMap;
import Buildings.BuildingType;
import Entities.ElectroWall;
import Buildings.Building;
import Buildings.Castle;
import Entities.Hero;
import Entities.Unit;
import Entities.UnitType;
import GameLogic.GameLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ElectroWallTest {

    private Castle castle;
    private Hero hero;
    private Unit enemyUnit;
    private ElectroWall electroWall;
    private GameMap gameMap;

    @BeforeEach //найтройка игрового теста, запуск перед каждым тестом
    public void setUp() throws IOException { //ошибка ввода-вывода
        // Создаём замок и героя
        castle = new Castle(1000, 'P', 'H', 0, 0); // gold, castleSymbol, heroSymbol, x, y
        hero = castle.getHeroes().get(0); // герой создаётся автоматически в конструкторе Castle

        // Покупаем ELECTROHOUSE (чтобы можно было ставить стену)
        castle.buyBuilding(BuildingType.ELECTROHOUSE);

        // Размещаем ElectroWall
        castle.placeWall(2, 2); // ставим стену на (2, 2)
        electroWall = castle.getElectroWall();

        // Создаём вражеского юнита рядом со стеной
        Hero enemyHero = new Hero(new Castle(1000, 'E', 'X', 5, 5), 'X');
        enemyUnit = new Unit(enemyHero, UnitType.SWORDSMAN, 1); // тип SWORDSMAN (у него есть здоровье)
        enemyUnit.setxCoord(2);
        enemyUnit.setyCoord(3); // ставим рядом со стеной (2, 3)

        // Инициализируем карту
        gameMap = new GameMap(10); // размер 10x10
        gameMap.setSymbol(2, 2, '^'); // символ стены
        gameMap.setSymbol(2, 3, enemyUnit.getSymbol()); // ставим юнита на карту
    }

    @Test
    public void testElectroWallDamagesEnemyUnits() {
        // 1. Создаем вражеского героя с юнитом рядом со стеной
        Castle enemyCastle = new Castle(1000, 'E', 'X', 5, 5);
        Hero enemyHero = new Hero(enemyCastle, 'X');
        Unit enemyUnit = new Unit(enemyHero, UnitType.SWORDSMAN, 1);
        enemyUnit.setxCoord(2);
        enemyUnit.setyCoord(3);
        enemyHero.addUnit(enemyUnit);

        // 2. Проверяем начальное здоровье
        int healthBefore = enemyUnit.getCurrentHealth();

        // 3. Получаем урон стены
        int wallDamage = electroWall.getDamage();
        assertTrue(wallDamage > 0);

        // 4. Наносим урон ВРАЖЕСКОМУ герою
        enemyHero.damageFromTheWall(castle);  // Теперь работает!

        // 5. Проверяем результат
        int healthAfter = enemyUnit.getCurrentHealth();
        assertTrue(healthAfter < healthBefore,
                "Enemy unit should take damage from wall. Damage: " + wallDamage);
    }

    @Test
    public void testElectroWallPlacement() {
        assertNotNull(electroWall, "ElectroWall should be placed by Castle");
        assertEquals(2, electroWall.getxCoord(), "Wall should be at x=2");
        assertEquals(2, electroWall.getyCoord(), "Wall should be at y=2");
    }

    @Test
    public void testElectroWallBelongsToCastle() throws Exception {
        Field field = ElectroWall.class.getDeclaredField("homeCastle");
        field.setAccessible(true);
        Castle homeCastle = (Castle) field.get(electroWall);
        assertEquals(castle, homeCastle);
    }
}
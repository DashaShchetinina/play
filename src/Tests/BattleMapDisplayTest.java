package Tests;

import Entities.*;
import GameLogic.BattleMap;
import Buildings.Castle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
//размещение юноитов на поле боя
public class BattleMapDisplayTest {

    @Test
    public void testBattleMapDisplayCorrectness() throws Exception {
        // Создаем замок, героев и юнитов
        Castle dummyCastle = new Castle(1000, 'P', 'H', 0, 0);
        Hero playerHero = dummyCastle.getHeroes().get(0);
        Hero computerHero = new Hero(dummyCastle, 'E');

        // Создаем юниты
        Unit playerUnit = new Unit(playerHero, UnitType.SWORDSMAN, 1);
        Unit computerUnit = new Unit(computerHero, UnitType.SPEARMAN, 1);

        // Устанавливаем координаты юнитов
        playerUnit.setxCoord(7);
        playerUnit.setyCoord(2);

        computerUnit.setxCoord(1);
        computerUnit.setyCoord(3);

        // Добавляем юниты в армию
        playerHero.getArmy().clear();
        computerHero.getArmy().clear();
        playerHero.getArmy().add(playerUnit);
        computerHero.getArmy().add(computerUnit);

        // Создаем боевую карту
        BattleMap battleMap = new BattleMap(9, playerHero, computerHero);

        // Обновляем карту
        battleMap.updateMap();

        // Проверяем, что юниты стоят на своих местах
        assertEquals(playerUnit.getSymbol(), battleMap.getSymbol(playerUnit.getxCoord(), playerUnit.getyCoord()),
                "Юнит игрока должен корректно отображаться на карте");

        assertEquals(computerUnit.getSymbol(), battleMap.getSymbol(computerUnit.getxCoord(), computerUnit.getyCoord()),
                "Юнит компьютера должен корректно отображаться на карте");

        // Проверяем, что остальные клетки пустые или радиус обозначен '*'
        for (int i = 0; i < battleMap.getSize(); i++) {
            for (int j = 0; j < battleMap.getSize(); j++) {
                char symbol = battleMap.getSymbol(i, j);
                boolean isPlayerHere = (i == playerUnit.getxCoord() && j == playerUnit.getyCoord());
                boolean isComputerHere = (i == computerUnit.getxCoord() && j == computerUnit.getyCoord());

                if (!isPlayerHere && !isComputerHere) {
                    assertTrue(symbol == ' ' || symbol == '*',
                            "Ожидается пустая клетка или символ дальности '*', но найдено: " + symbol);
                }
            }
        }
    }
}

package Tests;

import Buildings.*;
import Entities.*;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//работа зданий
public class BuildingFunctionalityTest {

    @Test
    public void testAllBuildingsConstructionAndEffects() {
        // Создаем замок с большим запасом золота
        Castle castle = new Castle(5000, 'P', 'H', 0, 0);

        // Проверяем: изначально зданий нет
        assertTrue(castle.getBuildings().isEmpty(), "Изначально зданий быть не должно"); //после создания в замке нет зданий

        // Перебираем все типы зданий
        for (BuildingType type : BuildingType.values()) {
            int initialGold = castle.getGold();
            castle.buyBuilding(type);

            // Проверка: здание должно появиться в списке
            boolean hasBuilding = castle.getBuildings()
                    .stream()
                    .anyMatch(b -> b.getType() == type);
            assertTrue(hasBuilding, "Замок должен иметь здание типа: " + type);

            // Проверка: золото должно уменьшиться на стоимость здания
            int buildingCost = new Building(type).getCost();
            assertEquals(initialGold - buildingCost, castle.getGold(),
                    "Количество золота должно уменьшиться после покупки " + type);

            // Обновляем ход, чтобы можно было строить следующее здание
            castle.newTurn();
        }

        // Проверяем, что все здания построены
        List<Building> allBuildings = castle.getBuildings();
        assertEquals(BuildingType.values().length, allBuildings.size(),
                "Все здания должны быть построены");
    }
}

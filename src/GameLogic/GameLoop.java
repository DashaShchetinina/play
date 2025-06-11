package GameLogic;

import Buildings.*;
import Entities.*;
import java.util.Arrays;

public class GameLoop {
    public static void main(String[] args) {
        // Создаем игровые здания
        ServiceBuilding hotel = new ServiceBuilding(
                BuildingType.HOTEL,  // Вместо строки передаем BuildingType
                5, Arrays.asList(
                new Service("Короткий отдых", 24 * 60, "health", 2),
                new Service("Длинный отдых", 3 * 24 * 60, "health", 3)
        )
        );
        ServiceBuilding cafe = new ServiceBuilding(
                BuildingType.CAFE,  // Используем соответствующий BuildingType
                12, Arrays.asList(
                new Service("Просто перекус", 15, "move", 2),
                new Service("Плотный обед", 30, "move", 3)
        )
        );
        ServiceBuilding barbershop = new ServiceBuilding(
                BuildingType.BARBERSHOP,  // Используем соответствующий BuildingType
                2, Arrays.asList(
                new Service("Просто стрижка", 10, "none", 0),
                new Service("Модная стрижка", 30, "capture_speed", 1)
        )
        );

        // Массив зданий
        ServiceBuilding[] buildings = new ServiceBuilding[] { hotel, cafe, barbershop };

        // Создаем объект GameTime
        GameTime gameTime = new GameTime();

        // Создаем замок игрока (пример)
        Castle playerCastle = new Castle(5000, 'P', '1', 0, 0);

        // Запускаем 10 NPC
        TownNPC[] npcs = new TownNPC[10];
        for (int i = 0; i < 10; i++) {
            npcs[i] = new TownNPC("NPC-" + (i + 1), buildings, gameTime, playerCastle);  // Создание NPC
            npcs[i].start();
        }

        // Главный игровой цикл
        while (true) {
            try {

                Thread.sleep(10000); // Ждем 1 секунду реального времени (или более)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

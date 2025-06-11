package Entities;

import Buildings.Castle;
import Buildings.Service;
import Buildings.ServiceBuilding;
import GameLogic.GameTime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class TownNPC extends Thread {
    private final ServiceBuilding[] serviceBuildings;
    private final GameTime gameTime;
    private final Castle playerCastle;
    private boolean running = true;
    private long busyUntil = 0; // игровое время до которого NPC занят (в мс)
    private ServiceBuilding currentBuilding = null;
    private String name;

    public TownNPC(String name, ServiceBuilding[] buildings, GameTime gameTime, Castle playerCastle) {
        this.name = name;
        this.serviceBuildings = buildings;
        this.gameTime = gameTime;
        this.playerCastle = playerCastle;
    }

    @Override
    public void run() {
        while (running) {
            long now = gameTime.getCurrentGameTime();

            // Если NPC не занят, выбираем новое действие
            if (now >= busyUntil) {
                if (currentBuilding != null) {
                    // Освобождаем здание (NPC выходит)
                    currentBuilding.release(name);
                    logActionToFile(name + " вышел из " + currentBuilding.getBuildingName());
                    currentBuilding = null;
                }

                // Выбираем новое здание и услугу случайно
                int buildingIdx = (int)(Math.random() * serviceBuildings.length);
                ServiceBuilding building = serviceBuildings[buildingIdx];
                int serviceIdx = (int)(Math.random() * building.getServices().size());
                Service service = building.getServices().get(serviceIdx);

                long finishTime = now + gameTime.convertToMilliseconds(service.getDurationMinutes());
                if (building.tryEnter(name, finishTime)) {
                    logActionToFile(name + " зашел в " + building.getBuildingName() + " на услугу: " + service.getName());
                    busyUntil = finishTime;
                    currentBuilding = building;
                } else {
                    logActionToFile(name + " не смог попасть в " + building.getBuildingName() + " — занято");
                    busyUntil = now + gameTime.convertToMilliseconds(5); // ждем 5 игровых минут
                }
            }
            // Спим небольшой промежуток, чтобы не грузить процессор, но не привязываемся к игровому времени!
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void logActionToFile(String actionLog) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("npc_actions.log", true))) {
            writer.write(actionLog);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл: " + e.getMessage());
        }
    }

    public void stopNPC() {
        running = false;
        this.interrupt();
    }
}

package Tests;

import org.junit.jupiter.api.Test;
import Buildings.Castle;
import Entities.Hero;
import GameMap.GameMap;
import saves.GameSaveState;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameSaveStateTest{

    @Test
    public void testSaveAndLoadGameState() throws Exception {
        GameMap map = new GameMap(5);
        Castle playerCastle = new Castle(5000, 'P', '1', 0, 0); // <-- используем '1' для symbol
        Castle computerCastle = new Castle(5000, 'C', '2', 4, 4);

        // Можно добавить героя в замок, если нужно проверить героев
        // playerCastle.hireHero('H'); // если hireHero возвращает void, не присваивай переменной

        // --- Сохраняем состояние
        saves.GameSaveState saveState = new saves.GameSaveState(
                "TestPlayer",
                map,
                playerCastle,
                computerCastle,
                true,
                null,
                null // HeroState, если нужно
        );

        // --- Сохраняем в файл
        String filename = "test_save.sav";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(saveState);
        }

        // --- Загружаем обратно
        saves.GameSaveState loadedSave;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            loadedSave = (saves.GameSaveState) ois.readObject();
        }

        // --- Проверки
        assertEquals("TestPlayer", loadedSave.getPlayerName());
        assertEquals('P', loadedSave.getPlayerCastle().getSymbol()); // исправили ожидание
        assertEquals(5000, loadedSave.getPlayerCastle().getGold());
        assertEquals(0, loadedSave.getPlayerCastle().getXCoord());
        assertEquals(0, loadedSave.getPlayerCastle().getYCoord());
        // и т.д.

        // Удаляем файл после теста (по желанию)
        new File(filename).delete();
    }
}

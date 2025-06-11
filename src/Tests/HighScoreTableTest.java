package Tests;

import highscores.HighScoreEntry;
import highscores.HighScoreTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HighScoreTableTest {

    private static final String HIGHSCORE_FILE = "game_data/highscores.dat";

    @AfterEach
    public void cleanup() {
        // Очищаем файл после теста, чтобы тесты были изолированы
        File file = new File(HIGHSCORE_FILE);
        if (file.exists()) file.delete();
    }

    @Test
    public void testAddAndLoadHighScores() {
        HighScoreTable table = new HighScoreTable();

        // Добавляем рекорд
        HighScoreEntry entry1 = new HighScoreEntry(
                "Player1", "default", 150, 5, 10, new Date()
        );
        table.addOrUpdateEntry(entry1);

        // Сохраняется ли рекорд?
        List<HighScoreEntry> entries = table.getTopEntries();
        assertEquals(1, entries.size());
        assertEquals("Player1", entries.get(0).getPlayerName());
        assertEquals(150, entries.get(0).getScore());

        // Добавляем хуже рекорд для того же игрока — не должен обновиться
        HighScoreEntry entry1_worse = new HighScoreEntry(
                "Player1", "default", 120, 3, 15, new Date()
        );
        table.addOrUpdateEntry(entry1_worse);

        entries = table.getTopEntries();
        assertEquals(1, entries.size());
        assertEquals(150, entries.get(0).getScore()); // всё ещё 150

        // Добавляем лучший рекорд — должен обновиться
        HighScoreEntry entry1_better = new HighScoreEntry(
                "Player1", "default", 200, 10, 8, new Date()
        );
        table.addOrUpdateEntry(entry1_better);

        entries = table.getTopEntries();
        assertEquals(1, entries.size());
        assertEquals(200, entries.get(0).getScore());

        // Добавляем ещё одного игрока
        HighScoreEntry entry2 = new HighScoreEntry(
                "Player2", "default", 180, 8, 12, new Date()
        );
        table.addOrUpdateEntry(entry2);

        entries = table.getTopEntries();
        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(e -> e.getPlayerName().equals("Player1")));
        assertTrue(entries.stream().anyMatch(e -> e.getPlayerName().equals("Player2")));
    }

    @Test
    public void testLimitToFiveScores() {
        HighScoreTable table = new HighScoreTable();

        // Добавляем 6 разных игроков
        for (int i = 1; i <= 6; i++) {
            HighScoreEntry entry = new HighScoreEntry(
                    "P" + i, "default", 100 * i, i, 10 + i, new Date()
            );
            table.addOrUpdateEntry(entry);
        }

        List<HighScoreEntry> entries = table.getTopEntries();
        assertEquals(5, entries.size()); // должно остаться только топ-5

        // Проверяем, что лучший рекорд на первом месте
        assertEquals("P6", entries.get(0).getPlayerName());
        assertEquals(600, entries.get(0).getScore());
    }
}

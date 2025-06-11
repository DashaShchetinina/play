package highscores;

import java.io.*;
import java.util.*;

public class HighScoreTable {
    private static final String SCORES_FILE = "game_data/score/highscores.dat";
    private List<HighScoreEntry> entries = new ArrayList<>();

    public HighScoreTable() {
        load();
    }

    public void addOrUpdateEntry(HighScoreEntry entry) { //запись и обновление
        Optional<HighScoreEntry> existing = entries.stream()
                .filter(e -> e.getPlayerName().equals(entry.getPlayerName()))
                .findFirst();
        if (existing.isPresent()) {
            if (entry.getScore() > existing.get().getScore()) {
                entries.remove(existing.get());
                entries.add(entry);
            }
        } else {
            entries.add(entry);
        }
        Collections.sort(entries);
        if (entries.size() > 5) {
            entries = new ArrayList<>(entries.subList(0, 5));
        }
        save();
    }

    public List<HighScoreEntry> getTopEntries() { //сортирует и возвращает список
        Collections.sort(entries);
        return entries;
    }

    private void load() {
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            entries = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            entries = (List<HighScoreEntry>) ois.readObject();
        } catch (Exception e) {
            entries = new ArrayList<>();
        }
    }

    private void save() {
        try {
            File file = new File(SCORES_FILE);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(entries);
            }
        } catch (Exception e) {
            System.err.println("Ошибка сохранения рекордов: " + e.getMessage());
        }
    }
}

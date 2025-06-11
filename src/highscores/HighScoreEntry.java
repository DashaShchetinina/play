package highscores;

import java.io.Serializable;
import java.util.Date;

public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    private final String playerName;
    private final String mapName;
    private final int score;
    private final int unitsKilled;
    private final int turnsTaken;
    private final Date date;

    public HighScoreEntry(String playerName, String mapNameUsed, int score, int unitsKilled, int turnsTaken, Date date) {
        this.playerName = playerName;
        this.mapName = mapNameUsed;
        this.score = score;
        this.unitsKilled = unitsKilled;
        this.turnsTaken = turnsTaken;
        this.date = date;
    }

    public String getPlayerName() { return playerName; }
    public String getMapName() { return mapName; }
    public int getScore() { return score; }
    public int getUnitsKilled() { return unitsKilled; }
    public int getTurnsTaken() { return turnsTaken; }
    public Date getDate() { return date; }

    @Override
    public int compareTo(HighScoreEntry other) {
        return Integer.compare(other.score, this.score);
    }
}

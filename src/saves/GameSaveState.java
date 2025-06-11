package saves;

import java.io.Serializable;
import java.util.Date;
import Buildings.Castle;
import Entities.Hero;
import GameMap.GameMap;

public class GameSaveState implements Serializable {
    private String playerName;
    private char[][] mapGrid;
    private CastleState playerCastle;
    private CastleState computerCastle;
    private boolean isPlayerTurn;
    private Date saveDate;
    private HeroState playerHero;

    public GameSaveState(String playerName, char[][] mapGrid, Castle playerCastle, Castle computerCastle, boolean isPlayerTurn, Hero playerGold) {}

    public GameSaveState(String playerName, GameMap map, Castle playerCastle,
                         Castle computerCastle, boolean isPlayerTurn, Hero playerGold, HeroState botsave) {
        this.playerName = playerName;
        this.mapGrid = map.getGridCopy();
        this.playerCastle = new CastleState(playerCastle);
        this.computerCastle = new CastleState(computerCastle);
        this.isPlayerTurn = isPlayerTurn;
        this.saveDate = new Date();
    }

    public GameSaveState(String playerName, GameMap map, Castle playerCastle, Castle computerCastle, boolean isPlayerTurn, Hero playerGold) {
    }

    // Геттеры и сеттеры
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public char[][] getMapGrid() { return mapGrid; }
    public void setMapGrid(char[][] mapGrid) { this.mapGrid = mapGrid; }

    public CastleState getPlayerCastle() { return playerCastle; }
    public void setPlayerCastle(CastleState playerCastle) { this.playerCastle = playerCastle; }

    public CastleState getComputerCastle() { return computerCastle; }
    public void setComputerCastle(CastleState computerCastle) { this.computerCastle = computerCastle; }

    public boolean isPlayerTurn() { return isPlayerTurn; }
    public void setPlayerTurn(boolean playerTurn) { isPlayerTurn = playerTurn; }

    public HeroState getPlayerHero() {
        return playerHero; // Возвращаем сохраненное состояние героя
    }

    public Date getSaveDate() { return saveDate; }
    public void setSaveDate(Date saveDate) { this.saveDate = saveDate; }
}

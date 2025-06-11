package saves;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import Buildings.Building;
import Buildings.BuildingType;
import Buildings.Castle;
import Entities.Hero;
import Entities.Unit;

public class CastleState implements Serializable {
    private int gold;
    private List<BuildingState> buildings;
    private List<HeroState> heroes;
    private int xCoord;
    private int yCoord;
    private char symbol;

    public CastleState(Castle castle) {
        this.gold = castle.getGold();
        this.xCoord = castle.getxCoord();
        this.yCoord = castle.getyCoord();
        this.symbol = castle.getSymbol();

        // Сохраняем здания
        this.buildings = new ArrayList<>();
        for (Building building : castle.getBuildings()) {
            this.buildings.add(new BuildingState(building));
        }

        // Сохраняем героев
        this.heroes = new ArrayList<>();
        for (Hero hero : castle.getHeroes()) {
            this.heroes.add(new HeroState(hero));
        }
    }

    // Геттеры
    public int getGold() { return gold; }
    public List<BuildingState> getBuildings() { return buildings; }
    public List<HeroState> getHeroes() { return heroes; }
    public int getXCoord() { return xCoord; }
    public int getYCoord() { return yCoord; }
    public char getSymbol() { return symbol; }
}

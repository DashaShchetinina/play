package saves;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Buildings.Castle;
import Entities.Hero;
import Entities.Unit;
import Entities.UnitType;

public class HeroState implements Serializable {
    private char symbol;
    private List<UnitState> army;
    private int xCoord;
    private int yCoord;

    public HeroState(Hero hero) {
        this.symbol = hero.getSymbol();
        this.xCoord = hero.getxCoord();
        this.yCoord = hero.getyCoord();

        this.army = new ArrayList<>();
        for (Unit unit : hero.getArmy()) {
            this.army.add(new UnitState(unit));
        }
    }

    public Hero toHero(Castle homeCastle) {
        Hero hero = new Hero(homeCastle, symbol);
        hero.setxCoord(xCoord);
        hero.setyCoord(yCoord);

        for (UnitState unitState : army) {
            hero.addUnit(unitState.toUnit(hero)); // передаём героя как владельца юнита
        }

        return hero;
    }

    // Геттеры
    public char getSymbol() { return symbol; }
    public List<UnitState> getArmy() { return army; }
    public int getXCoord() { return xCoord; }
    public int getYCoord() { return yCoord; }
}

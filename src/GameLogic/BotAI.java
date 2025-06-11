package GameLogic;

import java.io.IOException;
import java.util.List;

import Entities.Hero;
import Entities.Unit;
import Entities.UnitType;
import Entities.Direction;
import Buildings.Castle;
import GameMap.GameMap;
import Buildings.BuildingType;

public class BotAI {
    private int turnNumber;
    private GameMap map;
    private Castle castle;
    private BattleMap battleMap;
    private Castle enemy;
    private Hero enemyHero;
    private GameLogic gameLogic;

    public BotAI(GameMap map, Castle castle, GameLogic gameLogic) throws IOException {
        this.map = map;
        this.castle = castle;
        this.gameLogic = gameLogic;
        enemy = gameLogic.getPlayerCastle();
        turnNumber = 0;
    }

    public BotAI(BattleMap map, Castle castle) throws IOException {
        this.battleMap = map;
        this.castle = castle;
        turnNumber = 0;
    }

    public void performMapAction() throws IOException {
        turnNumber++;
        List<Hero> computerHeroes = castle.getHeroes();
        if(computerHeroes.isEmpty()) {
            if(castle.canHireHero(enemy)) {
                castle.hireHero('O');
                turnNumber = 1;
            } else
            {
                if(!castle.isHaveBuilding(BuildingType.TAVERN))
                    castle.buyBuilding(BuildingType.TAVERN);
                castle.newTurn();
                return;
            }
        }
        int x = computerHeroes.get(0).getxCoord();
        int y = computerHeroes.get(0).getyCoord();
        if (turnNumber == 1) {
            if(!castle.isHaveBuilding(BuildingType.CHURCH))
                castle.buyBuilding(BuildingType.CHURCH);
            computerHeroes.get(0).buyUnit(UnitType.PALADIN, Math.min(10, castle.getGold() / (new Unit(null, UnitType.PALADIN, 1).getPrice())));
        }
        if (turnNumber == 2) {
            if(!castle.isHaveBuilding(BuildingType.TAVERN))
                castle.buyBuilding(BuildingType.TAVERN);
            computerHeroes.get(0).buyUnit(UnitType.PALADIN, Math.min(10, castle.getGold() / (new Unit(null, UnitType.PALADIN, 1).getPrice())));
        }
        if (turnNumber == 3) {
            if(!castle.isHaveBuilding(BuildingType.RANGE))
                castle.buyBuilding(BuildingType.RANGE);
            computerHeroes.get(0).buyUnit(UnitType.CROSSBOWMAN, Math.min(100, castle.getGold() / (new Unit(null, UnitType.CROSSBOWMAN, 1).getPrice())));
        }
        if (turnNumber == 4) {
            if(!castle.isHaveBuilding(BuildingType.ACADEMY))
                castle.buyBuilding(BuildingType.ACADEMY);
            computerHeroes.get(0).buyUnit(UnitType.CAVALRYMAN, Math.min(15, castle.getGold() / (new Unit(null, UnitType.CAVALRYMAN, 1).getPrice())));

            while(computerHeroes.get(0).getCurrentMovement() > 0)
            {
                x = computerHeroes.get(0).getxCoord();
                y = computerHeroes.get(0).getyCoord();
                String sym = map.getSymbol(x, y) + "";
                if(sym.equals("V") || sym.equals("v") || sym.equals("^"))
                {
                    computerHeroes.get(0).damageFromTheWall(enemy);
                    if(computerHeroes.get(0).isDead()) {
                        computerHeroes.remove(0);
                        return;
                    }
                }

                map.setSymbol(x, y, computerHeroes.get(0).getPrevSymbol());
                if (x - 1 >= 0 && y - 1 >= 0) {
                    if ((enemyHero = gameLogic.isBattle(enemy, x - 1, y - 1)) == null) {
                        computerHeroes.get(0).move(Direction.UPLEFT, map.getSymbol(x - 1, y - 1));
                    } else {
                        gameLogic.setBattleMap(new BattleMap(9, enemyHero, computerHeroes.get(0)));
                        break;
                    }
                }

            }
        }
        if(turnNumber >= 5) {
            while(computerHeroes.get(0).getCurrentMovement() > 0)
            {
                x = computerHeroes.get(0).getxCoord();
                y = computerHeroes.get(0).getyCoord();
                String sym = map.getSymbol(x, y) + "";

                if(sym.equals("V") || sym.equals("v") || sym.equals("^"))
                {
                    computerHeroes.get(0).damageFromTheWall(enemy);
                    if(computerHeroes.get(0).isDead()) {
                        computerHeroes.remove(0);
                        return;
                    }
                }

                if(enemy.isHeroInCastle(computerHeroes.get(0)))
                    return;
                map.setSymbol(x, y, computerHeroes.get(0).getPrevSymbol());
                if (x - 1 >= 0 && y - 1 >= 0)
                    if ((enemyHero = gameLogic.isBattle(enemy, x - 1, y - 1)) == null)
                        computerHeroes.get(0).move(Direction.UPLEFT, map.getSymbol(x - 1, y - 1));
                    else {
                        gameLogic.setBattleMap(new BattleMap(9, enemyHero, computerHeroes.get(0)));
                        return;
                    }
            }
        }
        castle.newTurn();
    }

    public void performBattleAction() {
        Hero playerHero = battleMap.getPlayerHero();
        Hero computerHero = battleMap.getComputerHero();

        List<Unit> playerArmy = playerHero.getArmy();
        List<Unit> computerArmy = computerHero.getArmy();

        if(playerArmy.isEmpty() || computerArmy.isEmpty())
            return;

        int maxRange = 0;
        Unit selectedUnit = null;
        for (Unit unit : computerArmy) {
            if (unit.getRange() > maxRange) {
                maxRange = unit.getRange();
                selectedUnit = unit;
            }
        }

        int x = selectedUnit.getxCoord();
        int y = selectedUnit.getyCoord();
        while (selectedUnit.getCurrentMovement() > 0) {
            Unit selectedEnemyUnit = null;
            maxRange = 0;

            for(Unit unit : playerArmy) {
                if(battleMap.isEnemyInRange(selectedUnit, unit))
                    if(maxRange < unit.getRange()) {
                        maxRange = unit.getRange();
                        selectedEnemyUnit = unit;
                    }
            }

            if(selectedEnemyUnit != null) {
                battleMap.updateLog("Computer Attacks" + "\n");
                int gold = battleMap.attack(selectedUnit, selectedEnemyUnit);
                computerHero.getHomeCastle().addGold(gold);
                playerHero.removeDeadUnits();
                break;
            }

            battleMap.setSymbol(x, y, selectedUnit.getPrevSymbol());
            if (x + 1 < battleMap.getSize()) {
                if (battleMap.checkMove(x + 1, y)) {
                    selectedUnit.move(Direction.DOWN, battleMap.getSymbol(x + 1, y));
                }
            }
        }

        for (Unit unit : computerHero.getArmy()) {
            unit.newTurn();
        }
    }
}

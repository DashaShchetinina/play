package GameLogic;

import jline.console.ConsoleReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Entities.Hero;
import Entities.Unit;
import Entities.Direction;

public class BattleMap {
    private char[][] grid;
    private int size;
    private Hero playerHero;
    private Hero computerHero;
    private String clearStr;
    private ConsoleReader console = new ConsoleReader();
    private boolean isPlayerTurn = true;

    private Hero winner = null;
    private Hero loser = null;

    Unit selectedUnit = null;
    private int selectedUnitIndex = 0;
    private boolean madeAMove = false;
    private boolean isAttacked = false;
    String log;
    int playerTurnNumber;
    int computerTurnNumber;

    private BotAI botAI;

    public BattleMap(int size, Hero player, Hero computer) throws IOException {
        log = new String();
        this.size = size;
        grid = new char[size][size];
        playerHero = player;
        computerHero = computer;
        botAI = new BotAI(this, computer.getHomeCastle());
        initializeMap();
    }

    public Hero startFight() throws IOException {
        selectedUnit = playerHero.getArmy().get(0);
        selectedUnitIndex = 0;
        playerTurnNumber = computerTurnNumber = 0;
        while (!isBattleOver()) {
            updateMap();
            printMap();
            if (isPlayerTurn) {
                playerTurnNumber++;
                playerTurn();
                updateMap();
                printMap();
            } else {
                computerTurnNumber++;
                computerTurn();
                updateMap();
                printMap();
            }
            isPlayerTurn = !isPlayerTurn;
        }
        return winner;
    }

    private void playerTurn() throws IOException {
        String input;
        if (isBattleOver()) {
            showLog();
            return;
        }
        log += "\n" + "Player Turn " + playerTurnNumber + ":\n";
        if(!playerHero.getArmy().contains(selectedUnit))
        {
            selectedUnit = playerHero.getArmy().get(0);
            selectedUnitIndex = 0;
        }
        do {
            updateMap();
            printMap();
            console.println(playerHero.toString());
            console.println(computerHero.toString());
            console.println("Selected unit: " + selectedUnit.getSymbol());
            input = console.readLine(
                    """
                            (Q - next turn)
                            (R - next unit)
                            (W - up)
                            (A - left)
                            (S - down)
                            (D - right)
                            (WD - up right)
                            (WA - up left)
                            (SA - down left)
                            (SD - down right)
                            (Y - attack)
                            (L - show battle log)
                            action>""");
            chooseAction(input);
            updateMap();
            printMap();
        } while(!input.equals("q"));

        for (Unit unit : playerHero.getArmy()) {
            unit.newTurn();
        }
        madeAMove = false;
        isAttacked = false;
    }

    private void computerTurn()
    {
        log += "\n" + "Computer Turn " + computerTurnNumber + ":\n";
        botAI.performBattleAction();
    }

    private void chooseAction(String input) throws IOException {
        int x = selectedUnit.getxCoord();
        int y = selectedUnit.getyCoord();
        switch (input) {
            case "w" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (x - 1 >= 0) {
                    if(checkMove(x - 1, y)) {
                        selectedUnit.move(Direction.UP, getSymbol(x - 1, y));
                        madeAMove = true;
                    }
                }
            }
            case "a" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (y - 1 >= 0) {
                    if(checkMove(x, y - 1)) {
                        selectedUnit.move(Direction.LEFT, getSymbol(x, y - 1));
                        madeAMove = true;
                    }
                }
            }
            case "s" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (x + 1 < size) {
                    if(checkMove(x + 1, y)) {
                        selectedUnit.move(Direction.DOWN, getSymbol(x + 1, y));
                        madeAMove = true;
                    }
                }
            }
            case "d" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (y + 1 < size) {
                    if(checkMove(x, y + 1)) {
                        selectedUnit.move(Direction.RIGHT, getSymbol(x, y + 1));
                        madeAMove = true;
                    }
                }
            }
            case "wd" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (x - 1 >= 0 && y + 1 < size) {
                    if(checkMove(x - 1, y + 1)) {
                        selectedUnit.move(Direction.UPRIGHT, getSymbol(x - 1, y + 1));
                        madeAMove = true;
                    }
                }
            }
            case "wa" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (x - 1 >= 0 && y - 1 >= 0) {
                    if(checkMove(x - 1, y - 1)) {
                        selectedUnit.move(Direction.UPLEFT, getSymbol(x - 1, y - 1));
                        madeAMove = true;
                    }
                }
            }
            case "sa" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (x + 1 < size && y - 1 >= 0) {
                    if(checkMove(x + 1, y - 1)) {
                        selectedUnit.move(Direction.DOWNLEFT, getSymbol(x + 1, y - 1));
                        madeAMove = true;
                    }
                }
            }
            case "sd" ->
            {
                setSymbol(x, y, selectedUnit.getPrevSymbol());
                if (x + 1 < size && y + 1 < size) {
                    if(checkMove(x + 1, y + 1)) {
                        selectedUnit.move(Direction.DOWNRIGHT, getSymbol(x + 1, y + 1));
                        madeAMove = true;
                    }
                }
            }
            case "r" ->
            {
                if(madeAMove) {
                    break;
                }

                selectedUnitIndex++;
                if (selectedUnitIndex >= playerHero.getArmy().size())
                    selectedUnitIndex = 0;
                selectedUnit = playerHero.getArmy().get(selectedUnitIndex);
            }
            case "y" ->
            {
                if(isAttacked) {
                    return;
                }
                List<Unit> enemyInRange = new ArrayList<>();
                for(Unit enemy: computerHero.getArmy()) {
                    if(isEnemyInRange(selectedUnit, enemy))
                        enemyInRange.add(enemy);
                }
                Unit enemy = selectToAttack(enemyInRange);
                if(enemy == null)
                    break;

                log += "Player Attacks" + "\n";
                int gold = attack(selectedUnit, enemy);
                playerHero.getHomeCastle().addGold(gold);
                isAttacked = true;

                computerHero.removeDeadUnits();
            }
            case "l" ->
            {
                showLog();
            }
            default -> {
            }
        }
    }

    public boolean checkMove(int x, int y)
    {
        return grid[x][y] == ' ' || grid[x][y] == '*';
    }

    public String showLog() throws IOException {
        console.clearScreen();
        console.flush();
        console.readLine(log);
        return log;
    }

    public int attack(Unit unit, Unit enemy)
    {

        int totalUnitHealth = (unit.getAmount() - 1) * unit.getMaxHealth() + unit.getCurrentHealth();
        int unitDamage = unit.getAmount() * unit.getAttack();

        List<Integer> causalities = enemy.acceptDamage(unitDamage);
        int enemyKilled = causalities.get(0);
        int enemySurv = causalities.get(1);

        log += "<" + unit.getOwner().getSymbol() + ">:" + unit.getSymbol() + " attacks <"+ enemy.getOwner().getSymbol()
                + ">:" + enemy.getSymbol() + "  Damage: " + unitDamage
                + "  Killed: " + enemyKilled + "  Survived: " + enemySurv + "\n";

        if(enemy.isDead())
        {
            log += "<"+ enemy.getOwner().getSymbol() + ">:" + enemy.getSymbol() + " is Destroyed!\n";
        }

        return enemyKilled * Math.max(enemy.getPrice() / 2, 1);
    }

    private Unit selectToAttack(List<Unit> enemies) throws IOException {
        if(enemies.isEmpty()) {
            return null;
        }

        console.print("You can attack: ");
        for(Unit unit:enemies)
        {
            console.print(unit.getSymbol() + " ");
        }
        console.println();

        Unit selectedEnemy = null;
        while(selectedEnemy == null)
        {
            String input = console.readLine("Who would it be?(Or Q to exit): ");

            if(input.charAt(0) == 'q')
                return null;

            for (Unit unit : enemies) {
                if (unit.getSymbol() == input.charAt(0)) {
                    selectedEnemy = unit;
                }
            }
        }
        return selectedEnemy;
    }

    public boolean isEnemyInRange(Unit unit, Unit enemy) {
        return distance(unit.getxCoord(), unit.getyCoord(), enemy.getxCoord(), enemy.getyCoord()) <= unit.getRange();
    }

    public boolean isBattleOver() {
        if (playerHero.getArmy().isEmpty()) {
            winner = computerHero;
            loser = playerHero;
            return true;
        }
        if (computerHero.getArmy().isEmpty()) {
            winner = playerHero;
            loser = computerHero;
            return true;
        }
        return false;
    }

    private void initializeMap() throws IOException {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                grid[x][y] = ' ';

            }
        }
        int x = size - 1;
        int y = 1;
        for (Unit unit: playerHero.getArmy()) {
            unit.setxCoord(x);
            unit.setyCoord(y);
            y++;
        }
        x = 0;
        y = 1;
        for (Unit unit: computerHero.getArmy()) {
            unit.setxCoord(x);
            unit.setyCoord(y);
            y++;
        }
    }

    public void updateMap() throws IOException {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                grid[x][y] = ' ';

            }
        }

        for (Unit unit : playerHero.getArmy()) {
            grid[unit.getxCoord()][unit.getyCoord()] = unit.getSymbol();
        }

        for (Unit unit : computerHero.getArmy()) {
            grid[unit.getxCoord()][unit.getyCoord()] = unit.getSymbol();
        }

        if(selectedUnit != null) {
            updateRange(selectedUnit);
        }
    }

    public void updateRange(Unit unit) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (distance(unit.getxCoord(), unit.getyCoord(), i, j) <= unit.getRange()) {
                    if(grid[i][j] == ' ') {
                        grid[i][j] = '*';
                    }
                };
            }
        }
    }

    public int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public void printMap() throws IOException {
        // Вывод карты в консоль
        console.flush();
        console.clearScreen();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                console.print(grid[i][j] + " ");
            }
            console.println();
        }
    }

    public void setSymbol(int x, int y, char symbol){
        grid[x][y] = symbol;
    }

    public void updateLog(String input){
        log += input;
    }

    public char getSymbol(int x, int y){
        return grid[x][y];
    }

    public Hero getWinner(){
        return winner;
    }

    public Hero getLoser(){
        return loser;
    }

    public Hero getPlayerHero(){
        return playerHero;
    }

    public Hero getComputerHero(){
        return computerHero;
    }

    public int getSize(){
        return size;
    }

}
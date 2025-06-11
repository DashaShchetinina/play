package Entities;

import Buildings.BuildingType;
import Buildings.Castle;
import saves.HeroState;
import saves.UnitState;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Hero {
    private List<Unit> army = new ArrayList<>();
    private int movementBonus = 2; // бонус от конюшни
    private Castle homeCastle;
    private int maxMovement = 3;
    private int currentMovement;

    private int xCoord;
    private int yCoord;
    private char symbol;

    private int prevxCoord;
    private int prevyCoord;
    private char prevSymbol;
    private int health;
    private int move;
    private int castleCaptureTime;
    private boolean wasRobbed = false;
    private int robbedHP = 0;
    private int robbedGold = 0;
    private int robbedHour = -1; // Время ограбления (игровой час, не миллисекунды)
    private boolean canCallPolice = false;
    private int policeCalledMinute = -1; // Время вызова полиции (в минутах)
    private List<UnitCompensation> robbedUnits = new ArrayList<>();
    int robbedMinute;
    private long policeCompensateGameTime = -1;
    private long policeCompensateAtGameTime = -1;


    public Hero(Castle homeCastle, char symbol) {
        if (homeCastle == null) {
            throw new IllegalArgumentException("homeCastle cannot be null");
        }
        this.homeCastle = homeCastle;
        currentMovement = maxMovement;
        this.symbol = symbol;
        this.xCoord = homeCastle.getxCoord();
        this.yCoord = homeCastle.getyCoord();
        this.health = 100;
        this.move = 10;
        this.castleCaptureTime = 2;
    }

    public Hero(HeroState state) {
        this.symbol = state.getSymbol();
        this.xCoord = state.getXCoord();
        this.yCoord = state.getYCoord();
        this.army = new ArrayList<>();

        for (UnitState unitState : state.getArmy()) {
            Unit unit = unitState.toUnit(this); // <-- создаём Unit на основе UnitState
            this.army.add(unit);
        }
    }
    public void rob(int currentHour) {
        if (!wasRobbed && !army.isEmpty()) {
            robbedUnits.clear(); // очищаем старое

            int totalArmyHealth = 0;
            for (Unit unit : army) {
                totalArmyHealth += (unit.getAmount() - 1) * unit.getMaxHealth() + unit.getCurrentHealth();
            }
            int lostHP = Math.max(10, (int) (totalArmyHealth * 0.25));
            int damageToEach = lostHP / army.size();
            int totalKilled = 0;

            for (Unit unit : army) {
                int beforeAmount = unit.getAmount();
                int beforeHealth = unit.getCurrentHealth();
                List<Integer> dmgResult = unit.acceptDamage(damageToEach);
                int killed = dmgResult.get(0);

                if (killed > 0) {
                    // Запоминаем, чтобы потом восстановить
                    robbedUnits.add(new UnitCompensation(
                            unit.getType(),
                            killed,
                            beforeHealth
                    ));
                }
                totalKilled += killed;
            }
            removeDeadUnits();

            int lostGold = Math.max(20, (int) (this.getHomeCastle().getGold() * 0.3));
            this.getHomeCastle().addGold(-lostGold);

            this.wasRobbed = true;
            this.robbedHP = lostHP;
            this.robbedGold = lostGold;
            this.robbedHour = currentHour;
            this.canCallPolice = true;
            this.policeCalledMinute = -1;

            System.out.println("Вас ограбили! Армия потеряла в сумме " + lostHP + " HP (убито юнитов: " + totalKilled + "), золото: -" + lostGold);
        }
    }


    public boolean callPolice(long currentGameTime) {
        if (wasRobbed && canCallPolice && policeCompensateAtGameTime == -1) {
            canCallPolice = false;
            policeCompensateAtGameTime = currentGameTime + 1 * 60 * 60 * 1000L; // 3 игровых часа в мс
            System.out.println("Вы вызвали полицию! Деньги и HP вернутся через 3 игровых часа.");
            return true;
        }
        System.out.println("Вы не можете вызвать полицию (или уже ждете компенсацию).");
        return false;
    }

    public boolean shouldCompensate(long currentGameTime) {
        return policeCompensateAtGameTime > 0 && currentGameTime >= policeCompensateAtGameTime;
    }

    public boolean isWaitingCompensation() {
        return policeCompensateAtGameTime > 0 && wasRobbed && !canCallPolice;
    }

    // Время возврата (для сравнения в updateGame)
    public long getPoliceCompensateGameTime() {
        return policeCompensateAtGameTime;
    }


    public void tryCompensate() {
        if (wasRobbed && policeCompensateAtGameTime > 0) {
            // Вернуть золото
            this.getHomeCastle().addGold(robbedGold);

            // Оживить юнитов
            for (UnitCompensation comp : robbedUnits) {
                Unit found = null;
                for (Unit unit : army) {
                    if (unit.getType() == comp.type) {
                        found = unit;
                        break;
                    }
                }
                if (found != null) {
                    found.setAmount(found.getAmount() + comp.amount);
                    found.setCurrentHealth(Math.max(found.getCurrentHealth(), comp.health));
                } else {
                    Unit revived = new Unit(this, comp.type, comp.amount);
                    revived.setCurrentHealth(comp.health);
                    army.add(revived);
                }
            }
            robbedUnits.clear();

            System.out.println("Полиция вернула ваши HP и золото, а также оживила юнитов!");
            this.wasRobbed = false;
            this.robbedHP = 0;
            this.robbedGold = 0;
            this.robbedHour = -1;
            this.policeCalledMinute = -1;
            this.policeCompensateAtGameTime = -1; // сброс таймера
        }
    }




    public boolean canCallPoliceNow(int currentHour) {
        return wasRobbed && canCallPolice && (currentHour - robbedHour <= 10);
    }

    public boolean isRobbed() {
        return wasRobbed;
    }


    public void addUnit(Unit unit) {
        army.add(unit);
    }
    // Другие методы
    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public int getxCoord() {
        return this.xCoord;
    }

    public int getyCoord() {
        return this.yCoord;
    }

    public char getSymbol() {
        return this.symbol;
    }

    public int getPrevxCoord() {
        return this.prevxCoord;
    }

    public int getPrevyCoord() {
        return this.prevyCoord;
    }

    public int getCurrentMovement() {
        return this.currentMovement;
    }

    public char getPrevSymbol() {
        return this.prevSymbol;
    }

    public void move(Direction dir, char symbol)
    {
        if(currentMovement <= 0)
            return;

        currentMovement--;

        prevxCoord = xCoord;
        prevyCoord = yCoord;

        if (prevSymbol != symbol) {
            if (symbol == '+')
                currentMovement *= 2;
        }
        prevSymbol = symbol;



        switch (dir) {
            case UP -> {
                xCoord--;
            }
            case RIGHT -> {
                yCoord++;
            }
            case DOWN -> {
                xCoord++;
            }
            case LEFT -> {
                yCoord--;
            }
            case UPRIGHT -> {
                xCoord--;
                yCoord++;
            }
            case UPLEFT -> {
                xCoord--;
                yCoord--;
            }
            case DOWNLEFT -> {
                xCoord++;
                yCoord--;
            }
            case DOWNRIGHT -> {
                xCoord++;
                yCoord++;
            }
        }
    }

    public void newTurn() {
        currentMovement = maxMovement;
        if (homeCastle.isHeroInCastle(this) && homeCastle.isHaveBuilding(BuildingType.STABLE)) {
            currentMovement += movementBonus;
        }
        for (Unit unit : army) {
            unit.newTurn();
        }
    }

    public List<Unit> getArmy() {
        return army;
    }

    public Castle getHomeCastle() {
        return homeCastle;
    }

    public void buyUnit(UnitType type, int amount) {
        if (!homeCastle.isHeroInCastle(this)) {
            return; //  герой не в замке — нанимать нельзя
        }
        int price = amount * (new Unit(this, type, 1).getPrice());
        if(price > homeCastle.getGold())
            return;
        homeCastle.addGold(-price);

        if (army.stream().anyMatch(b -> b.getType().equals(type))) {
            for (Unit unit : army) {
                if (unit.getType().equals(type)) {
                    unit.setAmount(unit.getAmount() + amount);
                    break;
                }
            }
        } else {
            Unit unit = new Unit(this, type, amount);
            army.add(unit);
        }
    }

    @Override
    public String toString() //возвращает строку с армией героя, заправшивает у юнита
    {
        StringBuilder output = new StringBuilder("<" + this.symbol + ">Army:[ ");
        for (Unit unit : army) {
            output.append(unit.toString()).append("(").append(unit.getAmount()).append(".").append(unit.getCurrentHealth()).append(") ");
        }
        output.append("]");
        return output.toString();
    }

    
    public void damageFromTheWall(Castle enemy)
    {
        ElectroWall wall = enemy.getElectroWall();
        for (Unit unit : army) {
            unit.acceptDamage(wall.getDamage());
        }
        removeDeadUnits();
    }
    // Увеличение здоровья всех юнитов в отряде (например, в том числе для NPC)
    public void increaseHealthAll(int amount) {
        this.health += amount;
        logHeroAction("Здоровье увеличено на " + amount + ", текущее здоровье: " + this.health);
    }

    public void increaseMoveAll(int amount) {
        this.move += amount;
        logHeroAction("Перемещение увеличено на " + amount + ", текущее перемещение: " + this.move);
    }
    public void logHeroAction(String actionLog) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("hero_actions.log", true))) {
            writer.write(actionLog);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл: " + e.getMessage());
        }

        // Выводим действие в консоль
    }

    // Ускорение захвата замка (снижение времени захвата)
    public void setCastleCaptureTime(int newCaptureTime) {
        this.castleCaptureTime = newCaptureTime;
        logHeroAction("Время захвата замка уменьшено до " + this.castleCaptureTime);
    }


    // Методы для получения бонусов
    public int getHealth() {
        return health;
    }

    public int getMove() {
        return move;
    }

    public int getCastleCaptureTime() {
        return castleCaptureTime;
    }

    public void removeDeadUnits()
    {
        army.removeIf(Unit::isDead);
    }

    public boolean isDead()
    {
        return army.isEmpty();
    }
}
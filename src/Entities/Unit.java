package Entities;

import java.util.ArrayList;
import java.util.List;

public class Unit {
    private UnitType type;
    private Hero owner;
    private int amount;
    private int level;
    private int price;
    private int attack;
    private int maxHealth;
    private int currentHealth;
    private int movement;
    private int range;
    private char symbol; // символ для отображения на карте
    private char prevSymbol;
    private int health;
    private int move;

    private int xCoord;
    private int yCoord;

    private int prevxCoord;
    private int prevyCoord;

    private int currentMovement;

    private boolean isDead = false;

    public Unit(Hero owner, UnitType type, int amount) {
        // конструктор
        this.type = type;
        this.owner = owner;
        switch (type) {
            case SPEARMAN -> {
                this.level = 1;
                this.price = 5;
                this.attack = 1;
                this.currentHealth = this.maxHealth = 3;
                this.currentMovement = this.movement = 1;
                this.range = 9;
                this.symbol = 'S';
            }

            case CROSSBOWMAN -> {
                this.level = 2;
                this.price = 10;
                this.attack = 3;
                this.currentHealth = this.maxHealth = 2;
                this.currentMovement = this.movement = 1;
                this.range = 9;
                this.symbol = 'C';
            }

            case SWORDSMAN -> {
                this.level = 3;
                this.price = 20;
                this.attack = 2;
                this.currentHealth = this.maxHealth = 8;
                this.currentMovement = this.movement = 3;
                this.range = 1;
                this.symbol = 'W';
            }

            case CAVALRYMAN -> {
                this.level = 4;
                this.price = 50;
                this.attack = 6;
                this.currentHealth = this.maxHealth = 8;
                this.currentMovement = this.movement = 5;
                this.range = 2;
                this.symbol = 'A';
            }

            case PALADIN -> {
                this.level = 5;
                this.price = 100;
                this.attack = 10;
                this.currentHealth = this.maxHealth = 15;
                this.currentMovement = this.movement = 5;
                this.range = 1;
                this.symbol = 'P';
            }
        }
        this.amount = amount;
        this.health = 50;
        this.move = 5;
    }
    // Увеличение здоровья
    public void increaseHealth(int amount) {
        this.health += amount;
        System.out.println("Здоровье юнита увеличено на " + amount + ", текущее здоровье: " + this.health);
    }

    // Увеличение перемещения
    public void increaseMove(int amount) {
        this.move += amount;
        System.out.println("Перемещение юнита увеличено на " + amount + ", текущее перемещение: " + this.move);
    }
    // Геттеры и сеттеры
    public UnitType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDead() {
        this.isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }
    
    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAttack() {
        return attack;
    }

    public void setCurrentHealth(int amount) {
        this.currentHealth = amount;
    }

    public int getPrice() {
        return price;
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public int getRange() {
        return range;
    }

    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public char getSymbol() {
        return symbol;
    }

    public char getPrevSymbol() {
        return this.prevSymbol;
    }

    public int getCurrentMovement() {
        return this.currentMovement;
    }

    public void move(Direction dir, char symbol)
    {
        if(currentMovement <= 0)
            return;

        currentMovement--;

        prevxCoord = xCoord;
        prevyCoord = yCoord;

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
        currentMovement = movement;
    }

    public Hero getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return this.symbol + "";
    } //возвращает символ юнита и строку (из чар в строку)
// еще используется для вывода армии во время карты и сражения
    public List<Integer> acceptDamage(int damage) { //наносит урон юниту

        int totalHealth = (amount - 1) * maxHealth + currentHealth;
        int totalSurvHealth = totalHealth - damage;
        int enemySurv;
        int enemyHealth;
        int enemyKilled;

        if(totalSurvHealth > 0) {
            enemySurv = totalSurvHealth / maxHealth;
            enemyHealth = totalSurvHealth % maxHealth;
            if(enemyHealth == 0)
            {
                enemySurv--;
                enemyHealth = maxHealth;
            }
        } else
        {
            enemySurv = 0;
            enemyHealth = 0;
            setDead();
        }

        enemyKilled = amount - enemySurv;
        amount = enemySurv;
        currentHealth = enemyHealth;

        List<Integer> output = new ArrayList<Integer>();
        output.add(enemyKilled); //сколько убило
        output.add(enemySurv); //сколько выжило
        return output;
    }
}

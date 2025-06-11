package Entities;

import Buildings.Building;
import Buildings.BuildingType;
import Buildings.Castle;

public class ElectroWall {
    private String symbolString;
    private int wallLength = 5;
    private Castle homeCastle;
    private int maxMovement = 1;
    private int currentMovement;
    private int wallLevel;
    private int maxDuration;
    private int currentDuration;
    private int damage;

    private int xCoord;
    private int yCoord;

    public ElectroWall(Castle castle, int xCoord, int yCoord) { //то как она будет выглядеть
        homeCastle = castle;
        currentMovement = maxMovement;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        symbolString = generateString();
        currentDuration = maxDuration = 3;
        damage = 100 * castle.getBuildingByType(BuildingType.ELECTROHOUSE).getLevel();
    }

    private String generateString() { //длина стены 5 создает строку из 3х символов
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < wallLength; i++) {
            output.append(getRandomSymbol());
        }
        return output.toString();
    }

    private String getRandomSymbol() {
        String chars = "^Vv";
        return chars.charAt((int) (Math.random() * chars.length())) + "";
    }

    public void newTurn(){
        currentMovement = maxMovement;
        currentDuration--;
    }

    public int getxCoord() {
        return this.xCoord;
    }

    public int getyCoord() {
        return this.yCoord;
    }

    public int getDamage() {
        return this.damage;
    }

    public String getString() {
        return this.symbolString;
    } //получаем то как выглядит стена

    public int getCurrentDuration(){
        return this.currentDuration;
    } //сколько осталось жить

    public void move(Direction dir)
    {
        if(currentMovement <= 0)
            return;

        currentMovement--;

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
        }
    }
}

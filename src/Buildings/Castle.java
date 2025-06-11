package Buildings;

import java.util.ArrayList;
import java.util.List;

import Entities.ElectroWall;
import Entities.Hero;
import GameLogic.GameLogic;
import saves.BuildingState;
import saves.CastleState;
import saves.HeroState;

public class Castle {
    private List<Building> buildings = new ArrayList<>();
    private List<Hero> heroes = new ArrayList<>();
    private ElectroWall wall;
    private int gold;
    private char symbol;
    private int wallCooldown;

    private int xCoord;
    private int yCoord;

    private boolean isBuiltThisTurn;

    public Castle(int gold, char castleSymbol, char heroSymbol, int xCoord, int yCoord) {
        this.gold = gold;
        symbol = castleSymbol;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        heroes.add(new Hero(this, heroSymbol));
        wallCooldown = 0;
    }

    public Castle(CastleState computerCastle) {
    }

    public boolean isHeroInCastle() {
        for (Hero hero : heroes) {
            if (hero.getxCoord() == xCoord && hero.getyCoord() == yCoord)
                return true;
        }
        return false;
    }

    public boolean isHeroInCastle(Hero hero) {
        return hero.getxCoord() == xCoord && hero.getyCoord() == yCoord;
    }

    public boolean isEnemyHeroInCastle(Castle castle) {
        for (Hero hero : castle.getHeroes()) {
            if (hero.getxCoord() == xCoord && hero.getyCoord() == yCoord)
                return true;
        }
        return false;
    }

    public boolean isHaveBuilding(BuildingType building) {
        return buildings.stream().anyMatch(b -> b.getType().equals(building));
    }

    public boolean canHireHero(Castle enemy) {
        return !isHeroInCastle() && !isEnemyHeroInCastle(enemy) && isHaveBuilding(BuildingType.TAVERN) && (gold - 100) > 0;
    }

    public void hireHero(char name) {
        gold -= 100;
        heroes.add(new Hero(this, name));
    }

    public List<Hero> getHeroes() {
        return heroes;
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

    public void buyBuilding(BuildingType type) {
        if (!isBuiltThisTurn) {
            Building newBuilding;  // Объявляем переменную для нового здания

            // В зависимости от типа здания создаем нужный конкретный подкласс Building
            switch (type) {
                case TAVERN:
                    newBuilding = new ServiceBuilding(BuildingType.TAVERN, 10, new ArrayList<>());
                    break;
                case STABLE:
                    newBuilding = new ServiceBuilding(BuildingType.STABLE, 10, new ArrayList<>());
                    break;
                case BARRACKS:
                    newBuilding = new ServiceBuilding(BuildingType.BARRACKS, 10, new ArrayList<>());
                    break;
                case RANGE:
                    newBuilding = new ServiceBuilding(BuildingType.RANGE, 10, new ArrayList<>());
                    break;
                case SMITH:
                    newBuilding = new ServiceBuilding(BuildingType.SMITH, 10, new ArrayList<>());
                    break;
                case ACADEMY:
                    newBuilding = new ServiceBuilding(BuildingType.ACADEMY, 10, new ArrayList<>());
                    break;
                case CHURCH:
                    newBuilding = new ServiceBuilding(BuildingType.CHURCH, 10, new ArrayList<>());
                    break;
                case ELECTROHOUSE:
                    newBuilding = new ServiceBuilding(BuildingType.ELECTROHOUSE, 10, new ArrayList<>());
                    break;
                // Добавьте другие типы зданий по аналогии
                default:
                    throw new IllegalArgumentException("Unknown building type: " + type);
            }

            // Проверка, если здание уже существует
            for (Building building : buildings) {
                if (building.getType().equals(type)) {
                    // Если здание - Electrohouse, то делаем его апгрейд
                    if (building.getType().equals(BuildingType.ELECTROHOUSE)) {
                        if (gold - newBuilding.getCost() < 0)
                            return;
                        gold -= newBuilding.getCost();
                        building.upgradeBuilding(1);
                    }
                    return;  // Если здание уже существует, выходим из метода
                }
            }

            // Проверяем, хватает ли золота на покупку здания
            if (gold - newBuilding.getCost() < 0)
                return;

            // Снимаем деньги за покупку нового здания
            gold -= newBuilding.getCost();
            buildings.add(newBuilding);  // Добавляем новое здание в список
            isBuiltThisTurn = true;  // Обновляем флаг, что здание построено в этом ходу
        }
    }


    public void addGold(int in) {
        this.gold += in;
    }

    public int getGold() {
        return this.gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void newTurn() {
        isBuiltThisTurn = false;
        for (Hero hero : heroes) {
            hero.newTurn();
        }

        if(wallCooldown > 0) {
            wallCooldown--;
        }

        if(wall != null) {
            wall.newTurn();
            if(wall.getCurrentDuration() <= 0) {
                wallCooldown = Math.max(0, 3 - getBuildingByType(BuildingType.ELECTROHOUSE).getLevel());
                wall = null;
            }
        }
    }

    public void placeWall(int xCoord, int yCoord) {
        wall = new ElectroWall(this, xCoord, yCoord);
    }

    public Building getBuildingByType(BuildingType type)
    {
        for(Building building : buildings)
        {
            if(building.getType().equals(type))
                return building;
        }
        return null;
    }

    public ElectroWall getElectroWall()
    {
        return wall;
    }

    public int getElectroWallCooldown(){
        return this.wallCooldown;
    }

    // Метод для создания Castle из CastleState
    public static Castle fromState(CastleState state) {
        // Создаем базовый объект Castle
        Castle castle = new Castle(
                state.getGold(),
                state.getSymbol(),
                'H', // Временный символ героя
                state.getXCoord(),
                state.getYCoord()
        );

        // Очищаем автоматически созданного героя
        castle.heroes.clear();

        // Восстанавливаем здания
        for (BuildingState buildingState : state.getBuildings()) {
            Building building;

            // В зависимости от типа здания создаем конкретный класс
            switch (buildingState.getType()) {
                case TAVERN:
                    building = new ServiceBuilding(BuildingType.TAVERN, 10, new ArrayList<>()); // Поставьте нужные параметры для услуги
                    break;
                case STABLE:
                    building = new ServiceBuilding(BuildingType.STABLE, 10, new ArrayList<>());
                    break;
                case BARRACKS:
                    building = new ServiceBuilding(BuildingType.BARRACKS, 10, new ArrayList<>());
                    break;
                // Добавьте другие случаи для других типов зданий
                default:
                    throw new IllegalArgumentException("Unknown building type: " + buildingState.getType());
            }

            building.setLevel(buildingState.getLevel());
            castle.buildings.add(building);
        }

        // Восстанавливаем героев
        for (HeroState heroState : state.getHeroes()) {
            Hero hero = new Hero(castle, heroState.getSymbol());
            hero.setxCoord(heroState.getXCoord());
            hero.setyCoord(heroState.getYCoord());
            castle.heroes.add(hero);
        }

        return castle;
    }
}
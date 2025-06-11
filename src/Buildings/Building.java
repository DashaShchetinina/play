package Buildings;

import Entities.Hero;
import Entities.Unit;
import Entities.UnitType;

import java.util.List;

public class Building {
    private BuildingType type;  // Тип здания
    private UnitType unitType;  // Тип юнитов, которые могут быть созданы в этом здании
    private String name;        // Имя здания
    private int cost;           // Стоимость здания
    private int level;          // Уровень здания

    // Конструктор
    public Building(BuildingType type) {
        this.type = type; // Присваиваем тип зданию

        // Заполняем данные здания в зависимости от типа
        switch (type) {
            case TAVERN -> {
                this.cost = 100;
                this.name = "Tavern";
                this.unitType = null;
            }
            case STABLE -> {
                this.cost = 100;
                this.name = "Stable";
                this.unitType = null;
            }
            case BARRACKS -> {
                this.cost = 100;
                this.name = "Barracks";
                this.unitType = UnitType.SPEARMAN;
            }
            case RANGE -> {
                this.cost = 100;
                this.name = "Range";
                this.unitType = UnitType.CROSSBOWMAN;
            }
            case SMITH -> {
                this.cost = 100;
                this.name = "Smith";
                this.unitType = UnitType.SWORDSMAN;
            }
            case ACADEMY -> {
                this.cost = 100;
                this.name = "Academy";
                this.unitType = UnitType.CAVALRYMAN;
            }
            case CHURCH -> {
                this.cost = 100;
                this.name = "Church";
                this.unitType = UnitType.PALADIN;
            }
            case ELECTROHOUSE -> {
                this.cost = 100;
                this.name = "Electrohouse";
                this.unitType = null;
            }
        }
        this.level = 1; // Изначально уровень здания — 1
    }

    public String getBuildingName() {
        return type.name();  // Возвращаем строковое представление типа здания
    }

    // Методы для применения бонусов
    public void applyBonus(Hero hero, Service service) {
        // Применение бонусов к герою
    }

    public void applyBonus(List<Unit> units, Service service) {
        // Применение бонусов к юнитам
    }

    // Геттеры и сеттеры
    public BuildingType getType() {
        return type;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public int getCost() {
        return cost;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // Метод для апгрейда уровня здания
    public void upgradeBuilding(int level) {
        this.level += level;
    }
}

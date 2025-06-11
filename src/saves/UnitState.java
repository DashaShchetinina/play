package saves;

import java.io.Serializable;

import Entities.Hero;
import Entities.Unit;
import Entities.UnitType;

public class UnitState implements Serializable {
    private UnitType type;       // Тип юнита
    private int amount;          // Количество юнитов
    private int currentHealth;   // Текущее здоровье юнитов

    // Конструктор, инициализируем состояние из юнита
    public UnitState(Unit unit) {
        this.type = unit.getType();
        this.amount = unit.getAmount();
        this.currentHealth = unit.getCurrentHealth();
    }

    // Геттеры и сеттеры
    public UnitType getType() { return type; }
    public int getAmount() { return amount; }
    public int getCurrentHealth() { return currentHealth; }

    // Воссоздание юнита на основе состояния
    public Unit toUnit(Hero owner) {
        Unit unit = new Unit(owner, type, amount);
        unit.setCurrentHealth(currentHealth); // ВОССТАНАВЛИВАЕМ здоровье!
        return unit;
    }
}

package Entities;

public class UnitCompensation {
    UnitType type;
    int amount; // сколько потеряли
    int health; // hp последнего юнита (если нужен возврат hp)
    UnitCompensation(UnitType type, int amount, int health) {
        this.type = type;
        this.amount = amount;
        this.health = health;
    }
}
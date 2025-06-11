package Buildings;

// Описание услуги здания
public class Service {
    private final String name;
    private final int durationMinutes; // Длительность услуги в минутах (можно использовать для дней, если нужно)
    private final String bonusType;    // Тип бонуса: "health", "move", "capture_speed", "none"
    private final int bonusValue;      // Численное значение бонуса

    public Service(String name, int durationMinutes, String bonusType, int bonusValue) {
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.bonusType = bonusType;
        this.bonusValue = bonusValue;
    }

    public String getName() {
        return name;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getBonusType() {
        return bonusType;
    }

    public int getBonusValue() {
        return bonusValue;
    }
}

package saves;

import java.io.Serializable; //в код и обратно

import Buildings.Building;
import Buildings.BuildingType;

public class BuildingState implements Serializable {
    private BuildingType type;
    private int level;

    public BuildingState(Building building) {
        this.type = building.getType();
        this.level = building.getLevel();
    }

    // Геттеры
    public BuildingType getType() { return type; }
    public int getLevel() { return level; }
}
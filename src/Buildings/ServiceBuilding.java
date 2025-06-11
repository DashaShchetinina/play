package Buildings;

import Entities.Hero;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceBuilding extends Building {
    private final Map<String, Long> busyUntil = new ConcurrentHashMap<>();
    private final int maxCapacity;
    private final List<Service> services;
    private final ReentrantLock lock = new ReentrantLock();

    public ServiceBuilding(BuildingType type, int maxCapacity, List<Service> services) {
        super(type);
        this.maxCapacity = maxCapacity;
        this.services = services;
    }
    public boolean isHeroInside(Hero hero) {
        return busyUntil.containsKey(String.valueOf(hero.getSymbol()));
    }

    // Теперь метод принимает только npcName и finishTime (в миллисекундах!)
    public boolean tryEnter(String npcName, long finishTime) {
        lock.lock();
        try {
            if (busyUntil.size() < maxCapacity) {
                busyUntil.put(npcName, finishTime);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void release(String npcName) {
        lock.lock();
        try {
            busyUntil.remove(npcName);
        } finally {
            lock.unlock();
        }
    }

    public void update(long currentTime) {
        lock.lock();
        try {
            busyUntil.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Long> getBusyUntil() {
        return new HashMap<>(busyUntil);
    }

    public List<Service> getServices() {
        return services;
    }

    public void applyBonus(Hero hero, Service service) {
        switch (service.getBonusType()) {
            case "health" -> hero.increaseHealthAll(service.getBonusValue());
            case "move" -> hero.increaseMoveAll(service.getBonusValue());
            case "capture_speed" -> hero.setCastleCaptureTime(service.getBonusValue());
        }
    }
}

package GameLogic;

public class GameTime {
    private volatile long currentGameTime; // в миллисекундах игрового времени
    private final long realTimeToGameTimeRatio = 1000; // 1 реальная секунда = 1000 игровых миллисекунд
    private Thread timeThread;
    private volatile boolean running;

    public GameTime() {
        this.currentGameTime = 0;
        this.running = true;
        startTimeThread();
    }

    private void startTimeThread() {
        timeThread = new Thread(() -> {
            try {
                while (running) {
                    Thread.sleep(500);
                    currentGameTime += 60 * 1000; // +1 игровая минута
                    // 1 реальная секунда = 2 игровые минуты
                    // 3 часа (180 мин) = 90 секунд реального времени
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }

    public void stop() {
        running = false;
        timeThread.interrupt();
    }

    public long getCurrentGameTime() {
        return currentGameTime;
    }

    public long getCurrentGameTimeInMinutes() {
        return currentGameTime / (60 * 1000);
    }

    public int getCurrentHour() {
        return (int)((getCurrentGameTimeInMinutes() / 60) % 24);
    }

    public long convertToMilliseconds(long minutes) {
        return minutes * 60 * 1000; // минуты -> игровые миллисекунды
    }
}
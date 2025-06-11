package Entities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NPCLogger {
    private static final String LOG_FILE = "npc_actions.log";
    public static void logToFile(String logLine) {
        synchronized (NPCLogger.class) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(logLine);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Ошибка записи NPC лога: " + e.getMessage());
            }
        }
    }
}

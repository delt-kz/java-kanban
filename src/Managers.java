import java.nio.file.Paths;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBacked() {
        return FileBackedTaskManager.loadFromFile(Paths.get("src/saves.csv"));
    }
}

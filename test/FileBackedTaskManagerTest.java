import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            return new FileBackedTaskManager(Files.createTempFile("temp", "csv"));
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't create a temporary csv file", e);
        }
    }

    @Test
    public void shouldLoadFromFileCorrectly() throws IOException {
        LocalDateTime dt = LocalDateTime.now();
        Duration duration = Duration.ofDays(2);
        Path path = Files.createTempFile("temp", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(path);
        Task task = new Task(1, "Task" , "task", TaskStatus.NEW, dt, duration);
        manager1.createTask(task);
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(path);
        assertEquals(manager1.getListOfTasks().getFirst(), manager2.getListOfTasks().getFirst());
    }
}
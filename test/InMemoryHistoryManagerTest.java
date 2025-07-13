import manager.InMemoryTaskManager;
import manager.Managers;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryHistoryManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    public void shouldRemoveFromHistoryDeletedTasks() {
        Task task1 = new Task("model.Task", "This is task", TaskStatus.NEW);
        Task task2 = new Task("model.Task", "This is task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.deleteTaskById(task1.getId());
        assertEquals(List.of(task2), manager.getHistory(),
                "Deleting a task does not remove it from the history.");
        manager.deleteAllTasks();
        assertTrue(manager.getHistory().isEmpty(),
                "Deleting all tasks does not remove them from history");
        Epic epic = new Epic("model.Epic", "This is epic");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("model.Subtask", "This is subtask", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
        manager.deleteEpicById(epic.getId());
        assertTrue(manager.getHistory().isEmpty(), "model.Subtask of deleted epic is not removed from history");
    }

    @Test
    public void shouldAddToHistoryAfterUsingGetMethod() {
        Task task = new Task("model.Task", "This is task", TaskStatus.NEW);
        manager.createTask(task);
        Epic epic = new Epic("model.Epic", "This is epic");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("model.Subtask", "This is subtask", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());
        assertEquals(new ArrayList<>(Arrays.asList(task,epic,subtask)),
                manager.getHistory(), "Tasks are not added to history properly");
        manager.getTaskById(task.getId());
        assertEquals(List.of(epic,subtask,task), manager.getHistory(),
                "Duplicate of task in history is not removed");
    }
}

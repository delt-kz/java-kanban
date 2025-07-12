import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.IllegalStateException;


abstract class TaskManagerTest<T extends TaskManager>  {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    public void shouldAssignEpicsTimeCorrectly() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask sb1 = new Subtask("Sb", "sb", TaskStatus.NEW, epicId, startTime, duration);
        manager.createSubtask(sb1);
        assertEquals(epic.getEndTime(), sb1.getEndTime(), "model.Epic with one subtask has different end time from its subtask");
        assertEquals(epic.getStartTime(), sb1.getStartTime(), "model.Epic with one subtask has different start time from its subtask");
        Subtask sb2 = new Subtask("Sb", "sb", TaskStatus.NEW, epicId, startTime.plusMinutes(30), duration);
        manager.createSubtask(sb2);
        assertEquals(epic.getEndTime(), sb2.getEndTime(), "Epics end time is not extended with added subtask");
        Subtask sb3 = new Subtask("Sb", "sb", TaskStatus.NEW, epicId, startTime.minusMinutes(30), duration);
        manager.createSubtask(sb3);
        assertEquals(epic.getStartTime(), sb3.getStartTime(), "Epics start time is not extended with added subtask");
        manager.deleteSubtaskById(sb3.getId());
        assertEquals(epic.getStartTime(), sb1.getStartTime(), "Deleting earliest subtask is not changing epics start time");
        manager.deleteSubtaskById(sb2.getId());
        assertEquals(epic.getEndTime(), sb1.getEndTime(), "Deleting latest subtask is not changing epics end time");
    }

    @Test
    public void shouldNotCreateTasksWithOverlappingTimes() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);
        Task task1 = new Task("model.Task", "task", TaskStatus.NEW, startTime, duration);
        manager.createTask(task1);
        Task task2 = new Task("model.Task", "task", TaskStatus.NEW, startTime.plusMinutes(31), duration);
        assertDoesNotThrow(() -> manager.createTask(task2),
                "model.Task with time interval from future is shown as overlapped");
        Task task3 = new Task("model.Task", "task", TaskStatus.NEW, startTime.minusMinutes(31), duration);
        assertDoesNotThrow(() -> manager.createTask(task3),
                "model.Task with time interval from past is shown as overlapped");
        Task task4 = new Task("model.Task", "task", TaskStatus.NEW, startTime.plusMinutes(29), duration);
        assertThrows(IllegalStateException.class, () -> manager.createTask(task4),
                "model.Task with overlapping interval is not throwing exception");
        Task task5 = new Task("model.Task", "task", TaskStatus.NEW, startTime.minusMinutes(29), duration);
        assertThrows(IllegalStateException.class, () -> manager.createTask(task5),
                "model.Task with overlapping interval is not throwing exception");
    }

    @Test
    public void getPrioritizedTasks_returnsTasksSortedByStartTime() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);
        Epic epic = new Epic("A", "A");
        manager.createEpic(epic);
        Task task1 = new Task("model.Task", "task", TaskStatus.NEW, startTime, duration);
        Subtask subtask  = new Subtask("model.Subtask", "task", TaskStatus.NEW, epic.getId(),  startTime.plusHours(1), duration);
        Task task2 = new Task("model.Task", "task", TaskStatus.NEW, startTime.plusHours(2), duration);
        manager.createTask(task1);
        manager.createSubtask(subtask);
        manager.createTask(task2);
        assertEquals(List.of(task1, subtask, task2), manager.getPrioritizedTasks(), "Method getPrioritizedTasks is not functioning right");
    }

    @Test
    public void shouldAssignEndTimeCorrectlyForTasksNSubtasks() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofDays(2);
        LocalDateTime endTime = startTime.plus(duration);
        Task task = new Task("model.Task", "task", TaskStatus.NEW, startTime, duration);
        assertEquals(task.getEndTime(), endTime, "End time of task is not assigned correctly");
    }

    //edge cases
    @Test
    public void getNonExistingTaskShouldReturnNull() {
        assertNull(manager.getTaskById(999));
    }

    @Test
    public void getNonExistingEpicShouldReturnNull() {
        assertNull(manager.getEpicById(999));
    }

    @Test
    public void getNonExistingSubtaskShouldReturnNull() {
        assertNull(manager.getSubtaskById(999));
    }

    //happy paths
    @Test
    public void shouldUpdateEpicsStatusCorrectly() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        assertEquals(TaskStatus.NEW, epic.getStatus(), "New epic's status is not new");

        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.DONE, epicId);
        manager.createSubtask(subtask1);
        assertEquals(subtask1.getStatus(),
                epic.getStatus(), "model.Epic does not have the same status as its single subtask");

        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask2);
        int subtask2Id = subtask2.getId();
        assertEquals(TaskStatus.IN_PROGRESS,
                epic.getStatus(), "model.Epic's status is not IN_PROGRESS having subtasks with different status");

        manager.deleteSubtaskById(subtask2Id);
        assertEquals(TaskStatus.DONE, epic.getStatus(),
                "model.Epic's status is not updated after removal of subtask");

        manager.createSubtask(subtask2);
        manager.updateSubtaskById(
                new Subtask(subtask2Id, "model.Subtask", "subtask", TaskStatus.DONE, epicId));
        assertEquals(TaskStatus.IN_PROGRESS,
                epic.getStatus(), "model.Epic's status is not updated with subtask");

        manager.deleteAllSubtasks();
        assertEquals(TaskStatus.NEW,
                epic.getStatus(), "model.Epic's status is not reset after deleting all subtasks");
    }

    @Test
    public void epicShouldSaveListOfSubtasksAfterUpdate() {
        Epic oldEpic = new Epic("model.Epic", "epic");
        manager.createEpic(oldEpic);
        int epicId = oldEpic.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        Epic newEpic = new Epic(epicId, "model.Epic", "epic");
        manager.updateEpicById(newEpic);
        assertEquals(List.of(subtask1, subtask2),
                manager.getSubtasksOfEpic(epicId), "List of subtasks was not transferred correctly to new model.Epic");
    }

    @Test
    public void shouldDeleteAllSubtasksIfAllEpicsDeleted() {
        Epic epic1 = new Epic("model.Epic", "epic");
        Epic epic2 = new Epic("model.Epic", "epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic1Id = epic1.getId();
        int epic2Id = epic2.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epic2Id);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteAllEpics();
        assertTrue(manager.getListOfSubtasks().isEmpty(),
                "List of subtasks in not empty after removal of all Epics");
    }

    @Test
    public void shouldDeleteSubtasksOfEpic() {
        Epic epic1 = new Epic("model.Epic", "epic");
        Epic epic2 = new Epic("model.Epic", "epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic1Id = epic1.getId();
        int epic2Id = epic2.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epic2Id);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteEpicById(epic1Id);
        assertEquals(List.of(subtask2),
                manager.getListOfSubtasks(), "model.Subtask of epic was not deleted with epic");
    }

    @Test
    public void shouldReturnSubtasksOfEpicCorrectlyAfterModifications() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        int subtask1Id = subtask1.getId();

        assertFalse(manager.getSubtasksOfEpic(epicId).isEmpty(), "List of subtasks in epic is empty");
        assertEquals(List.of(subtask1, subtask2),
                manager.getSubtasksOfEpic(epicId), "Subtasks were not added to Epics list correctly");


        manager.deleteSubtaskById(subtask1Id);
        assertEquals(List.of(subtask2),
                manager.getSubtasksOfEpic(epicId), "model.Subtask was not removed from Epics list of Subtasks");
    }


    @Test
    public void shouldCreateTaskCorrectly() {
        Task task = new Task("model.Task", "task", TaskStatus.NEW);
        manager.createTask(task);

        assertFalse(manager.getListOfTasks().isEmpty(), "model.Task wasn't added to the list");
    }


    @Test
    public void shouldDeleteAllTasks() {
        Task task1 = new Task("model.Task", "task", TaskStatus.NEW);
        Task task2 = new Task("model.Task", "task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.deleteAllTasks();

        assertTrue(manager.getListOfTasks().isEmpty(), "List is not empty after deleting all Tasks");
    }

    @Test
    public void shouldAssignCorrectIdForTask() {
        Task task1 = new Task("model.Task", "task", TaskStatus.NEW);
        Task task2 = new Task("model.Task", "task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        int task2Id = task2.getId();
        assertNotNull(manager.getTaskById(task2Id), "Couldn't get the task with ID");
        assertEquals(task2, manager.getTaskById(task2Id), "Miss match in task IDs");
    }

    @Test
    public void shouldUpdateTaskCorrectly() {
        Task oldTask = new Task("model.Task", "task", TaskStatus.NEW);
        manager.createTask(oldTask);
        int oldTaskId = oldTask.getId();
        Task newTask = new Task(oldTaskId,"model.Task", "task", TaskStatus.NEW);
        manager.updateTaskById(newTask);
        assertEquals(newTask, manager.getTaskById(oldTaskId), "model.Task is not updating");
    }

    @Test
    public void shouldDeleteTaskById() {
        Task task1 = new Task("model.Task", "task", TaskStatus.NEW);
        Task task2 = new Task("model.Task", "task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        int task1Id = task1.getId();
        manager.deleteTaskById(task1Id);

        assertNull(manager.getTaskById(task1Id));
    }

    @Test
    public void shouldCreateEpicCorrectly() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);

        assertFalse(manager.getListOfEpics().isEmpty(), "model.Epic wasn't added to the list");
    }

    @Test
    public void shouldDeleteAllEpics() {
        Epic epic1 = new Epic("model.Epic", "epic");
        Epic epic2 = new Epic("model.Epic", "epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.deleteAllEpics();

        assertTrue(manager.getListOfEpics().isEmpty(), "List is not empty after deleting all Epics");
    }

    @Test
    public void shouldAssignCorrectIdForEpic() {
        Epic epic1 = new Epic("model.Epic", "epic");
        Epic epic2 = new Epic("model.Epic", "epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic2Id = epic2.getId();
        assertNotNull(manager.getEpicById(epic2Id), "Couldn't get the epic with ID");
        assertEquals(epic2, manager.getEpicById(epic2Id), "Miss match in epic IDs");
    }

    @Test
    public void shouldDeleteEpicById() {
        Epic epic1 = new Epic("model.Epic", "epic");
        Epic epic2 = new Epic("model.Epic", "epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic1Id = epic1.getId();
        manager.deleteEpicById(epic1Id);

        assertNull(manager.getEpicById(epic1Id));
    }

    @Test
    public void shouldUpdateEpicCorrectly() {
        Epic oldEpic = new Epic("model.Epic", "epic");
        manager.createEpic(oldEpic);
        int oldEpicId = oldEpic.getId();
        Epic newEpic = new Epic(oldEpicId,"model.Epic", "epic");
        manager.updateEpicById(newEpic);
        assertEquals(newEpic, manager.getEpicById(oldEpicId), "model.Epic is not updating");
    }

    @Test
    public void shouldCreateSubtaskCorrectly() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask);

        assertFalse(manager.getListOfSubtasks().isEmpty(), "model.Subtask wasn't added to the list");
    }

    @Test
    public void shouldDeleteAllSubtasks() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteAllSubtasks();

        assertTrue(manager.getListOfSubtasks().isEmpty(), "List is not empty after deleting all Subtasks");
    }

    @Test
    public void shouldAssignCorrectIdForSubtasks() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        int subtask2Id = subtask2.getId();
        assertNotNull(manager.getSubtaskById(subtask2Id), "Couldn't get the subtask with ID");
        assertEquals(subtask2, manager.getSubtaskById(subtask2Id), "Miss match in model.Subtask IDs");
    }

    @Test
    public void shouldDeleteSubtaskById() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        int subtask1Id = subtask1.getId();
        manager.deleteSubtaskById(subtask1Id);

        assertNull(manager.getSubtaskById(subtask1Id));
    }

    @Test
    public void shouldUpdateSubtaskCorrectly() {
        Epic epic = new Epic("model.Epic", "epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask oldSubtask = new Subtask("model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(oldSubtask);
        int oldSubtaskId = oldSubtask.getId();
        Subtask newSubtask = new Subtask(oldSubtaskId,"model.Subtask", "subtask", TaskStatus.NEW, epicId);
        manager.updateSubtaskById(newSubtask);
        assertEquals(newSubtask, manager.getSubtaskById(oldSubtaskId), "model.Subtask is not updating");
    }
}
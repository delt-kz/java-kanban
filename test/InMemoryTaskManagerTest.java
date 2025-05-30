import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;


    @BeforeEach
    public void BeforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void shouldRemoveFromHistoryDeletedTasks() {
        Task task1 = new Task("Task", "This is task", TaskStatus.NEW);
        Task task2 = new Task("Task", "This is task", TaskStatus.NEW);
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
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
        manager.deleteEpicById(epic.getId());
        assertTrue(manager.getHistory().isEmpty(), "Subtask of deleted epic is not removed from history");
    }

    @Test
    public void shouldAddToHistoryAfterUsingGetMethod() {
        Task task = new Task("Task", "This is task", TaskStatus.NEW);
        manager.createTask(task);
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epic.getId());
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
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        assertEquals(TaskStatus.NEW, epic.getStatus(), "New epic's status is not new");

        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.DONE, epicId);
        manager.createSubtask(subtask1);
        assertEquals(subtask1.getStatus(),
                epic.getStatus(), "Epic does not have the same status as its single subtask");

        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask2);
        int subtask2Id = subtask2.getId();
        assertEquals(TaskStatus.IN_PROGRESS,
                epic.getStatus(), "Epic's status is not IN_PROGRESS having subtasks with different status");

        manager.deleteSubtaskById(subtask2Id);
        assertEquals(TaskStatus.DONE, epic.getStatus(),
                "Epic's status is not updated after removal of subtask");

        manager.createSubtask(subtask2);
        manager.updateSubtaskById(
                new Subtask(subtask2Id, "Subtask", "This is subtask", TaskStatus.DONE, epicId));
        assertEquals(TaskStatus.IN_PROGRESS,
                epic.getStatus(), "Epic's status is not updated with subtask");

        manager.deleteAllSubtasks();
        assertEquals(TaskStatus.NEW,
                epic.getStatus(), "Epic's status is not reset after deleting all subtasks");
    }

    @Test
    public void epicShouldSaveListOfSubtasksAfterUpdate() {
        Epic oldEpic = new Epic("Epic", "This is epic");
        manager.createEpic(oldEpic);
        int epicId = oldEpic.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        Epic newEpic = new Epic(epicId, "Epic", "This is epic");
        manager.updateEpicById(newEpic);
        assertEquals(new ArrayList<>(Arrays.asList(subtask1, subtask2)),
                manager.getSubtasksOfEpic(epicId), "List of subtasks was not transferred correctly to new Epic");
    }

    @Test
    public void shouldDeleteAllSubtasksIfAllEpicsDeleted() {
        Epic epic1 = new Epic("Epic", "This is epic");
        Epic epic2 = new Epic("Epic", "This is epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic1Id = epic1.getId();
        int epic2Id = epic2.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epic2Id);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteAllEpics();
        assertTrue(manager.getListOfSubtasks().isEmpty(),
                "List of subtasks in not empty after removal of all Epics");
    }

    @Test
    public void shouldDeleteSubtasksOfEpic() {
        Epic epic1 = new Epic("Epic", "This is epic");
        Epic epic2 = new Epic("Epic", "This is epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic1Id = epic1.getId();
        int epic2Id = epic2.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epic2Id);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteEpicById(epic1Id);
        assertEquals(new ArrayList<>(List.of(subtask2)),
                manager.getListOfSubtasks(), "Subtask of epic was not deleted with epic");
    }

    @Test
    public void shouldReturnSubtasksOfEpicCorrectlyAfterModifications() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        int subtask1Id = subtask1.getId();

        assertFalse(manager.getSubtasksOfEpic(epicId).isEmpty(), "List of subtasks in epic is empty");
        assertEquals(new ArrayList<>(Arrays.asList(subtask1, subtask2)),
                manager.getSubtasksOfEpic(epicId), "Subtasks were not added to Epics list correctly");


        manager.deleteSubtaskById(subtask1Id);
        assertEquals(new ArrayList<>(List.of(subtask2)),
                manager.getSubtasksOfEpic(epicId), "Subtask was not removed from Epics list of Subtasks");
    }


    @Test
    public void shouldCreateTaskCorrectly() {
        Task task = new Task("Task", "This is task", TaskStatus.NEW);
        manager.createTask(task);

        assertFalse(manager.getListOfTasks().isEmpty(), "Task wasn't added to the list");
    }


    @Test
    public void shouldDeleteAllTasks() {
        Task task1 = new Task("Task", "This is task", TaskStatus.NEW);
        Task task2 = new Task("Task", "This is task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.deleteAllTasks();

        assertTrue(manager.getListOfTasks().isEmpty(), "List is not empty after deleting all Tasks");
    }

    @Test
    public void shouldAssignCorrectIdForTask() {
        Task task1 = new Task("Task", "This is task", TaskStatus.NEW);
        Task task2 = new Task("Task", "This is task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        int task2Id = task2.getId();
        assertNotNull(manager.getTaskById(task2Id), "Couldn't get the task with ID");
        assertEquals(task2, manager.getTaskById(task2Id), "Miss match in task IDs");
    }

    @Test
    public void shouldUpdateTaskCorrectly() {
        Task oldTask = new Task("Task", "This is task", TaskStatus.NEW);
        manager.createTask(oldTask);
        int oldTaskId = oldTask.getId();
        Task newTask = new Task(oldTaskId,"Task", "This is task", TaskStatus.NEW);
        manager.updateTaskById(newTask);
        assertEquals(newTask, manager.getTaskById(oldTaskId), "Task is not updating");
    }

    @Test
    public void shouldDeleteTaskById() {
        Task task1 = new Task("Task", "This is task", TaskStatus.NEW);
        Task task2 = new Task("Task", "This is task", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        int task1Id = task1.getId();
        manager.deleteTaskById(task1Id);

        assertNull(manager.getTaskById(task1Id));
    }

    @Test
    public void shouldCreateEpicCorrectly() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);

        assertFalse(manager.getListOfEpics().isEmpty(), "Epic wasn't added to the list");
    }

    @Test
    public void shouldDeleteAllEpics() {
        Epic epic1 = new Epic("Epic", "This is epic");
        Epic epic2 = new Epic("Epic", "This is epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.deleteAllEpics();

        assertTrue(manager.getListOfEpics().isEmpty(), "List is not empty after deleting all Epics");
    }

    @Test
    public void shouldAssignCorrectIdForEpic() {
        Epic epic1 = new Epic("Epic", "This is epic");
        Epic epic2 = new Epic("Epic", "This is epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic2Id = epic2.getId();
        assertNotNull(manager.getEpicById(epic2Id), "Couldn't get the epic with ID");
        assertEquals(epic2, manager.getEpicById(epic2Id), "Miss match in epic IDs");
    }

    @Test
    public void shouldDeleteEpicById() {
        Epic epic1 = new Epic("Epic", "This is epic");
        Epic epic2 = new Epic("Epic", "This is epic");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        int epic1Id = epic1.getId();
        manager.deleteEpicById(epic1Id);

        assertNull(manager.getEpicById(epic1Id));
    }

    @Test
    public void shouldUpdateEpicCorrectly() {
        Epic oldEpic = new Epic("Epic", "This is epic");
        manager.createEpic(oldEpic);
        int oldEpicId = oldEpic.getId();
        Epic newEpic = new Epic(oldEpicId,"Epic", "This is epic");
        manager.updateEpicById(newEpic);
        assertEquals(newEpic, manager.getEpicById(oldEpicId), "Epic is not updating");
    }

    @Test
    public void shouldCreateSubtaskCorrectly() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask);

        assertFalse(manager.getListOfSubtasks().isEmpty(), "Subtask wasn't added to the list");
    }

    @Test
    public void shouldDeleteAllSubtasks() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteAllSubtasks();

        assertTrue(manager.getListOfSubtasks().isEmpty(), "List is not empty after deleting all Subtasks");
    }

    @Test
    public void shouldAssignCorrectIdForSubtasks() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        int subtask2Id = subtask2.getId();
        assertNotNull(manager.getSubtaskById(subtask2Id), "Couldn't get the subtask with ID");
        assertEquals(subtask2, manager.getSubtaskById(subtask2Id), "Miss match in Subtask IDs");
    }

    @Test
    public void shouldDeleteSubtaskById() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        int subtask1Id = subtask1.getId();
        manager.deleteSubtaskById(subtask1Id);

        assertNull(manager.getSubtaskById(subtask1Id));
    }

    @Test
    public void shouldUpdateSubtaskCorrectly() {
        Epic epic = new Epic("Epic", "This is epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask oldSubtask = new Subtask("Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.createSubtask(oldSubtask);
        int oldSubtaskId = oldSubtask.getId();
        Subtask newSubtask = new Subtask(oldSubtaskId,"Subtask", "This is subtask", TaskStatus.NEW, epicId);
        manager.updateSubtaskById(newSubtask);
        assertEquals(newSubtask, manager.getSubtaskById(oldSubtaskId), "Subtask is not updating");
    }
}
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int taskIdCounter = 0;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public List<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        task.setId(++taskIdCounter);
        tasks.put(task.getId(), task);
    }

    public void updateTaskById(Task task) {
        tasks.put(task.getId(), task);
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getSubtasksOfEpic(int id) {
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subtaskIds = epics.get(id).getSubtaskIds();
        for (Integer subtaskId : subtaskIds) {
            subtasksOfEpic.add(subtasks.get(subtaskId));
        }
        return subtasksOfEpic;
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createEpic(Epic epic) {
        epic.setId(++taskIdCounter);
        epics.put(epic.getId(), epic);
    }

    public void updateEpicById(Epic newEpic) {
        Epic epic = epics.get(newEpic.getId());
        newEpic.setSubtaskIds(epic.getSubtaskIds());
        updateEpicsStatus(epic.getId());
        epics.put(newEpic.getId(), newEpic);
    }

    public void deleteEpicById(int id) {
        for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    public void updateEpicsStatus(int id) {
        Epic epic = epics.get(id);
        if (epic.getSubtaskIds().isEmpty()) epic.setStatus(TaskStatus.NEW);

        int countDone = 0;
        int countNew = 0;

        for (int subtaskId : epic.getSubtaskIds()) {
            if (subtasks.get(subtaskId).getStatus() == TaskStatus.DONE) countDone++;
            if (subtasks.get(subtaskId).getStatus() == TaskStatus.NEW) countNew++;
        }

        if (countDone == subtasks.size()) epic.setStatus(TaskStatus.DONE);
        else if (countNew == subtasks.size()) epic.setStatus(TaskStatus.NEW);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }


    public List<Subtask> getListOfSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void createSubtask(Subtask subtask) {
        subtask.setId(++taskIdCounter);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpicsStatus(epic.getId());
    }

    public void updateSubtaskById(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        updateEpicsStatus(epic.getId());
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskIds().remove(id);
        updateEpicsStatus(epic.getId());
    }
}

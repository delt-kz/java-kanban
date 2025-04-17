import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Manager {
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
        tasks.put(task.getId(), task);
    }

    public void updateTaskById(int id, Task task) {
        tasks.put(id, task);
        task.setId(id);
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
        for (Epic epic : epics.values()) {
            for (Integer subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
        epics.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void updateEpicById(int id, Epic newEpic) {
        Epic epic = epics.get(id);
        newEpic.setId(id);
        newEpic.setSubtaskIds(epic.getSubtaskIds());
        newEpic.updateStatus(new ArrayList<>(subtasks.values()));
        epics.put(id, newEpic);
    }

    public void deleteEpicById(int id) {
        for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
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
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        epic.updateStatus(new ArrayList<>(subtasks.values()));
    }

    public void updateSubtaskById(int id, Subtask subtask) {
        subtasks.put(id, subtask);
        subtask.setId(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.updateStatus(new ArrayList<>(subtasks.values()));
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskIds().remove(id);
        epic.updateStatus(new ArrayList<>(subtasks.values()));
    }
}

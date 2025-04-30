import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final List<Task> history = historyManager.getHistory();
    private int taskIdCounter = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    @Override
    public HistoryManager getHistoryManager(){
        return historyManager;
    }

    @Override
    public List<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) return null;
        historyManager.add(task);
        return task;
    }

    @Override
    public void createTask(Task task) {
        if (task == null) return;
        task.setId(++taskIdCounter);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTaskById(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int id) {
        if(epics.get(id) == null) return null;
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subtaskIds = epics.get(id).getSubtaskIds();
        for (Integer subtaskId : subtaskIds) {
            subtasksOfEpic.add(subtasks.get(subtaskId));
        }
        return subtasksOfEpic;
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return null;
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(++taskIdCounter);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateEpicById(Epic newEpic) {
        if (newEpic == null || !epics.containsKey(newEpic.getId())) return;
        Epic epic = epics.get(newEpic.getId());
        newEpic.setSubtaskIds(epic.getSubtaskIds());
        updateEpicsStatus(epic.getId());
        epics.put(newEpic.getId(), newEpic);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return;
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    @Override
    public void updateEpicsStatus(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return;
        if (epic.getSubtaskIds().isEmpty()) epic.setStatus(TaskStatus.NEW);

        int countDone = 0;
        int countNew = 0;

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;
            if (subtask.getStatus() == TaskStatus.DONE) countDone++;
            if (subtask.getStatus() == TaskStatus.NEW) countNew++;
        }

        if (countDone == epic.getSubtaskIds().size()) epic.setStatus(TaskStatus.DONE);
        else if (countNew == epic.getSubtaskIds().size()) epic.setStatus(TaskStatus.NEW);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }


    @Override
    public List<Subtask> getListOfSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if(subtask == null) return null;
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) return;
        subtask.setId(++taskIdCounter);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpicsStatus(epic.getId());
    }

    @Override
    public void updateSubtaskById(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        updateEpicsStatus(epic.getId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return;
        subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskIds().remove(Integer.valueOf(id));
        updateEpicsStatus(epic.getId());
    }
}

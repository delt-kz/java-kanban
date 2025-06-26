import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int taskIdCounter = 0;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
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
        if (hasOverlaps(task)) {
            throw new IllegalStateException("Task's time overlaps with others");
        }
        task.setId(++taskIdCounter);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTaskById(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        if (hasOverlaps(task)) {
            throw new IllegalStateException("Task's time overlaps with others");
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int id) {
        if (epics.get(id) == null) return null;
        return getEpicById(id).getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
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
        epics.put(newEpic.getId(), newEpic);
        updateEpicsStatus(epic.getId());
        updateEpicsTime(epic.getId());
    }

    @Override
    public void deleteEpicById(int id) {
        historyManager.remove(id);
        Epic epic = epics.get(id);
        if (epic == null) return;
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    private void updateEpicsStatus(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return;
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        }

        int countDone = 0;
        int countNew = 0;

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;
            if (subtask.getStatus() == TaskStatus.DONE) countDone++;
            if (subtask.getStatus() == TaskStatus.NEW) countNew++;
        }

        if (countDone == epic.getSubtaskIds().size()) {
            epic.setStatus(TaskStatus.DONE);
        } else if (countNew == epic.getSubtaskIds().size()) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void updateEpicsTime(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return;
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }


        LocalDateTime earliestDT = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime latestDT = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (earliestDT != null && latestDT != null) {
            epic.setStartTime(earliestDT);
            epic.setEndTime(latestDT);
            epic.setDuration(Duration.between(earliestDT, latestDT));
        }
    }


    @Override
    public List<Subtask> getListOfSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return null;
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) return;
        if (hasOverlaps(subtask)) {
            throw new IllegalStateException("Task's time overlaps with others");
        }
        subtask.setId(++taskIdCounter);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpicsStatus(epic.getId());
        updateEpicsTime(epic.getId());
    }

    @Override
    public void updateSubtaskById(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        if (hasOverlaps(subtask)) {
            throw new IllegalStateException("Task's time overlaps with others");
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        updateEpicsStatus(epic.getId());
        updateEpicsTime(epic.getId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        historyManager.remove(id);
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return;
        subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskIds().remove(Integer.valueOf(id));
        updateEpicsStatus(epic.getId());
        updateEpicsTime(epic.getId());
    }

    @Override
    public void printAllTasks() {
        System.out.println(tasks);
        System.out.println(epics);
        System.out.println(subtasks);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        Set<Task> sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        tasks.values().stream()
                .filter(task -> task.getStartTime() != null)
                .forEach(sortedTasks::add);
        subtasks.values().stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .forEach(sortedTasks::add);
        return new ArrayList<>(sortedTasks);
    }

    private boolean isOverlapping(Task task1, Task task2) {
        return task1.getEndTime().isAfter(task2.getStartTime()) && task1.getStartTime().isBefore(task2.getEndTime());
    }

    @Override
    public boolean hasOverlaps(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }
        return getPrioritizedTasks().stream()
                .filter(otherTask -> !otherTask.equals(task))
                .anyMatch(otherTask -> isOverlapping(otherTask, task));
    }
}

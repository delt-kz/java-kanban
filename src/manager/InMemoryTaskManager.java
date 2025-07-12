package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final Set<Task> sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    protected int taskIdCounter = 0;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
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
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
        sortedTasks.removeIf(task -> !(task instanceof Subtask));
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
        validateNoOverlap(task);
        task.setId(++taskIdCounter);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            sortedTasks.add(task);
        }
    }

    @Override
    public void updateTaskById(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        validateNoOverlap(task);
        tasks.put(task.getId(), task);
        sortedTasks.removeIf(srtTask -> srtTask.getId() == task.getId());
        if (task.getStartTime() != null) {
            sortedTasks.add(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id);
        tasks.remove(id);
        sortedTasks.removeIf(task -> task.getId() == id);
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
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.clear();
        sortedTasks.removeIf(task -> task instanceof Subtask);
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
        Epic epic = epics.remove(id);
        if (epic == null) return;
        epic.getSubtaskIds().forEach(sbId -> {
            historyManager.remove(sbId);
            subtasks.remove(sbId);
        });
        Set<Integer> victimIds = new HashSet<>(epic.getSubtaskIds());
        victimIds.add(id);
        sortedTasks.removeIf(t -> victimIds.contains(t.getId()));
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
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
        });

        sortedTasks.removeIf(task -> task instanceof Subtask);
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
        validateNoOverlap(subtask);
        subtask.setId(++taskIdCounter);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpicsStatus(epic.getId());
        updateEpicsTime(epic.getId());
        if (subtask.getStartTime() != null) {
            sortedTasks.add(subtask);
        }
    }


    @Override
    public void updateSubtaskById(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        validateNoOverlap(subtask);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        updateEpicsStatus(epic.getId());
        updateEpicsTime(epic.getId());
        sortedTasks.removeIf(srtTask -> srtTask.getId() == subtask.getId());
        if (subtask.getStartTime() != null) {
            sortedTasks.add(subtask);
        }
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
        sortedTasks.removeIf(task -> task.getId() == id);
    }

    @Override
    public void printAllTasks() {
        System.out.println(tasks);
        System.out.println(epics);
        System.out.println(subtasks);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(sortedTasks);
    }

    private boolean isOverlapping(Task task1, Task task2) {
        return task1.getEndTime().isAfter(task2.getStartTime()) && task1.getStartTime().isBefore(task2.getEndTime());
    }

    @Override
    public void validateNoOverlap(Task task) {
        if (task.getStartTime() == null) return;
        boolean overlaps = getPrioritizedTasks().stream()
                .filter(t -> !t.equals(task))
                .anyMatch(t -> isOverlapping(t, task));
        if (overlaps) {
            throw new IllegalStateException(
                    "model.Task %d overlaps with existing tasks".formatted(task.getId()));
        }
    }
}

import java.util.List;

public interface TaskManager {

    HistoryManager getHistoryManager();

    List<Task> getListOfTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    void createTask(Task task);

    void updateTaskById(Task task);

    void deleteTaskById(int id);

    List<Epic> getListOfEpics();

    List<Subtask> getSubtasksOfEpic(int id);

    void deleteAllEpics();

    Epic getEpicById(int id);

    void createEpic(Epic epic);

    void updateEpicById(Epic newEpic);

    void deleteEpicById(int id);

    void updateEpicsStatus(int id);

    List<Subtask> getListOfSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    void createSubtask(Subtask subtask);

    void updateSubtaskById(Subtask subtask);

    void deleteSubtaskById(int id);
}

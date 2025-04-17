import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{
    private ArrayList<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();

    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtask(int id) {
        subtaskIds.add(id);
    }

    public void updateStatus(List<Subtask> allSubtasks) {
        //creating a new subtasks list to keep subtasks that are related to this epic. Норм тема?
        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (Subtask subtask : allSubtasks) {
            if (subtask.getEpicId() == getId()) subtasks.add(subtask);
        }

        if (subtasks.isEmpty()) return;

        int countDone = 0;
        int countNew = 0;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() == TaskStatus.DONE) countDone++;
            if (subtask.getStatus() == TaskStatus.NEW) countNew++;
        }

        if (countDone == subtasks.size()) setStatus(TaskStatus.DONE);
        else if (countNew == subtasks.size()) setStatus(TaskStatus.NEW);
        else setStatus(TaskStatus.IN_PROGRESS);
    }
}

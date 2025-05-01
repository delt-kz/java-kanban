import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{
    private List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtask(int id) {
        subtaskIds.add(id);
    }
}

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{
    private ArrayList<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW);
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
}

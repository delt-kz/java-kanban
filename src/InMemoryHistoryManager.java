import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        if (history.size() == 10) history.removeFirst();
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}

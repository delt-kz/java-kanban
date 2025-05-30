import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;
    private int size = 0;

    @Override
    public void add(Task task) {
        if (task == null) return;
        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }
        history.put(task.getId(), linkLast(task));
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node node = head;
        while (node != null) {
            historyList.add(node.value);
            node = node.next;
        }
        return historyList;
    }

    @Override
    public void remove(int id) {
        if (history.containsKey(id)) removeNode(history.get(id));
    }

    public Node linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(task);
        newNode.prev = oldTail;
        tail = newNode;
        if (oldTail == null) head = newNode;
        else oldTail.next = newNode;
        size++;
        return newNode;
    }

    public void removeNode(Node node) {
        if (node == null) return;
        if (node.prev == null)  {
            head = node.next;
        } else {
            node.prev.next = node.next;
        }
        if (node.next == null) {
            tail = node.prev;
        } else {
            node.next.prev = node.prev;
        }
        history.remove(node.value.getId());
        size--;
    }

}

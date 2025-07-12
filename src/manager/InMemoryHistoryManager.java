package manager;

import model.Task;
import utils.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

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
            historyList.add(node.getValue());
            node = node.getNext();
        }
        return historyList;
    }

    @Override
    public void remove(int id) {
        if (history.containsKey(id)) {
            removeNode(history.get(id));
        }
    }

    public Node linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(task);
        newNode.setPrev(oldTail);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.setNext(newNode);
        }
        return newNode;
    }

    public void removeNode(Node node) {
        if (node == null) return;
        if (node.getPrev() == null)  {
            head = node.getNext();
        } else {
            node.getPrev().setNext(node.getNext());
        }
        if (node.getNext() == null) {
            tail = node.getPrev();
        } else {
            node.getNext().setPrev(node.getPrev());
        }
        history.remove(node.getValue().getId());
    }

}

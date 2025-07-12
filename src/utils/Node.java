package utils;

import model.Task;

public class Node {
    private Task value;
    private Node next;
    private Node prev;

    public Node(Task value) {
        this.value = value;
    }

    public Task getValue() {
        return value;
    }

    public Node getNext() {
        return next;
    }

    public Node getPrev() {
        return prev;
    }

    public void setValue(Task value) {
        this.value = value;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }
}

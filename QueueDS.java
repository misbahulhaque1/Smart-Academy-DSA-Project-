package dsas;

import java.util.NoSuchElementException;

public class QueueDS<T> {

    private SLinkedList<T> list;

    public QueueDS() {
        this.list = new SLinkedList<>();
    }

    // add element
    public void enqueue(T data) {
        list.add(data);
    }

    // remove first
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return list.remove(0);
    }

    // see first
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return list.get(0);
    }

    // check empty
    public boolean isEmpty() {
        return list.isEmpty();
    }

    // size
    public int size() {
        return list.size();
    }

    // clear all
    public void clear() {
        list.clear();
    }

    @Override
    public String toString() {
        return "Queue: " + list.toString();
    }
}

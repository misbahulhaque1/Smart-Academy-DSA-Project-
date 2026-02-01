package dsas;

import java.util.EmptyStackException;

public class StackDS<T> {

    private SLinkedList<T> list;

    public StackDS() {
        this.list = new SLinkedList<>();
    }

    // push (add first)
    public void push(T data) {
        list.addAt(0, data);
    }

    // pop (remove first)
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return list.remove(0);
    }

    // peek top
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
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
        return "Stack: " + list.toString();
    }
}

package utils;

import model.Subtask;

import java.util.List;

public interface TaskAccess<T> {
    T getById(int id);

    List<T> getAll();

    void delete(int id);

    void create(T task);

    void update(T task);

    default List<Subtask> getSubtasksOfEpic(int epicId) {
        throw new UnsupportedOperationException();
    }
}
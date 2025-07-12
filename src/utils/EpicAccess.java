package utils;

import model.Epic;
import model.Subtask;

import java.util.List;

public interface EpicAccess extends TaskAccess<Epic> {
    @Override
    List<Subtask> getSubtasksOfEpic(int epicId);
}

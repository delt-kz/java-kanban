package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, TaskStatus status, int epicId, LocalDateTime startTime, Duration duration) {
        super(title, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId, LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}

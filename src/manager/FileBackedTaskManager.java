package manager;

import model.*;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            br.readLine();
            int maxId = 0;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",");
                String taskType = splitLine[1];
                int taskId = Integer.parseInt(splitLine[0]);
                maxId = Math.max(maxId, taskId);
                Task task = fromString(line);
                switch (taskType) {
                    case "TASK":
                        manager.tasks.put(taskId, task);
                        if (task.getStartTime() != null) {
                            manager.sortedTasks.add(task);
                        }
                        break;
                    case "EPIC":
                        manager.epics.put(taskId, ((Epic) task));
                        if (task.getStartTime() != null) {
                            manager.sortedTasks.add(task);
                        }
                        break;
                    case "SUBTASK":
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(taskId, subtask);
                        manager.epics.get(subtask.getEpicId()).getSubtaskIds().add(taskId);
                        if (subtask.getStartTime() != null) {
                            manager.sortedTasks.add(subtask);
                        }
                        break;
                }
            }
            manager.taskIdCounter = maxId;
        } catch (IOException e) {
            throw new ManagerFileLoadException("Не удалось выгрузить задачи из файла");
        }
        return manager;
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()))) {
            bw.write("id,type,name,status,description,epic,startTime,duration\n");
            for (Map<Integer, ? extends Task> map : List.of(tasks, epics, subtasks)) {
                for (Integer id : map.keySet()) {
                    bw.write(toString(map.get(id)));
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось записать задачи в файл");
        }
    }

    private static String toString(Task task) {
        if (task instanceof Subtask subtask) {

            return String.format("%d,%s,%s,%s,%s,%d,%s,%s\n",
                    subtask.getId(),
                    TaskType.SUBTASK,
                    subtask.getTitle(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getEpicId(),
                    subtask.getStartTime() != null ? subtask.getStartTime().toString() : "null",
                    subtask.getDuration() != null ? String.valueOf(subtask.getDuration().toMinutes()) : "null"
            );
        } else if (task instanceof Epic epic) {
            return String.format("%d,%s,%s,%s,%s\n",
                    epic.getId(),
                    TaskType.EPIC,
                    epic.getTitle(),
                    epic.getStatus(),
                    epic.getDescription()
            );
        } else {
            return String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    task.getId(),
                    TaskType.TASK,
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription(),
                    task.getStartTime() != null ? task.getStartTime().toString() : "null",
                    task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "null"
            );
        }
    }

    private static Task fromString(String task) {
        String[] val = task.split(",");
        return switch (val[1]) {
            case "TASK" ->
                    new Task(
                            Integer.parseInt(val[0]),
                            val[2],
                            val[4],
                            TaskStatus.valueOf(val[3]),
                            val[5].equals("null") ? null : LocalDateTime.parse(val[5]),
                            val[6].equals("null") ? null : Duration.ofMinutes(Long.parseLong(val[6]))
                    );
            case "EPIC" -> new Epic(Integer.parseInt(val[0]), val[2], val[4]);
            case "SUBTASK" ->
                    new Subtask(
                            Integer.parseInt(val[0]),
                            val[2],
                            val[4],
                            TaskStatus.valueOf(val[3]),
                            Integer.parseInt(val[5]),
                            val[6].equals("null") ? null : LocalDateTime.parse(val[6]),
                            val[7].equals("null") ? null : Duration.ofMinutes(Long.parseLong(val[7]))
                    );
            default -> throw new IllegalArgumentException("Unknown task type" + val[1]);
        };
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTaskById(Task task) {
        super.updateTaskById(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpicById(Epic newEpic) {
        super.updateEpicById(newEpic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtaskById(Subtask subtask) {
        super.updateSubtaskById(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }
}
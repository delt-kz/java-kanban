import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        try(BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            br.readLine();
            int maxId = 0;
            while((line = br.readLine()) != null) {
                String taskType = line.split(",")[1];
                int taskId = Integer.parseInt(line.split(",")[0]);
                maxId = Math.max(maxId, taskId);
                switch (taskType){
                    case "TASK":
                        manager.tasks.put(taskId, fromString(line));
                        break;
                    case "EPIC":
                        manager.epics.put(taskId, ((Epic) fromString(line)));
                        break;
                    case "SUBTASK":
                        Subtask subtask = (Subtask) fromString(line);
                        manager.subtasks.put(taskId, subtask);
                        manager.epics.get(subtask.getEpicId()).getSubtaskIds().add(taskId);
                        break;
                }
            }
            manager.taskIdCounter = maxId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return manager;
    }

    public void save() {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()))) {
            bw.write("id,type,name,status,description,epic\n");
            for (Map<Integer, ? extends Task> map : List.of(tasks, epics, subtasks)) {
                for (Integer id : map.keySet()) {
                    bw.write(toString(map.get(id)));
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    private static String toString(Task task) {
        if (task instanceof Subtask subtask) {
            return String.format("%d,%s,%s,%s,%s,%d\n",
                    subtask.getId(),
                    TaskType.SUBTASK,
                    subtask.getTitle(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getEpicId());
        } else if (task instanceof Epic epic) {
            return String.format("%d,%s,%s,%s,%s\n",
                    epic.getId(),
                    TaskType.EPIC,
                    epic.getTitle(),
                    epic.getStatus(),
                    epic.getDescription());
        } else {
            return String.format("%d,%s,%s,%s,%s\n",
                    task.getId(),
                    TaskType.TASK,
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription());
        }
    }

    private static Task fromString(String task) {
        String[] val = task.split(",");
        switch (val[1]) {
            case "TASK":
                return new Task(Integer.parseInt(val[0]), val[2], val[4], TaskStatus.valueOf(val[3]));
            case "EPIC":
                return new Epic(Integer.parseInt(val[0]), val[2], val[4]);
            case "SUBTASK":
                return new Subtask(Integer.parseInt(val[0]), val[2], val[4], TaskStatus.valueOf(val[3]), Integer.parseInt(val[5]));
            default:
                throw new IllegalArgumentException("Unknown task type" + val[1]);
        }
    }

    public void see() {
        System.out.println(tasks);
        System.out.println(epics);
        System.out.println(subtasks);
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
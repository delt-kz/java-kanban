package server.handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;
import utils.TaskAccess;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class TaskHandler extends BaseHttpHandler<Task> {
    public static final String PATH = "/tasks";

    public TaskHandler(TaskManager manager) {
        super(new TaskAccess<>() {
            public Task getById(int id) {
                return manager.getTaskById(id);
            }

            public List<Task> getAll() {
                return manager.getListOfTasks();
            }

            public void delete(int id) {
                manager.deleteTaskById(id);
            }

            public void create(Task task) {
                manager.createTask(task);
            }

            public void update(Task task) {
                manager.updateTaskById(task);
            }
        });
    }

    @Override
    public void handle(HttpExchange exchange) {
        super.handle(exchange);
    }

    @Override
    protected Type getTypeToken() {
        return Task.class;
    }
}

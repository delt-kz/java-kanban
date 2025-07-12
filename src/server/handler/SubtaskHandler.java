package server.handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Subtask;
import utils.TaskAccess;

import java.lang.reflect.Type;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler<Subtask> {
    public static final String PATH = "/subtasks";

    public SubtaskHandler(TaskManager manager) {
        super(new TaskAccess<>() {
            public Subtask getById(int id) {
                return manager.getSubtaskById(id);
            }

            public List<Subtask> getAll() {
                return manager.getListOfSubtasks();
            }

            public void delete(int id) {
                manager.deleteSubtaskById(id);
            }

            public void create(Subtask subtask) {
                manager.createSubtask(subtask);
            }

            public void update(Subtask subtask) {
                manager.updateSubtaskById(subtask);
            }
        });
    }

    @Override
    public void handle(HttpExchange exchange) {
        super.handle(exchange);
    }

    @Override
    protected Type getTypeToken() {
        return Subtask.class;
    }
}

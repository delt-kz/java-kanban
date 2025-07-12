package server.handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import utils.EpicAccess;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class EpicHandler extends BaseHttpHandler<Epic> {
    public static final String PATH = "/epics";

    public EpicHandler(TaskManager manager) {
        super(new EpicAccess() {
            public Epic getById(int id) {
                return manager.getEpicById(id);
            }

            public List<Epic> getAll() {
                return manager.getListOfEpics();
            }

            public void delete(int id) {
                manager.deleteEpicById(id);
            }

            public void create(Epic epic) {
                manager.createEpic(epic);
            }

            public void update(Epic epic) {
                manager.updateEpicById(epic);
            }
            public List<Subtask> getSubtasksOfEpic(int epicId) {
                return manager.getSubtasksOfEpic(epicId);
            }
        });
    }

    @Override
    public void handle(HttpExchange exchange) {
        super.handle(exchange);
    }

    @Override
    protected Type getTypeToken() {
        return Epic.class;
    }
}

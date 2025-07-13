package server;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import server.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.manager = manager;
        httpServer.createContext(TaskHandler.PATH, new TaskHandler(manager));
        httpServer.createContext(SubtaskHandler.PATH, new SubtaskHandler(manager));
        httpServer.createContext(EpicHandler.PATH, new EpicHandler(manager));
        httpServer.createContext(HistoryHandler.PATH, new HistoryHandler(manager));
        httpServer.createContext(PrioritizedHandler.PATH, new PrioritizedHandler(manager));
    }

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getFileBacked());
//        addTasksForTesting(Managers.getFileBacked());
        httpTaskServer.start();

    }

    public static void addTasksForTesting(TaskManager taskManager) {
        Task task1 = new Task("Купить хлеб", "черный бездрожжевой", TaskStatus.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Купить томатный соус", "В небольшой банке", TaskStatus.DONE);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Приготовить спагетти", "2 порций");
        taskManager.createEpic(epic1);

        Subtask subtask1OfEpic1 = new Subtask("Сварить лапшу",
                "На кипящей подсоленной воде", TaskStatus.IN_PROGRESS, epic1.getId());
        taskManager.createSubtask(subtask1OfEpic1);

        Subtask subtask2OfEpic1 = new Subtask("Приготовить соус",
                "Оливковое масло чеснок добавить томатный соус", TaskStatus.NEW, epic1.getId());
        taskManager.createSubtask(subtask2OfEpic1);

        Epic epic2 = new Epic("Заварить чай", "Черный листовой чай");
        taskManager.createEpic(epic2);
        Subtask subtask1OfEpic2 = new Subtask("Вскипятить воду", "Налить воду в чайник", TaskStatus.DONE, epic2.getId());
        taskManager.createSubtask(subtask1OfEpic2);

        taskManager.updateTaskById(new Task(task1.getId(), "Купить хлеб", "любой", TaskStatus.DONE));
        taskManager.updateTaskById(new Task(task2.getId(), "Купить томатный соус", "В большой банке", TaskStatus.IN_PROGRESS));
        taskManager.updateEpicById(new Epic(epic1.getId(), "Приготовить спагетти", "Одна порция"));
        taskManager.updateEpicById(new Epic(epic2.getId(), "Заварить чай", "Зеленый в пакетике"));
        taskManager.updateSubtaskById(new Subtask(subtask1OfEpic1.getId(), "Сварить лапшу",
                "На кипящей подсоленной воде", TaskStatus.DONE, epic1.getId()));
        taskManager.updateSubtaskById(new Subtask(subtask2OfEpic1.getId(), "Приготовить соус",
                "Оливковое масло чеснок добавить томатный соус", TaskStatus.NEW, epic1.getId()));
        taskManager.updateSubtaskById(new Subtask(subtask1OfEpic2.getId(), "Вскипятить воду",
                "Налить воду в чайник", TaskStatus.NEW, epic2.getId()));


        System.out.println("History: " + taskManager.getHistory());
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}



import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getListOfTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getListOfEpics()) {
            System.out.println(epic);

            for (Task task : manager.getSubtasksOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getListOfSubtasks()) {
            System.out.println(subtask);
        }
    }


    public static void addTasksForTesting(TaskManager taskManager) {
        LocalDateTime dt = LocalDateTime.now();
        Duration dur = Duration.ofMinutes(50);
        Task task1 = new Task("Купить хлеб", "черный бездрожжевой", TaskStatus.NEW, dt, dur);
        taskManager.createTask(task1);
        Task task2 = new Task("Купить томатный соус", "В небольшой банке", TaskStatus.DONE, dt, dur);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Приготовить спагетти", "2 порций");
        taskManager.createEpic(epic1);

        Subtask subtask1OfEpic1 = new Subtask("Сварить лапшу",
                "На кипящей подсоленной воде",TaskStatus.IN_PROGRESS,epic1.getId(), dt, dur);
        taskManager.createSubtask(subtask1OfEpic1);

        Subtask subtask2OfEpic1 = new Subtask("Приготовить соус",
                "Оливковое масло чеснок добавить томатный соус", TaskStatus.NEW, epic1.getId(), dt, dur);
        taskManager.createSubtask(subtask2OfEpic1);

        Epic epic2 = new Epic("Заварить чай","Черный листовой чай");
        taskManager.createEpic(epic2);
        Subtask subtask1OfEpic2 = new Subtask("Вскипятить воду", "Налить воду в чайник", TaskStatus.DONE, epic2.getId(), dt, dur);
        taskManager.createSubtask(subtask1OfEpic2);

        taskManager.updateTaskById(new Task(task1.getId(), "Купить хлеб", "любой", TaskStatus.DONE, dt, dur));
        taskManager.updateTaskById(new Task(task2.getId(), "Купить томатный соус", "В большой банке", TaskStatus.IN_PROGRESS, dt, dur));
        taskManager.updateEpicById(new Epic(epic1.getId(), "Приготовить спагетти", "Одна порция"));
        taskManager.updateEpicById(new Epic(epic2.getId(),"Заварить чай","Зеленый в пакетике"));
        taskManager.updateSubtaskById(new Subtask(subtask1OfEpic1.getId(),"Сварить лапшу",
                "На кипящей подсоленной воде", TaskStatus.DONE, epic1.getId(), dt, dur));
        taskManager.updateSubtaskById(new Subtask(subtask2OfEpic1.getId(),"Приготовить соус",
                "Оливковое масло чеснок добавить томатный соус", TaskStatus.NEW, epic1.getId(), dt, dur));
        taskManager.updateSubtaskById(new Subtask(subtask1OfEpic2.getId(),"Вскипятить воду",
                "Налить воду в чайник", TaskStatus.NEW, epic2.getId(), dt, dur));


        System.out.println("History: " + taskManager.getHistory());
    }
}

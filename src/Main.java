
public class Main {
    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();
        addTasksForTesting(manager);
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
        Task task1 = new Task("Купить хлеб", "черный, бездрожжевой", TaskStatus.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Купить томатный соус", "В небольшой банке", TaskStatus.DONE);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Приготовить спагетти", "2 порций");
        taskManager.createEpic(epic1);

        Subtask subtask1OfEpic1 = new Subtask("Сварить лапшу",
                "На кипящей, подсоленной воде",TaskStatus.IN_PROGRESS,epic1.getId());
        taskManager.createSubtask(subtask1OfEpic1);

        Subtask subtask2OfEpic1 = new Subtask("Приготовить соус",
                "Оливковое масло, чеснок, добавить томатный соус", TaskStatus.NEW, epic1.getId());
        taskManager.createSubtask(subtask2OfEpic1);

        Epic epic2 = new Epic("Заварить чай","Черный, листовой чай");
        taskManager.createEpic(epic2);
        Subtask subtask1OfEpic2 = new Subtask("Вскипятить воду", "Налить воду в чайник", TaskStatus.DONE, epic2.getId());
        taskManager.createSubtask(subtask1OfEpic2);

        System.out.println(taskManager.getListOfTasks());
        System.out.println(taskManager.getListOfEpics());
        System.out.println(taskManager.getListOfSubtasks());


        taskManager.updateTaskById(new Task(task1.getId(), "Купить хлеб", "любой", TaskStatus.DONE));
        taskManager.updateTaskById(new Task(task2.getId(), "Купить томатный соус", "В большой банке", TaskStatus.IN_PROGRESS));
        taskManager.updateEpicById(new Epic(epic1.getId(), "Приготовить спагетти", "Одна порций"));
        taskManager.updateEpicById( new Epic(epic2.getId(),"Заварить чай","Зеленый, в пакетике"));
        taskManager.updateSubtaskById( new Subtask(subtask1OfEpic1.getId(),"Сварить лапшу",
                "На кипящей, подсоленной воде",TaskStatus.DONE, epic1.getId()));
        taskManager.updateSubtaskById( new Subtask(subtask2OfEpic1.getId(),"Приготовить соус",
                "Оливковое масло, чеснок, добавить томатный соус", TaskStatus.NEW, epic1.getId()));
        taskManager.updateSubtaskById( new Subtask(subtask1OfEpic2.getId(),"Вскипятить воду",
                "Налить воду в чайник", TaskStatus.NEW, epic2.getId()));

        System.out.println(taskManager.getListOfTasks());
        System.out.println(taskManager.getListOfEpics());
        System.out.println(taskManager.getListOfSubtasks());
        System.out.println(taskManager.getTaskById(1));
        System.out.println(taskManager.getTaskById(2));

        System.out.println("History: " + taskManager.getHistory());
    }
}

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager();

        Task task1 = new Task("Купить хлеб", "черный, бездрожжевой", TaskStatus.NEW);
        manager.createTask(task1);
        Task task2 = new Task("Купить томатный соус", "В небольшой банке", TaskStatus.DONE);
        manager.createTask(task2);

        Epic epic1 = new Epic("Приготовить спагетти", "2 порций");
        manager.createEpic(epic1);

        Subtask subtask1OfEpic1 = new Subtask("Сварить лапшу",
                "На кипящей, подсоленной воде",TaskStatus.IN_PROGRESS,epic1.getId());
        manager.createSubtask(subtask1OfEpic1);

        Subtask subtask2OfEpic1 = new Subtask("Приготовить соус",
                "Оливковое масло, чеснок, добавить томатный соус", TaskStatus.NEW, epic1.getId());
        manager.createSubtask(subtask2OfEpic1);

        Epic epic2 = new Epic("Заварить чай","Черный, листовой чай");
        manager.createEpic(epic2);
        Subtask subtask1OfEpic2 = new Subtask("Вскипятить воду", "Налить воду в чайник", TaskStatus.DONE, epic2.getId());
        manager.createSubtask(subtask1OfEpic2);

        System.out.println(manager.getListOfTasks());
        System.out.println(manager.getListOfEpics());
        System.out.println(manager.getListOfSubtasks());


        manager.updateTaskById(task1.getId(), new Task("Купить хлеб", "любой", TaskStatus.DONE));
        manager.updateTaskById(task2.getId(), new Task("Купить томатный соус", "В большой банке", TaskStatus.IN_PROGRESS));
        manager.updateEpicById(epic1.getId(), new Epic("Приготовить спагетти", "Одна порций"));
        manager.updateEpicById(epic2.getId(), new Epic("Заварить чай","Зеленый, в пакетике"));
        manager.updateSubtaskById(subtask1OfEpic1.getId(), new Subtask("Сварить лапшу",
                "На кипящей, подсоленной воде",TaskStatus.DONE, epic1.getId()));
        manager.updateSubtaskById(subtask2OfEpic1.getId(), new Subtask("Приготовить соус",
                "Оливковое масло, чеснок, добавить томатный соус", TaskStatus.NEW, epic1.getId()));
        manager.updateSubtaskById(subtask1OfEpic2.getId(), new Subtask("Вскипятить воду",
                "Налить воду в чайник", TaskStatus.NEW, epic2.getId()));

        System.out.println(manager.getListOfTasks());
        System.out.println(manager.getListOfEpics());
        System.out.println(manager.getListOfSubtasks());

        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());

        System.out.println(manager.getListOfTasks());
        System.out.println(manager.getListOfEpics());
        System.out.println(manager.getListOfSubtasks());
    }
}

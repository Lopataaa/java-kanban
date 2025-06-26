public class Main { //Привет, Ярослав! Прошу прощения. Утром на работе, когда начала 5-й спринт проходить,

    static void main(String[] args){ // поняла, что где-то накосячила, но до вечера уже не было возможности исправить

        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.addTask("Задача 1", "Описание задачи 1");
        Task task2 = taskManager.addTask("Задача 2", "Описание задачи 2");

        Epic epic1 = taskManager.addEpic("Эпик 1", "Описание эпика 1");
        SubTask subTask1 = taskManager.addSubTask("Подзадача 1 для эпика 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = taskManager.addSubTask("Подзадача 2 для эпика 1", "Описание подзадачи 2", epic1.getId());

        Epic epic2 = taskManager.addEpic("Эпик 2", "Описание эпика 2");
        SubTask subTask3 = taskManager.addSubTask("Подзадача для эпика 2", "Описание подзадачи", epic2.getId());

        System.out.println("Задачи: " + taskManager.getTasks());
        System.out.println("Эпики: " + taskManager.getEpics());
        System.out.println("Подзадачи: " + taskManager.getSubTasks());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        subTask1.setStatus(TaskStatus.DONE);

        System.out.println("Изменённые задачи: " + taskManager.getTasks());
        System.out.println("Изменённые подзадачи: " + taskManager.getSubTasks());

        System.out.println("Статус задачи 1: " + task1.getStatus());
        System.out.println("Статус подзадачи 1: " + subTask1.getStatus());

        taskManager.deleteAllTasks();
        taskManager.deleteEpic();

        System.out.println("Задачи после удаления: " + taskManager.getTasks());
        System.out.println("Эпики после удаления: " + taskManager.getEpics());
    }
}

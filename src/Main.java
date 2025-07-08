import manager.Managers;
import manager.TaskManager;
import task.Epic;
import manager.TaskStatus;
import task.SubTask;
import task.Task;

public class Main {

    public static void main(String[] args){
        int newId = 1;
        String name = "Имя задачи";
        String description = "Описание задачи";
        TaskStatus status = TaskStatus.NEW;
        int statusInt = status.ordinal();

        TaskManager taskManager = Managers.getTaskManager();

        SubTask subTask = new SubTask(newId++, description, name, statusInt);
        taskManager.addSubTask(subTask);

        Epic epic = new Epic(newId++, description, name);
        taskManager.addEpic(epic);

        Task task1 = new Task(newId, "Задача 1", "Описание задачи 1");
        taskManager.addTask(task1);
        Task task2 = new Task(newId,"Задача 2", "Описание задачи 2");
        taskManager.addTask(task2);

        Epic epic1 = new Epic(newId, "Эпик 1", "Описание эпика 1");
        taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1", epic1.getId());
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask(newId, "Подзадача 2 для эпика 1", "Описание подзадачи 2", epic1.getId());
        taskManager.addSubTask(subTask2);

        Epic epic2 = new Epic(newId, "Эпик 2", "Описание эпика 2");
        taskManager.addEpic(epic2);
        SubTask subTask3 = new SubTask(newId, "Подзадача 3 для эпика 2", "Описание подзадачи 3", epic2.getId());
        taskManager.addSubTask(subTask3);

        // Вызываем методы и проверяем историю просмотров
        System.out.println("История просмотров после добавления задач:");
        System.out.println(taskManager.getHistory()); /*метод getHistory для проверки истории просмотров
        для всех типов задач: Task, SubTask, Epic*/

        System.out.println("Задачи: " + taskManager.getTasks());
        System.out.println("Эпики: " + taskManager.getEpics());
        System.out.println("Подзадачи: " + taskManager.getSubTasks());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        subTask1.setStatus(TaskStatus.DONE);

        System.out.println("Изменённые задачи: " + taskManager.getTasks());
        System.out.println("Изменённые подзадачи: " + taskManager.getSubTasks());

        System.out.println("Статус задачи 1: " + task1.getStatus());
        System.out.println("Статус подзадачи 1: " + subTask1.getStatus());

        taskManager.deleteAllSubtasks();
        taskManager.deleteEpic();

        System.out.println("Задачи после удаления: " + taskManager.getTasks());
        System.out.println("Эпики после удаления: " + taskManager.getEpics());
    }
}


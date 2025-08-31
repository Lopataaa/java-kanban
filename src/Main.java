import manager.Managers;
import manager.TaskManager;
import task.Epic;
import task.TaskStatus;
import task.SubTask;
import task.Task;

import java.io.FileOutputStream; //Для сохранения объекта Epic в файл
import java.io.ObjectOutputStream; //Для сохранения объекта Epic в файл

public class Main {

    public static void main(String[] args) {
        int newId = 1;
        String name = "Имя задачи";
        String description = "Описание задачи";
        TaskStatus status = TaskStatus.NEW;
        int statusInt = status.ordinal();

        TaskManager taskManager = Managers.getDefault();

        SubTask subTask = new SubTask(newId++, description, name, statusInt);
        taskManager.addSubTask(subTask);

        Epic epic = new Epic(newId++, description, name);
        taskManager.addEpic(epic);

        try (FileOutputStream fileOut = new FileOutputStream("epic.ser");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(epic);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Epic epic1 = new Epic(newId, "Эпик 1", "Описание эпика 1");
        taskManager.addEpic(epic1);

        // Нужно ли мне сохранение epic1 в файл?????
        try (FileOutputStream fileOut = new FileOutputStream("epic1.ser");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(epic1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Task task1 = new Task(newId, "Задача 1", "Описание задачи 1"); // создайте две задачи
        taskManager.addTask(task1);
        Task task2 = new Task(newId, "Задача 2", "Описание задачи 2");
        taskManager.addTask(task2);

        //Epic epic1 = new Epic(newId, "Эпик 1", "Описание эпика 1"); // эпик с тремя подзадачами
        taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1", epic1.getId());
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask(newId, "Подзадача 2 для эпика 1", "Описание подзадачи 2", epic1.getId());
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask(newId, "Подзадача 3 для эпика 1", "Описание подзадачи 3", epic1.getId());
        taskManager.addSubTask(subTask3);

        Epic epic2 = new Epic(newId, "Эпик 2", "Описание эпика 2"); // эпик без подзадач
        taskManager.addEpic(epic2);

        // Вызываем методы и проверяем историю просмотров
        System.out.println("История просмотров после добавления задач:");
        System.out.println(taskManager.getHistory()); /*метод getHistory для проверки истории просмотров
        для всех типов задач: Task, SubTask, Epic*/

        // Запросите созданные задачи несколько раз в разном порядке
        System.out.println("Задачи: " + taskManager.getTasks());
        System.out.println("Эпики: " + taskManager.getEpics());
        System.out.println("Подзадачи: " + taskManager.getSubTasks());

        // После каждого запроса выведите историю и убедитесь, что в ней нет повторов
        System.out.println("История просмотров после создания задач");
        System.out.println(taskManager.getHistory());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        subTask1.setStatus(TaskStatus.DONE);

        System.out.println("Изменённые задачи: " + taskManager.getTasks());
        System.out.println("Изменённые подзадачи: " + taskManager.getSubTasks());

        // Проверка истории после изменения
        System.out.println("Измененные задачи" + taskManager.getTasks());
        System.out.println("Измененные подзадачи" + taskManager.getSubTasks());

        System.out.println("История после изменений");
        System.out.println(taskManager.getHistory());

        // Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться
        taskManager.deleteTask(task1.getId());
        System.out.println("История после удаления задачи");
        System.out.println(taskManager.getHistory());

        // Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи
        taskManager.deleteEpic(epic1.getId());
        System.out.println("История после удаления эпика:");
        System.out.println(taskManager.getHistory());

        System.out.println("Статус задачи 1: " + task1.getStatus());

        taskManager.deleteAllSubtasks();
        taskManager.deleteEpic(epic1.getId());

        System.out.println("Задачи после удаления: " + taskManager.getTasks());
        System.out.println("Эпики после удаления: " + taskManager.getEpics());
    }
}


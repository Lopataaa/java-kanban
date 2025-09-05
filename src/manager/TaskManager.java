package manager;

import task.SubTask;
import task.Task;
import task.Epic;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public interface TaskManager {
    /* реализовала п.1
     * список методов, которые д.б. у любого объекта-менеджера. Для этого удалила всё тело методов и оставила
     * только сигнатуры методов
     */

    // Task
    int addTask(Task task);
    int updateTask(Task updateTask);
    void deleteTask(int id);
    List<Task> getTasks();
    Task findTaskById(int id);

    // SubTask
    int addSubTask(SubTask subTask);
    int updateSubTask(SubTask updateSubTask);
    void deleteSubTask();
    void deleteAllSubtasks();
    List<SubTask> getSubTasks();
    SubTask findSubTaskById(int id);

    // Epic
    int addEpic(Epic epic);
    int updateEpic(Epic updateEpic);
    void deleteEpic(int id);
    Epic findEpicById(int id);
    List<Epic> getEpics();
    void updateEpicStatus(Epic epic);

    List<SubTask> getSubTasksByEpicId(int epicId);

    // сигнатура метода, который будет возвращать последние 10 просмотренных задач,
    // а его реализация будет в классе InMemoryTaskManager
    List<Task> getHistory();

    Set<Task> tasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null || t2.getStartTime() == null) {
            return 0; // Не учитываем задачу в сортировке, если время начала не задано
        }
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    default Set<Task> getPrioritizedTasks() {
        return tasks;
    }

    default boolean isOverIntersection(Task task1, Task task2) {
        return !(task1.getEndTime().isBefore(task2.getStartTime()) ||
                task2.getEndTime().isBefore(task1.getStartTime()));
    }

    // Метод для проверки пересечения новой задачи с существующими
    default boolean hasOverIntersectionTasks(Task newTask) {
        for (Task existingTask : tasks) {
            if (isOverIntersection(newTask, existingTask)) {
                return true; // Пересечение найдено
            }
        }
        return false; // Пересечений нет
    }
}


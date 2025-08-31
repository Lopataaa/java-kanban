package manager;

import task.SubTask;
import task.Task;
import task.Epic;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public interface TaskManager { //реализовала п.1
    List<Task> getTasks(); /* список методов, которые д.б. у любого объекта-менеджера. Для этого
    удалила всё тело методов и оставила только сигнатуры методов*/

    List<SubTask> getSubTasks();

    List<Epic> getEpics();

    int addTask(Task task);

    void updateEpicStatus(Epic epic);

    int updateTask(Task updateTask);

    Task findTaskById(int id);

    int addSubTask(SubTask subTask);

    int updateSubTask(SubTask updateSubTask);

    SubTask findSubTaskById(int id);

    int addEpic(Epic epic);

    int updateEpic(Epic updateEpic);

    Epic findEpicById(int id);

    void deleteTask(int id);

    void deleteAllSubtasks();

    void deleteSubTask();

    void deleteEpic(int id);

    List<SubTask> getSubTasksByEpicId(int epicId);

    List<Task> getHistory(); /* сигнатура метода, который будет возвращать последние 10 просмотренных задач,
    а его реализация будет в классе InMemoryTaskManager*/

    Set<Task> tasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null || t2.getStartTime() == null) {
            return 0; // Не учитываем задачу в сортировке, если время начала не задано
        }
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    public default Set<Task> getPrioritizedTasks() {
        return tasks;
    }

    public default boolean isOverIntersection(Task task1, Task task2) {
        return !(task1.getEndTime().isBefore(task2.getStartTime()) ||
                task2.getEndTime().isBefore(task1.getStartTime()));
    }

    // Метод для проверки пересечения новой задачи с существующими
    public default boolean hasOverIntersectionTasks(Task newTask) {
        for (Task existingTask : tasks) {
            if (isOverIntersection(newTask, existingTask)) {
                return true; // Пересечение найдено
            }
        }
        return false; // Пересечений нет
    }
}


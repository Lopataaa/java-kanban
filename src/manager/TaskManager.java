package manager;

import task.SubTask;
import task.Task;
import task.Epic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public interface TaskManager {

    // Task
    int addTask(Task task);

    int updateTask(Task updateTask);

    void deleteTask(int id);

    void deleteAllTasks();

    List<Task> getTasks();

    Task findTaskById(int id);

    // SubTask
    int addSubTask(SubTask subTask);

    int updateSubTask(SubTask updateSubTask);

    void deleteSubTask();

    void deleteSubTask(int id);

    void deleteAllSubtasks();

    List<SubTask> getSubTasks();

    SubTask findSubTaskById(int id);

    void updateEpicStatus(Epic epic);

    // Epic
    int addEpic(Epic epic);

    int updateEpic(Epic updateEpic);

    void deleteEpic(int id);

    void deleteAllEpics();

    Epic findEpicById(int id);

    List<Epic> getEpics();

    List<SubTask> getSubTasksByEpicId(int epicId);

    List<Task> getHistory();

    Set<Task> tasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null || t2.getStartTime() == null) {
            return 0;
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

    default boolean hasOverIntersectionTasks(Task newTask) {
        for (Task existingTask : tasks) {
            if (isOverIntersection(newTask, existingTask)) {
                return true;
            }
        }
        return false;
    }

    Epic getEpic(Integer integer);

    void clearEpics();

    ArrayList<SubTask> getEpicSubTasks(Epic epic);
}


package manager;

import task.SubTask;
import task.Task;
import task.Epic;
import java.util.List;

public interface TaskManager { //реализовала п.1
    List<Task> getTasks(); /* список методов, которые д.б. у любого объекта-менеджера. Для этого
    нужно удалила всё тело методов и оставила только сигнатуры методов*/

    List<SubTask> getSubTasks();

    List<Epic> getEpics();

    int addTask(Task task);

    void updateEpicStatus(Epic epic);

    Task updateTask(Task updateTask);

    Task findTaskById(int id);

    void addSubTask(SubTask subTask);

    boolean updateSubTask(SubTask updateSubTask);

    SubTask findSubTaskById(int id);

    void addEpic(Epic epic);

    boolean updateEpic(Epic updateEpic);

    Epic findEpicById(int id);

    void deleteAllSubtasks();

    void deleteSubTask();

    void deleteEpic();

    List<SubTask> getSubTasksByEpicId(int epicId);

    List<Task> getHistory(); /* сигнатура метода, который будет возвращать последние 10 просмотренных задач,
    а его реализация будет в классе InMemoryTaskManager*/

}


package manager;

import task.Task;
import java.util.List;

public interface HistoryManager {
    //void add(Task task); // метод должен помечать задачи как просмотренные

    void addToHistory(Task task);

    List<Task> getHistory(); // должен возвращать просмотренные задачи в список
}

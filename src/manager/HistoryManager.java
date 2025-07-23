package manager;

import task.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task); // переименовала метод addToHistory в add

    void remove(int id);

    List<Task> getHistory(); // должен возвращать просмотренные задачи в список
}

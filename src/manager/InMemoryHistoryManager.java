package manager;

import task.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> history = new LinkedList<>();

    @Override
    public List<Task> getHistory() { // реализация метода, который возвращает последние 10 просмотренных задач. Обяъявлен в TaskManager
        return new ArrayList<>(history);
    }


    // Обновление истории просмотров
    public void addToHistory(Task task) { //метод добавляет задачу в список истории просмотров
        // Если размер списка больше 10, по ТЗ необходимо удалить самый старый элемент — тот, который находится в начале списка
        if (history.size() > 10) {
            history.remove(0);
        }
        history.add(task); //просмотренные задачи должны добавляться в конец
    }

}
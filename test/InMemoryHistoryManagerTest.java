import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryHistoryManagerTest {

    @Test
    public void savingThePreviousIssueVersion() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        Task task = new Task(1, "Задача 1", "Описание задачи 1"); // Создание задачи

        historyManager.add(task); // Добавляем задачу в историю

        List<Task> history = historyManager.getHistory(); //проверка предыдущей версии
        assertTrue(!history.isEmpty(), "История должна содержать хотя бы одну задачу");
        assertEquals(task, history.get(history.size() - 1), "Добавленная задача последняя в истории");
    }

    @Test
    public void testOfAddingDeleteOperations() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        Task task1 = new Task(1, "Задача 1", "Описание задачи 1");
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2");
        Task task3 = new Task(3, "Задача 3", "Описание задачи 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        int taskId = task2.getId();

        historyManager.remove(taskId);

        history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));

        historyManager.remove(10);
    }

    @Test
    public void testAddToEmptyHistory() {
        // Создаем экземпляр HistoryManager
        HistoryManager historyManager = new InMemoryHistoryManager();

        // Добавляем задачу в пустую историю
        Task task = new Task(1, "Задача 1", "Описание задачи 1");
        historyManager.add(task);

        // Проверяем, что задача была добавлена
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertTrue(history.contains(task));
    }

    @Test
    public void testDuplicateAdd() {
        // Создаем экземпляр HistoryManager
        HistoryManager historyManager = new InMemoryHistoryManager();

        // Добавляем дублирующуюся задачу
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1");
        Task task2 = new Task(1, "Задача 1", "Описание задачи 1"); // Дублирующаяся задача

        historyManager.add(task1);
        historyManager.add(task2);

        // Проверяем, что дублирующаяся задача была добавлена как отдельная запись
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task2));
    }

    @Test
    public void testRemoveFromHistory() {
        // Создаем экземпляр HistoryManager
        HistoryManager historyManager = new InMemoryHistoryManager();

        // Добавляем задачи в историю
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1");
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2");
        Task task3 = new Task(3, "Задача 3", "Описание задачи 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем задачу из середины истории
        historyManager.remove(task2.getId());

        // Проверяем, что задача была удалена
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task3));
    }
}


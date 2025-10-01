import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryHistoryManagerTest {
    private static final String TASK_NAME_1 = "Задача 1";
    private static final String TASK_DESCRIPTION_1 = "Описание задачи 1";
    private static final String TASK_NAME_2 = "Задача 2";
    private static final String TASK_DESCRIPTION_2 = "Описание задачи 2";
    private static final String TASK_NAME_3 = "Задача 3";
    private static final String TASK_DESCRIPTION_3 = "Описание задачи 3";
    private static final int TASK_ID_1 = 1;
    private static final int TASK_ID_2 = 2;
    private static final int TASK_ID_3 = 3;
    private static final int NON_EXISTENT_TASK_ID = 10;
    private static final int EXPECTED_HISTORY_SIZE_1 = 1;
    private static final int EXPECTED_HISTORY_SIZE_2 = 2;
    private static final int EXPECTED_HISTORY_SIZE_3 = 3;
    private static final int LAST_ELEMENT_INDEX_OFFSET = 1;

    @Test
    public void savingThePreviousIssueVersion() {
        // Given
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);

        // When
        historyManager.add(task);

        // Then
        List<Task> history = historyManager.getHistory();
        assertTrue(!history.isEmpty(), "История должна содержать хотя бы одну задачу");
        assertEquals(task, history.get(history.size() - LAST_ELEMENT_INDEX_OFFSET), "Добавленная задача последняя в истории");
    }

    @Test
    public void testOfAddingDeleteOperations() {
        // Given
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);
        Task task2 = new Task(TASK_ID_2, TASK_NAME_2, TASK_DESCRIPTION_2);
        Task task3 = new Task(TASK_ID_3, TASK_NAME_3, TASK_DESCRIPTION_3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // When
        List<Task> history = historyManager.getHistory();
        assertEquals(EXPECTED_HISTORY_SIZE_3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        // When
        int taskId = task2.getId();
        historyManager.remove(taskId);

        // Then
        history = historyManager.getHistory();
        assertEquals(EXPECTED_HISTORY_SIZE_2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));

        // When
        historyManager.remove(NON_EXISTENT_TASK_ID);
    }

    @Test
    public void testAddToEmptyHistory() {
        // Given
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);

        // When
        historyManager.add(task);

        // Then
        List<Task> history = historyManager.getHistory();
        assertEquals(EXPECTED_HISTORY_SIZE_1, history.size());
        assertTrue(history.contains(task));
    }

    @Test
    public void testDuplicateAdd() {
        // Given
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);
        Task task2 = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);

        // When
        historyManager.add(task1);
        historyManager.add(task2);

        // Then
        List<Task> history = historyManager.getHistory();
        assertEquals(EXPECTED_HISTORY_SIZE_2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task2));
    }

    @Test
    public void testRemoveFromHistory() {
        // Given
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);
        Task task2 = new Task(TASK_ID_2, TASK_NAME_2, TASK_DESCRIPTION_2);
        Task task3 = new Task(TASK_ID_3, TASK_NAME_3, TASK_DESCRIPTION_3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // When
        historyManager.remove(task2.getId());

        // Then
        List<Task> history = historyManager.getHistory();
        assertEquals(EXPECTED_HISTORY_SIZE_2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task3));
    }
}


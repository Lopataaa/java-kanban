import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {
    private static final String TASK_NAME_1 = "Задача 1";
    private static final String TASK_DESCRIPTION_1 = "Описание задачи 1";
    private static final String TASK_NAME_2 = "Задача 2";
    private static final String TASK_DESCRIPTION_2 = "Описание задачи 2";
    private static final String EPIC_NAME_1 = "Эпик 1";
    private static final String EPIC_DESCRIPTION_1 = "Описание эпика 1";
    private static final String SUBTASK_NAME_1 = "Подзадача 1 для эпика 1";
    private static final String SUBTASK_DESCRIPTION_1 = "Описание подзадачи 1";
    private static final String TEST_TASK_NAME = "Тест";
    private static final String TEST_TASK_DESCRIPTION = "Описание теста";
    private static final int NEW_ID = 1;
    private static final int TASK_ID_1 = 1;
    private static final int HOURS_DURATION_1 = 1;
    private static final int HOURS_DURATION_2 = 2;

    @Test
    public void idEqualsForTaskClasses() {
        // Given
        int newId = NEW_ID;
        Task task1 = new Task(newId, TASK_NAME_1, TASK_DESCRIPTION_1);
        Task task2 = new Task(newId, TASK_NAME_2, TASK_DESCRIPTION_2);

        // When
        // Then
        if (task1.getId() == task2.getId()) {
            assertEquals(task1, task2);
        }
    }

    @Test
    public void subTaskCannotBeEpic() {
        // Given
        int newId = NEW_ID;
        Epic epic = new Epic(newId, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        SubTask subTask = new SubTask(newId, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, epic.getId());

        // When
        // Then
        assertNotEquals(epic.getClass(), subTask.getClass(), "SubTask не должен быть экземпляром Epic");
    }

    @Test
    public void testDuration() {
        // Given
        Task task = new Task(TASK_ID_1, TEST_TASK_NAME, TEST_TASK_DESCRIPTION);
        Duration expectedDuration = Duration.ofHours(HOURS_DURATION_2);

        // When
        task.setDuration(expectedDuration);

        // Then
        assertEquals(expectedDuration, task.getDuration());
    }

    @Test
    public void testStartTime() {
        // Given
        LocalDateTime expectedStartTime = LocalDateTime.now();
        Task task = new Task(TASK_ID_1, TEST_TASK_NAME, TEST_TASK_DESCRIPTION);

        // When
        task.setStartTime(expectedStartTime);

        // Then
        assertEquals(expectedStartTime, task.getStartTime());
    }
}
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    @Test
    public void idEqualsForTaskClasses() {
        int newId = 1;
        Task task1 = new Task(newId, "Задача 1", "Описание задачи 1");
        Task task2 = new Task(newId, "Задача 2", "Описание задачи 2");
        if (task1.getId() == task2.getId()) {
            assertEquals(task1, task2);
        }
    }

    @Test
    public void subTaskCannotBeEpic() {
        int newId = 1;
        Epic epic = new Epic(newId, "Эпик 1", "Описание эпика 1", LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1",
                epic.getId());
        assertNotEquals(epic.getClass(), subTask.getClass(), "SubTask не должен быть экземпляром Epic");
    }

    @Test
    public void testDuration() {
        Task task = new Task(1, "Тест", "Описание теста");
        Duration expectedDuration = Duration.ofHours(2);
        task.setDuration(expectedDuration);
        assertEquals(expectedDuration, task.getDuration());
    }

    @Test
    public void testStartTime() {
        LocalDateTime expectedStartTime = LocalDateTime.now();
        Task task = new Task(1, "Тест", "Описание теста");
        task.setStartTime(expectedStartTime);
        assertEquals(expectedStartTime, task.getStartTime());
    }
}
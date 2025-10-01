import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InMemoryTaskManagerTest {
    private static final String SUBTASK_NAME_1 = "Подзадача 1";
    private static final String SUBTASK_DESCRIPTION_1 = "Описание подзадачи 1";
    private static final String EPIC_NAME_1 = "Эпик 1";
    private static final String EPIC_DESCRIPTION_1 = "Описание эпика 1";
    private static final String TASK_NAME_1 = "Задача 1";
    private static final String TASK_DESCRIPTION_1 = "Описание задачи 1";
    private static final String TASK_NEW_NAME = "Новое название";
    private static final String TASK_NEW_DESCRIPTION = "Новое описание";
    private static final String TASK_GENERATED_ID_PREFIX = "ID сгенерирован";
    private static final String TASK_DESCRIPTION_PREFIX = "Описание задачи ";
    private static final int TASK_ID_1 = 1;
    private static final int TASK_ID_2 = 2;
    private static final int TASK_ID_3 = 3;
    private static final int EPIC_ID_1 = 1;
    private static final int SUBTASK_ID_1 = 1;
    private static final int SUBTASK_ID_2 = 2;
    private static final int TASKS_COUNT_10 = 10;
    private static final int EXPECTED_EMPTY_SIZE = 0;
    private static final int HOURS_DURATION_1 = 1;

    @Test
    public void testWithoutStoringOldId() {
        // Given
        InMemoryTaskManager manager = new InMemoryTaskManager();
        SubTask subTask = new SubTask(SUBTASK_ID_1, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, TASK_ID_1);

        // When
        manager.addSubTask(subTask);
        assertTrue(manager.getSubTasks().contains(subTask));

        manager.deleteSubTask();

        // Then
        assertEquals(EXPECTED_EMPTY_SIZE, manager.getSubTasks().size());
    }

    @Test
    public void testWithoutIrrelevantSubtasksId() {
        // Given
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Epic epic = new Epic(EPIC_ID_1, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        SubTask subTask = new SubTask(SUBTASK_ID_2, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, EPIC_ID_1);

        manager.addEpic(epic);
        manager.addSubTask(subTask);

        // When
        List<SubTask> subTasksBeforeDeletion = manager.getSubTasksByEpicId(epic.getId());
        assertTrue(subTasksBeforeDeletion.contains(subTask));

        // When
        manager.deleteSubTask();

        // Then
        List<SubTask> subTasksAfterDeletion = manager.getSubTasksByEpicId(epic.getId());
        assertFalse(subTasksAfterDeletion.contains(subTask));
    }

    @Test
    public void testChangingTaskFields() {
        // Given
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);

        manager.addTask(task);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(TASK_DESCRIPTION_1, task.getDescription());

        // When
        task.setName(TASK_NEW_NAME);
        task.setDescription(TASK_NEW_DESCRIPTION);

        // Then
        Task updatedTask = manager.findTaskById(task.getId());
        assertNotNull(updatedTask);
        assertEquals(TASK_NEW_NAME, updatedTask.getName());
        assertEquals(TASK_NEW_DESCRIPTION, updatedTask.getDescription());
    }

    @Test
    public void addAndFindTasks() {
        // Given
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        SubTask subTask = new SubTask(TASK_ID_2, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, TASK_ID_1);
        Epic epic = new Epic(TASK_ID_3, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));

        // When
        manager.addTask(task);
        manager.addSubTask(subTask);
        manager.addEpic(epic);

        // Then
        assertNotNull(manager.findTaskById(TASK_ID_1), "Задача должна быть найдена");
        assertNotNull(manager.findSubTaskById(TASK_ID_2), "Подзадача должна быть найдена");
        assertNotNull(manager.findEpicById(TASK_ID_3), "Эпик должен быть найден");
    }

    @Test
    public void taskDoNotConflict() {
        // Given
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task taskWithGivenId = new Task(TASK_ID_1, "ID задан", "Описание задачи");
        manager.addTask(taskWithGivenId);

        // When
        for (int i = 0; i < TASKS_COUNT_10; i++) {
            Task task = new Task(i, TASK_GENERATED_ID_PREFIX + i, TASK_DESCRIPTION_PREFIX + i);
            manager.addTask(task);
        }

        // Then
        List<Task> tasks = manager.getTasks();
        ArrayList<Integer> uniqueIds = new ArrayList<>();

        for (Task task : tasks) {
            int currentId = task.getId();
            boolean isUnique = true;

            for (int existingId : uniqueIds) {
                if (existingId == currentId) {
                    isUnique = false;
                    break;
                }
            }

            assertTrue(isUnique, "Все id должны быть уникальными");
            if (isUnique) {
                uniqueIds.add(currentId);
            }
        }
    }

    @Test
    public void taskFieldsDoNotChanged() {
        // Given
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task originalTask = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);

        int originalId = originalTask.getId();
        String originalName = originalTask.getName();
        String originalDescription = originalTask.getDescription();

        // When
        manager.addTask(originalTask);

        // Then
        Task retrievedTask = manager.findTaskById(originalId);
        assertEquals(retrievedTask.getId(), originalId, "ID задачи не изменяется");
        assertEquals(retrievedTask.getName(), originalName, "Название задачи не изменяется");
        assertEquals(retrievedTask.getDescription(), originalDescription, "Описание задачи не изменяется");
    }
}


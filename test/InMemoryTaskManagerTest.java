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

    @Test
    public void testWithoutStoringOldId() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        SubTask subTask = new SubTask(1, "Подзадача 1", "Описание подзадачи 1", 1);
        manager.addSubTask(subTask);

        assertTrue(manager.getSubTasks().contains(subTask));

        manager.deleteSubTask();

        assertEquals(0, manager.getSubTasks().size()); // Проверка после удаления
    }

    @Test
    public void testWithoutIrrelevantSubtasksId() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Epic epic = new Epic(1, "Эпик 1", "Описание эпика 1", LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask = new SubTask(2, "Подзадача 1", "Описание подзадачи 1", 1);

        manager.addEpic(epic);
        manager.addSubTask(subTask);

        List<SubTask> subTasksBeforeDeletion = manager.getSubTasksByEpicId(epic.getId());
        assertTrue(subTasksBeforeDeletion.contains(subTask)); // добавлена ли подзадача в эпик

        manager.deleteSubTask();

        List<SubTask> subTasksAfterDeletion = manager.getSubTasksByEpicId(epic.getId());
        assertFalse(subTasksAfterDeletion.contains(subTask)); // проверка удаленной подзадачи из эпика
    }

    @Test
    public void testChangingTaskFields() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task(1, "Задача 1", "Описание задачи 1");

        manager.addTask(task);

        assertEquals("Задача 1", task.getName());
        assertEquals("Описание задачи 1", task.getDescription());

        task.setName("Новое название"); // изменение полей
        task.setDescription("Новое описание");

        Task updatedTask = manager.findTaskById(task.getId());
        assertNotNull(updatedTask);
        assertEquals("Новое название", updatedTask.getName());
        assertEquals("Новое описание", updatedTask.getDescription());
    }

    @Test
    public void addAndFindTasks() {
        InMemoryTaskManager manager;
        manager = new InMemoryTaskManager() {

        };

        Task task = new Task(1, "Задача 1", "Описание задачи 1", LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask = new SubTask(2, "Подзадача 1", "Описание подзадачи 1", 1);
        Epic epic = new Epic(3, "Эпик 1", "Описание эпика 1", LocalDateTime.now(), Duration.ofHours(1));

        manager.addTask(task);
        assertNotNull(manager.findTaskById(1), "Задача должна быть найдена");

        manager.addSubTask(subTask);
        assertNotNull(manager.findSubTaskById(2), "Подзадача должна быть найдена");

        manager.addEpic(epic);
        assertNotNull(manager.findEpicById(3), "Эпик должен быть найден");
    }

    @Test
    public void taskDoNotConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager() {

        };

        Task taskWithGivenId = new Task(1, "ID задан", "Описание задачи");
        manager.addTask(taskWithGivenId);

        for (int i = 0; i < 10; i++) {
            Task task = new Task(i, "ID сгенерирован" + i, "Описание задачи " + i);
            manager.addTask(task);
        }

        List<Task> tasks = manager.getTasks(); // проверка на уникальность id
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

        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task originalTask = new Task(1, "Задача 1", "Описание задачи 1");

        int originalId = originalTask.getId();
        String originalName = originalTask.getName();
        String originalDescription = originalTask.getDescription();

        manager.addTask(originalTask);

        Task retrievedTask = manager.findTaskById(originalId);

        assertEquals(retrievedTask.getId(), originalId, "ID задачи не изменяется");
        assertEquals(retrievedTask.getName(), originalName, "Название задачи не изменяется");
        assertEquals(retrievedTask.getDescription(), originalDescription, "Описание задачи не изменяется");
    }
}


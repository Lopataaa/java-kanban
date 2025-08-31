import org.junit.jupiter.api.Test;
import manager.Managers;
import manager.HistoryManager;
import manager.TaskManager;
import manager.InMemoryTaskManager;
import manager.InMemoryHistoryManager;
import manager.FileBackedTaskManager;
import manager.HistoryManager;
import task.Epic;
import task.SubTask;
import task.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.TaskStatus;

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
    public void idEqualsForSubTaskClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, "Эпик 1", "Описание эпика 1");
        SubTask subTask1 = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask(newId, "Подзадача 2 для эпика 1", "Описание подзадачи 2", epic1.getId());
        if (subTask1.getId() == subTask2.getId()) {
            assertEquals(subTask1, subTask2);
        }
    }

    @Test
    public void idEqualsForEpicClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1");
        Epic epic2 = new Epic(newId, "Подзадача 2 для эпика 1", "Описание подзадачи 2");
        if (epic1.getId() == epic2.getId()) {
            assertEquals(epic1, epic2);
        }
    }

    @Test
    public void subTaskCannotBeEpic() {
        int newId = 1;
        Epic epic = new Epic(newId, "Эпик 1", "Описание эпика 1");
        SubTask subTask = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1", epic.getId());
        assertNotEquals(epic.getClass(), subTask.getClass(), "SubTask не должен быть экземпляром Epic");
    }

    @Test
    public void epicCannotBeSubTask() {
        int newId = 1;
        Epic epic = new Epic(newId, "Эпик 1", "Описание эпика 1");
        SubTask subTask = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1", epic.getId());
        assertNotEquals(subTask.getClass(), epic.getClass(), "объект Subtask нельзя сделать своим же эпиком");
    }

    @Test
    public void managersInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");
    }

    @Test
    public void addAndFindTasks() {
        InMemoryTaskManager manager;
        manager = new InMemoryTaskManager() {

        };

        Task task = new Task(1, "Задача 1", "Описание задачи 1");
        SubTask subTask = new SubTask(2, "Подзадача 1", "Описание подзадачи 1", 1);
        Epic epic = new Epic(3, "Эпик 1", "Описание эпика 1");

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
        Epic epic = new Epic(1, "Эпик 1", "Описание эпика 1");
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

    @Test // Сохранение и загрузка пустого файла
    public void testSaveAndLoadEmptyFile() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt");
            tempFile.deleteOnExit(); //После завершения программы удалить файл

            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
            manager.save(); // Сохраняем пустой менеджер

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
            assertTrue(loadedManager.getTasks().isEmpty()); // Проверяем, что задач нет
            assertTrue(loadedManager.getEpics().isEmpty()); // Проверяем, что эпиков нет
            assertTrue(loadedManager.getSubTasks().isEmpty()); // Проверяем, что подзадач нет
        } catch (IOException e) {
            e.printStackTrace();
            fail("Произошла ошибка ввода-вывода: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }


    @Test // Сохранение нескольких задач
    public void testSaveMultipleTasks() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt");
            tempFile.deleteOnExit(); //После завершения программы удалить файл

            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());

            Task task1 = new Task(1, "Задача 1", "Описание задачи 1");
            Epic epic1 = new Epic(2, "Эпик 1", "Описание эпика 1");
            SubTask subTask1 = new SubTask(3, "Подзадача 1", "Описание подзадачи 1", epic1.getId());

            manager.addTask(task1);
            manager.addEpic(epic1);
            manager.addSubTask(subTask1);

            manager.save(); // Сохраняем менеджер с задачами
        } catch (IOException e) {
            e.printStackTrace();
            fail("Произошла ошибка ввода-вывода: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }


    @Test // Загрузка нескольких задач
    public void testLoadMultipleTasks() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt");
            tempFile.deleteOnExit(); //После завершения программы удалить файл

            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
            Task task1 = new Task(1, "Задача 1", "Описание задачи 1");
            Epic epic1 = new Epic(2, "Эпик 1", "Описание эпика 1");
            SubTask subTask1 = new SubTask(3, "Подзадача 1", "Описание подзадачи 1", epic1.getId());

            manager.addTask(task1);
            manager.addEpic(epic1);
            manager.addSubTask(subTask1);
            manager.save();

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

            assertEquals(1, loadedManager.getTasks().size()); // Проверяем количество задач
        } catch (IOException e) {
            e.printStackTrace();
            fail("Произошла ошибка ввода-вывода: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    @Test
    public void testSaveAndLoadTasks() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt");
            tempFile.deleteOnExit();

            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
            Task task1 = new Task(1, "Задача 1", "Описание задачи 1");
            manager.addTask(task1);
            manager.save();

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

            assertEquals(1, loadedManager.getTasks().size());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Произошла ошибка ввода-вывода: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
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

    @Test
    public void testEndTime() {
        Epic epic = new Epic(1, "Эпик", "Описание эпика");
        epic.setStartTime(LocalDateTime.now());
        epic.setDuration(Duration.ofHours(2));

        assertEquals(epic.getStartTime().plus(epic.getDuration()), epic.getEndTime());
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

class EpicStatusTest {
    private Epic epic;
    private List<SubTask> subTasks;

    @BeforeEach
    public void setUp() {
        epic = new Epic(1, "Эпик", "Описание");
        subTasks = new ArrayList<>();
    }

    @Test
    public void testAllSubtasksNew() {
        // Создаём подзадачи со статусом NEW
        subTasks.add(new SubTask(1, "Подзадача 1", "Описание подзадачи 1", 1));
        subTasks.add(new SubTask(2, "Подзадача 2", "Описание подзадачи 2", 1));

        // Проверяем статус эпика
        assertEquals("NEW", epic.getStatus());
    }

    @Test
    public void testAllSubtasksDone() {
        // Создаём подзадачи со статусом DONE
        subTasks.add(new SubTask(1, "Подзадача 1", "Описание подзадачи 1", 1));
        subTasks.add(new SubTask(2, "Подзадача 2", "Описание подзадачи 2", 1));

        // Проверяем статус эпика
        assertEquals("DONE", epic.getStatus());
    }

    /*@Test
    void testEpicStatus() {
        Epic epicFromManager = manager.getEpic(epicId);

        SubTask subtask4 = new SubTask(4, "СабТаска1", "СабТаска1_Тест", epicId);
        SubTask subtask5 = new SubTask(5, "СабТаска2", "СабТаска2_Тест", epicId);
        SubTask subtask6 = new SubTask(6, "СабТаска3", "СабТаска3_Тест", epicId);

        manager.createSubTask(subtask4);
        manager.createSubTask(subtask5);

        subtask4.setDuration(Duration.ofHours(25));
        subtask5.setDuration(Duration.ofHours(49));
        subtask6.setStatus(TaskStatus.IN_PROGRESS);

        manager.createSubTask(subtask6);

        assertEquals("IN_PROGRESS", epicFromManager.getStatus().toString());
    }*/

    @Test
    public void testMixedSubtasksStatuses() {
        // Создаём подзадачи со смешанными статусами
        subTasks.add(new SubTask(1, "Подзадача 1", "Описание подзадачи 1", 1));
        subTasks.add(new SubTask(2, "Подзадача 2", "Описание подзадачи 2", 1));

        // Проверяем статус эпика
        assertEquals("IN_PROGRESS", epic.getStatus());
    }

    @Test
    public void testSubtasksInProgress() {
        // Создаём подзадачи со статусом IN_PROGRESS
        subTasks.add(new SubTask(1, "Подзадача 1", "Описание подзадачи 1", 1));
        subTasks.add(new SubTask(2, "Подзадача 2", "Описание подзадачи 2", 1));

        // Проверяем статус эпика
        assertEquals("IN_PROGRESS", epic.getStatus());
    }

    @Test // Для подзадач необходимо дополнительно убедиться в наличии связанного эпика
    public void testEpicSubTaskConnection() {
        // Создаем эпик
        Epic epic = new Epic(1, "Эпик", "Описание");

        // Создаем подзадачи и добавляем их в эпик
        SubTask subTask1 = new SubTask(10, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask subTask2 = new SubTask(11, "Подзадача 2", "Описание подзадачи 2", epic.getId());

        epic.addSubTaskId(subTask1.getId());
        epic.addSubTaskId(subTask2.getId());

        // Проверяем, что идентификаторы эпика в подзадачах соответствуют идентификатору эпика
        assertEquals(epic.getId(), subTask1.getEpicId());
        assertEquals(epic.getId(), subTask2.getEpicId());
    }

    @Test // Для эпиков нужно проверить корректность расчёта статуса на основании состояния подзадач
    public void testEpicStatusCalculation() {
        // Создаем эпик
        Epic epic = new Epic(1, "Эпик", "Описание");

        // Создаем подзадачи с разными статусами
        SubTask subTask1 = new SubTask(10, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask subTask2 = new SubTask(11, "Подзадача 2", "Описание подзадачи 2", epic.getId());
        SubTask subTask3 = new SubTask(12, "Подзадача 3", "Описание подзадачи 3", epic.getId());

        epic.addSubTaskId(subTask1.getId());
        epic.addSubTaskId(subTask2.getId());
        epic.addSubTaskId(subTask3.getId());

         epic.getSubTaskIds(Arrays.asList(subTask1, subTask2, subTask3));

        // Проверяем статус эпика
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test //Тест на проверку пересечения интервалов
    public void testHasOverIntersectionTasks() {
        // Создаем задачи с разными временными интервалами
        Task task1 = new Task(1, "Задача 1", "Описание подзадачи 1",
                LocalDateTime.of(2025, 8, 24, 10, 0),
                LocalDateTime.of(2025, 8, 24, 13, 0));
        Task task2 = new Task(2, "Задача 2", "Описание подзадачи 2",
                LocalDateTime.of(2025, 8, 24, 12, 0),
                LocalDateTime.of(2025, 8, 24, 15, 0)); // Пересекается с task1
        Task task3 = new Task(3, "Задача 3", "Описание подзадачи 3",
                LocalDateTime.of(2025, 8, 24, 16, 0),
                LocalDateTime.of(2025, 8, 24, 19, 0)); // Не пересекается
        // с task1 и task2

        // Добавляем задачи в менеджер
        TaskManager taskManager = new InMemoryTaskManager();
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        // Проверяем наличие пересечения
        assertTrue(taskManager.hasOverIntersectionTasks(task2)); // task2 пересекается с task1
        assertFalse(taskManager.hasOverIntersectionTasks(task3)); // task3 не пересекается ни с одной из добавленных задач
    }
}

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    @BeforeEach
    public void setUp() {
        // Инициализация taskManager для конкретной реализации
    }

    @Test
    public void testAddTask() {
        Task task = new Task("Задача", "Описание");
        int id = taskManager.addTask(task);
        Assertions.assertEquals(task, taskManager.findTaskById(id));
    }
}
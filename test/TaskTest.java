import org.junit.jupiter.api.Test;
import manager.Managers;
import manager.HistoryManager;
import manager.TaskManager;
import manager.InMemoryTaskManager;
import manager.InMemoryHistoryManager;
import task.Epic;
import task.SubTask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        InMemoryTaskManager manager = new InMemoryTaskManager();

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
        InMemoryTaskManager manager = new InMemoryTaskManager();

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

}
package task;

import org.junit.jupiter.api.Test;
import manager.Managers;
import manager.HistoryManager;
import manager.TaskManager;
import manager.InMemoryTaskManager;
import manager.InMemoryHistoryManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    public void idEqualsForTaskClasses() {
        int newId = 1;
        Task task1 = new Task(newId, "Задача 1", "Описание задачи 1");
        Task task2 = new Task(newId, "Задача 2", "Описание задачи 2");
        if (task1.id == task2.id) {
            assertEquals(task1, task2);
        }
    }

    @Test
    public void idEqualsForSubTaskClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, "Эпик 1", "Описание эпика 1");
        SubTask subTask1 = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask(newId, "Подзадача 2 для эпика 1", "Описание подзадачи 2", epic1.getId());
        if (subTask1.id == subTask2.id) {
            assertEquals(subTask1, subTask2);
        }
    }

    @Test
    public void idEqualsForEpicClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1");
        Epic epic2 = new Epic(newId, "Подзадача 2 для эпика 1", "Описание подзадачи 2");
        if (epic1.id == epic2.id) {
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
            Task task = new Task(i,"ID сгенерирован" + i, "Описание задачи " + i);
            manager.addTask(task);
        }

        ArrayList<Task> tasks = manager.getTasks(); // проверка на уникальность id
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
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task(1, "Задача 1", "Описание задачи 1"); // Создание задачи

        taskManager.addTask(task); // Добавляем задачу в менеджер задач
        historyManager.addToHistory(task); // Добавляем задачу в историю

        task.setName("Задача 1");
        taskManager.updateTask(task);

        List<Task> history = historyManager.getHistory(); //проверка предыдущей версии
        assertTrue(!history.isEmpty(), "История должна содержать хотя бы одну задачу");
        Task previousTask = history.get(history.size() - 1);
        assertEquals("Задача 1", previousTask.getName(), "Предыдущая версия задачи должна содержать исходное название");
    }
}
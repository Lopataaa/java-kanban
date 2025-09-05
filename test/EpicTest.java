import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void idEqualsForSubTaskClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, "Эпик 1", "Описание эпика 1");
        SubTask subTask1 = new SubTask(
                newId,
                "Подзадача 1 для эпика 1", "Описание подзадачи 1",
                epic1.getId());
        SubTask subTask2 = new SubTask(
                newId,
                "Подзадача 2 для эпика 1", "Описание подзадачи 2",
                epic1.getId());

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
    public void epicCannotBeSubTask() {
        int newId = 1;
        Epic epic = new Epic(newId, "Эпик 1", "Описание эпика 1");
        SubTask subTask = new SubTask(newId, "Подзадача 1 для эпика 1", "Описание подзадачи 1",
                epic.getId());
        assertNotEquals(subTask.getClass(), epic.getClass(), "объект Subtask нельзя сделать своим же эпиком");
    }

    @Test
    public void testEndTime_Epic() {
        TaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic(1, "Эпик", "Описание");
        tm.addEpic(epic);

        SubTask s1 = new SubTask(2, "S1", "D1", epic.getId());
        s1.setStartTime(LocalDateTime.now());
        s1.setDuration(Duration.ofHours(1));
        tm.addSubTask(s1);

        SubTask s2 = new SubTask(3, "S2", "D2", epic.getId());
        s2.setStartTime(LocalDateTime.now().plusHours(1));
        s2.setDuration(Duration.ofHours(2));
        tm.addSubTask(s2);

        // теперь endTime эпика = max(endTime подзадач)
        assertEquals(s2.getEndTime(), epic.getEndTime());
    }

    @Test
    void allSubtasksNew_epicIsNEW() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(100, "Эпик", "Описание");
        tm.addEpic(epic);

        // SubTasks (оба NEW)
        SubTask s1 = new SubTask(2, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask s2 = new SubTask(3, "Подзадача 2", "Описание подзадачи 2", epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.NEW);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверки статуса и связей
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertEquals(List.of(2, 3), epic.getSubTaskIds());
    }

    @Test
    public void allSubtasksDone_epicIsDONE() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(1, "Эпик", "Описание");
        tm.addEpic(epic);

        // Создаём подзадачи со статусом DONE
        SubTask s1 = new SubTask(1, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask s2 = new SubTask(2, "Подзадача 2", "Описание подзадачи 2", epic.getId());
        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверяем статус эпика
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void mixedNewAndDone_epicIsIN_PROGRESS() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(1, "Эпик", "Описание");
        tm.addEpic(epic);

        // Создаём подзадачи со статусом DONE
        SubTask s1 = new SubTask(1, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask s2 = new SubTask(2, "Подзадача 2", "Описание подзадачи 2", epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверяем статус эпика
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void allSubtasksInProgress_epicIsIN_PROGRESS() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(1, "Эпик", "Описание");
        tm.addEpic(epic);

        // Создаём подзадачи со статусом DONE
        SubTask s1 = new SubTask(1, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask s2 = new SubTask(2, "Подзадача 2", "Описание подзадачи 2", epic.getId());
        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.IN_PROGRESS);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверяем статус эпика
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void epicStatus_inProgress_whenAnySubtaskInProgress_andDurationsLong() {
        TaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic(1, "Эпик", "Описание эпика");
        manager.addEpic(epic);

        // Две NEW с «граничными» длительностями:
        SubTask s4 = new SubTask(4, "Подзадача 1", "Подзадача 1 Тест", epic.getId());
        s4.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        s4.setDuration(Duration.ofHours(25));     // 25 часов
        s4.setStatus(TaskStatus.NEW);

        SubTask s5 = new SubTask(5, "Подзадача 2", "Подзадача 2 Тест", epic.getId());
        s5.setStartTime(LocalDateTime.of(2025, 1, 2, 0, 0));
        s5.setDuration(Duration.ofHours(49));     // 49 часов
        s5.setStatus(TaskStatus.NEW);

        // Одна IN_PROGRESS
        SubTask s6 = new SubTask(6, "Подзадача 3", "Подзадача 3 Тест", epic.getId());
        s6.setStartTime(LocalDateTime.of(2025, 1, 3, 0, 0));
        s6.setDuration(Duration.ofHours(1));
        s6.setStatus(TaskStatus.IN_PROGRESS);

        manager.addSubTask(s4);
        manager.addSubTask(s5);
        manager.addSubTask(s6);

        // Проверка статуса
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Эпик должен стать IN_PROGRESS");

        // Дополнительно проверим агрегаты эпика
        assertEquals(
                s4.getStartTime(),
                epic.getStartTime(),
                "startTime эпика — минимум startTime его подзадач"
        );
        assertEquals(
                s5.getEndTime(),
                epic.getEndTime(),
                "endTime эпика — максимум endTime его подзадач"
        );
        assertEquals(
                s4.getDuration().plus(s5.getDuration()).plus(s6.getDuration()),
                epic.getDuration(),
                "duration эпика — сумма длительностей подзадач"
        );
    }

    @Test // Для подзадач необходимо убедиться в наличии связанного эпика
    public void epicSubTaskConnection_managerMaintainsLinks() {
        TaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic(1, "Эпик", "Описание");
        manager.addEpic(epic);

        SubTask subTask1 = new SubTask(10, "Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask subTask2 = new SubTask(11, "Подзадача 2", "Описание подзадачи 2", epic.getId());

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        // 1) у подзадач должен быть корректный epicId
        assertEquals(epic.getId(), subTask1.getEpicId());
        assertEquals(epic.getId(), subTask2.getEpicId());

        // 2) эпик должен содержать id обеих подзадач
        assertTrue(epic.getSubTaskIds().containsAll(List.of(10, 11)));

        // 3) менеджер должен уметь вернуть подзадачи по epicId
        List<SubTask> byEpic = manager.getSubTasksByEpicId(epic.getId());
        assertEquals(2, byEpic.size());
        assertTrue(byEpic.stream().map(SubTask::getId).toList().containsAll(List.of(10, 11)));
    }

    @Test // Тест на проверку пересечения интервалов
    public void hasOverIntersectionTasks_detectsOverlaps() {
        Task t1 = new Task(1, "Задача 1", "Описание 1");
        t1.setStartTime(LocalDateTime.of(2025, 8, 24, 10, 0));
        t1.setDuration(Duration.ofHours(3)); // 10:00–13:00

        Task t2 = new Task(2, "Задача 2", "Описание 2");
        t2.setStartTime(LocalDateTime.of(2025, 8, 24, 12, 0));
        t2.setDuration(Duration.ofHours(3)); // 12:00–15:00  (пересекается с t1)

        Task t3 = new Task(3, "Задача 3", "Описание 3");
        t3.setStartTime(LocalDateTime.of(2025, 8, 24, 16, 0));
        t3.setDuration(Duration.ofHours(3)); // 16:00–19:00  (не пересекается)

        TaskManager taskManager = new InMemoryTaskManager();
        taskManager.addTask(t1);
        taskManager.addTask(t2);

        assertTrue(taskManager.hasOverIntersectionTasks(t2), "t2 должен пересекаться с t1");
        assertFalse(taskManager.hasOverIntersectionTasks(t3), "t3 не должен пересекаться ни с t1, ни с t2");
    }
}
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.DisplayName;
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

    private static final String SUBTASK_NAME = "Имя подзадачи";
    private static final String SUBTASK_DESCRIPTION = "Описание подзадачи";
    private static final String SUBTASK_NAME_1 = "Имя подзадачи 1";
    private static final String SUBTASK_DESCRIPTION_1 = "Описание подзадачи 1";
    private static final String SUBTASK_NAME_2 = "Имя подзадачи 2";
    private static final String SUBTASK_DESCRIPTION_2 = "Описание подзадачи 2";
    private static final String EPIC_NAME = "Имя эпика";
    private static final String EPIC_DESCRIPTION = "Описание эпика";
    private static final String EPIC_NAME_1 = "Имя эпика 1";
    private static final String EPIC_DESCRIPTION_1 = "Описание эпика 1";
    private static final String EPIC_NAME_2 = "Имя эпика 2";
    private static final String EPIC_DESCRIPTION_2 = "Описание эпика2";

    @Test
    @DisplayName("Checking the Id of two subtasks")
    public void idEquals_ForSubTaskClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask1 = new SubTask(
                newId, SUBTASK_NAME_1,SUBTASK_DESCRIPTION_1, epic1.getId());
        SubTask subTask2 = new SubTask(
                newId,SUBTASK_NAME_2,SUBTASK_DESCRIPTION_2, epic1.getId());

        if (subTask1.getId() == subTask2.getId()) {
            assertEquals(subTask1, subTask2);
        }
    }

    @Test
    @DisplayName("Checking the Id of two epics")
    public void idEqualsForEpicClasses() {
        int newId = 1;
        Epic epic1 = new Epic(newId, EPIC_NAME_1,EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(1));
        Epic epic2 = new Epic(newId, EPIC_NAME_2,EPIC_DESCRIPTION_2, LocalDateTime.now(), Duration.ofHours(1));
        if (epic1.getId() == epic2.getId()) {
            assertEquals(epic1, epic2);
        }
    }

    @Test
    @DisplayName("An object of the SubTask class cannot be an instance of the Epic class")
    public void epicCannotBeSubTask() {
        int newId = 1;
        Epic epic = new Epic(newId, EPIC_NAME,EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask = new SubTask(newId, SUBTASK_NAME,SUBTASK_DESCRIPTION, epic.getId());
        assertNotEquals(subTask.getClass(), epic.getClass(), "объект Subtask нельзя сделать своим же эпиком");
    }

    @Test
    @DisplayName("Checking the epic's time calculation")
    public void testEndTime_Epic() {
        TaskManager tm = new InMemoryTaskManager();
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);

        Epic epic = new Epic(1, EPIC_NAME,EPIC_DESCRIPTION, now, Duration.ofHours(1));
        tm.addEpic(epic);

        SubTask s1 = new SubTask(2, "S1", "D1", epic.getId());
        s1.setStartTime(now);                    // 10:00
        s1.setDuration(Duration.ofHours(1));     // → 11:00
        tm.addSubTask(s1);

        SubTask s2 = new SubTask(3, "S2", "D2", epic.getId());
        s2.setStartTime(now.plusHours(1).plusMinutes(1)); // 11:01 — гарантируем зазор
        s2.setDuration(Duration.ofHours(2));              // → 13:01
        tm.addSubTask(s2);

        assertEquals(s2.getEndTime(), epic.getEndTime(), "endTime эпика должен быть равен endTime последней подзадачи");
    }

    @Test
    @DisplayName("If all the subtasks of an epic have a NEW status, then the status of the epic itself will also be NEW.")
    void allSubtasksNew_epicIsNEW() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(100, EPIC_NAME,EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(1));
        tm.addEpic(epic);

        // SubTasks (оба NEW)
        SubTask s1 = new SubTask(2, SUBTASK_NAME_1,SUBTASK_DESCRIPTION_1, epic.getId());
        SubTask s2 = new SubTask(3, SUBTASK_NAME_2,SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.NEW);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверки статуса и связей
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertEquals(List.of(2, 3), epic.getSubTaskIds());
    }

    @Test
    @DisplayName("If all the subtasks of an epic have the status of DONE, then the status of the epic itself also becomes DONE.")
    public void allSubtasksDone_epicIsDONE() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(1, EPIC_NAME,EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(1));
        tm.addEpic(epic);

        // Создаём подзадачи со статусом DONE
        SubTask s1 = new SubTask(1, SUBTASK_NAME_1,SUBTASK_DESCRIPTION_1, epic.getId());
        SubTask s2 = new SubTask(2, SUBTASK_NAME_2,SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверяем статус эпика
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    @DisplayName("If an epic has subtasks with mixed statuses, then the status of the epic itself becomes IN_PROGRESS.")
    void mixedNewAndDone_epicIsIN_PROGRESS() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(1, EPIC_NAME,EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(1));
        tm.addEpic(epic);

        // Создаём подзадачи со статусом DONE
        SubTask s1 = new SubTask(1, SUBTASK_NAME_1,SUBTASK_DESCRIPTION_1, epic.getId());
        SubTask s2 = new SubTask(2, SUBTASK_NAME_2,SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверяем статус эпика
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    @DisplayName("If all the subtasks of the epic have the status IN_PROGRESS, then the status of the epic itself also becomes IN_PROGRESS.")
    void allSubtasksInProgress_epicIsIN_PROGRESS() {
        TaskManager tm = new InMemoryTaskManager();

        // Epic
        Epic epic = new Epic(1, EPIC_NAME,EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(1));
        tm.addEpic(epic);

        // Создаём подзадачи со статусом DONE
        SubTask s1 = new SubTask(1, SUBTASK_NAME_1,SUBTASK_DESCRIPTION_2, epic.getId());
        SubTask s2 = new SubTask(2, SUBTASK_NAME_1,SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.IN_PROGRESS);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // Проверяем статус эпика
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    @DisplayName("Checking the calculation of the epic's status and its time and duration")
    void epicStatus_inProgress_whenAnySubtaskInProgress_andDurationsLong() {
        TaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic(1, EPIC_NAME,EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(1));
        manager.addEpic(epic);

        // Используем непересекающиеся интервалы
        SubTask s4 = new SubTask(4, "Подзадача 1", "Подзадача 1 Тест", epic.getId());
        s4.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        s4.setDuration(Duration.ofHours(24));     // 00:00 → 24:00 (2025-01-02 00:00)
        s4.setStatus(TaskStatus.NEW);

        SubTask s5 = new SubTask(5, "Подзадача 2", "Подзадача 2 Тест", epic.getId());
        s5.setStartTime(LocalDateTime.of(2025, 1, 2, 0, 0)); // начинается сразу после s4
        s5.setDuration(Duration.ofHours(48));     // 00:00 → 48:00 (2025-01-04 00:00)
        s5.setStatus(TaskStatus.NEW);

        SubTask s6 = new SubTask(6, "Подзадача 3", "Подзадача 3 Тест", epic.getId());
        s6.setStartTime(LocalDateTime.of(2025, 1, 4, 0, 0)); // начинается сразу после s5
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
                s6.getEndTime(), // теперь s6 — последняя по времени
                epic.getEndTime(),
                "endTime эпика — максимум endTime его подзадач"
        );
        assertEquals(
                s4.getDuration().plus(s5.getDuration()).plus(s6.getDuration()),
                epic.getDuration(),
                "duration эпика — сумма длительностей подзадач"
        );
    }



    @Test // Тест на проверку пересечения интервалов
    @DisplayName("Checking the intersection of intervals")
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
        taskManager.addTask(t1); // добавляем только t1

        // Проверяем пересечения БЕЗ добавления t2 и t3 в менеджер
        assertTrue(taskManager.hasOverIntersectionTasks(t2), "t2 должен пересекаться с t1");
        assertFalse(taskManager.hasOverIntersectionTasks(t3), "t3 не должен пересекаться ни с t1, ни с t2");

        // Дополнительно: проверим, что при попытке добавить t2 — будет исключение
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(t2),
                "Должно быть исключение при добавлении пересекающейся задачи"
        );
        assertTrue(exception.getMessage().contains("пересекается"));
    }
}
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
    private static final String SUBTASK_S1_NAME = "S1";
    private static final String SUBTASK_S1_DESC = "D1";
    private static final String SUBTASK_S2_NAME = "S2";
    private static final String SUBTASK_S2_DESC = "D2";
    private static final String SUBTASK_1_NAME = "Подзадача 1";
    private static final String SUBTASK_1_DESC = "Подзадача 1 Тест";
    private static final String SUBTASK_2_NAME = "Подзадача 2";
    private static final String SUBTASK_2_DESC = "Подзадача 2 Тест";
    private static final String SUBTASK_3_NAME = "Подзадача 3";
    private static final String SUBTASK_3_DESC = "Подзадача 3 Тест";
    private static final String TASK_1_NAME = "Задача 1";
    private static final String TASK_1_DESC = "Описание 1";
    private static final String TASK_2_NAME = "Задача 2";
    private static final String TASK_2_DESC = "Описание 2";
    private static final String TASK_3_NAME = "Задача 3";
    private static final String TASK_3_DESC = "Описание 3";
    private static final int NEW_ID = 1;
    private static final int EPIC_ID_1 = 1;
    private static final int SUBTASK_ID_2 = 2;
    private static final int SUBTASK_ID_3 = 3;
    private static final int SUBTASK_ID_4 = 4;
    private static final int SUBTASK_ID_5 = 5;
    private static final int SUBTASK_ID_6 = 6;
    private static final int EPIC_ID_100 = 100;
    private static final int HOURS_DURATION_1 = 1;
    private static final int HOURS_DURATION_2 = 2;
    private static final int HOURS_DURATION_3 = 3;
    private static final int HOURS_DURATION_24 = 24;
    private static final int HOURS_DURATION_48 = 48;
    private static final int YEAR_2025 = 2025;
    private static final int MONTH_1 = 1;
    private static final int DAY_1 = 1;
    private static final int HOUR_10 = 10;
    private static final int MINUTE_0 = 0;
    private static final int MINUTE_1 = 1;
    private static final int MONTH_8 = 8;
    private static final int DAY_24 = 24;
    private static final int HOUR_12 = 12;
    private static final int HOUR_16 = 16;

    @Test
    @DisplayName("Checking the Id of two subtasks")
    public void idEquals_ForSubTaskClasses() {
        // Given
        Epic epic1 = new Epic(NEW_ID, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        SubTask subTask1 = new SubTask(NEW_ID, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, epic1.getId());
        SubTask subTask2 = new SubTask(NEW_ID, SUBTASK_NAME_2, SUBTASK_DESCRIPTION_2, epic1.getId());

        // When
        // Then
        if (subTask1.getId() == subTask2.getId()) {
            assertEquals(subTask1, subTask2);
        }
    }

    @Test
    @DisplayName("Checking the Id of two epics")
    public void idEqualsForEpicClasses() {
        // Given
        Epic epic1 = new Epic(NEW_ID, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        Epic epic2 = new Epic(NEW_ID, EPIC_NAME_2, EPIC_DESCRIPTION_2, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));

        // When
        // Then
        if (epic1.getId() == epic2.getId()) {
            assertEquals(epic1, epic2);
        }
    }

    @Test
    @DisplayName("An object of the SubTask class cannot be an instance of the Epic class")
    public void epicCannotBeSubTask() {
        // Given
        Epic epic = new Epic(NEW_ID, EPIC_NAME, EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        SubTask subTask = new SubTask(NEW_ID, SUBTASK_NAME, SUBTASK_DESCRIPTION, epic.getId());

        // When
        // Then
        assertNotEquals(subTask.getClass(), epic.getClass(), "объект Subtask нельзя сделать своим же эпиком");
    }

    @Test
    @DisplayName("Checking the epic's time calculation")
    public void testEndTime_Epic() {
        // Given
        TaskManager tm = new InMemoryTaskManager();
        LocalDateTime now = LocalDateTime.of(YEAR_2025, MONTH_1, DAY_1, HOUR_10, MINUTE_0);

        Epic epic = new Epic(EPIC_ID_1, EPIC_NAME, EPIC_DESCRIPTION, now, Duration.ofHours(HOURS_DURATION_1));
        tm.addEpic(epic);

        SubTask s1 = new SubTask(SUBTASK_ID_2, SUBTASK_S1_NAME, SUBTASK_S1_DESC, epic.getId());
        s1.setStartTime(now);
        s1.setDuration(Duration.ofHours(HOURS_DURATION_1));
        tm.addSubTask(s1);

        SubTask s2 = new SubTask(SUBTASK_ID_3, SUBTASK_S2_NAME, SUBTASK_S2_DESC, epic.getId());
        s2.setStartTime(now.plusHours(HOURS_DURATION_1).plusMinutes(MINUTE_1));
        s2.setDuration(Duration.ofHours(HOURS_DURATION_2));
        tm.addSubTask(s2);

        // When
        // Then
        assertEquals(s2.getEndTime(), epic.getEndTime(), "endTime эпика должен быть равен endTime последней подзадачи");
    }

    @Test
    @DisplayName("If all the subtasks of an epic have a NEW status, then the status of the epic itself will also be NEW.")
    void allSubtasksNew_epicIsNEW() {
        // Given
        TaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic(EPIC_ID_100, EPIC_NAME, EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        tm.addEpic(epic);

        SubTask s1 = new SubTask(SUBTASK_ID_2, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, epic.getId());
        SubTask s2 = new SubTask(SUBTASK_ID_3, SUBTASK_NAME_2, SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.NEW);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // When
        // Then
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertEquals(List.of(SUBTASK_ID_2, SUBTASK_ID_3), epic.getSubTaskIds());
    }

    @Test
    @DisplayName("If all the subtasks of an epic have the status of DONE, then the status of the epic itself also becomes DONE.")
    public void allSubtasksDone_epicIsDONE() {
        // Given
        TaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic(EPIC_ID_1, EPIC_NAME, EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        tm.addEpic(epic);

        SubTask s1 = new SubTask(EPIC_ID_1, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, epic.getId());
        SubTask s2 = new SubTask(SUBTASK_ID_2, SUBTASK_NAME_2, SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // When
        // Then
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    @DisplayName("If an epic has subtasks with mixed statuses, then the status of the epic itself becomes IN_PROGRESS.")
    void mixedNewAndDone_epicIsIN_PROGRESS() {
        // Given
        TaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic(EPIC_ID_1, EPIC_NAME, EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        tm.addEpic(epic);

        SubTask s1 = new SubTask(EPIC_ID_1, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, epic.getId());
        SubTask s2 = new SubTask(SUBTASK_ID_2, SUBTASK_NAME_2, SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // When
        // Then
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    @DisplayName("If all the subtasks of the epic have the status IN_PROGRESS, then the status of the epic itself also becomes IN_PROGRESS.")
    void allSubtasksInProgress_epicIsIN_PROGRESS() {
        // Given
        TaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic(EPIC_ID_1, EPIC_NAME, EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        tm.addEpic(epic);

        SubTask s1 = new SubTask(EPIC_ID_1, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_2, epic.getId());
        SubTask s2 = new SubTask(SUBTASK_ID_2, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_2, epic.getId());
        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.IN_PROGRESS);

        tm.addSubTask(s1);
        tm.addSubTask(s2);

        // When
        // Then
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    @DisplayName("Checking the calculation of the epic's status and its time and duration")
    void epicStatus_inProgress_whenAnySubtaskInProgress_andDurationsLong() {
        // Given
        TaskManager manager = new InMemoryTaskManager();
        Epic epic = new Epic(EPIC_ID_1, EPIC_NAME, EPIC_DESCRIPTION, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        manager.addEpic(epic);

        SubTask s4 = new SubTask(SUBTASK_ID_4, SUBTASK_1_NAME, SUBTASK_1_DESC, epic.getId());
        s4.setStartTime(LocalDateTime.of(YEAR_2025, MONTH_1, DAY_1, MINUTE_0, MINUTE_0));
        s4.setDuration(Duration.ofHours(HOURS_DURATION_24));
        s4.setStatus(TaskStatus.NEW);

        SubTask s5 = new SubTask(SUBTASK_ID_5, SUBTASK_2_NAME, SUBTASK_2_DESC, epic.getId());
        s5.setStartTime(LocalDateTime.of(YEAR_2025, MONTH_1, DAY_1 + 1, MINUTE_0, MINUTE_0));
        s5.setDuration(Duration.ofHours(HOURS_DURATION_48));
        s5.setStatus(TaskStatus.NEW);

        SubTask s6 = new SubTask(SUBTASK_ID_6, SUBTASK_3_NAME, SUBTASK_3_DESC, epic.getId());
        s6.setStartTime(LocalDateTime.of(YEAR_2025, MONTH_1, DAY_1 + 3, MINUTE_0, MINUTE_0));
        s6.setDuration(Duration.ofHours(HOURS_DURATION_1));
        s6.setStatus(TaskStatus.IN_PROGRESS);

        manager.addSubTask(s4);
        manager.addSubTask(s5);
        manager.addSubTask(s6);

        // When
        // Then
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Эпик должен стать IN_PROGRESS");
        assertEquals(s4.getStartTime(), epic.getStartTime(), "startTime эпика — минимум startTime его подзадач");
        assertEquals(s6.getEndTime(), epic.getEndTime(), "endTime эпика — максимум endTime его подзадач");
        assertEquals(s4.getDuration().plus(s5.getDuration()).plus(s6.getDuration()), epic.getDuration(), "duration эпика — сумма длительностей подзадач");
    }

    @Test
    @DisplayName("Checking the intersection of intervals")
    public void hasOverIntersectionTasks_detectsOverlaps() {
        // Given
        Task t1 = new Task(EPIC_ID_1, TASK_1_NAME, TASK_1_DESC);
        t1.setStartTime(LocalDateTime.of(YEAR_2025, MONTH_8, DAY_24, HOUR_10, MINUTE_0));
        t1.setDuration(Duration.ofHours(HOURS_DURATION_3));

        Task t2 = new Task(SUBTASK_ID_2, TASK_2_NAME, TASK_2_DESC);
        t2.setStartTime(LocalDateTime.of(YEAR_2025, MONTH_8, DAY_24, HOUR_12, MINUTE_0));
        t2.setDuration(Duration.ofHours(HOURS_DURATION_3));

        Task t3 = new Task(SUBTASK_ID_3, TASK_3_NAME, TASK_3_DESC);
        t3.setStartTime(LocalDateTime.of(YEAR_2025, MONTH_8, DAY_24, HOUR_16, MINUTE_0));
        t3.setDuration(Duration.ofHours(HOURS_DURATION_3));

        TaskManager taskManager = new InMemoryTaskManager();
        taskManager.addTask(t1);

        // When
        // Then
        assertTrue(taskManager.hasOverIntersectionTasks(t2), "t2 должен пересекаться с t1");
        assertFalse(taskManager.hasOverIntersectionTasks(t3), "t3 не должен пересекаться ни с t1, ни с t2");

        // When
        // Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(t2), "Должно быть исключение при добавлении пересекающейся задачи");
        assertTrue(exception.getMessage().contains("пересекается"));
    }
}
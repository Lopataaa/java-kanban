import manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import task.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private static final String FILE_EMPTY_TASKS = "empty_tasks.csv";
    private static final String FILE_ONLY_TASKS = "only_tasks.csv";
    private static final String FILE_TASKS_ONLY = "tasks_only.csv";
    private static final String FILE_TASKS_ALL = "tasks_all.csv";
    private static final String CSV_HEADER = "id,type,name,description,status,epic,startTime,duration";
    private static final String TASK_NAME_1 = "Задача 1";
    private static final String TASK_DESCRIPTION_1 = "Описание задачи 1";
    private static final String EPIC_NAME_1 = "Эпик 1";
    private static final String EPIC_DESCRIPTION_1 = "Описание эпика 1";
    private static final String SUBTASK_NAME_1 = "Подзадача 1";
    private static final String SUBTASK_DESCRIPTION_1 = "Описание подзадачи 1";
    private static final String TASK_TYPE_TASK = "TASK";
    private static final String EMPTY_COLUMN = "";
    private static final int TASK_ID_1 = 1;
    private static final int EPIC_ID_2 = 2;
    private static final int SUBTASK_ID_3 = 3;
    private static final int EXPECTED_LINES_COUNT_1 = 1;
    private static final int EXPECTED_LINES_COUNT_2 = 2;
    private static final int EXPECTED_TASKS_COUNT_1 = 1;
    private static final int EXPECTED_EMPTY_COUNT = 0;
    private static final int HOURS_DURATION_1 = 1;
    private static final int CSV_PARTS_LENGTH = 8;
    private static final int TYPE_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int DESCRIPTION_INDEX = 3;
    private static final int EPIC_COLUMN_INDEX = 5;

    @Test
    void saveAndLoadEmptyFile_headerOnly(@TempDir File tempDir) throws IOException {
        // Given
        File file = new File(tempDir, FILE_EMPTY_TASKS);
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // When
        manager.save(TaskType.TASK);

        // Then
        assertTrue(file.exists(), "Файл должен существовать");
        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(EXPECTED_LINES_COUNT_1, lines.size(), "В пустом менеджере должна сохраниться только строка заголовка");
        assertEquals(CSV_HEADER, lines.get(0), "Неверный header");

        // When
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Then
        assertAll(
                () -> assertTrue(loaded.getTasks().isEmpty(), "Задачи должны быть пустыми"),
                () -> assertTrue(loaded.getEpics().isEmpty(), "Эпики должны быть пустыми"),
                () -> assertTrue(loaded.getSubTasks().isEmpty(), "Подзадачи должны быть пустыми")
        );
    }

    @Test
    void saveOnlyTasks_writesOnlyTaskLines(@TempDir File tempDir) throws IOException {
        // Given
        File file = new File(tempDir, FILE_ONLY_TASKS);
        FileBackedTaskManager m = new FileBackedTaskManager(file.getAbsolutePath());
        Task t1 = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);
        Epic e1 = new Epic(EPIC_ID_2, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));
        SubTask s1 = new SubTask(SUBTASK_ID_3, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, e1.getId());

        m.addEpic(e1);
        m.addTask(t1);
        m.addSubTask(s1);

        // When
        m.save(TaskType.TASK);

        // Then
        List<String> lines = Files.readAllLines(file.toPath());
        assertFalse(lines.isEmpty(), "CSV не должен быть пустым");
        assertEquals(CSV_HEADER, lines.get(0), "Неверный header");
        assertEquals(EXPECTED_LINES_COUNT_2, lines.size(), "Должна сохраниться ровно одна TASK-строка");

        String[] parts = lines.get(1).split(",", -1);
        assertEquals(CSV_PARTS_LENGTH, parts.length, "Неверное количество колонок в CSV");
        assertEquals(TASK_TYPE_TASK, parts[TYPE_INDEX], "В файл должны попадать только TASK-и");
        assertEquals(EMPTY_COLUMN, parts[EPIC_COLUMN_INDEX], "Колонка epic у обычной задачи должна быть пустой");
        assertEquals(TASK_NAME_1, parts[NAME_INDEX]);
        assertEquals(TASK_DESCRIPTION_1, parts[DESCRIPTION_INDEX]);
    }

    @Test
    void load_from_tasksOnlyFile_restoresOnlyTasks(@TempDir File tempDir) {
        // Given
        File file = new File(tempDir, FILE_TASKS_ONLY);
        FileBackedTaskManager m = new FileBackedTaskManager(file.getAbsolutePath());
        m.addTask(new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1));
        m.addEpic(new Epic(EPIC_ID_2, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1)));
        m.addSubTask(new SubTask(SUBTASK_ID_3, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, EPIC_ID_2));

        // When
        m.save(TaskType.TASK);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Then
        assertEquals(EXPECTED_TASKS_COUNT_1, loaded.getTasks().size());
        assertEquals(EXPECTED_EMPTY_COUNT, loaded.getEpics().size());
        assertEquals(EXPECTED_EMPTY_COUNT, loaded.getSubTasks().size());
    }

    @Test
    void saveAll_and_load_roundTrip(@TempDir File tempDir) {
        // Given
        File file = new File(tempDir, FILE_TASKS_ALL);
        FileBackedTaskManager m = new FileBackedTaskManager(file.getAbsolutePath());
        Task t1 = new Task(TASK_ID_1, TASK_NAME_1, TASK_DESCRIPTION_1);
        t1.setStatus(TaskStatus.NEW);

        Epic e1 = new Epic(EPIC_ID_2, EPIC_NAME_1, EPIC_DESCRIPTION_1, LocalDateTime.now(), Duration.ofHours(HOURS_DURATION_1));

        SubTask s1 = new SubTask(SUBTASK_ID_3, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, e1.getId());
        s1.setStatus(TaskStatus.NEW);

        m.addEpic(e1);
        m.addTask(t1);
        m.addSubTask(s1);

        // When
        m.saveAll();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Then
        assertEquals(EXPECTED_TASKS_COUNT_1, loaded.getTasks().size(), "Должна загрузиться 1 Task");
        assertEquals(EXPECTED_TASKS_COUNT_1, loaded.getEpics().size(), "Должен загрузиться 1 Epic");
        assertEquals(EXPECTED_TASKS_COUNT_1, loaded.getSubTasks().size(), "Должна загрузиться 1 SubTask");

        // Then
        Task lt = loaded.getTasks().get(0);
        assertEquals(TASK_NAME_1, lt.getName());
        assertEquals(TASK_DESCRIPTION_1, lt.getDescription());
        assertEquals(TaskStatus.NEW, lt.getStatus());

        Epic le = loaded.getEpics().get(0);
        assertEquals(EPIC_NAME_1, le.getName());
        assertEquals(EPIC_DESCRIPTION_1, le.getDescription());

        SubTask ls = loaded.getSubTasks().get(0);
        assertEquals(SUBTASK_NAME_1, ls.getName());
        assertEquals(SUBTASK_DESCRIPTION_1, ls.getDescription());
        assertEquals(TaskStatus.NEW, ls.getStatus());
        assertEquals(le.getId(), ls.getEpicId(), "SubTask должен ссылаться на Epic");
    }
}

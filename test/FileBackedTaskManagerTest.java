import manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import task.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    // Сохранение и загрузка пустого файла (пишется только header)
    @Test
    void saveAndLoadEmptyFile_headerOnly(@TempDir File tempDir) throws IOException {
        File file = new File(tempDir, "empty_tasks.csv");

        // пустой менеджер
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        manager.save(TaskType.TASK); // сохраняем «только задачи», которых нет

        // файл должен существовать и содержать только заголовок
        assertTrue(file.exists(), "Файл должен существовать");
        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(1, lines.size(), "В пустом менеджере должна сохраниться только строка заголовка");
        assertEquals("id,type,name,description,status,epic", lines.get(0), "Неверный header");

        // загружаем и проверяем, что всё пусто
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertAll(
                () -> assertTrue(loaded.getTasks().isEmpty(), "Задачи должны быть пустыми"),
                () -> assertTrue(loaded.getEpics().isEmpty(), "Эпики должны быть пустыми"),
                () -> assertTrue(loaded.getSubTasks().isEmpty(), "Подзадачи должны быть пустыми")
        );
    }


    // Сохранение и загрузка только TASK задачи
    @Test
    void saveOnlyTasks_writesOnlyTaskLines(@TempDir File tempDir) throws IOException {
        File file = new File(tempDir, "only_tasks.csv");

        FileBackedTaskManager m = new FileBackedTaskManager(file.getAbsolutePath());
        Task t1 = new Task(1, "Задача 1", "Описание задачи 1");
        Epic e1 = new Epic(2, "Эпик 1", "Описание эпика 1");
        SubTask s1 = new SubTask(3, "Подзадача 1", "Описание подзадачи 1", e1.getId());

        m.addEpic(e1);
        m.addTask(t1);
        m.addSubTask(s1);

        // Сохраняем только TASK
        m.save(TaskType.TASK);

        List<String> lines = Files.readAllLines(file.toPath());
        assertFalse(lines.isEmpty(), "CSV не должен быть пустым");
        assertEquals("id,type,name,description,status,epic", lines.get(0), "Неверный header");

        // Должна быть ровно 1 строка с задачей + заголовок
        assertEquals(2, lines.size(), "Должна сохраниться ровно одна TASK-строка");
        String[] parts = lines.get(1).split(",", -1);
        assertEquals("TASK", parts[1], "В файл должны попадать только TASK-и");
        assertEquals("", parts[5], "Колонка epic у обычной задачи должна быть пустой");
        assertEquals("Задача 1", parts[2]);
        assertEquals("Описание задачи 1", parts[3]);
    }

    @Test
    void load_from_tasksOnlyFile_restoresOnlyTasks(@TempDir File tempDir) {
        File file = new File(tempDir, "tasks_only.csv");

        FileBackedTaskManager m = new FileBackedTaskManager(file.getAbsolutePath());
        m.addTask(new Task(1, "Задача 1", "Описание задачи 1"));
        m.addEpic(new Epic(2, "Эпик 1", "Описание эпика 1"));
        m.addSubTask(new SubTask(3, "Подзадача 1", "Описание подзадачи 1", 2));

        m.save(TaskType.TASK); // файл содержит ТОЛЬКО tasks

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loaded.getTasks().size());
        assertEquals(0, loaded.getEpics().size());
        assertEquals(0, loaded.getSubTasks().size());
    }

    // Сохранение и загрузка нескольких задач
    @Test
    void saveAll_and_load_roundTrip(@TempDir File tempDir) {
        File file = new File(tempDir, "tasks_all.csv");

        FileBackedTaskManager m = new FileBackedTaskManager(file.getAbsolutePath());
        Task t1 = new Task(1, "Задача 1", "Описание задачи 1");
        t1.setStatus(TaskStatus.NEW);

        Epic e1 = new Epic(2, "Эпик 1", "Описание эпика 1");

        SubTask s1 = new SubTask(3, "Подзадача 1", "Описание подзадачи 1", e1.getId());
        s1.setStatus(TaskStatus.NEW);

        m.addEpic(e1);
        m.addTask(t1);
        m.addSubTask(s1);

        m.saveAll();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Количества по типам
        assertEquals(1, loaded.getTasks().size(), "Должна загрузиться 1 Task");
        assertEquals(1, loaded.getEpics().size(), "Должен загрузиться 1 Epic");
        assertEquals(1, loaded.getSubTasks().size(), "Должна загрузиться 1 SubTask");

        // Содержимое
        Task lt = loaded.getTasks().get(0);
        assertEquals("Задача 1", lt.getName());
        assertEquals("Описание задачи 1", lt.getDescription());
        assertEquals(TaskStatus.NEW, lt.getStatus());

        Epic le = loaded.getEpics().get(0);
        assertEquals("Эпик 1", le.getName());
        assertEquals("Описание эпика 1", le.getDescription());

        SubTask ls = loaded.getSubTasks().get(0);
        assertEquals("Подзадача 1", ls.getName());
        assertEquals("Описание подзадачи 1", ls.getDescription());
        assertEquals(TaskStatus.NEW, ls.getStatus());
        assertEquals(le.getId(), ls.getEpicId(), "SubTask должен ссылаться на Epic");
    }
}

package manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import exception.ManagerSaveException;
import task.SubTask;
import task.Task;
import task.Epic;
import task.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String CSV_HEADER = "id,type,name,description,status,epic,startTime,duration";
    private static final String CSV_DELIMITER = ",";
    private static final String NEW_LINE = "\n";
    private static final String EMPTY_VALUE = "";

    private final String filePath;

    public FileBackedTaskManager(String filePath) {
        this.filePath = Objects.requireNonNull(filePath, "File path must not be null");
    }

    private String toCsv(Task task) {
        String epicColumn = (task instanceof SubTask subTask) ? String.valueOf(subTask.getEpicId()) : EMPTY_VALUE;
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : EMPTY_VALUE;
        String duration = task.getDuration() != null ? task.getDuration().toString() : EMPTY_VALUE;

        return String.join(CSV_DELIMITER,
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getDescription(),
                task.getStatus().name(),
                epicColumn,
                startTime,
                duration
        );
    }

    public void saveAll() {
        saveFiltered(task -> true);
    }

    public void save(TaskType type) {
        saveFiltered(task -> task.getType() == type);
    }

    private void saveFiltered(Predicate<Task> filter) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(CSV_HEADER);
            writer.write(NEW_LINE);

            Stream<Task> allTasks = Stream.of(
                    getTasks().stream(),
                    getEpics().stream(),
                    getSubTasks().stream()
            ).flatMap(stream -> stream);

            allTasks.filter(filter)
                    .map(this::toCsv)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.write(NEW_LINE);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

        } catch (IOException | UncheckedIOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл", e);
        }
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        saveAll();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        saveAll();
        return id;
    }

    @Override
    public int addSubTask(SubTask subTask) {
        int id = super.addSubTask(subTask);
        saveAll();
        return id;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split(NEW_LINE);

            for (int i = 1; i < lines.length; i++) {
                Task task = Task.fromString(lines[i]);
                addTaskToManager(manager, task);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки данных", e);
        }
        return manager;
    }

    private static void addTaskToManager(FileBackedTaskManager manager, Task task) {
        if (task instanceof Epic) {
            manager.addEpic((Epic) task);
        } else if (task instanceof SubTask) {
            manager.addSubTask((SubTask) task);
        } else {
            manager.addTask(task);
        }
    }
}
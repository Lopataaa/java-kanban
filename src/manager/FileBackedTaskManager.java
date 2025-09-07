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
    private final String filePath;

    public FileBackedTaskManager(String filePath) {
        this.filePath = Objects.requireNonNull(filePath);
    }

    private String toCsv(Task t) {
        String epicCol = (t instanceof SubTask st) ? String.valueOf(st.getEpicId()) : "";
        return String.join(",",
                String.valueOf(t.getId()),
                t.getType().name(),
                t.getName(),
                t.getDescription(),
                t.getStatus().name(),
                epicCol,
                t.getStartTime() != null ? t.getStartTime().toString() : "",
                t.getDuration() != null ? t.getDuration().toString() : ""
        );
    }

    public void saveAll() {
        saveFiltered(task -> true);
    }

    public void save(TaskType type) {
        saveFiltered(task -> task.getType() == type);
    }

    private void saveFiltered(Predicate<Task> filter) {
        try (FileWriter w = new FileWriter(filePath)) {
            w.write("id,type,name,description,status,epic,startTime,duration");
            w.write('\n');

            // объединяем все коллекции в один поток
            java.util.stream.Stream<Task> all = Stream.of(
                    getTasks().stream(),
                    getEpics().stream(),
                    getSubTasks().stream()
            ).flatMap(s -> s);

            all.filter(filter)
                    .map(this::toCsv)
                    .forEach(line -> {
                        try {
                            w.write(line);
                            w.write('\n');
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

    //Метод будет восстанавливать данные менеджера из файла при запуске программы
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getName());
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                Task task = Task.fromString(line);
                if (task instanceof Epic) {
                    manager.addEpic((Epic) task);
                } else if (task instanceof SubTask) {
                    manager.addSubTask((SubTask) task);
                } else {
                    manager.addTask(task);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки данных", e);
        }
        return manager;
    }
}
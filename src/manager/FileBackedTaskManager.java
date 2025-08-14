package manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import task.SubTask;
import task.Task;
import task.Epic;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private String namefile;

    public FileBackedTaskManager(String namefile) {

        this.namefile = namefile;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(namefile)) {
            writer.write("id,type,description,name,status,epic\n");

            for (Task task : getTasks()) {
                writer.write(String.format("%d,%s,%s,%s,%s,%d\n",
                        task.getId(), task.getType(), task.getDescription(), task.getName(), task.getStatus(),
                        task.getEpic()));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл", e);
        }
    }

    @Override
    public int addTask(Task task) {
        int result = super.addTask(task);
        save();
        return result;
    }

    @Override
    public int addEpic(Epic epic) {
        int result = super.addEpic(epic);
        save();
        return result;
    }

    @Override
    public int addSubTask(SubTask subTask) {
        int result = super.addSubTask(subTask);
        save();
        return result;
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
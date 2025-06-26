import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int newId = 1;

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public Task addTask(String name, String description) {
        Task task = new Task(newId++, description, name);
        tasks.put(task.getId(), task);
        return task;

    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public boolean updateTask(Task updateTask) {
        if (updateTask == null) {
            return false;
        }
        tasks.put(updateTask.getId(), updateTask);
        return true;

    }

    public Task findTaskById(int id) {
        return tasks.get(id);
    }

    public SubTask addSubTask(String name, String description, int epicId) {
        SubTask subTask = new SubTask(newId++, name, description, epicId);
        subTasks.put(subTask.getId(), subTask);

        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubTaskId(subTask.getId());
        }
        return subTask;
    }

    public void deleteSubTask() {
        subTasks.clear();
    }

    public boolean updateSubTask(SubTask updateSubTask) {
        if (updateSubTask == null) {
            return false;
        }
        subTasks.put(updateSubTask.getId(), updateSubTask);
        return true;
    }

    public SubTask findSubTaskById(int id) {
        return subTasks.get(id);
    }

    public Epic addEpic(String name, String description) {
        Epic epic = new Epic(newId++, name, description);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public void deleteEpic() {
        epics.clear();
    }

    public boolean updateEpic(Epic updateEpic) {
        if (updateEpic == null) {
            return false;
        }
        epics.put(updateEpic.getId(), updateEpic);
        return true;
    }

    public Epic findEpicById(int id) {
        return epics.get(id);
    }
}

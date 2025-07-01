package manager;

import task.SubTask;
import task.Task;
import task.Epic;

import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int newId = 1;

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpicStatus(Epic epic) {
        if (epic == null) {
            return;
        }

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();
        if (subTaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean anyNotNew = false;

        for (int subTaskId : subTaskIds) {
            SubTask subTask = subTasks.get(subTaskId);
            if (subTask != null) {
                if (subTask.getStatus() != TaskStatus.DONE) {
                    allDone = false;
                }
                if (subTask.getStatus() != TaskStatus.NEW) {
                    anyNotNew = true;
                }
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (anyNotNew) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }

    public void deleteAllSubtasks() {
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

    public void addSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);

        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubTaskId(subTask.getId());
        }
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

    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
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


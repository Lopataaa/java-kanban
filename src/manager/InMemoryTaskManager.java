package manager;

import task.SubTask;
import task.Task;
import task.Epic;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

public class InMemoryTaskManager implements TaskManager { /* переименовала созданный ранее
класс менеджер в InMemoryTaskManager, тем самым реализовала п.2 "не забыть имплементировать TaskManager,
ведь в Java класс должен явно заявить, что он подходит под требования интерфейса"*/

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int newId = 1;
    private HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    //private List<Task> tasks = new ArrayList<>();

    @Override
    public int addTask(Task task) {
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public void updateEpicStatus(Epic epic) { //если этот метод вспомогательный, то его не нужно было переносить из менеджера
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

    @Override
    public void deleteAllSubtasks() {
        tasks.clear();
    }

    @Override
    public Task updateTask(Task updateTask) {
        if (updateTask == null) {
            return null;
        }
        tasks.put(updateTask.getId(), updateTask);
        return updateTask;
    }

    /* надо удалить, тк обновила этот метод ниже
    @Override
    public Task findTaskById(int id) {
        return tasks.get(id);
    }*/

    @Override
    public void addSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);

        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubTaskId(subTask.getId());
        }
    }

    @Override
    public void deleteSubTask() {
        subTasks.clear();
    }

    @Override
    public boolean updateSubTask(SubTask updateSubTask) {
        if (updateSubTask == null) {
            return false;
        }
        subTasks.put(updateSubTask.getId(), updateSubTask);
        return true;
    }

    /* надо удалить, тк обновила этот метод ниже
    @Override
    public SubTask findSubTaskById(int id) {
        return subTasks.get(id);
    }*/

    @Override
    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteEpic() {
        epics.clear();
    }

    @Override
    public boolean updateEpic(Epic updateEpic) {
        if (updateEpic == null) {
            return false;
        }
        epics.put(updateEpic.getId(), updateEpic);
        return true;
    }

    /* надо удалить, тк обновила этот метод ниже
    @Override
    public Epic findEpicById(int id) {
        return epics.get(id);
    }*/

    @Override
    public List<SubTask> getSubTasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        List<SubTask> subTasksList = new ArrayList<>();
        for (int subTaskId : epic.getSubTaskIds()) {
            SubTask subTask = subTasks.get(subTaskId);
            if (subTask != null) {
                subTasksList.add(subTask);
            }
        }
        return subTasksList;
    }

    // История просмотров задач
    private List<Task> history = new LinkedList<>(); //реализация поля для хранения истории просмотров

    /* закомментировала, тк, вроде это есть в строках 20-23
    @Override
    public List<Task> getTasks() { // Просмотром будем считать вызов тех методов, которые получают задачу по идентификатору
        return new ArrayList<>(tasks.values());
    }*/

    /* Далее обновление методов findTaskById(int id), findSubTaskById(int id), findEpicById(int id)
    для добавления просмотренных задач в список
     */

    // Перенесла в класс InMemoryTaskManager, потом вернула
    @Override
    public List<Task> getHistory() { // реализация метода, который возвращает последние 10 просмотренных задач. Обяъявлен в TaskManager
        return new ArrayList<>(history);
    }

    @Override
    public Task findTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addToHistory(task);
        }
        return task;
    }

    @Override
    public SubTask findSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            historyManager.addToHistory((Task) subTask);
        }
        return subTask;
    }

    @Override
    public Epic findEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addToHistory((Task) epic);
        }
        return epic;
    }

    /* Перенесла в класс InMemoryTaskManager
    // Обновление истории просмотров
    private void addToHistory(Task task) { //метод добавляет задачу в список истории просмотров
        history.add(task); //просмотренные задачи должны добавляться в конец

        // Если размер списка больше 10, по ТЗ необходимо удалить самый старый элемент — тот, который находится в начале списка
        if (history.size() > 10) {
            history.remove(0);
        }
    }*/


}
package manager;

import task.SubTask;
import task.Task;
import task.Epic;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    /*
     * Переименовала созданный ранее класс менеджер в InMemoryTaskManager,
     * тем самым реализовала п.2 "не забыть имплементировать TaskManager,
     * ведь в Java класс должен явно заявить, что он подходит под требования интерфейса"
     */

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Приоритезированный набор задач и подзадач по startTime
    private final NavigableSet<Task> prioritizedTask = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingInt(Task::getId));
    private int nextId = 1;


    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public int addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null");
        }
        if (hasOverIntersectionTasks(task)) { // ДОБАВЛЕНА ВАЛИДАЦИЯ
            throw new IllegalArgumentException("Задача пересекается по времени с существующей");
        }
        tasks.put(task.getId(), task);
        upsertPrioritized(task);
        return task.getId();
    }

    @Override
    public int updateTask(Task updateTask) {
        if (updateTask == null) {
            throw new IllegalArgumentException("Task must not be null");
        }
        if (hasOverIntersectionTasks(updateTask)) { // ДОБАВЛЕНА ВАЛИДАЦИЯ
            throw new IllegalArgumentException("Задача пересекается по времени с существующей");
        }
        removeFromPrioritized(tasks.get(updateTask.getId()));
        tasks.put(updateTask.getId(), updateTask);
        upsertPrioritized(updateTask);
        return updateTask.getId();
    }

    @Override
    public void deleteTask(int id) {
        removeFromPrioritized(tasks.remove(id));
    }

    @Override
    public Task findTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public int addSubTask(SubTask subTask) {
        if (subTask == null) {
            throw new IllegalArgumentException("SubTask must not be null");
        }
        // ДОБАВЛЕНА ВАЛИДАЦИЯ
        if (hasOverIntersectionTasks(subTask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей");
        }
        subTasks.put(subTask.getId(), subTask);
        upsertPrioritized(subTask);

        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubTaskId(subTask.getId());
            refreshEpic(epic);
        }
        return subTask.getId();
    }

    @Override
    public int updateSubTask(SubTask updateSubTask) {
        if (updateSubTask == null) {
            throw new IllegalArgumentException("SubTask must not be null");
        }
        if (hasOverIntersectionTasks(updateSubTask)) { // ДОБАВЛЕНА ВАЛИДАЦИЯ
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей");
        }

        removeFromPrioritized(subTasks.get(updateSubTask.getId()));
        subTasks.put(updateSubTask.getId(), updateSubTask);
        upsertPrioritized(updateSubTask);

        Epic epic = epics.get(updateSubTask.getEpicId());
        if (epic != null) refreshEpic(epic);
        return updateSubTask.getId();
    }

    @Override
    public void deleteSubTask() {
        // удалить из приоритета все старые сабтаски
        subTasks.values().forEach(this::removeFromPrioritized);
        subTasks.clear();

        // очистить связи у эпиков
        for (Epic e : epics.values()) {
            e.getSubTaskIds().clear();
            refreshEpic(e);
        }
    }

    @Override
    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.remove(id);
        if (subTask != null) {
            // Удаляем из приоритизированного списка
            removeFromPrioritized(subTask);

            // Удаляем из эпика
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.deleteSubTaskId(id);
                refreshEpic(epic); // Обновляем статус и время эпика
            }

            // Удаляем из истории
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        // Удаляем все подзадачи из приоритизированного списка
        for (SubTask subTask : subTasks.values()) {
            removeFromPrioritized(subTask);
        }

        // Удаляем все подзадачи из истории
        for (Integer subTaskId : subTasks.keySet()) {
            historyManager.remove(subTaskId);
        }

        // Очищаем карту подзадач
        subTasks.clear();

        // Очищаем связи у всех эпиков и обновляем их
        for (Epic epic : epics.values()) {
            epic.getSubTaskIds().clear();
            refreshEpic(epic); // Обновляем статус и время эпиков
        }
    }

//    @Override
//    public void deleteAllSubTasks() {
//        // Удаляем все подзадачи из приоритизированного списка
//        subTasks.values().forEach(this::removeFromPrioritized);
//
//        // Удаляем все подзадачи из истории
//        for (Integer subTaskId : subTasks.keySet()) {
//            historyManager.remove(subTaskId);
//        }
//
//        // Очищаем карту подзадач
//        subTasks.clear();
//
//        // Очищаем связи у всех эпиков
//        for (Epic epic : epics.values()) {
//            epic.getSubTaskIds().clear();
//            refreshEpic(epic); // Обновляем статус и время эпиков
//        }
//    }

//    @Override
//    public SubTask findSubTaskById(int id) {
//        SubTask st = subTasks.get(id);
//        if (st != null) historyManager.add(st);
//        return st;
//    }

    @Override
    public SubTask findSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        refreshEpic(epic);
    }

    @Override
    public int addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        refreshEpic(epic);
        return epic.getId();
    }

    @Override
    public int updateEpic(Epic updateEpic) {
        if (updateEpic == null) {
            throw new IllegalArgumentException("Epic must not be null");
        }
        epics.put(updateEpic.getId(), updateEpic);
        refreshEpic(updateEpic);
        return updateEpic.getId();
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;

        for (Integer sid : new ArrayList<>(epic.getSubTaskIds())) {
            SubTask st = subTasks.remove(sid);
            removeFromPrioritized(st);
        }
        epic.getSubTaskIds().clear();
    }

    @Override
    public void deleteAllEpics() {
        // Удаляем все эпики и связанные подзадачи
        for (Epic epic : epics.values()) {
            for (Integer subTaskId : epic.getSubTaskIds()) {
                SubTask subTask = subTasks.remove(subTaskId);
                removeFromPrioritized(subTask);
                historyManager.remove(subTaskId);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
        subTasks.clear(); // Очищаем все подзадачи
    }

    @Override
    public Epic findEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public List<SubTask> getSubTasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();

        List<SubTask> list = new ArrayList<>();
        for (int sid : epic.getSubTaskIds()) {
            SubTask st = subTasks.get(sid);
            if (st != null) list.add(st);
        }
        return list;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(prioritizedTask));
    }

    @Override
    public boolean isOverIntersection(Task t1, Task t2) {
        if (t1 == null || t2 == null) return false;
        LocalDateTime aStart = t1.getStartTime();
        LocalDateTime aEnd = t1.getEndTime();
        LocalDateTime bStart = t2.getStartTime();
        LocalDateTime bEnd = t2.getEndTime();
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;

        // Пересекаются, если один интервал НАЧИНАЕТСЯ ДО ОКОНЧАНИЯ другого, и наоборот
        // Но не считаем пересечением, если один заканчивается ровно тогда, когда другой начинается
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    @Override
    public boolean hasOverIntersectionTasks(Task newTask) {
        if (newTask == null) return false;
        for (Task existing : prioritizedTask) {
            if (existing.getId() == newTask.getId()) continue;
            if (isOverIntersection(newTask, existing)) return true;
        }
        return false;
    }

//    @Override
//    public void clearTasks() {
//        tasks.values().forEach(this::removeFromPrioritized);
//        tasks.clear();
//    }

    @Override
    public Epic getEpic(Integer id) {
        if (id == null) {
            return null;
        }
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void clearEpics() {
        // Удаляем все эпики и связанные подзадачи
        for (Epic epic : epics.values()) {
            // Удаляем все подзадачи этого эпика
            for (Integer subTaskId : epic.getSubTaskIds()) {
                SubTask subTask = subTasks.remove(subTaskId);
                removeFromPrioritized(subTask);
            }
            removeFromPrioritized(epic);
        }
        epics.clear();
        subTasks.clear(); // Очищаем карту подзадач
    }

    @Override
    public ArrayList<SubTask> getEpicSubTasks(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<SubTask> epicSubTasks = new ArrayList<>();
        for (Integer subTaskId : epic.getSubTaskIds()) {
            SubTask subTask = subTasks.get(subTaskId);
            if (subTask != null) {
                epicSubTasks.add(subTask);
                historyManager.add(subTask);
            }
        }
        return epicSubTasks;
    }

    private void refreshEpic(Epic epic) {
        if (epic == null) return;

        List<SubTask> subs = getSubTasksByEpicId(epic.getId());

        if (subs.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        boolean allDone = subs.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);
        boolean allNew = subs.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);
        epic.setStatus(allDone ? TaskStatus.DONE : (allNew ? TaskStatus.NEW : TaskStatus.IN_PROGRESS));

        // duration = сумма
        epic.setDuration(subs.stream()
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus)
                .orElse(null));

        // start = min
        epic.setStartTime(subs.stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null));

        // end = max (у сабтасков end считается как start + duration)
        epic.setEndTime(subs.stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
    }

    private void upsertPrioritized(Task t) {
        if (t == null) return;
        prioritizedTask.remove(t);   // безопасно: по equals/hashCode (по id)
        prioritizedTask.add(t);
    }

    private void removeFromPrioritized(Task t) {
        if (t != null) prioritizedTask.remove(t);
    }

    private int getNextId() {
        return nextId++;
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
            removeFromPrioritized(tasks.get(taskId));
        }
        tasks.clear();
    }

//    @Override
//    public void createTask(Task task) {
//        if (task == null) {
//            return;
//        }
//        int id = getNextId();
//        task.setId(id);
//        tasks.put(id, task);
//    }

//    @Override
//    public void createSubTask(SubTask subTask) {
//        if (subTask == null) {
//            return;
//        }
//        int id = getNextId();
//        subTask.setId(id);
//        subTasks.put(id, subTask);
//
//        // Добавляем подзадачу в эпик
//        Epic epic = epics.get(subTask.getEpicId());
//        if (epic != null) {
//            epic.addSubTaskId(id);
//        }
//    }

//   @Override
//    public void createEpic(Epic epic) {
//        if (epic == null) {
//            return;
//        }
//        int id = getNextId();
//        epic.setId(id);
//        epics.put(id, epic);
//    }
}
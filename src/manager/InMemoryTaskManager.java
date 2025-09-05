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

    // Хранилища
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();

    // История
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Приоритезированный набор задач и подзадач по startTime
    private final NavigableSet<Task> prioritized =
            new TreeSet<>((a, b) -> {
                LocalDateTime sa = a.getStartTime();
                LocalDateTime sb = b.getStartTime();
                if (sa == null && sb == null) return Integer.compare(a.getId(), b.getId());
                if (sa == null) return 1;    // nullы — в конец
                if (sb == null) return -1;
                int cmp = sa.compareTo(sb);
                return (cmp != 0) ? cmp : Integer.compare(a.getId(), b.getId());
            });

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public int addTask(Task task) {
        tasks.put(task.getId(), task);
        upsertPrioritized(task);
        return task.getId();
    }

    @Override
    public int updateTask(Task updateTask) {
        if (updateTask == null) {
            throw new IllegalArgumentException("Task must not be null");
        }
        // убрать старый экземпляр из prioritized (если был)
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

        // очистить связи у эпиков и пересчитать агрегаты
        for (Epic e : epics.values()) {
            e.getSubTaskIds().clear();
            refreshEpic(e);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        deleteSubTask();
    }

    @Override
    public SubTask findSubTaskById(int id) {
        SubTask st = subTasks.get(id);
        if (st != null) historyManager.add(st);
        return st;
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

        // по желанию: удалить связанные сабтаски целиком
        for (Integer sid : new ArrayList<>(epic.getSubTaskIds())) {
            SubTask st = subTasks.remove(sid);
            removeFromPrioritized(st);
        }
        epic.getSubTaskIds().clear();
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
        // отдаём немодифицируемую копию/вид
        return Collections.unmodifiableSet(new LinkedHashSet<>(prioritized));
    }

    @Override
    public boolean isOverIntersection(Task t1, Task t2) {
        if (t1 == null || t2 == null) return false;
        LocalDateTime aStart = t1.getStartTime();
        LocalDateTime aEnd = t1.getEndTime();
        LocalDateTime bStart = t2.getStartTime();
        LocalDateTime bEnd = t2.getEndTime();
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;

        // пересекаются, если интервалы не лежат строго «до» друг друга
        return !(aEnd.isBefore(bStart) || bEnd.isBefore(aStart));
    }

    @Override
    public boolean hasOverIntersectionTasks(Task newTask) {
        if (newTask == null) return false;
        for (Task existing : prioritized) {
            if (existing.getId() == newTask.getId()) continue;
            if (isOverIntersection(newTask, existing)) return true;
        }
        return false;
    }

    /**
     * Пересчёт агрегатов эпика: статус, duration, startTime, endTime
     */
    private void refreshEpic(Epic epic) {
        if (epic == null) return;

        List<SubTask> subs = getSubTasksByEpicId(epic.getId());

        if (subs.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.setEndTime(null);
            // Эпики НЕ участвуют в prioritized, но если участвуют — убери/добавь логику
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
        prioritized.remove(t);   // безопасно: по equals/hashCode (по id)
        prioritized.add(t);
    }

    private void removeFromPrioritized(Task t) {
        if (t != null) prioritized.remove(t);
    }
}
package manager;

import task.SubTask;
import task.Task;
import task.Epic;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private static final String ERROR_NULL_TASK = "Task must not be null";
    private static final String ERROR_NULL_SUBTASK = "SubTask must not be null";
    private static final String ERROR_NULL_EPIC = "Epic must not be null";
    private static final String ERROR_TIME_OVERLAP = "Задача пересекается по времени с существующей";

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final NavigableSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );
    private int nextId = 1;

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public int addTask(Task task) {
        validateTaskNotNull(task);
        validateNoTimeOverlap(task);

        int id = generateNextId();
        task.setId(id);
        tasks.put(id, task);
        addToPrioritized(task);
        return id;
    }

    @Override
    public int updateTask(Task task) {
        validateTaskNotNull(task);
        validateNoTimeOverlap(task);

        removeFromPrioritized(tasks.get(task.getId()));
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task.getId();
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritized(task);
            historyManager.remove(id);
        }
    }

    @Override
    public Task findTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public int addSubTask(SubTask subTask) {
        validateSubTaskNotNull(subTask);

        int id = generateNextId();
        subTask.setId(id);
        subTasks.put(id, subTask);
        addToPrioritized(subTask);
        linkSubTaskToEpic(subTask);
        return id;
    }

    @Override
    public int updateSubTask(SubTask subTask) {
        validateSubTaskNotNull(subTask);
        validateNoTimeOverlap(subTask);

        removeFromPrioritized(subTasks.get(subTask.getId()));
        subTasks.put(subTask.getId(), subTask);
        addToPrioritized(subTask);
        refreshLinkedEpic(subTask);
        return subTask.getId();
    }

    @Override
    public void deleteSubTask() {
        clearAllSubTasks();
    }

    @Override
    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.remove(id);
        if (subTask != null) {
            removeFromPrioritized(subTask);
            unlinkSubTaskFromEpic(subTask);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        clearAllSubTasks();
    }

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
        validateEpicNotNull(epic);

        int id = generateNextId();
        epic.setId(id);
        epics.put(id, epic);
        refreshEpic(epic);
        return id;
    }

    @Override
    public int updateEpic(Epic epic) {
        validateEpicNotNull(epic);
        epics.put(epic.getId(), epic);
        refreshEpic(epic);
        return epic.getId();
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            removeEpicSubTasks(epic);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllEpics() {
        clearAllEpics();
    }

    @Override
    public Epic findEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<SubTask> getSubTasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        return epic.getSubTaskIds().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean isOverIntersection(Task task1, Task task2) {
        if (task1 == null || task2 == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    @Override
    public boolean hasOverIntersectionTasks(Task newTask) {
        if (newTask == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(existing -> existing.getId() != newTask.getId())
                .anyMatch(existing -> isOverIntersection(newTask, existing));
    }

    @Override
    public Epic getEpic(Integer id) {
        if (id == null) {
            return null;
        }
        return findEpicById(id);
    }

    @Override
    public void clearEpics() {
        clearAllEpics();
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

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(this::removeFromPrioritized);
        tasks.clear();
    }

    private void validateTaskNotNull(Task task) {
        if (task == null) {
            throw new IllegalArgumentException(ERROR_NULL_TASK);
        }
    }

    private void validateSubTaskNotNull(SubTask subTask) {
        if (subTask == null) {
            throw new IllegalArgumentException(ERROR_NULL_SUBTASK);
        }
    }

    private void validateEpicNotNull(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException(ERROR_NULL_EPIC);
        }
    }

    private void validateNoTimeOverlap(Task task) {
        if (hasOverIntersectionTasks(task)) {
            throw new IllegalArgumentException(ERROR_TIME_OVERLAP);
        }
    }

    private int generateNextId() {
        return nextId++;
    }

    private void addToPrioritized(Task task) {
        if (task != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        if (task != null) {
            prioritizedTasks.remove(task);
        }
    }

    private void linkSubTaskToEpic(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubTaskId(subTask.getId());
            refreshEpic(epic);
        }
    }

    private void unlinkSubTaskFromEpic(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.deleteSubTaskId(subTask.getId());
            refreshEpic(epic);
        }
    }

    private void refreshLinkedEpic(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            refreshEpic(epic);
        }
    }

    private void refreshEpic(Epic epic) {
        if (epic == null) return;

        List<SubTask> subTasksList = getSubTasksByEpicId(epic.getId());

        if (subTasksList.isEmpty()) {
            setEpicToNewState(epic);
            return;
        }

        updateEpicStatus(epic, subTasksList);
        updateEpicTiming(epic, subTasksList);
    }

    private void setEpicToNewState(Epic epic) {
        epic.setStatus(TaskStatus.NEW);
        epic.setDuration(null);
        epic.setStartTime(null);
        epic.setEndTime(null);
    }

    private void updateEpicStatus(Epic epic, List<SubTask> subTasks) {
        boolean allDone = subTasks.stream().allMatch(sub -> sub.getStatus() == TaskStatus.DONE);
        boolean allNew = subTasks.stream().allMatch(sub -> sub.getStatus() == TaskStatus.NEW);

        TaskStatus status = allDone ? TaskStatus.DONE : (allNew ? TaskStatus.NEW : TaskStatus.IN_PROGRESS);
        epic.setStatus(status);
    }

    private void updateEpicTiming(Epic epic, List<SubTask> subTasks) {
        epic.setDuration(calculateTotalDuration(subTasks));
        epic.setStartTime(findEarliestStartTime(subTasks));
        epic.setEndTime(findLatestEndTime(subTasks));
    }

    private Duration calculateTotalDuration(List<SubTask> subTasks) {
        return subTasks.stream()
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus)
                .orElse(null);
    }

    private LocalDateTime findEarliestStartTime(List<SubTask> subTasks) {
        return subTasks.stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime findLatestEndTime(List<SubTask> subTasks) {
        return subTasks.stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private void clearAllSubTasks() {
        subTasks.values().forEach(this::removeFromPrioritized);
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubTaskIds().clear();
            refreshEpic(epic);
        });
    }

    private void clearAllEpics() {
        epics.values().forEach(epic -> {
            epic.getSubTaskIds().forEach(subTaskId -> {
                SubTask subTask = subTasks.remove(subTaskId);
                removeFromPrioritized(subTask);
                historyManager.remove(subTaskId);
            });
            historyManager.remove(epic.getId());
        });
        epics.clear();
        subTasks.clear();
    }

    private void removeEpicSubTasks(Epic epic) {
        if (epic != null) {
            for (Integer subTaskId : new ArrayList<>(epic.getSubTaskIds())) {
                SubTask subTask = subTasks.remove(subTaskId);
                removeFromPrioritized(subTask);
                historyManager.remove(subTaskId);
            }
            epic.getSubTaskIds().clear();
        }
    }
}
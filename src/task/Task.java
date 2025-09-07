package task;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Task {
    private int id;
    private String name;
    private String description;
    public TaskStatus status;
    public TaskType type;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String taskName, String taskDescription) {
    }

    public Task(int id, String name, String description) { // Основной конструктор — без времени (бессрочная задача)
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.startTime = null; // по умолчанию — нет времени
        this.duration = null; // по умолчанию — нет длительности
        this.type = TaskType.TASK;
    }

    // Конструктор для задач с временем
    public Task(int id, String name, String description, LocalDateTime startTime, Duration duration) {
        this(id, name, description); // вызываем основной конструктор
        this.startTime = startTime;
        this.duration = duration; // может быть null — тогда задача "моментальная" или бессрочная
    }

    public LocalDateTime getEndTime() { // вычисляемый endTime
        if (startTime == null || duration == null) return null;
        return startTime.plus(duration);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskType getType() {
        return type;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    /*public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) return null;
        return startTime.plus(duration);
    }*/

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    // Метод для определения пересечения задач
    private boolean isOverIntersection(Task task1, Task task2) {
        LocalDateTime endTime1 = task1.getEndTime();
        LocalDateTime endTime2 = task2.getEndTime();
        return !(endTime1.isBefore(task2.getStartTime()) || endTime2.isBefore(task1.getStartTime()));
    }

    public boolean hasOverlaps(List<Task> tasks) {
        for (int i = 0; i < tasks.size() - 1; i++) {
            if (isOverIntersection(tasks.get(i), tasks.get(i + 1))) {
                return true; // Пересечение найдено
            }
        }
        return false; // Пересечений нет
    }

    public static Task fromString(String line) {
        // -1 чтобы не отбрасывать пустую последнюю колонку (epic)
        String[] parts = line.split(",", -1);

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        String description = parts[3];
        TaskStatus status = (parts[4] == null || parts[4].isEmpty())
                ? TaskStatus.NEW
                : TaskStatus.valueOf(parts[4]);

        LocalDateTime startTime = (parts.length > 6 && !parts[6].isEmpty())
                ? LocalDateTime.parse(parts[6])
                : null;

        Duration duration = (parts.length > 7 && !parts[7].isEmpty())
                ? Duration.parse(parts[7])
                : null;

        int epicId = (parts.length > 5 && !parts[5].isEmpty())
                ? Integer.parseInt(parts[5])
                : 0;

        Task t;
        switch (type) {
            case TASK -> t = new Task(id, name, description);
            case EPIC -> t = new Epic(id, name, description, startTime, duration);
            case SUBTASK -> t = new SubTask(id, name, description, epicId);
            default -> throw new IllegalArgumentException("Unknown task type: " + type);
        }
        t.setStatus(status);
        return t;
    }

    @Override
    public String toString() {
        String epicCol = (this instanceof SubTask st) ? String.valueOf(st.getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s",
                getId(),           // 0
                getType(),         // 1
                getName(),         // 2
                getDescription(),  // 3
                getStatus(),       // 4
                epicCol            // 5
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return getId() == task.getId();
    }
}

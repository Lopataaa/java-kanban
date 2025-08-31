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
    private TaskType getType;
    private String getName;
    private TaskStatus getStatus;
    private int getEpic;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(int id, String taskName, String taskDescription, LocalDateTime startTime, LocalDateTime endTime) {
    }

    public Task(String taskName, String taskDescription) {
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Task(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.status = null;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public int getEpic() {
        return getEpic;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d",
                getId(), getType(), getDescription(), getName(), getStatus(), getEpic());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return getId() == task.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription());
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Task fromString(String line) {
        String[] parts = line.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String description = parts[2];
        String name = parts[3];
        TaskStatus status;
        if (parts[4] == null || parts[4].isEmpty()) {
            status = TaskStatus.NEW; // или любое другое значение по умолчанию
        } else {
            try {
                status = TaskStatus.valueOf(parts[4]);
            } catch (IllegalArgumentException e) {
                // Если значение не соответствует ни одному из значений enum, устанавливаем значение по умолчанию
                status = TaskStatus.NEW;
            }
        }

        int epic = "".equals(parts[5]) ? 0 : Integer.parseInt(parts[5]); // преобразование в целое число

        switch (type) {
            case TASK:
                return new Task(id, description, name);
            case EPIC:
                return new Epic(id, description, name);
            case SUBTASK:
                return new SubTask(id, description, name, epic);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime.plus(duration);
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
}

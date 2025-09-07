package task;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task implements Serializable {

    public List<Integer> subTaskIds = new ArrayList<>();
    private TaskStatus status = TaskStatus.NEW;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(int id, String name, String description, LocalDateTime startTime, Duration duration) {
        super(id, name, description);
        this.startTime = startTime;
        this.duration = duration;
        setType(TaskType.EPIC);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(int id) {
        if (!subTaskIds.contains(id)) subTaskIds.add(id);
    }

    public void deleteSubTaskId(int id) {
        subTaskIds.remove((Integer) id);
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }


    public void setDuration(Duration d) {
        this.duration = d;
    }

    public void setStartTime(LocalDateTime t) {
        this.startTime = t;
    }

    public void setEndTime(LocalDateTime t) {
        this.endTime = t;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return getId() == epic.getId();
    }

    @Override
    public String toString() {
        return "Epic {" +
                "id=" + getId() +
                ", status=" + status +
                ", description='" + getDescription() + '\'' +
                ", name='" + getName() + '\'' +
                ", subtaskIds=" + getSubTaskIds() +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}


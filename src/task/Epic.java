package task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task implements Serializable {
    @SerializedName("subTaskIds")
    private List<Integer> subTaskIds = new ArrayList<>();

    @SerializedName("epicStatus")
    private TaskStatus status = TaskStatus.NEW;

    @SerializedName("epicDuration")
    private Duration duration;

    @SerializedName("epicStartTime")
    private LocalDateTime startTime;

    @SerializedName("epicEndTime")
    private LocalDateTime endTime;

    // Конструктор с временными параметрами
    public Epic(int id, String name, String description, LocalDateTime startTime, Duration duration) {
        super(id, name, description);
        this.startTime = startTime;
        this.duration = duration;
        this.subTaskIds = new ArrayList<>();
        setType(TaskType.EPIC);
    }

    // Конструктор без временных параметров
    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.subTaskIds = new ArrayList<>();
        setType(TaskType.EPIC);
    }

    // Конструктор по умолчанию (для GSON)
    public Epic() {
        super(0, "", "");
        this.subTaskIds = new ArrayList<>();
        setType(TaskType.EPIC);
    }

    // Геттер с защитой от null
    public List<Integer> getSubTaskIds() {
        if (subTaskIds == null) {
            subTaskIds = new ArrayList<>();
        }
        return subTaskIds;
    }

    // Сеттер с защитой от null
    public void setSubTaskIds(List<Integer> subTaskIds) {
        this.subTaskIds = subTaskIds != null ? new ArrayList<>(subTaskIds) : new ArrayList<>();
    }

    public void addSubTaskId(int id) {
        getSubTaskIds().add(id);
    }

    public void deleteSubTaskId(int id) {
        getSubTaskIds().remove((Integer) id);
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

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + status +
                ", subTaskIds=" + getSubTaskIds() +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
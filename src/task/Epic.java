package task;

import java.io.Serializable;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.io.Serializable;

public class Epic extends Task implements Serializable {

    private final ArrayList<Integer> subTaskIds = new ArrayList<>();
    private final TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return getId() == epic.getId();
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription());
    }

    public void deleteSubTaskId(int subTaskId) {
        subTaskIds.remove((Integer) subTaskId);
    }

    public void calculateDuration(List<SubTask> subTasks) { //Продолжительность эпика — сумма продолжительностей
        // всех его подзадач
        duration = subTasks.stream()// создала поток из списка подзадач
                .map(SubTask::getDuration)//возврат продолжительности подзадачи
                .reduce(Duration::plus) //суммирование длительности подзадач
                .orElse(Duration.ZERO); //если подзадачи отсутствуют
    }

    public void determineStartTime(List<SubTask> subTasks) { //Время начала — дата старта самой ранней подзадачи
        Optional<LocalDateTime> earliestStartTime = subTasks.stream()
                .map(SubTask::getStartTime) //возврат времени начала подзадачи
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()); //минимальное значение времени начала
        startTime = earliestStartTime.orElse(null); //время начала или null в случае, если нет подзадач
    }

    public void determineEndTime(List<SubTask> subTasks) { //время завершения — время окончания самой поздней из задач
        Optional<LocalDateTime> latestEndTime = subTasks.stream()
                .map(SubTask::getEndTime)//возврат времени окончания подзадачи
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()); //максимальное значение времени окончания
        endTime = latestEndTime.orElse(null); //время завершения или null в случае, если нет подзадач
    }

    @Override
    public String toString() {
    return "Epic{" +
            "id=" + getId() +
            ", status=" + getStatus() +
            ", description='" + getDescription() + '\'' +
            ", name='" + getName() + '\'' +
            ", subtaskIds=" + subTaskIds +
            ", duration=" + duration +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            '}';
}

    public <T> void getSubTaskIds(List<T> list) {
    }
}

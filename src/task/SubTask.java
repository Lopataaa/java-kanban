package task;

import java.util.Objects;

public class SubTask extends Task {

    private int epicId;

    public int getEpicId() {
        return epicId;
    }

    public SubTask(int id, String name, String description, int epicId) {
        super(id, name, description);
        //this.status = TaskStatus.NEW;
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubTask subTask = (SubTask) o;
        return getId() == subTask.getId() &&
                getEpicId() == subTask.getEpicId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getEpicId());
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", id=" + getId() +
                ", epicId=" + epicId +
                '}';
    }
}


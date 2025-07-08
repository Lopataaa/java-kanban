package task;

public class SubTask extends Task {

    private int epicId;

    public int getEpicId() {
        return epicId;
    }

    public SubTask(int id, String name, String description, int epicId) {
        super(id, name, description);
        this.epicId = epicId;
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


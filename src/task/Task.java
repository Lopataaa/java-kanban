package task;

import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;
    private TaskType getType;
    private String getName;
    private TaskStatus getStatus;
    private int getEpic;

    public void setDescription(String description) {
        this.description = description;
    }

    public Task(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
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

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d",
                getId(), getType(), getDescription(), getName(), getStatus(), getEpic());
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        task.Task task = (task.Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
        TaskStatus status = TaskStatus.valueOf(parts[4]);
        int epic = "".equals(parts[5]) ? 0 : Integer.parseInt(parts[5]);

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

}

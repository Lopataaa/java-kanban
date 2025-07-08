package task;

import java.util.*;

public class Epic extends Task {

    private final ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(int id, String name, String description) {
        super(id, name, description);
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void deleteSubTaskId(int subTaskId) {
        subTaskIds.remove((Integer) subTaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", name='" + getName() + '\'' +
                ", subtaskIds=" + subTaskIds +
                '}';
    }
}

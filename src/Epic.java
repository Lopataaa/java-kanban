import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subTaskIds;

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.subTaskIds = new ArrayList<>();
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
                "id=" + id +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", subtaskIds=" + subTaskIds +
                '}';
    }
}

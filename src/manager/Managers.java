package manager;

import task.Task;

import java.util.Set;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager() {

            @Override
            public Set<Task> getPrioritizedTasks() {
                return Set.of();
            }

            @Override
            public boolean isOverIntersection(Task task1, Task task2) {
                return false;
            }

            @Override
            public boolean hasOverIntersectionTasks(Task newTask) {
                return false;
            }
        };
    }

    public static HistoryManager getDefaultHistory() {

        return new InMemoryHistoryManager();
    }
}
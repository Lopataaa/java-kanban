package manager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager() {
            @Override
            public void save() {

            }
        };
    }

    public static HistoryManager getDefaultHistory() {

        return new InMemoryHistoryManager();
    }

    public static TaskManager getTaskManager() {
        return new InMemoryTaskManager() {
            @Override
            public void save() {

            }
        };
    }
}

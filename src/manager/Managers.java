package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.adapters.DurationTypeAdapter;
import http.adapters.LocalDateTimeTypeAdapter;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager() {

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

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }
}
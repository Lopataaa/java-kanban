package http;

import org.junit.jupiter.api.DisplayName;
import task.TaskStatus;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpPriorityTest extends HttpTasksTest {
    private static final String TASK_NAME_EARLY = "Task Early";
    private static final String TASK_DESCRIPTION_EARLY = "Early task";
    private static final String TASK_NAME_LATE = "Task Late";
    private static final String TASK_DESCRIPTION_LATE = "Late task";
    private static final String TASK_NAME_NO_TIME = "No Time Task";
    private static final String TASK_DESCRIPTION_NO_TIME = "Task without time";
    private static final String PATH_TASKS = "/tasks";
    private static final String PATH_PRIORITIZED = "/prioritized";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int HOURS_OFFSET_1 = 1;
    private static final int HOURS_OFFSET_3 = 3;
    private static final int MINUTES_DURATION_60 = 60;
    private static final int MINUTES_DURATION_30 = 30;
    private static final int EXPECTED_TASKS_COUNT = 2;
    private static final int EXPECTED_EMPTY_COUNT = 0;
    private static final int STATUS_OK = 200;

    @Test
    @DisplayName("Получение приоритетных задач")
    void test_Get_Prioritized_Tasks() throws IOException, InterruptedException {
        // Given
        Task task1 = new Task(0, TASK_NAME_EARLY, TASK_DESCRIPTION_EARLY,
                LocalDateTime.now().plusHours(HOURS_OFFSET_1), Duration.ofMinutes(MINUTES_DURATION_60));
        Task task2 = new Task(0, TASK_NAME_LATE, TASK_DESCRIPTION_LATE,
                LocalDateTime.now().plusHours(HOURS_OFFSET_3), Duration.ofMinutes(MINUTES_DURATION_30));

        task1.setStatus(TaskStatus.NEW);
        task2.setStatus(TaskStatus.IN_PROGRESS);

        // When
        createTask(gson.toJson(task1));
        createTask(gson.toJson(task2));

        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_PRIORITIZED))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, priorityResponse.statusCode(), "Неверный статус код для приоритетных задач");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);

        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(EXPECTED_TASKS_COUNT, prioritizedTasks.length, "Должно быть 2 приоритетные задачи");

        if (prioritizedTasks.length >= 2) {
            assertTrue(prioritizedTasks[0].getStartTime().isBefore(prioritizedTasks[1].getStartTime()),
                    "Задачи должны быть отсортированы по времени начала");
            assertEquals(TASK_NAME_EARLY, prioritizedTasks[0].getName(),
                    "Первая задача должна быть 'Task Early'");
        }
    }

    @Test
    @DisplayName("Получение пустого списка из приоритетных задач")
    void testGet_Empty_Prioritized_Tasks() throws IOException, InterruptedException {
        // Given
        // When
        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_PRIORITIZED))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, priorityResponse.statusCode(), "Неверный статус код для пустого списка");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(EXPECTED_EMPTY_COUNT, prioritizedTasks.length, "Список приоритетных задач должен быть пустым");
    }

    @Test
    @DisplayName("Задачи без времени не попадут в приоритетный список")
    void testTasks_Without_TimeNot_InPrioritized() throws IOException, InterruptedException {
        // Given - создание задачи без времени начала
        Task taskWithoutTime = new Task(0, TASK_NAME_NO_TIME, TASK_DESCRIPTION_NO_TIME);
        taskWithoutTime.setStatus(TaskStatus.NEW);

        createTask(gson.toJson(taskWithoutTime));

        // When
        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_PRIORITIZED))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, priorityResponse.statusCode());

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks);

        assertEquals(EXPECTED_EMPTY_COUNT, prioritizedTasks.length,
                "Задачи без времени не должны быть в приоритетном списке");
    }

    private HttpResponse<String> createTask(String taskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }
}
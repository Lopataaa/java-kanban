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

class HttpHistoryTest extends HttpTasksTest {
    private static final String TASK_NAME = "History Task";
    private static final String TASK_DESCRIPTION = "Desc";
    private static final String PATH_TASKS = "/tasks";
    private static final String PATH_HISTORY = "/history";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int DURATION_MINUTES = 30;
    private static final int STATUS_OK = 200;
    private static final String EMPTY_JSON_ARRAY = "[]";

    @Test
    @DisplayName("Получение истории задач")
    void testGetHistory() throws IOException, InterruptedException {
        // Given
        Task task = new Task(0, TASK_NAME, TASK_DESCRIPTION,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES));
        task.setStatus(TaskStatus.NEW);

        String json = gson.toJson(task);
        HttpResponse<String> postResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_TASKS))
                        .header("Content-Type", CONTENT_TYPE_JSON)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Task created = gson.fromJson(postResp.body(), Task.class);

        // When
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_TASKS + "/" + created.getId()))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> historyResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_HISTORY))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        // Then
        assertEquals(STATUS_OK, historyResp.statusCode());
        assertTrue(historyResp.body().contains(TASK_NAME));
    }

    @Test
    @DisplayName("Пустая история")
    void testGetEmptyHistory() throws IOException, InterruptedException {
        // Given
        // When
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_HISTORY))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        // Then
        assertEquals(STATUS_OK, resp.statusCode());
        assertEquals(EMPTY_JSON_ARRAY, resp.body().trim());
    }
}
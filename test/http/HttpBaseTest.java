package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HttpBaseTest extends HttpTasksTest {
    private static final String TASK_NAME = "Test Task";
    private static final String TASK_DESCRIPTION = "Description";
    private static final String TASK_STATUS_NEW = "NEW";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String PATH_TASKS = "/tasks";
    private static final int NON_EXISTENT_TASK_ID = 999;
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_NOT_FOUND = 404;

    private static final String TASK_JSON_TEMPLATE = """
        {
            "id": 0,
            "name": "%s",
            "description": "%s",
            "status": "%s"
        }
        """;

    @Test
    @DisplayName("Получение списка задач")
    void testGetTasks() throws IOException, InterruptedException {
        // Given
        String taskJson = String.format(TASK_JSON_TEMPLATE, TASK_NAME, TASK_DESCRIPTION, TASK_STATUS_NEW);

        // When
        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        client.send(post, HttpResponse.BodyHandlers.ofString());

        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .GET()
                .build();
        HttpResponse<String> response = client.send(get, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, response.statusCode());
        assertTrue(response.body().contains(TASK_NAME));
    }

    @Test
    @DisplayName("Получение задачи по идентификатору")
    void testGetTaskById() throws IOException, InterruptedException {
        // Given
        String taskJson = String.format(TASK_JSON_TEMPLATE, TASK_NAME, TASK_DESCRIPTION, TASK_STATUS_NEW);

        // When
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_CREATED, postResponse.statusCode());

        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int actualTaskId = createdTask.getId();

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS + "/" + actualTaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        //Then
        assertEquals(STATUS_OK, response.statusCode(),
                "Should find task with ID: " + actualTaskId + ". Response: " + response.body());

        assertTrue(response.body().contains(TASK_NAME));
        assertTrue(response.body().contains(TASK_DESCRIPTION));
    }

    @Test
    @DisplayName("Получение несуществующей задачи")
    void testGetTaskById_NotFound() throws IOException, InterruptedException {
        // Given
        // When
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS + "/" + NON_EXISTENT_TASK_ID))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_NOT_FOUND, response.statusCode());
    }

    @Test
    @DisplayName("Удаление задачи")
    void testDeleteTask() throws IOException, InterruptedException {
        // Given
        String taskJson = String.format(TASK_JSON_TEMPLATE, TASK_NAME, TASK_DESCRIPTION, TASK_STATUS_NEW);

        // When
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_CREATED, postResponse.statusCode());

        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int actualTaskId = createdTask.getId();

        // When
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS + "/" + actualTaskId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, deleteResponse.statusCode());

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS + "/1"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_NOT_FOUND, getResponse.statusCode());
    }
}
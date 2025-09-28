package http;

import com.google.gson.Gson;
import manager.TaskManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTasksTest {

    private static final String BASE_URL = "http://localhost:8080/tasks/task/";
    private HttpTaskServer server;
    private TaskManager taskManager;
    HttpClient client;
    Gson gson;

    @Test
    @DisplayName("Получение списка задач")
    void testGetTasks() throws IOException, InterruptedException {
        // Создаем тестовую задачу
        Task task = new Task(1, "Test Task", "Description");
        taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    @DisplayName("Создание новой задачи")
    void testCreateTask() throws IOException, InterruptedException {
        String taskJson = """
        {
            "id": 0,
            "name": "New Task",
            "description": "New Description",
            "status": "NEW"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    @DisplayName("Получение задачи по индентификатору")
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task(1, "Test Task", "Description");
        taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    @DisplayName("Получение несуществующей задачи")
    void testGetTaskById_NotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Удаление задачи")
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test Task", "Description");
        taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getTasks().size());
    }
}

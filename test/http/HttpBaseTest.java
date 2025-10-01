//// http/HttpTasksTest.java
//package http;
//
//import com.google.gson.Gson;
//import manager.Managers;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
//// ВАЖНО: класс абстрактный и НЕ содержит @Test-методов!
//public abstract class HttpBaseTest {
//
//    // Поля — protected static, чтобы наследники имели доступ
//    protected static HttpClient client;
//    protected static Gson gson;
//    protected static String BASE_URL;
//    private static HttpTaskServer server;
//
//    @BeforeAll
//    static void setUp() throws IOException {
//        gson = Managers.getGson();
//        client = HttpClient.newHttpClient();
//        server = new HttpTaskServer();
//        server.start();
//        BASE_URL = "http://localhost:" + server.getPort();
//    }
//
//    @AfterAll
//    static void tearDown() {
//        if (server != null) {
//            server.stop();
//        }
//    }
//
//    @BeforeEach
//    void clearAllData() throws IOException, InterruptedException {
//        // Очистка через HTTP — чтобы не зависеть от внутреннего состояния
//        client.send(
//                HttpRequest.newBuilder()
//                        .uri(URI.create(BASE_URL + "/tasks"))
//                        .DELETE()
//                        .build(),
//                HttpResponse.BodyHandlers.discarding()
//        );
//        client.send(
//                HttpRequest.newBuilder()
//                        .uri(URI.create(BASE_URL + "/subtasks"))
//                        .DELETE()
//                        .build(),
//                HttpResponse.BodyHandlers.discarding()
//        );
//        client.send(
//                HttpRequest.newBuilder()
//                        .uri(URI.create(BASE_URL + "/epics"))
//                        .DELETE()
//                        .build(),
//                HttpResponse.BodyHandlers.discarding()
//        );
//    }
//}

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

    @Test
    @DisplayName("Получение списка задач")
    void testGetTasks() throws IOException, InterruptedException {
        // Создаём задачу через POST
        String taskJson = """
        {
            "id": 0,
            "name": "Test Task",
            "description": "Description",
            "status": "NEW"
        }
        """;

        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        client.send(post, HttpResponse.BodyHandlers.ofString());

        // Получаем список
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(get, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    // Добавь остальные тесты для /tasks здесь (create, get by id, delete и т.д.)
    @Test
    @DisplayName("Получение задачи по идентификатору")
    void testGetTaskById() throws IOException, InterruptedException {
        // Создаем задачу через POST
        String taskJson = """
    {
        "id": 0,
        "name": "Test Task",
        "description": "Description",
        "status": "NEW"
    }
    """;

        // Создаем задачу
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());

        // Получаем задачу по ID
        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int actualTaskId = createdTask.getId();

        System.out.println("Created task with ID: " + actualTaskId); // Для отладки

        // ✅ ИСПОЛЬЗУЕМ РЕАЛЬНЫЙ ID ДЛЯ ПОЛУЧЕНИЯ
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + actualTaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "Should find task with ID: " + actualTaskId + ". Response: " + response.body());

        assertTrue(response.body().contains("Test Task"));
        assertTrue(response.body().contains("Description"));
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
        // Создаем задачу через POST
        String taskJson = """
    {
        "id": 0,
        "name": "Test Task",
        "description": "Description",
        "status": "NEW"
    }
    """;

        // Создаем задачу
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());

        // ✅ ИЗВЛЕКАЕМ РЕАЛЬНЫЙ ID ИЗ ОТВЕТА
        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int actualTaskId = createdTask.getId();

        System.out.println("Created task with ID: " + actualTaskId); // Для отладки

        // Удаляем задачу
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + actualTaskId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());

        // Проверяем что задача удалена
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }
}
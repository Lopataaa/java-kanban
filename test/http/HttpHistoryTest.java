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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HttpHistoryTest extends HttpTasksTest {

    @Test
    @DisplayName("Получение истории задач")
    void testGetHistory() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Task 1", "Description 1",
                LocalDateTime.now(), Duration.ofMinutes(60));
        Task task2 = new Task(0, "Task 2", "Description 2",
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(30));

        task1.setStatus(TaskStatus.NEW);
        task2.setStatus(TaskStatus.IN_PROGRESS);

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Неверный статус код для первой задачи");
        Task createdTask1 = gson.fromJson(response1.body(), Task.class);
        int taskId1 = createdTask1.getId();

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode(), "Неверный статус код для второй задачи");
        Task createdTask2 = gson.fromJson(response2.body(), Task.class);
        int taskId2 = createdTask2.getId();

        HttpRequest getRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId1))
                .GET()
                .build();
        client.send(getRequest1, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId2))
                .GET()
                .build();
        client.send(getRequest2, HttpResponse.BodyHandlers.ofString());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode(), "Неверный статус код для истории");

        Task[] history = gson.fromJson(historyResponse.body(), Task[].class);
        assertNotNull(history, "История не вернулась");
        assertEquals(2, history.length, "В истории должно быть 2 задачи");

        Set<Integer> historyIds = Arrays.stream(history)
                .map(Task::getId)
                .collect(Collectors.toSet());
        assertTrue(historyIds.contains(taskId1), "В истории должна быть первая задача");
        assertTrue(historyIds.contains(taskId2), "В истории должна быть вторая задача");
    }

    @Test
    @DisplayName("Получение пустой истории задач")
    void testGet_Empty_History() throws IOException, InterruptedException {
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode(), "Неверный статус код для пустой истории");

        Task[] history = gson.fromJson(historyResponse.body(), Task[].class);
        assertNotNull(history, "История не вернулась");
        assertEquals(0, history.length, "История должна быть пустой");
    }
}
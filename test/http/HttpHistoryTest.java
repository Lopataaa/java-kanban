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
        // Создаём задачу
        Task task = new Task(0, "History Task", "Desc",
                LocalDateTime.now(), Duration.ofMinutes(30));
        task.setStatus(TaskStatus.NEW);

        String json = gson.toJson(task);
        HttpResponse<String> postResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/tasks"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Task created = gson.fromJson(postResp.body(), Task.class);

        // Запрашиваем её — попадает в историю
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/tasks/" + created.getId()))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        // Получаем историю
        HttpResponse<String> historyResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/history"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, historyResp.statusCode());
        assertTrue(historyResp.body().contains("History Task"));
    }

    @Test
    @DisplayName("Пустая история")
    void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/history"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body().trim()); // пустой JSON-массив
    }
}
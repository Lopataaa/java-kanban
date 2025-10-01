package http;

import org.junit.jupiter.api.DisplayName;
import task.SubTask;
import task.TaskStatus;
import org.junit.jupiter.api.Test;
import task.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HttpEpicsTest extends HttpTasksTest {

    @Test
    @DisplayName("Добавление эпика")
    void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Test Epic", "Test Description",
                LocalDateTime.now(), Duration.ofMinutes(60));
        epic.setStatus(TaskStatus.NEW);

        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        // Проверка через GET
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(getResponse.body().contains("Test Epic"));
    }

    @Test
    @DisplayName("Получение подзадач эпика")
    void testGet_Epic_Subtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic(0, "Test Epic", "Test Description",
                LocalDateTime.now(), Duration.ofMinutes(60));
        epic.setStatus(TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpResponse<String> epicResponse = createEpic(epicJson);
        assertEquals(201, epicResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
        int epicId = createdEpic.getId();

        // Получаем подзадачи эпика
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный статус код для подзадач эпика");

        // Проверяем что возвращается корректный JSON (пустой массив или с подзадачами)
        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);
        assertNotNull(subtasks, "Подзадачи не должны быть null");
    }

    @Test
    @DisplayName("Удаление эпика")
    void testDeleteEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic(0, "Test Epic", "Test Description",
                LocalDateTime.now(), Duration.ofMinutes(60));
        epic.setStatus(TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpResponse<String> postResponse = createEpic(epicJson);
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(postResponse.body(), Epic.class);
        int epicId = createdEpic.getId();

        // Проверяем что эпик создан
        Epic[] epicsBefore = getAllEpics();
        boolean epicExists = Arrays.stream(epicsBefore)
                .anyMatch(e -> e.getId() == epicId);
        assertTrue(epicExists, "Эпик должен существовать перед удалением");

        // Удаляем эпик
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении эпика");

        // Проверяем что эпик удален
        Epic[] epicsAfter = getAllEpics();
        boolean epicStillExists = Arrays.stream(epicsAfter)
                .anyMatch(e -> e.getId() == epicId);
        assertFalse(epicStillExists, "Эпик не должен существовать после удаления");
        assertEquals(0, epicsAfter.length, "Эпик не был удален");
    }

    private HttpResponse<String> createEpic(String epicJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Epic[] getAllEpics() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), Epic[].class);
    }
}
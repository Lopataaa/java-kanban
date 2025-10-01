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
    private static final String EPIC_NAME = "Test Epic";
    private static final String EPIC_DESCRIPTION = "Test Description";
    private static final String PATH_EPICS = "/epics";
    private static final String PATH_SUBTASKS = "/subtasks";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int DURATION_MINUTES = 60;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_OK = 200;

    @Test
    @DisplayName("Добавление эпика")
    void testAddEpic() throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic(0, EPIC_NAME, EPIC_DESCRIPTION,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES));
        epic.setStatus(TaskStatus.NEW);

        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // When
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_CREATED, response.statusCode());

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertTrue(getResponse.body().contains(EPIC_NAME));
    }

    @Test
    @DisplayName("Получение подзадач эпика")
    void testGet_Epic_Subtasks() throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic(0, EPIC_NAME, EPIC_DESCRIPTION,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES));
        epic.setStatus(TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpResponse<String> epicResponse = createEpic(epicJson);
        assertEquals(STATUS_CREATED, epicResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
        int epicId = createdEpic.getId();

        // When
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS + "/" + epicId + PATH_SUBTASKS))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, response.statusCode(), "Неверный статус код для подзадач эпика");

        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);
        assertNotNull(subtasks, "Подзадачи не должны быть null");
    }

    @Test
    @DisplayName("Удаление эпика")
    void testDeleteEpic() throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic(0, EPIC_NAME, EPIC_DESCRIPTION,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES));
        epic.setStatus(TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpResponse<String> postResponse = createEpic(epicJson);
        assertEquals(STATUS_CREATED, postResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(postResponse.body(), Epic.class);
        int epicId = createdEpic.getId();

        Epic[] epicsBefore = getAllEpics();
        boolean epicExists = Arrays.stream(epicsBefore)
                .anyMatch(e -> e.getId() == epicId);
        assertTrue(epicExists, "Эпик должен существовать перед удалением");

        // When
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS + "/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, deleteResponse.statusCode(), "Неверный статус код при удалении эпика");

        Epic[] epicsAfter = getAllEpics();
        boolean epicStillExists = Arrays.stream(epicsAfter)
                .anyMatch(e -> e.getId() == epicId);
        assertFalse(epicStillExists, "Эпик не должен существовать после удаления");
        assertEquals(0, epicsAfter.length, "Эпик не был удален");
    }

    private HttpResponse<String> createEpic(String epicJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Epic[] getAllEpics() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), Epic[].class);
    }
}
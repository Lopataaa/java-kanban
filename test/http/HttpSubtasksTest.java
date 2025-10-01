package http;

import task.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpSubtasksTest extends HttpTasksTest {
    private static final String EPIC_NAME = "Test Epic";
    private static final String EPIC_DESCRIPTION = "Test Epic Description";
    private static final String SUBTASK_NAME = "Test Subtask";
    private static final String SUBTASK_DESCRIPTION = "Test Description";
    private static final String SUBTASK_NAME_1 = "Subtask 1";
    private static final String SUBTASK_DESCRIPTION_1 = "Description 1";
    private static final String SUBTASK_NAME_2 = "Subtask 2";
    private static final String SUBTASK_DESCRIPTION_2 = "Description 2";
    private static final String PATH_EPICS = "/epics";
    private static final String PATH_SUBTASKS = "/subtasks";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int NON_EXISTENT_SUBTASK_ID = 999;
    private static final int DURATION_MINUTES = 60;
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_NOT_FOUND = 404;
    private static final int EXPECTED_SUBTASKS_COUNT_1 = 1;
    private static final int EXPECTED_SUBTASKS_COUNT_2 = 2;
    private static final int EXPECTED_EMPTY_COUNT = 0;

    private int createEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(0, EPIC_NAME, EPIC_DESCRIPTION);
        epic.setStatus(TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_EPICS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(STATUS_CREATED, epicResponse.statusCode(), "Не удалось создать эпик");

        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
        return createdEpic.getId();
    }

    @Test
    @DisplayName("Создание новой подзадачи")
    void testAddSubtask() throws IOException, InterruptedException {
        // Given
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, SUBTASK_NAME, SUBTASK_DESCRIPTION, epicId);
        subtask.setStatus(TaskStatus.NEW);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofMinutes(DURATION_MINUTES));

        String subtaskJson = gson.toJson(subtask);

        // When
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_CREATED, response.statusCode(), "Неверный статус код");

        SubTask[] subtasks = getAllSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(EXPECTED_SUBTASKS_COUNT_1, subtasks.length, "Некорректное количество подзадач");
        assertEquals(SUBTASK_NAME, subtasks[0].getName(), "Некорректное имя подзадачи");
    }

    @Test
    @DisplayName("Получение подзадачи по ID")
    void testGetSubtaskById() throws IOException, InterruptedException {
        // Given
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, SUBTASK_NAME, SUBTASK_DESCRIPTION, epicId);
        subtask.setStatus(TaskStatus.NEW);
        String subtaskJson = gson.toJson(subtask);

        HttpResponse<String> postResponse = createSubtask(subtaskJson);
        assertEquals(STATUS_CREATED, postResponse.statusCode());

        SubTask createdSubtask = gson.fromJson(postResponse.body(), SubTask.class);
        int subtaskId = createdSubtask.getId();

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS + "/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, getResponse.statusCode());

        SubTask responseSubtask = gson.fromJson(getResponse.body(), SubTask.class);
        assertNotNull(responseSubtask, "Подзадача не вернулась");
        assertEquals(subtaskId, responseSubtask.getId(), "Неверный ID подзадачи");
    }

    @Test
    @DisplayName("Получение кода 404 при запросе несуществующей подзадачи")
    void testGet_SubtaskById_NotFound() throws IOException, InterruptedException {
        // Given
        // When
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS + "/" + NON_EXISTENT_SUBTASK_ID))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_NOT_FOUND, response.statusCode(), "Неверный статус код для несуществующей подзадачи");
    }

    @Test
    @DisplayName("Удаление подзадачи по ID")
    void testDelete_Subtask() throws IOException, InterruptedException {
        // Given
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, SUBTASK_NAME, SUBTASK_DESCRIPTION, epicId);
        subtask.setStatus(TaskStatus.NEW);
        String subtaskJson = gson.toJson(subtask);

        HttpResponse<String> postResponse = createSubtask(subtaskJson);
        assertEquals(STATUS_CREATED, postResponse.statusCode(), "Неверный статус код при создании");

        SubTask createdSubtask = gson.fromJson(postResponse.body(), SubTask.class);
        int subtaskId = createdSubtask.getId();

        // When
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS + "/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, deleteResponse.statusCode(), "Неверный статус код при удалении");

        SubTask[] subtasksAfter = getAllSubtasks();
        assertEquals(EXPECTED_EMPTY_COUNT, subtasksAfter.length, "Подзадача не была удалена");
    }

    @Test
    @DisplayName("Получение всех подзадач")
    void testGet_AllSubtasks() throws IOException, InterruptedException {
        // Given
        int epicId = createEpic();

        SubTask subtask1 = new SubTask(0, SUBTASK_NAME_1, SUBTASK_DESCRIPTION_1, epicId);
        subtask1.setStatus(TaskStatus.NEW);

        SubTask subtask2 = new SubTask(0, SUBTASK_NAME_2, SUBTASK_DESCRIPTION_2, epicId);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);

        createSubtask(gson.toJson(subtask1));
        createSubtask(gson.toJson(subtask2));

        // When
        SubTask[] subtasks = getAllSubtasks();

        // Then
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(EXPECTED_SUBTASKS_COUNT_2, subtasks.length, "Некорректное количество подзадач");
    }

    @Test
    @DisplayName("Удаление всех подзадач через DELETE /subtasks")
    void testDelete_AllSubtasks() throws IOException, InterruptedException {
        // Given
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, SUBTASK_NAME, SUBTASK_DESCRIPTION, epicId);
        subtask.setStatus(TaskStatus.NEW);
        createSubtask(gson.toJson(subtask));

        // When
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, deleteResponse.statusCode(), "Неверный статус код при удалении всех подзадач");

        SubTask[] subtasksAfter = getAllSubtasks();
        assertEquals(EXPECTED_EMPTY_COUNT, subtasksAfter.length, "Подзадачи не были удалены");
    }

    private HttpResponse<String> createSubtask(String subtaskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private SubTask[] getAllSubtasks() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_SUBTASKS))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), SubTask[].class);
    }
}
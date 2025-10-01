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

    private int createEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Test Epic", "Test Epic Description");
        epic.setStatus(TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode(), "Не удалось создать эпик");

        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
        return createdEpic.getId();
    }

    @Test
    @DisplayName("Создание новой подзадачи")
    void testAddSubtask() throws IOException, InterruptedException {
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, "Test Subtask", "Test Description", epicId);
        subtask.setStatus(TaskStatus.NEW);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofMinutes(60));

        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный статус код");

        SubTask[] subtasks = getAllSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(1, subtasks.length, "Некорректное количество подзадач");
        assertEquals("Test Subtask", subtasks[0].getName(), "Некорректное имя подзадачи");
    }

    @Test
    @DisplayName("Получение подзадачи по ID")
    void testGetSubtaskById() throws IOException, InterruptedException {
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, "Test Subtask", "Test Description", epicId);
        subtask.setStatus(TaskStatus.NEW);
        String subtaskJson = gson.toJson(subtask);

        // Создаем подзадачу и получаем ответ
        HttpResponse<String> postResponse = createSubtask(subtaskJson);
        assertEquals(201, postResponse.statusCode());

        // Извлекаем реальный ID из ответа
        SubTask createdSubtask = gson.fromJson(postResponse.body(), SubTask.class);
        int subtaskId = createdSubtask.getId(); // Это будет 2 (или другой сгенерированный ID)

        System.out.println("Created subtask with ID: " + subtaskId);

        // Используем реальный ID для запроса
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        SubTask responseSubtask = gson.fromJson(getResponse.body(), SubTask.class);
        assertNotNull(responseSubtask, "Подзадача не вернулась");
        assertEquals(subtaskId, responseSubtask.getId(), "Неверный ID подзадачи");
    }

    @Test
    @DisplayName("Получение кода 404 при запросе несуществующей подзадачи")
    void testGet_SubtaskById_NotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Неверный статус код для несуществующей подзадачи");
    }

    @Test
    @DisplayName("Удаление подзадачи по ID")
    void testDelete_Subtask() throws IOException, InterruptedException {
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, "Test Subtask", "Test Description", epicId);
        subtask.setStatus(TaskStatus.NEW);
        String subtaskJson = gson.toJson(subtask);

        HttpResponse<String> postResponse = createSubtask(subtaskJson);
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании");

        SubTask createdSubtask = gson.fromJson(postResponse.body(), SubTask.class);
        int subtaskId = createdSubtask.getId();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении");

        SubTask[] subtasksAfter = getAllSubtasks();
        assertEquals(0, subtasksAfter.length, "Подзадача не была удалена");
    }

    @Test
    @DisplayName("Получение всех подзадач")
    void testGet_AllSubtasks() throws IOException, InterruptedException {
        int epicId = createEpic();

        SubTask subtask1 = new SubTask(0, "Subtask 1", "Description 1", epicId);
        subtask1.setStatus(TaskStatus.NEW);

        SubTask subtask2 = new SubTask(0, "Subtask 2", "Description 2", epicId);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);

        createSubtask(gson.toJson(subtask1));
        createSubtask(gson.toJson(subtask2));

        SubTask[] subtasks = getAllSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(2, subtasks.length, "Некорректное количество подзадач");
    }

    @Test
    @DisplayName("Удаление всех подзадач через DELETE /subtasks")
    void testDelete_AllSubtasks() throws IOException, InterruptedException {
        int epicId = createEpic();

        SubTask subtask = new SubTask(0, "Test Subtask", "Test Description", epicId);
        subtask.setStatus(TaskStatus.NEW);
        createSubtask(gson.toJson(subtask));

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении всех подзадач");

        SubTask[] subtasksAfter = getAllSubtasks();
        assertEquals(0, subtasksAfter.length, "Подзадачи не были удалены");
    }

    private HttpResponse<String> createSubtask(String subtaskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private SubTask[] getAllSubtasks() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), SubTask[].class);
    }
}
package http;

import task.TaskStatus;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.*;

class HttpSubtasksTest extends http.HttpTasksTest {

//    private int createEpic() throws IOException, InterruptedException {
//        Epic epic = new Epic("Test Epic", "Test Epic Description", 0, TaskStatus.NEW,
//                null, 0);
//        String epicJson = gson.toJson(epic);
//
//        HttpRequest epicRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/epic/"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
//                .build();
//
//        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(201, epicResponse.statusCode(), "Не удалось создать эпик");
//
//        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
//        return createdEpic.getTaskId();
//    }
//
//    @Test
//    void testAddSubtask() throws IOException, InterruptedException {
//        int epicId = createEpic();
//        SubTask subtask = new SubTask("Test Subtask", "Test Description", 0, TaskStatus.NEW,
//                epicId, LocalDateTime.now(), 60);
//        String subtaskJson = gson.toJson(subtask);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        assertEquals(201, response.statusCode(), "Неверный статус код");
//
//        SubTask[] subtasks = getAllSubtasks();
//        assertNotNull(subtasks, "Подзадачи не возвращаются");
//        assertEquals(1, subtasks.length, "Некорректное количество подзадач");
//        assertEquals("Test Subtask", subtasks[0].getTaskName(), "Некорректное имя подзадачи");
//    }
//
//    @Test
//    void testGetSubtaskById() throws IOException, InterruptedException {
//        int epicId = createEpic();
//        SubTask subtask = new SubTask("Test Subtask", "Test Description", 0, TaskStatus.NEW,
//                epicId, LocalDateTime.now(), 60);
//        String subtaskJson = gson.toJson(subtask);
//
//        HttpResponse<String> postResponse = createSubtask(subtaskJson);
//        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании");
//
//        StructuredTaskScope.Subtask createdSubtask = gson.fromJson(postResponse.body(), SubTask.class);
//        int subtaskId = createdSubtask.getTaskId();
//
//        HttpRequest getRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/subtask/" + subtaskId))
//                .GET()
//                .build();
//
//        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(200, getResponse.statusCode(), "Неверный статус код");
//
//        SubTask responseSubtask = gson.fromJson(getResponse.body(), SubTask.class);
//        assertNotNull(responseSubtask, "Подзадача не вернулась");
//        assertEquals(subtaskId, responseSubtask.getTaskId(), "Неверный ID подзадачи");
//    }
//
//    @Test
//    void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/subtask/999"))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        assertEquals(404, response.statusCode(), "Неверный статус код для несуществующей подзадачи");
//    }
//
//    @Test
//    void testDeleteSubtask() throws IOException, InterruptedException {
//        int epicId = createEpic();
//        SubTask subtask = new SubTask("Test Subtask", "Test Description", 0, TaskStatus.NEW,
//                epicId, LocalDateTime.now(), 60);
//        String subtaskJson = gson.toJson(subtask);
//
//        HttpResponse<String> postResponse = createSubtask(subtaskJson);
//        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании");
//
//        SubTask createdSubtask = gson.fromJson(postResponse.body(), SubTask.class);
//        int subtaskId = createdSubtask.getTaskId();
//
//        HttpRequest deleteRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/subtask/" + subtaskId))
//                .DELETE()
//                .build();
//
//        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении");
//
//        SubTask[] subtasksAfter = getAllSubtasks();
//        assertEquals(0, subtasksAfter.length, "Подзадача не была удалена");
//    }
//
//    private HttpResponse<String> createSubtask(String subtaskJson) throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
//                .build();
//        return client.send(request, HttpResponse.BodyHandlers.ofString());
//    }
//
//    private SubTask[] getAllSubtasks() throws IOException, InterruptedException {
//        HttpRequest getRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
//                .GET()
//                .build();
//        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
//        return gson.fromJson(response.body(), SubTask[].class);
//    }
}
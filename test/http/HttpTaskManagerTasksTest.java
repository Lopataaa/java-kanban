package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerTasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer();
        gson = new Gson();
        client = HttpClient.newHttpClient();
        taskServer.start();

        clearAllData();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    private void clearAllData() {

        if (manager.getTasks() != null && !manager.getTasks().isEmpty()) {
            manager.deleteAllTasks();
        }
        if (manager.getSubTasks() != null && !manager.getSubTasks().isEmpty()) {
            manager.deleteAllSubtasks();
        }
        if (manager.getEpics() != null && !manager.getEpics().isEmpty()) {
            manager.deleteAllEpics();
        }
    }

    @Test
    @DisplayName("Добавление задачи")
    public void testAddTask() throws IOException, InterruptedException {

        Task task = new Task(0, "Test 2", "Testing task 2",
                LocalDateTime.now(), Duration.ofMinutes(5));
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        // создание HTTP-клиента и запроса
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус код при создании задачи");

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    @DisplayName("Получение задачи по ID")
    public void testGetTask() throws IOException, InterruptedException {

        Task task = new Task(0, "Test Task", "Test Description",
                LocalDateTime.now(), Duration.ofMinutes(10));
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Не удалось создать задачу");

        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int taskId = createdTask.getId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Неверный статус код при получении задачи");

        Task responseTask = gson.fromJson(getResponse.body(), Task.class);
        assertNotNull(responseTask, "Задача не вернулась");
        assertEquals(taskId, responseTask.getId(), "Неверный ID задачи");
        assertEquals("Test Task", responseTask.getName(), "Неверное имя задачи");
    }

    @Test
    @DisplayName("Получение всех задач")
    public void testGet_AllTasks() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Task 1", "Description 1",
                LocalDateTime.now(), Duration.ofMinutes(5));
        task1.setStatus(TaskStatus.NEW);

        Task task2 = new Task(0, "Task 2", "Description 2",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));
        task2.setStatus(TaskStatus.IN_PROGRESS);

        createTask(gson.toJson(task1));
        createTask(gson.toJson(task2));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный статус код при получении всех задач");

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(2, tasks.length, "Некорректное количество задач");
    }

    private HttpResponse<String> createTask(String taskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
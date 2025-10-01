package http;

import com.google.gson.Gson;
import manager.Managers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTasksEndpointTest extends HttpTasksTest{
    private static final String TASK_NAME_1 = "Test 2";
    private static final String TASK_DESCRIPTION_1 = "Testing task 2";
    private static final String TASK_NAME_2 = "Test Task";
    private static final String TASK_DESCRIPTION_2 = "Test Description";
    private static final String TASK_NAME_3 = "Task 1";
    private static final String TASK_DESCRIPTION_3 = "Description 1";
    private static final String TASK_NAME_4 = "Task 2";
    private static final String TASK_DESCRIPTION_4 = "Description 2";
    private static final String PATH_TASKS = "/tasks";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int DURATION_MINUTES_5 = 5;
    private static final int DURATION_MINUTES_10 = 10;
    private static final int HOURS_OFFSET_1 = 1;
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int EXPECTED_TASKS_COUNT = 2;

    private final Gson gson = Managers.getGson();

    @Test
    @DisplayName("Добавление задачи")
    public void testAddTask() throws IOException, InterruptedException {
        // Given
        Task task = new Task(0, TASK_NAME_1, TASK_DESCRIPTION_1,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES_5));
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        // When
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_CREATED, response.statusCode(), "Неверный статус код при создании задачи");

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertTrue(getResponse.body().contains(TASK_NAME_1));
    }

    @Test
    @DisplayName("Получение задачи по ID")
    public void testGetTask() throws IOException, InterruptedException {
        // Given
        Task task = new Task(0, TASK_NAME_2, TASK_DESCRIPTION_2,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES_10));
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(STATUS_CREATED, postResponse.statusCode(), "Не удалось создать задачу");

        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int taskId = createdTask.getId();

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS + "/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, getResponse.statusCode(), "Неверный статус код при получении задачи");

        Task responseTask = gson.fromJson(getResponse.body(), Task.class);
        assertNotNull(responseTask, "Задача не вернулась");
        assertEquals(taskId, responseTask.getId(), "Неверный ID задачи");
        assertEquals(TASK_NAME_2, responseTask.getName(), "Неверное имя задачи");
        assertEquals(TASK_DESCRIPTION_2, responseTask.getDescription(), "Неверное описание задачи");
        assertEquals(TaskStatus.NEW, responseTask.getStatus(), "Неверный статус задачи");
    }

    @Test
    @DisplayName("Получение всех задач")
    public void testGet_AllTasks() throws IOException, InterruptedException {
        // Given
        Task task1 = new Task(0, TASK_NAME_3, TASK_DESCRIPTION_3,
                LocalDateTime.now(), Duration.ofMinutes(DURATION_MINUTES_5));
        task1.setStatus(TaskStatus.NEW);

        Task task2 = new Task(0, TASK_NAME_4, TASK_DESCRIPTION_4,
                LocalDateTime.now().plusHours(HOURS_OFFSET_1), Duration.ofMinutes(DURATION_MINUTES_10));
        task2.setStatus(TaskStatus.IN_PROGRESS);

        createTask(gson.toJson(task1));
        createTask(gson.toJson(task2));

        // When
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(STATUS_OK, response.statusCode(), "Неверный статус код при получении всех задач");

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(EXPECTED_TASKS_COUNT, tasks.length, "Некорректное количество задач");

        boolean foundTask1 = false;
        boolean foundTask2 = false;
        for (Task task : tasks) {
            if (TASK_NAME_3.equals(task.getName())) {
                foundTask1 = true;
                assertEquals(TASK_DESCRIPTION_3, task.getDescription(), "Неверное описание задачи 1");
            }
            if (TASK_NAME_4.equals(task.getName())) {
                foundTask2 = true;
                assertEquals(TASK_DESCRIPTION_4, task.getDescription(), "Неверное описание задачи 2");
            }
        }
        assertTrue(foundTask1, "Задача 1 не найдена в списке");
        assertTrue(foundTask2, "Задача 2 не найдена в списке");
    }

    private HttpResponse<String> createTask(String taskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_TASKS))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
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

//Тесты для /task
public class HttpTasksEndpointTest extends HttpTasksTest{

    private final Gson gson = Managers.getGson();

    @Test
    @DisplayName("Добавление задачи")
    public void testAddTask() throws IOException, InterruptedException {

        Task task = new Task(0, "Test 2", "Testing task 2",
                LocalDateTime.now(), Duration.ofMinutes(5));
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный статус код при создании задачи");

        // Проверяем через GET запрос, а не через менеджер
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(getResponse.body().contains("Test 2"));
    }

    @Test
    @DisplayName("Получение задачи по ID")
    public void testGetTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task(0, "Test Task", "Test Description",
                LocalDateTime.now(), Duration.ofMinutes(10));
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Не удалось создать задачу");

        // Получаем ID созданной задачи из ответа
        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int taskId = createdTask.getId();

        // Получаем задачу по ID
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Неверный статус код при получении задачи");

        // Проверяем полученную задачу
        Task responseTask = gson.fromJson(getResponse.body(), Task.class);
        assertNotNull(responseTask, "Задача не вернулась");
        assertEquals(taskId, responseTask.getId(), "Неверный ID задачи");
        assertEquals("Test Task", responseTask.getName(), "Неверное имя задачи");
        assertEquals("Test Description", responseTask.getDescription(), "Неверное описание задачи");
        assertEquals(TaskStatus.NEW, responseTask.getStatus(), "Неверный статус задачи");
    }

    @Test
    @DisplayName("Получение всех задач")
    public void testGet_AllTasks() throws IOException, InterruptedException {
        // Создаем первую задачу
        Task task1 = new Task(0, "Task 1", "Description 1",
                LocalDateTime.now(), Duration.ofMinutes(5));
        task1.setStatus(TaskStatus.NEW);

        // Создаем вторую задачу
        Task task2 = new Task(0, "Task 2", "Description 2",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));
        task2.setStatus(TaskStatus.IN_PROGRESS);

        // Создаем задачи через HTTP
        createTask(gson.toJson(task1));
        createTask(gson.toJson(task2));

        // Получаем все задачи
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный статус код при получении всех задач");

        // Проверяем список задач
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(2, tasks.length, "Некорректное количество задач");

        // Проверяем что обе задачи присутствуют
        boolean foundTask1 = false;
        boolean foundTask2 = false;
        for (Task task : tasks) {
            if ("Task 1".equals(task.getName())) {
                foundTask1 = true;
                assertEquals("Description 1", task.getDescription(), "Неверное описание задачи 1");
            }
            if ("Task 2".equals(task.getName())) {
                foundTask2 = true;
                assertEquals("Description 2", task.getDescription(), "Неверное описание задачи 2");
            }
        }
        assertTrue(foundTask1, "Задача 1 не найдена в списке");
        assertTrue(foundTask2, "Задача 2 не найдена в списке");
    }

    private HttpResponse<String> createTask(String taskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
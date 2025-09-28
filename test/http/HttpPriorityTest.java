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

import static org.junit.jupiter.api.Assertions.*;

class HttpPriorityTest extends HttpTasksTest {

    @Test
    @DisplayName("Получение приоритетных задач")
    void test_Get_Prioritized_Tasks() throws IOException, InterruptedException {
        // Создание задачи с временем
        Task task1 = new Task(0, "Task Early", "Early task",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(60));
        Task task2 = new Task(0, "Task Late", "Late task",
                LocalDateTime.now().plusHours(3), Duration.ofMinutes(30));

        task1.setStatus(TaskStatus.NEW);
        task2.setStatus(TaskStatus.IN_PROGRESS);

        createTask(gson.toJson(task1));
        createTask(gson.toJson(task2));

        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, priorityResponse.statusCode(), "Неверный статус код для приоритетных задач");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(2, prioritizedTasks.length, "Должно быть 2 приоритетные задачи");

        assertEquals("Task Early", prioritizedTasks[0].getName(),
                "Задачи должны быть отсортированы по времени начала");
    }

    @Test
    @DisplayName("Получение пустого списка из приоритетных задач")
    void testGet_Empty_Prioritized_Tasks() throws IOException, InterruptedException {
        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, priorityResponse.statusCode(), "Неверный статус код для пустого списка");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(0, prioritizedTasks.length, "Список приоритетных задач должен быть пустым");
    }

    @Test
    @DisplayName("Задачи без времени не попадут в приоритетный список")
    void testTasks_Without_TimeNot_InPrioritized() throws IOException, InterruptedException {
        // Создание задачи без времени
        Task taskWithoutTime = new Task(0, "No Time Task", "Task without time");
        taskWithoutTime.setStatus(TaskStatus.NEW);

        createTask(gson.toJson(taskWithoutTime));

        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, priorityResponse.statusCode());

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks);

        assertEquals(0, prioritizedTasks.length,
                "Задачи без времени не должны быть в приоритетном списке");
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
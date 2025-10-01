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
        System.out.println("=== STARTING PRIORITIZED TEST ===");

        // Создание задачи с временем
        Task task1 = new Task(0, "Task Early", "Early task",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(60));
        Task task2 = new Task(0, "Task Late", "Late task",
                LocalDateTime.now().plusHours(3), Duration.ofMinutes(30));

        task1.setStatus(TaskStatus.NEW);
        task2.setStatus(TaskStatus.IN_PROGRESS);

        System.out.println("Task1 startTime: " + task1.getStartTime());
        System.out.println("Task2 startTime: " + task2.getStartTime());

        // Создаем задачи и проверяем ответы
        HttpResponse<String> response1 = createTask(gson.toJson(task1));
        System.out.println("Task1 creation: " + response1.statusCode() + " - " + response1.body());

        HttpResponse<String> response2 = createTask(gson.toJson(task2));
        System.out.println("Task2 creation: " + response2.statusCode() + " - " + response2.body());

        // Проверим что задачи действительно создались
        HttpRequest getAllRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> allTasksResponse = client.send(getAllRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("All tasks response: " + allTasksResponse.body());

        // Запрос приоритетных задач
        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Prioritized response status: " + priorityResponse.statusCode());
        System.out.println("Prioritized response body: " + priorityResponse.body());

        assertEquals(200, priorityResponse.statusCode(), "Неверный статус код для приоритетных задач");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        System.out.println("Parsed prioritized tasks count: " + (prioritizedTasks != null ? prioritizedTasks.length : "null"));

        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(2, prioritizedTasks.length, "Должно быть 2 приоритетные задачи");

        // Проверяем сортировку
        if (prioritizedTasks.length >= 2) {
            assertTrue(prioritizedTasks[0].getStartTime().isBefore(prioritizedTasks[1].getStartTime()),
                    "Задачи должны быть отсортированы по времени начала");
            assertEquals("Task Early", prioritizedTasks[0].getName(),
                    "Первая задача должна быть 'Task Early'");
        }
    }

    @Test
    @DisplayName("Получение пустого списка из приоритетных задач")
    void testGet_Empty_Prioritized_Tasks() throws IOException, InterruptedException {
        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
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
                .uri(URI.create(BASE_URL + "/prioritized"))
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
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Create task response: " + response.statusCode() + " - " + response.body());
        return response;
    }
}
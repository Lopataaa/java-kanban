/*package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.HttpMethod;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.regex.Pattern;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Set;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            HttpMethod httpMethod;

            try {
                httpMethod = HttpMethod.valueOf(exchange.getRequestMethod());
            } catch (IllegalArgumentException e) {
                sendNotFound(exchange);
                return;
            }

            if (httpMethod == HttpMethod.GET) {
                if (Pattern.matches("^/tasks/$", path)) {
                    handleGetPrioritizedTasks(exchange);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks(); // Изменили на Set
        Type taskSetType = new TypeToken<Set<Task>>(){}.getType();
        String jsonResponse = gson.toJson(prioritizedTasks, taskSetType);
        sendSuccess(exchange, jsonResponse);
    }
}*/

/*package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import http.HttpMethod;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGetRequest(h, path, "/tasks",
                            (Void) -> taskManager.getTasks(),
                            id -> taskManager.findTaskById(id));
                    break;
                case "POST":
                    handlePostRequest(h, Task.class,
                            task -> {
                                taskManager.addTask(task);
                                return task;
                            },
                            task -> {
                                taskManager.updateTask(task);
                                return task;
                            },
                            task -> task.getId() == 0);
                    break;
                case "DELETE":
                    handleDeleteRequest(h, path, "/tasks",
                            () -> taskManager.deleteAllTasks(),
                            id -> taskManager.findTaskById(id),
                            id -> taskManager.deleteTask(id));
                    break;
                default:
                    sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }

    private void handleDeleteTaskById(HttpExchange h) {
    }

    private void handleDeleteAllTasks(HttpExchange h) {
    }

    private void handlePostTask(HttpExchange h) throws IOException {
        try {
            // Чтение JSON из тела запроса
            String requestBody = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Парсинг JSON в объект Task
            Gson gson = new Gson();
            Task task = gson.fromJson(requestBody, Task.class);

            // Определение типа задачи и сохранение
            if (task.getType() == TaskType.TASK) {
                taskManager.addTask(task);
            } else if (task.getType() == TaskType.EPIC) {
                taskManager.addEpic((Epic) task);
            } else if (task.getType() == TaskType.SUBTASK) {
                taskManager.addSubTask((SubTask) task);
            }

            // Отправка успешного ответа
            String response = gson.toJson(task);
            sendText(h, response, 201); // 201 Created

        } catch (Exception e) {
            sendBadRequest(h, "Invalid task data: " + e.getMessage());
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange h) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks().stream().toList();
        sendSuccess(h, gson.toJson(prioritizedTasks));
    }

    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(400, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    public void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}*/

package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            switch (method) {
                case "GET":
                    if (Pattern.matches("^/tasks$", path)) {
                        handleGetRequest(h, path, "/tasks",
                                (Void) -> taskManager.getTasks(),
                                id -> taskManager.findTaskById(id));
                    } else if (Pattern.matches("^/prioritized$", path)) {
                        handleGetPrioritizedTasks(h); // обработка приоритетных задач
                    } else {
                        sendNotFound(h);
                    }
                    break;
                case "POST":
                    if (Pattern.matches("^/tasks$", path)) {
                        handlePostRequest(h, Task.class,
                                task -> {
                                    taskManager.addTask(task);
                                    return task;
                                },
                                task -> {
                                    taskManager.updateTask(task);
                                    return task;
                                },
                                task -> task.getId() == 0);
                    } else {
                        sendNotFound(h);
                    }
                    break;
                case "DELETE":
                    handleDeleteRequest(h, path, "/tasks",
                            () -> taskManager.deleteAllTasks(),
                            id -> taskManager.findTaskById(id),
                            id -> taskManager.deleteTask(id));
                    break;
                default:
                    sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange h) throws IOException {
        // Используем метод из BaseHttpHandler для отправки ответа
        sendSuccess(h, gson.toJson(taskManager.getPrioritizedTasks()));
    }
}
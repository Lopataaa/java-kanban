package http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            System.out.println("Received " + method + " request for path: " + path);

            switch (method) {
                case "GET":
                    if (path.equals("/tasks")) {
                        System.out.println("Handling GET /tasks");
                        handleGetAllTasks(exchange);
                    } else if (path.startsWith("/tasks/")) {
                        System.out.println("Handling GET /tasks/{id}");
                        handleGetTaskById(exchange, path);
                    } else {
                        System.out.println("Path not found: " + path);
                        sendNotFound(exchange);
                    }
                    break;

                case "POST":
                    if (path.equals("/tasks")) {
                        System.out.println("Handling POST /tasks");
                        handlePostTask(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;

                case "DELETE":
                    if (path.startsWith("/tasks/")) {
                        System.out.println("Handling DELETE /tasks/{id}");
                        handleDeleteTaskById(exchange, path);
                    } else if (path.equals("/tasks")) {
                        System.out.println("Handling DELETE /tasks");
                        handleDeleteAllTasks(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;

                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            sendInternalServerError(exchange);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        sendSuccess(exchange, gson.toJson(taskManager.getTasks()));
    }

    private void handleGetTaskById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(7);
            int id = Integer.parseInt(idStr);
            Task task = taskManager.findTaskById(id);
            if (task != null) {
                sendSuccess(exchange, gson.toJson(task));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid task ID format");
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void sendBadRequest(HttpExchange exchange, String invalidTaskIdFormat) {
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                taskManager.addTask(task);
                sendCreated(exchange, gson.toJson(task));
            } else {
                taskManager.updateTask(task);
                sendSuccess(exchange, gson.toJson(task));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(7);
            int id = Integer.parseInt(idStr);
            Task task = taskManager.findTaskById(id);
            if (task != null) {
                taskManager.deleteTask(id);
                sendSuccess(exchange, "Task deleted");
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid task ID format");
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendSuccess(exchange, "All tasks deleted");
    }
}
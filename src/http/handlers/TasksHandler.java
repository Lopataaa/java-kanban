package http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    private static final String PATH_TASKS = "/tasks";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_DELETE = "DELETE";

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case METHOD_GET:
                    handleGetRequest(exchange, path);
                    break;
                case METHOD_POST:
                    handlePostRequest(exchange, path);
                    break;
                case METHOD_DELETE:
                    handleDeleteRequest(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals(PATH_TASKS)) {
            handleGetAllTasks(exchange);
        } else if (path.startsWith(PATH_TASKS + "/")) {
            handleGetTaskById(exchange, path);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals(PATH_TASKS)) {
            handlePostTask(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith(PATH_TASKS + "/")) {
            handleDeleteTaskById(exchange, path);
        } else if (path.equals(PATH_TASKS)) {
            handleDeleteAllTasks(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        sendSuccess(exchange, gson.toJson(tasks));
    }

    private void handleGetTaskById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(PATH_TASKS.length() + 1);
        int id = parsePathId(idStr);

        if (id == -1) {
            sendBadRequest(exchange);
            return;
        }

        Task task = taskManager.findTaskById(id);
        if (task != null) {
            sendSuccess(exchange, gson.toJson(task));
        } else {
            sendNotFound(exchange);
        }
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
            sendBadRequest(exchange);
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(PATH_TASKS.length() + 1);
        int id = parsePathId(idStr);

        if (id == -1) {
            sendBadRequest(exchange);
            return;
        }

        Task task = taskManager.findTaskById(id);
        if (task != null) {
            taskManager.deleteTask(id);
            sendSuccess(exchange, "Task deleted");
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendSuccess(exchange, "All tasks deleted");
    }
}
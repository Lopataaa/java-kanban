/*package http.handlers;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;
import http.HttpMethod;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
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
                if (Pattern.matches("^/prioritized$", path)) { // Исправлен путь
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
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks(); // Теперь Set
        Type taskSetType = new TypeToken<Set<Task>>(){}.getType();
        String jsonResponse = gson.toJson(prioritizedTasks, taskSetType);
        sendSuccess(exchange, jsonResponse);
    }
}*/

package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;
import http.HttpMethod;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetPrioritized(exchange);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        try {
            sendSuccess(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
        } catch (Exception e) {
            // Логируем ошибку для debugging
            System.err.println("Error in getPrioritizedTasks: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange);
        }
    }
}
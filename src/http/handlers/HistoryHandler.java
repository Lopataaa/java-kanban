/*package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;
import http.HttpMethod;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
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
                if (Pattern.matches("^/tasks/history/$", path)) {
                    handleGetHistory(exchange);
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

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();

        // Правильная сериализация List<Task>
        Type taskListType = new TypeToken<List<Task>>(){}.getType();
        String jsonResponse = gson.toJson(history, taskListType);

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

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();

            if ("GET".equals(method)) {
                List<Task> history = taskManager.getHistory();
                sendSuccess(h, gson.toJson(history));
            } else {
                sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendSuccess(exchange, gson.toJson(history));
    }
}

package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import http.HttpMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SubtaskByEpicHandler extends BaseHttpHandler {

    public SubtaskByEpicHandler(TaskManager taskManager) {
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
                if (Pattern.matches("^/epics/\\d+subtasks/$", path)) {
                    handleGetSubTasksByEpic(exchange, path);
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

    private void handleGetSubTasksByEpic(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        int epicId = parsePathId(pathParts[4]);

        Epic epic = taskManager.getEpic(epicId);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            ArrayList<SubTask> subtasks = taskManager.getEpicSubTasks(epic);
            sendSuccess(exchange, gson.toJson(subtasks));
        }
    }
}
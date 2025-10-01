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
    private static final String PATH_PATTERN = "^/epics/\\d+/subtasks/$";
    private static final int EPIC_ID_PATH_INDEX = 2;
    private static final String METHOD_GET = "GET";

    public SubtaskByEpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (METHOD_GET.equals(method)) {
                handleGetRequest(exchange, path);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (Pattern.matches(PATH_PATTERN, path)) {
            handleGetSubTasksByEpic(exchange, path);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetSubTasksByEpic(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        int epicId = parsePathId(pathParts[EPIC_ID_PATH_INDEX]);

        if (epicId == -1) {
            sendBadRequest(exchange);
            return;
        }

        Epic epic = taskManager.getEpic(epicId);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            ArrayList<SubTask> subtasks = taskManager.getEpicSubTasks(epic);
            sendSuccess(exchange, gson.toJson(subtasks));
        }
    }
}
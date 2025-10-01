package http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private static final String PATH_EPICS = "/epics";
    private static final String PATH_SUBTASKS = "/subtasks";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_DELETE = "DELETE";

    public EpicsHandler(TaskManager taskManager) {
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
        if (path.equals(PATH_EPICS)) {
            handleGetAllEpics(exchange);
        } else if (path.startsWith(PATH_EPICS + "/") && path.endsWith(PATH_SUBTASKS)) {
            handleGetEpicSubTasks(exchange, path);
        } else if (path.startsWith(PATH_EPICS + "/")) {
            handleGetEpicById(exchange, path);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals(PATH_EPICS)) {
            handlePostEpic(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith(PATH_EPICS + "/")) {
            handleDeleteEpicById(exchange, path);
        } else if (path.equals(PATH_EPICS)) {
            handleDeleteAllEpics(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getEpics();
        sendSuccess(exchange, gson.toJson(epics));
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(PATH_EPICS.length() + 1);
        int id = parsePathId(idStr);

        if (id == -1) {
            sendBadRequest(exchange);
            return;
        }

        Epic epic = taskManager.findEpicById(id);
        if (epic != null) {
            sendSuccess(exchange, gson.toJson(epic));
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicSubTasks(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(PATH_EPICS.length() + 1, path.length() - PATH_SUBTASKS.length());
        int epicId = parsePathId(idStr);

        if (epicId == -1) {
            sendBadRequest(exchange);
            return;
        }

        Epic epic = taskManager.findEpicById(epicId);
        if (epic != null) {
            List<SubTask> subTasks = taskManager.getSubTasksByEpicId(epicId);
            sendSuccess(exchange, gson.toJson(subTasks));
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                taskManager.addEpic(epic);
                sendCreated(exchange, gson.toJson(epic));
            } else {
                taskManager.updateEpic(epic);
                sendSuccess(exchange, gson.toJson(epic));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        }
    }

    private void handleDeleteEpicById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(PATH_EPICS.length() + 1);
        int id = parsePathId(idStr);

        if (id == -1) {
            sendBadRequest(exchange);
            return;
        }

        Epic epic = taskManager.findEpicById(id);
        if (epic != null) {
            taskManager.deleteEpic(id);
            sendSuccess(exchange, "Epic deleted");
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.deleteAllEpics();
        sendSuccess(exchange, "All epics deleted");
    }
}
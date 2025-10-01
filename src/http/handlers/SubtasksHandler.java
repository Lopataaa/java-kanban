package http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private static final String PATH_SUBTASKS = "/subtasks";
    private static final int SUBTASK_PATH_PREFIX_LENGTH = 10;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_DELETE = "DELETE";

    public SubtasksHandler(TaskManager taskManager) {
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
        if (path.equals(PATH_SUBTASKS)) {
            handleGetAllSubTasks(exchange);
        } else if (path.startsWith(PATH_SUBTASKS + "/")) {
            handleGetSubTaskById(exchange, path);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals(PATH_SUBTASKS)) {
            handlePostSubTask(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith(PATH_SUBTASKS + "/")) {
            handleDeleteSubTaskById(exchange, path);
        } else if (path.equals(PATH_SUBTASKS)) {
            handleDeleteAllSubTasks(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetAllSubTasks(HttpExchange exchange) throws IOException {
        List<SubTask> subTasks = taskManager.getSubTasks();
        sendSuccess(exchange, gson.toJson(subTasks));
    }

    private void handleGetSubTaskById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(SUBTASK_PATH_PREFIX_LENGTH);
        int id = parsePathId(idStr);

        if (id == -1) {
            sendBadRequest(exchange);
            return;
        }

        SubTask subTask = taskManager.findSubTaskById(id);
        if (subTask != null) {
            sendSuccess(exchange, gson.toJson(subTask));
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostSubTask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            SubTask subTask = gson.fromJson(body, SubTask.class);

            validateEpicExists(subTask);

            if (subTask.getId() == 0) {
                int newId = taskManager.addSubTask(subTask);
                subTask.setId(newId);
                sendCreated(exchange, gson.toJson(subTask));
            } else {
                taskManager.updateSubTask(subTask);
                sendSuccess(exchange, gson.toJson(subTask));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    private void validateEpicExists(SubTask subTask) {
        if (subTask.getEpicId() > 0) {
            Epic epic = taskManager.findEpicById(subTask.getEpicId());
            if (epic == null) {
                throw new IllegalArgumentException("Epic not found");
            }
        }
    }

    private void handleDeleteSubTaskById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring(SUBTASK_PATH_PREFIX_LENGTH);
        int id = parsePathId(idStr);

        if (id == -1) {
            sendBadRequest(exchange);
            return;
        }

        SubTask subTask = taskManager.findSubTaskById(id);
        if (subTask != null) {
            taskManager.deleteSubTask(id);
            sendSuccess(exchange, "SubTask deleted");
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteAllSubTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllSubtasks();
        sendSuccess(exchange, "All subtasks deleted");
    }
}
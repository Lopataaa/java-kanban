/*package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import http.HttpMethod;

import java.io.IOException;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
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

            switch (httpMethod) {
                case GET:
                    handleGetRequest(exchange, path, "/epics",
                            v -> taskManager.getEpics(),
                            taskManager::getEpic);
                    break;
                case POST:
                    handlePostRequest(exchange, Epic.class,
                            taskManager::addEpic,
                            taskManager::updateEpic,
                            epic -> epic.getId() == 0);
                    break;
                case DELETE:
                    handleDeleteRequest(exchange, path, "/epics",
                            taskManager::clearEpics,
                            taskManager::getEpic,
                            taskManager::deleteEpic);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

}*/

package http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
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
                    if (path.equals("/epics")) {
                        System.out.println("Handling GET /epics");
                        handleGetAllEpics(exchange);
                    } else if (path.startsWith("/epics/") && path.endsWith("/subtasks")) {
                        System.out.println("Handling GET /epics/{id}/subtasks");
                        handleGetEpicSubTasks(exchange, path);
                    } else if (path.startsWith("/epics/")) {
                        System.out.println("Handling GET /epics/{id}");
                        handleGetEpicById(exchange, path);
                    } else {
                        System.out.println("Path not found: " + path);
                        sendNotFound(exchange);
                    }
                    break;

                case "POST":
                    if (path.equals("/epics")) {
                        System.out.println("Handling POST /epics");
                        handlePostEpic(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;

                case "DELETE":
                    if (path.startsWith("/epics/")) {
                        System.out.println("Handling DELETE /epics/{id}");
                        handleDeleteEpicById(exchange, path);
                    } else if (path.equals("/epics")) {
                        System.out.println("Handling DELETE /epics");
                        handleDeleteAllEpics(exchange);
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

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        try {
            List<Epic> epics = taskManager.getEpics();
            System.out.println("Found " + epics.size() + " epics");
            String json = gson.toJson(epics);
            System.out.println("JSON response: " + json);
            sendSuccess(exchange, json);
        } catch (Exception e) {
            System.err.println("Error in handleGetAllEpics: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange);
        }
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(7); // "/epics/".length() = 7
            int id = Integer.parseInt(idStr);
            Epic epic = taskManager.findEpicById(id);
            if (epic != null) {
                sendSuccess(exchange, gson.toJson(epic));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid epic ID format");
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void sendBadRequest(HttpExchange exchange, String invalidEpicIdFormat) {
    }

    private void handleGetEpicSubTasks(HttpExchange exchange, String path) throws IOException {
        try {
            // Из пути "/epics/123/subtasks" извлекаем "123"
            String idStr = path.substring(7, path.length() - 9); // "/epics/".length() = 7, "/subtasks".length() = 9
            int epicId = Integer.parseInt(idStr);
            Epic epic = taskManager.findEpicById(epicId);
            if (epic != null) {
                List<SubTask> subTasks = taskManager.getSubTasksByEpicId(epicId);
                sendSuccess(exchange, gson.toJson(subTasks));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid epic ID format");
        } catch (Exception e) {
            sendInternalServerError(exchange);
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
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDeleteEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(7); // "/epics/".length() = 7
            int id = Integer.parseInt(idStr);
            Epic epic = taskManager.findEpicById(id);
            if (epic != null) {
                taskManager.deleteEpic(id);
                sendSuccess(exchange, "Epic deleted");
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid epic ID format");
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.deleteAllEpics();
        sendSuccess(exchange, "All epics deleted");
    }
}
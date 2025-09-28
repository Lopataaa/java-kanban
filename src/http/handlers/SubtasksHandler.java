/*package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import http.HttpMethod;
import task.SubTask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            HttpMethod httpMethod;

            try {
                httpMethod = HttpMethod.valueOf(h.getRequestMethod());
            } catch (IllegalArgumentException e) {
                sendNotFound(h);
                return;
            }

            switch (httpMethod) {
                case GET:
                    handleGetRequest(h, path, "/subtasks",
                            v -> taskManager.getSubTasks(),
                            taskManager::getSubTask);
                    break;
                case POST:
                    handlePostRequest(h, SubTask.class,
                            taskManager::addSubTask,
                            taskManager::updateSubTask,
                            subtask -> subtask.getId() == 0);
                    break;
                case DELETE:
                    handleDeleteRequest(h, path, "/subtasks",
                            taskManager::deleteAllSubtasks,
                            taskManager::getSubTask,
                            taskManager::deleteSubTask);
                    break;
                default:
                    sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
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

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
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
                    if (path.equals("/subtasks")) {
                        System.out.println("Handling GET /subtasks");
                        handleGetAllSubTasks(exchange);
                    } else if (path.startsWith("/subtasks/")) {
                        System.out.println("Handling GET /subtasks/{id}");
                        handleGetSubTaskById(exchange, path);
                    } else {
                        System.out.println("Path not found: " + path);
                        sendNotFound(exchange);
                    }
                    break;

                case "POST":
                    if (path.equals("/subtasks")) {
                        System.out.println("Handling POST /subtasks");
                        handlePostSubTask(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;

                case "DELETE":
                    if (path.startsWith("/subtasks/")) {
                        System.out.println("Handling DELETE /subtasks/{id}");
                        handleDeleteSubTaskById(exchange, path);
                    } else if (path.equals("/subtasks")) {
                        System.out.println("Handling DELETE /subtasks");
                        handleDeleteAllSubTasks(exchange);
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

    private void handleGetAllSubTasks(HttpExchange exchange) throws IOException {
        try {
            List<SubTask> subTasks = taskManager.getSubTasks();
            System.out.println("Total subtasks found: " + subTasks.size());
            for (SubTask subTask : subTasks) {
                System.out.println("Subtask ID: " + subTask.getId() + ", Name: " + subTask.getName());
            }
            sendSuccess(exchange, gson.toJson(subTasks));
        } catch (Exception e) {
            System.err.println("Error getting all subtasks: " + e.getMessage());
            sendInternalServerError(exchange);
        }
    }

    private void handleGetSubTaskById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(10);
            int id = Integer.parseInt(idStr);

            System.out.println("=== GET /subtasks/" + id + " ===");
            System.out.println("Looking for subtask with ID: " + id);

            // Показываем все доступные подзадачи
            List<SubTask> allSubTasks = taskManager.getSubTasks();
            System.out.println("Available subtasks (" + allSubTasks.size() + "):");
            for (SubTask st : allSubTasks) {
                System.out.println("  - ID: " + st.getId() + ", Name: " + st.getName());
            }

            SubTask subTask = taskManager.findSubTaskById(id);

            if (subTask != null) {
                System.out.println("✓ Subtask found: " + subTask.getName());
                String json = gson.toJson(subTask);
                System.out.println("Sending JSON: " + json);
                sendSuccess(exchange, json);
            } else {
                System.out.println("✗ Subtask not found with ID: " + id);
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid subtask ID format: " + e.getMessage());
            sendBadRequest(exchange, "Invalid subtask ID format");
        } catch (Exception e) {
            System.err.println("Error getting subtask by ID: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange);
        }
    }

    private void sendBadRequest(HttpExchange exchange, String invalidSubtaskIdFormat) {
    }

    private void handlePostSubTask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            System.out.println("Subtask JSON: " + body);

            SubTask subTask = gson.fromJson(body, SubTask.class);
            System.out.println("Parsed subtask - ID: " + subTask.getId() + ", Name: " + subTask.getName() + ", EpicId: " + subTask.getEpicId());

            // Проверяем существование эпика
            if (subTask.getEpicId() > 0) {
                System.out.println("Checking if epic exists: " + subTask.getEpicId());
                Epic epic = taskManager.findEpicById(subTask.getEpicId());
                System.out.println("Epic found: " + (epic != null));
            }

            if (subTask.getId() == 0) {
                System.out.println("Creating new subtask");
                int newId = taskManager.addSubTask(subTask); // Получаем новый ID
                System.out.println("Created subtask with new ID: " + newId);

                // Обновляем ID у объекта
                subTask.setId(newId);
                String responseJson = gson.toJson(subTask);
                System.out.println("Sending response: " + responseJson);
                sendCreated(exchange, responseJson);
            } else {
                System.out.println("Updating existing subtask");
                taskManager.updateSubTask(subTask);
                sendSuccess(exchange, gson.toJson(subTask));
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (IllegalStateException e) {
            System.err.println("Task overlap: " + e.getMessage());
            sendHasInteractions(exchange);
        } catch (Exception e) {
            System.err.println("Error creating subtask: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange);
        }
    }

    private void handleDeleteSubTaskById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(10);
            int id = Integer.parseInt(idStr);

            SubTask subTask = taskManager.findSubTaskById(id);
            if (subTask != null) {
                taskManager.deleteSubTask(id);
                sendSuccess(exchange, "SubTask deleted");
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid subtask ID format");
        }
    }

    private void handleDeleteAllSubTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllSubtasks();
        sendSuccess(exchange, "All subtasks deleted");
    }
}
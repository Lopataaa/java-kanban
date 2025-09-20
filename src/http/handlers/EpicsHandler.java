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

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import http.HttpMethod;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGetRequest(h, path, "/epics",
                            (Void) -> taskManager.getEpics(),
                            id -> taskManager.findEpicById(id));
                    break;
                case "POST":
                    handlePostRequest(h, Epic.class,
                            epic -> {
                                taskManager.addEpic(epic);
                                return epic;
                            },
                            epic -> {
                                taskManager.updateEpic(epic);
                                return epic;
                            },
                            epic -> epic.getId() == 0);
                    break;
                case "DELETE":
                    handleDeleteRequest(h, path, "/epics",
                            () -> taskManager.deleteAllEpics(),
                            id -> taskManager.findEpicById(id),
                            id -> taskManager.deleteEpic(id));
                    break;
                default:
                    sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }
}
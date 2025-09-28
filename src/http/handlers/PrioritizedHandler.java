package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;

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
            System.err.println("Error in getPrioritizedTasks: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange);
        }
    }
}
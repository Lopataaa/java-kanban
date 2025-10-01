package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private static final String METHOD_GET = "GET";

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (METHOD_GET.equals(exchange.getRequestMethod())) {
            handleGetPrioritized(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        sendSuccess(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
    }
}
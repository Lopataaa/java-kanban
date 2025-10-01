package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private static final String METHOD_GET = "GET";

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (METHOD_GET.equals(method)) {
            handleGetHistory(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendSuccess(exchange, gson.toJson(history));
    }
}

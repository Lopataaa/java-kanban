package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();

            if ("GET".equals(method)) {
                List<Task> history = taskManager.getHistory();
                sendSuccess(h, gson.toJson(history));
            } else {
                sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendSuccess(exchange, gson.toJson(history));
    }
}

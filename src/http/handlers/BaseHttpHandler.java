package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.Managers;
import manager.TaskManager;
import task.Task;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = Managers.getGson();
    }

    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException { // для отправки общего ответа в случае успеха
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendSuccess(HttpExchange h, String resp) throws IOException {
        sendText(h, resp, 200);
    }

    protected void sendCreated(HttpExchange h, String resp) throws IOException {
        sendText(h, resp, 201);
    }

    protected void sendNotFound(HttpExchange h) throws IOException { // для отправки ответа в случае, если объект не был найден
        sendText(h, "Not Found", 404);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException { // для отправки ответа, если при создании или обновлении задача пересекается с уже существующими
        sendText(h, "Not Acceptable", 406);
    }

    protected void sendInternalServerError(HttpExchange h) throws IOException {
        sendText(h, "Internal Server Error", 500);

    }

    protected void sendBadRequest(HttpExchange h) throws IOException {
        sendText(h, "Bad Request", 400);
    }

    protected String readRequestBody(HttpExchange h) throws IOException {
        try (InputStream inputStream = h.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected <T extends Task> void handleGetRequest(HttpExchange h, String path, String basePath,
                                                     Function<Void, List<T>> getAllFunction,
                                                     Function<Integer, T> getByIdFunction) throws IOException {
        if (Pattern.matches("^" + basePath + "\\d+$", path)) {
            List<T> entities = getAllFunction.apply(null);
            sendSuccess(h, gson.toJson(entities));

        } else if (Pattern.matches("^" + basePath + "\\d+$", path)) {
            String[] pathParts = path.split("/");
            int id = parsePathId(pathParts[pathParts.length - 1]);

            if (id == -1) {
                sendBadRequest(h);
                return;
            }

            T entity = getByIdFunction.apply(id);
            if (entity == null) {
                sendNotFound(h);
            } else {
                sendSuccess(h, gson.toJson(entity));
            }
        } else {
            sendNotFound(h);
        }
    }

    protected <T> void handlePostRequest(HttpExchange h, Class<T> entityClass,
                                         Function<T, T> addFunction,
                                         Function<T, T> updateFunction,
                                         Function<T, Boolean> isNewFunction) throws IOException {
        try {
            String body = readRequestBody(h);
            T entity = gson.fromJson(body, entityClass);

            T result;
            if (isNewFunction.apply(entity)) {
                result = addFunction.apply(entity);
                sendCreated(h, gson.toJson(result));
            } else {
                result = updateFunction.apply(entity);
                sendSuccess(h, gson.toJson(result));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(h);
        } catch (IllegalStateException e) {
            sendHasInteractions(h);
        }
    }

    protected <T> void handleDeleteRequest(HttpExchange h, String path, String basePath,
                                           Runnable clearAllFunction,
                                           Function<Integer, T> getByIdFunction,
                                           java.util.function.Consumer<Integer> deleteByIdFunction) throws IOException {
        try {
            if (Pattern.matches("^" + basePath + "\\d+$", path)) {
                clearAllFunction.run();
                sendSuccess(h, "All entities cleared");

            } else if (Pattern.matches("^" + basePath + "\\d+$", path)) {
                String[] pathParts = path.split("/");
                int id = parsePathId(pathParts[pathParts.length - 1]);

                if (id == -1) {
                    sendBadRequest(h);
                    return;
                }

                T entityBefore = getByIdFunction.apply(id);
                if (entityBefore == null) {
                    sendNotFound(h);
                    return;
                }

                deleteByIdFunction.accept(id);

                T entityAfter = getByIdFunction.apply(id);
                if (entityAfter == null) {
                    sendSuccess(h, "Entity deleted");
                } else {
                    sendInternalServerError(h);
                }

            } else {
                sendNotFound(h);
            }
        } catch (Exception e) {
            System.err.println("Error in handleDeleteRequest: " + e.getMessage());
            sendInternalServerError(h);
        }
    }

    public abstract void handle(HttpExchange h) throws IOException;
}
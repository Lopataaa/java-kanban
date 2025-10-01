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
    protected static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    protected static final String CONTENT_TYPE_TEXT = "text/plain";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";

    protected static final int STATUS_OK = 200;
    protected static final int STATUS_CREATED = 201;
    protected static final int STATUS_BAD_REQUEST = 400;
    protected static final int STATUS_NOT_FOUND = 404;
    protected static final int STATUS_NOT_ACCEPTABLE = 406;
    protected static final int STATUS_INTERNAL_ERROR = 500;

    protected static final String MSG_NOT_FOUND = "Not Found";
    protected static final String MSG_BAD_REQUEST = "Bad Request";
    protected static final String MSG_INTERNAL_ERROR = "Internal Server Error";
    protected static final String MSG_HAS_INTERACTIONS = "Not Acceptable";

    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = Managers.getGson();
    }

    protected void sendResponse(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, STATUS_OK);
    }

    protected void sendCreated(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, STATUS_CREATED);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, MSG_NOT_FOUND, STATUS_NOT_FOUND);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, MSG_HAS_INTERACTIONS, STATUS_NOT_ACCEPTABLE);
    }

    protected void sendInternalServerError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, MSG_INTERNAL_ERROR, STATUS_INTERNAL_ERROR);
    }

    protected void sendBadRequest(HttpExchange exchange) throws IOException {
        sendResponse(exchange, MSG_BAD_REQUEST, STATUS_BAD_REQUEST);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
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

    protected <T extends Task> void handleGetRequest(HttpExchange exchange, String path, String basePath,
                                                     Function<Void, List<T>> getAllFunction,
                                                     Function<Integer, T> getByIdFunction) throws IOException {
        if (Pattern.matches("^" + basePath + "$", path)) {
            List<T> entities = getAllFunction.apply(null);
            sendSuccess(exchange, gson.toJson(entities));
        } else if (Pattern.matches("^" + basePath + "\\d+$", path)) {
            String[] pathParts = path.split("/");
            int id = parsePathId(pathParts[pathParts.length - 1]);

            if (id == -1) {
                sendBadRequest(exchange);
                return;
            }

            T entity = getByIdFunction.apply(id);
            if (entity == null) {
                sendNotFound(exchange);
            } else {
                sendSuccess(exchange, gson.toJson(entity));
            }
        } else {
            sendNotFound(exchange);
        }
    }

    protected <T> void handlePostRequest(HttpExchange exchange, Class<T> entityClass,
                                         Function<T, T> addFunction,
                                         Function<T, T> updateFunction,
                                         Function<T, Boolean> isNewFunction) throws IOException {
        try {
            String body = readRequestBody(exchange);
            T entity = gson.fromJson(body, entityClass);

            T result;
            if (isNewFunction.apply(entity)) {
                result = addFunction.apply(entity);
                sendCreated(exchange, gson.toJson(result));
            } else {
                result = updateFunction.apply(entity);
                sendSuccess(exchange, gson.toJson(result));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    public abstract void handle(HttpExchange exchange) throws IOException;
}
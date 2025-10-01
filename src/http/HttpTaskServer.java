package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import http.handlers.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 0;
    private static final Gson GSON = Managers.getGson();

    private static final String PATH_TASKS = "/tasks";
    private static final String PATH_SUBTASKS = "/subtasks";
    private static final String PATH_EPICS = "/epics";
    private static final String PATH_HISTORY = "/history";
    private static final String PATH_PRIORITIZED = "/prioritized";

    private HttpServer server;
    private TaskManager taskManager;
    private final int actualPort;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault();
        this.server = HttpServer.create(new InetSocketAddress(SERVER_ADDRESS, PORT), 1);
        this.actualPort = this.server.getAddress().getPort();

        registerHandlers();
    }

    private void registerHandlers() {
        server.createContext(PATH_TASKS, new TasksHandler(taskManager));
        server.createContext(PATH_SUBTASKS, new SubtasksHandler(taskManager));
        server.createContext(PATH_EPICS, new EpicsHandler(taskManager));
        server.createContext(PATH_HISTORY, new HistoryHandler(taskManager));
        server.createContext(PATH_PRIORITIZED, new PrioritizedHandler(taskManager));
    }

    public int getPort() {
        return actualPort;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    public static Gson getGson() {
        return GSON;
    }
}
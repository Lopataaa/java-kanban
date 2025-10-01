package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import http.handlers.*;
import manager.Managers;
import manager.TaskManager;


import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private HttpServer server; // убрала final
    private TaskManager taskManager;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 0;
    private static final Gson gson = Managers.getGson();

    private final int actualPort;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault();
        this.server = HttpServer.create(new InetSocketAddress(SERVER_ADDRESS, PORT), 1);
        this.actualPort = this.server.getAddress().getPort(); // - запоминаем реальный порт

        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public int getPort() {
        return actualPort;
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started on " + server.getAddress());
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP Task Server stopped");
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
        return gson;
    }
}

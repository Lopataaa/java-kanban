package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.*;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

//Базовый абстрактный класс
public abstract class HttpTasksTest {

    // Поля — protected static, чтобы наследники имели доступ
    protected static HttpClient client;
    protected static Gson gson;
    protected static String BASE_URL;
    private static HttpTaskServer server;

    @BeforeAll
    static void setUp() throws IOException {
        gson = Managers.getGson();
        client = HttpClient.newHttpClient();
        server = new HttpTaskServer();
        server.start();
        BASE_URL = "http://localhost:" + server.getPort();
    }

    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void clearAllData() throws IOException, InterruptedException {
        // Очистка через HTTP — чтобы не зависеть от внутреннего состояния
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/tasks"))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/subtasks"))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/epics"))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
    }
}

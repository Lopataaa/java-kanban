package http;

import com.google.gson.Gson;
import manager.Managers;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class HttpTasksTest {
    private static final String PATH_TASKS = "/tasks";
    private static final String PATH_SUBTASKS = "/subtasks";
    private static final String PATH_EPICS = "/epics";

    protected static HttpClient client;
    protected static Gson gson = HttpTaskServer.getGson();
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

    @BeforeAll
    public static void setUpAll() throws IOException {
        server = new HttpTaskServer();
        server.start();

        int port = server.getPort();
        BASE_URL = "http://localhost:" + port;

        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void clearAllData() throws IOException, InterruptedException {
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_TASKS))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_SUBTASKS))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + PATH_EPICS))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
    }
}
package http;

import com.google.gson.Gson;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.http.HttpClient;

class HttpTaskServerTest {
    private static final String BASE_URL = "http://localhost:8080";

    private HttpTaskServer server;
    private TaskManager taskManager;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = Managers.getDefault();
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
        gson = Managers.getGson();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }
}
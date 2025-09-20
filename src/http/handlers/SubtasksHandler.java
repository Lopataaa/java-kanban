/*package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import http.HttpMethod;
import task.SubTask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            HttpMethod httpMethod;

            try {
                httpMethod = HttpMethod.valueOf(h.getRequestMethod());
            } catch (IllegalArgumentException e) {
                sendNotFound(h);
                return;
            }

            switch (httpMethod) {
                case GET:
                    handleGetRequest(h, path, "/subtasks",
                            v -> taskManager.getSubTasks(),
                            taskManager::getSubTask);
                    break;
                case POST:
                    handlePostRequest(h, SubTask.class,
                            taskManager::addSubTask,
                            taskManager::updateSubTask,
                            subtask -> subtask.getId() == 0);
                    break;
                case DELETE:
                    handleDeleteRequest(h, path, "/subtasks",
                            taskManager::deleteAllSubtasks,
                            taskManager::getSubTask,
                            taskManager::deleteSubTask);
                    break;
                default:
                    sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }
}*/

package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.SubTask;
import http.HttpMethod;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGetRequest(h, path, "/subtasks",
                            (Void) -> taskManager.getSubTasks(),
                            id -> taskManager.findSubTaskById(id));
                    break;
                case "POST":
                    handlePostRequest(h, SubTask.class,
                            subTask -> {
                                taskManager.addSubTask(subTask);
                                return subTask;
                            },
                            subTask -> {
                                taskManager.updateSubTask(subTask);
                                return subTask;
                            },
                            subTask -> subTask.getId() == 0);
                    break;
                case "DELETE":
                    handleDeleteRequest(h, path, "/subtasks",
                            () -> taskManager.deleteAllSubtasks(),
                            id -> taskManager.findTaskById(id),
                            id -> taskManager.deleteSubTask());
                    break;
                default:
                    sendNotFound(h);
            }
        } catch (Exception e) {
            sendInternalServerError(h);
        }
    }
}
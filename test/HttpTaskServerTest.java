import com.google.gson.*;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import server.HttpTaskServer;
import server.handler.BaseHttpHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HttpTaskServerTest{
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson = BaseHttpHandler.gson;

    protected InMemoryTaskManager createManager() {
        return (InMemoryTaskManager) Managers.getDefault();
    }

    @BeforeEach
    void setUp() {
        manager = createManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson = gson.toJson(task);
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Wrong http status code");

        List<Task> tasks = manager.getListOfTasks();
        assertFalse(tasks.isEmpty(), "Tasks in manager is empty");
        assertEquals("Test 2", task.getTitle());
    }

    @Test
    public void shouldCreateEpic() throws IOException, InterruptedException {
        Epic task = new Epic("Test 2", "Testing task 2");

        String taskJson = gson.toJson(task);
        URI uri = URI.create("http://localhost:8080/epics");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Wrong http status code");

        List<Epic> tasks = manager.getListOfEpics();
        assertFalse(tasks.isEmpty(), "Tasks in manager is empty");
        assertEquals("Test 2", task.getTitle());
    }

    @Test
    public void shouldCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "epic");
        manager.createEpic(epic);
        Subtask task = new Subtask("Test 2", "Testing task 2",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson = gson.toJson(task);
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Wrong http status code");

        List<Subtask> tasks = manager.getListOfSubtasks();
        assertFalse(tasks.isEmpty(), "Tasks in manager is empty");
        assertEquals("Test 2", task.getTitle());
    }

    @Test
    public void shouldNotCreateOverlappingTask() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Epic epic = new Epic("Epic", "epic");
        manager.createEpic(epic);
        Subtask task2 = new Subtask("Test 2", "Testing task 2",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson = gson.toJson(task);
        String taskJson1 = gson.toJson(task1);
        String taskJson2 = gson.toJson(task2);

        URI uri = URI.create("http://localhost:8080/tasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request1 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(uri)
                .build();
        HttpRequest request2 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .uri(uri)
                .build();
        HttpRequest request3 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .uri(uri)
                .build();

        HttpResponse<String> response1 = httpClient.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = httpClient.send(request2, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response3 = httpClient.send(request3, HttpResponse.BodyHandlers.ofString());


        assertEquals(406, response2.statusCode());
        assertEquals(406, response3.statusCode());
    }

    @Test
    public void shouldReturnAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now().plusMinutes(10), Duration.ofMinutes(5));

        manager.createTask(task);
        manager.createTask(task1);

        URI uri = URI.create("http://localhost:8080/tasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong http status code");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "Response is not a JSON array");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Number of tasks in response is not 2");

        List<Task> tasks = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            assertTrue(element.isJsonObject(), "Element is not a JSON object");
            JsonObject taskJson = element.getAsJsonObject();
            Task tempTask = gson.fromJson(taskJson, Task.class);
            tasks.add(tempTask);
        }
        assertEquals(tasks, manager.getListOfTasks());
    }

    @Test
    public void shouldReturnAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "First epic");
        Epic epic2 = new Epic("Epic 2", "Second epic");

        manager.createEpic(epic1);
        manager.createEpic(epic2);

        URI uri = URI.create("http://localhost:8080/epics");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong http status code for epics");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "Response is not a JSON array");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Number of epics in response is not 2");

        List<Epic> epics = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            assertTrue(element.isJsonObject(), "Element is not a JSON object");
            JsonObject epicJson = element.getAsJsonObject();
            Epic tempEpic = gson.fromJson(epicJson, Epic.class);
            epics.add(tempEpic);
        }
        assertEquals(epics, manager.getListOfEpics());
    }


    @Test
    public void shouldReturnAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic for subtasks", "Epic desc");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask sub1 = new Subtask("Subtask 1", "First subtask", TaskStatus.NEW, epicId,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask sub2 = new Subtask("Subtask 2", "Second subtask", TaskStatus.IN_PROGRESS, epicId,
                LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(30));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong http status code for subtasks");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "Response is not a JSON array");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Number of subtasks in response is not 2");

        List<Subtask> subtasks = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            assertTrue(element.isJsonObject(), "Element is not a JSON object");
            JsonObject subtaskJson = element.getAsJsonObject();
            Subtask tempSubtask = gson.fromJson(subtaskJson, Subtask.class);
            subtasks.add(tempSubtask);
        }
        assertEquals(subtasks, manager.getListOfSubtasks());
    }

    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createTask(task);
        int taskId = task.getId();

        URI uri = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong status code for Task by ID");

        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(manager.getTaskById(taskId), receivedTask);
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        URI uri = URI.create("http://localhost:8080/epics/" + epicId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong status code for Epic by ID");

        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(manager.getEpicById(epicId), receivedEpic);
    }

    @Test
    public void shouldReturnSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic for Subtask", "Epic Desc");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Test Subtask", "Subtask Desc",
                TaskStatus.DONE, epicId, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(45));
        manager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        URI uri = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong status code for Subtask by ID");

        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(manager.getSubtaskById(subtaskId), receivedSubtask);
    }

    @Test
    public void shouldReturn404ForNonExistingTask() throws IOException, InterruptedException {
        int nonExistentId = 9999;

        URI uri = URI.create("http://localhost:8080/tasks/" + nonExistentId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 for non-existent Task ID");
    }


    @Test
    public void shouldReturn404ForNonExistingEpic() throws IOException, InterruptedException {
        int nonExistentId = 8888;

        URI uri = URI.create("http://localhost:8080/epics/" + nonExistentId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 for non-existent Epic ID");
    }


    @Test
    public void shouldReturn404ForNonExistingSubtask() throws IOException, InterruptedException {
        int nonExistentId = 7777;

        URI uri = URI.create("http://localhost:8080/subtasks/" + nonExistentId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 for non-existent Subtask ID");
    }

    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Old Task", "Old Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20));
        manager.createTask(task);
        int taskId = task.getId();

        Task updatedTask = new Task("Updated Task", "New Description", TaskStatus.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(25));
        updatedTask.setId(taskId); // Обязательно указываем ID, иначе создастся новый task

        String json = gson.toJson(updatedTask);

        URI uri = URI.create("http://localhost:8080/tasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Update Task: wrong status code");

        Task result = manager.getTaskById(taskId);
        assertEquals(updatedTask, result);
    }

    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Original Epic", "Original Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Epic updatedEpic = new Epic("Updated Epic", "Updated Description");
        updatedEpic.setId(epicId);

        String json = gson.toJson(updatedEpic);

        URI uri = URI.create("http://localhost:8080/epics");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Update Epic: wrong status code");

        Epic result = manager.getEpicById(epicId);
        assertEquals(updatedEpic, result);
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic for subtask", "Desc");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Subtask 1", "Initial", TaskStatus.NEW,
                epicId, LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        Subtask updatedSubtask = new Subtask("Subtask 1 - Updated", "Changed desc",
                TaskStatus.IN_PROGRESS, epicId, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(45));
        updatedSubtask.setId(subtaskId); // обязательно!

        String json = gson.toJson(updatedSubtask);

        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Update Subtask: wrong status code");

        Subtask result = manager.getSubtaskById(subtaskId);
        assertEquals(updatedSubtask, result);
    }

    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task to delete", "Will be deleted", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);
        int taskId = task.getId();

        URI uri = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Failed to delete Task");

        assertNull(manager.getTaskById(taskId), "Task still exists after deletion");
    }

    @Test
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic to delete", "This one dies");
        manager.createEpic(epic);
        int epicId = epic.getId();

        URI uri = URI.create("http://localhost:8080/epics/" + epicId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Failed to delete Epic");

        assertNull(manager.getEpicById(epicId), "Epic still exists after deletion");
        assertTrue(manager.getListOfSubtasks().isEmpty(), "Subtasks of Epic were not deleted");
    }

    @Test
    public void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "For subtask deletion");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Subtask to delete", "Bye bye", TaskStatus.NEW,
                epicId, LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        URI uri = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Failed to delete Subtask");

        assertNull(manager.getSubtaskById(subtaskId), "Subtask still exists after deletion");
    }

    @Test
    public void shouldReturnSubtasksOfEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Main Epic", "Contains subtasks");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask sub1 = new Subtask("Subtask 1", "First", TaskStatus.NEW,
                epicId, LocalDateTime.now(), Duration.ofMinutes(15));
        Subtask sub2 = new Subtask("Subtask 2", "Second", TaskStatus.IN_PROGRESS,
                epicId, LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(20));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        URI uri = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong status code when getting subtasks of epic");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "Response is not a JSON array");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Expected 2 subtasks for this epic");

        List<Subtask> receivedSubtasks = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            Subtask subtask = gson.fromJson(element, Subtask.class);
            receivedSubtasks.add(subtask);
        }

        List<Subtask> expected = manager.getSubtasksOfEpic(epicId);
        assertEquals(expected, receivedSubtasks, "Subtasks returned by server don't match manager");
    }

    @Test
    public void shouldReturn404IfEpicNotFoundWhenGettingSubtasks() throws IOException, InterruptedException {
        int nonExistentEpicId = 123456;

        URI uri = URI.create("http://localhost:8080/epics/" + nonExistentEpicId + "/subtasks");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 for non-existent epic");
    }

    @Test
    public void shouldReturnHistoryOfViewedTasks() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Epic epic = new Epic("Epic 1", "Epic Desc");
        manager.createTask(task);
        manager.createEpic(epic);
        int taskId = task.getId();
        int epicId = epic.getId();

        // Симулируем просмотр
        manager.getTaskById(taskId);
        manager.getEpicById(epicId);

        URI uri = URI.create("http://localhost:8080/history");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong status code for /history");

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Expected 2 tasks in history");
    }

    @Test
    public void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task Early", "Starts first", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(10), Duration.ofMinutes(30));
        Task task2 = new Task("Task Later", "Starts second", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(30));

        manager.createTask(task2); // создаем task2 раньше, чтобы проверить, что сортировка — не по ID
        manager.createTask(task1);

        URI uri = URI.create("http://localhost:8080/prioritized");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Wrong status code for /prioritized");

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        assertTrue(jsonArray.size() >= 2, "Expected at least 2 tasks in prioritized list");

        List<Task> prioritized = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            Task task = gson.fromJson(element, Task.class);
            prioritized.add(task);
        }

        // Проверяем, что список отсортирован по startTime
        for (int i = 0; i < prioritized.size() - 1; i++) {
            LocalDateTime time1 = prioritized.get(i).getStartTime();
            LocalDateTime time2 = prioritized.get(i + 1).getStartTime();
            if (time1 != null && time2 != null) {
                assertFalse(time1.isAfter(time2), "Tasks are not sorted by start time");
            }
        }
    }


}

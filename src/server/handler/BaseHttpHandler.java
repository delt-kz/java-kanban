package server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Subtask;
import model.Task;
import utils.DurationAdapter;
import utils.LocalDateTimeAdapter;
import utils.TaskAccess;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseHttpHandler<T extends Task> implements HttpHandler {
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
            .create();
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected TaskAccess<T> access;

    public BaseHttpHandler(TaskAccess<T> access) {
        this.access = access;
    }

    public void handle(HttpExchange exchange) {
        try {
            handleSafe(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ex) {
                e.printStackTrace();
            }
        } finally {
            exchange.close();
        }
    }

    public void handleSafe(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");
        if (path.length == 3) {
            handleWithId(exchange);
        } else if (path.length == 4) {
            int id;
            if ("subtasks".equals(path[3])) {
                try {
                    id = Integer.parseInt(path[2]);
                    List<Subtask> subtasksOfEpic = access.getSubtasksOfEpic(id);
                    if (subtasksOfEpic == null) {
                        exchange.sendResponseHeaders(404, -1);
                        return;
                    }
                    sendText(exchange, gson.toJson(subtasksOfEpic));
                } catch (NumberFormatException e) {
                    exchange.sendResponseHeaders(400, -1);
                }
            }
        } else {
            handleWithNoId(exchange);
        }
    }

    public void handleWithId(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");
        String method = exchange.getRequestMethod();
        int id;
        try {
            id = Integer.parseInt(path[2]);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        switch (method) {
            case "GET" -> {
                T task = access.getById(id);
                if (task != null) {
                    sendText(exchange, gson.toJson(task));
                } else {
                    sendNotFound(exchange);
                }
            }
            case "DELETE" -> {
                access.delete(id);
                sendText(exchange, "");
            }
            default -> exchange.sendResponseHeaders(405, -1);
        }
    }

    public void handleWithNoId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET" -> sendText(exchange, gson.toJson(access.getAll()));
            case "POST" -> {
                String json = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                T task = gson.fromJson(json, getTypeToken());
                int id = task.getId();
                if (id != 0) {
                    task.setId(id);
                    try {
                        access.update(task);
                    } catch (IllegalStateException e) {
                        exchange.sendResponseHeaders(406, -1);
                    }
                    exchange.sendResponseHeaders(201, -1);
                    return;
                }
                try {
                    access.create(task);
                    exchange.sendResponseHeaders(201, -1);
                } catch (IllegalStateException e) {
                    sendHasIntersections(exchange);
                }
            }
            default -> exchange.sendResponseHeaders(405, -1);
        }
    }


    public void sendText(HttpExchange exchange, String responseText) throws IOException {
        byte[] resp = responseText.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }

    public void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }

    public void sendHasIntersections(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, -1);
    }

    protected abstract Type getTypeToken();
}

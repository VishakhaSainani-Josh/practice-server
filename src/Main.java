import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final List<String> items = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Health endpoint
        server.createContext("/health", exchange -> {
            sendResponse(exchange, 200, "OK - working");
        });

        // Items endpoint
        server.createContext("/items", new ItemsHandler());

        server.setExecutor(null);
        System.out.println("Java server running on port 8080...");
        server.start();
    }

    static class ItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGetItems(exchange);
                    break;
                case "POST":
                    handlePostItems(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleGetItems(HttpExchange exchange) throws IOException {
            String json = items.toString();
            sendResponse(exchange, 200, json);
        }

        private void handlePostItems(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body.isEmpty()) {
                sendResponse(exchange, 400, "Invalid input");
                return;
            }
            items.add(body);
            sendResponse(exchange, 201, "Item added");
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseText.getBytes());
        os.close();
    }
}

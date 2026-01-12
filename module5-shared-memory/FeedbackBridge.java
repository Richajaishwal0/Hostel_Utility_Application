import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;

public class FeedbackBridge {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8084), 0);
        
        server.createContext("/feedback", new FeedbackHandler());
        server.createContext("/status", new StatusHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Feedback Bridge started on port 8084");
    }
    
    static class FeedbackHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                String[] parts = body.split("=");
                if (parts.length == 2 && "type".equals(parts[0])) {
                    String feedbackType = parts[1];
                    
                    try {
                        // Call Java shared memory directly
                        FeedbackServer.submitFeedback(feedbackType);
                        
                        String response = "{\"status\":\"success\",\"message\":\"Feedback submitted\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                        exchange.sendResponseHeaders(200, response.length());
                        exchange.getResponseBody().write(response.getBytes());
                    } catch (Exception e) {
                        String response = "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
                        exchange.sendResponseHeaders(500, response.length());
                        exchange.getResponseBody().write(response.getBytes());
                    }
                }
            }
            exchange.getResponseBody().close();
        }
    }
    
    static class StatusHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Get status from Java shared memory
                String response = FeedbackServer.getStatus();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } catch (Exception e) {
                String response = "{\"good\":0,\"average\":0,\"poor\":0,\"total\":0}";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }
}
import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.*;

public class SharedMemoryFeedbackServer {
    // Shared memory simulation using static variables
    private static final AtomicInteger goodCount = new AtomicInteger(0);
    private static final AtomicInteger averageCount = new AtomicInteger(0);
    private static final AtomicInteger poorCount = new AtomicInteger(0);
    private static final AtomicInteger totalCount = new AtomicInteger(0);
    
    // Semaphore simulation using ReentrantLock
    private static final ReentrantLock lock = new ReentrantLock();
    
    public static void main(String[] args) throws IOException {
        // Start HTTP server for web interface
        HttpServer server = HttpServer.create(new InetSocketAddress(8084), 0);
        server.createContext("/feedback", new FeedbackHandler());
        server.createContext("/status", new StatusHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Shared Memory Feedback Server started on port 8084");
        System.out.println("Shared memory initialized - All counters reset to 0");
        
        // Display status periodically
        while (true) {
            try {
                Thread.sleep(10000);
                displayStatus();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    public static void submitFeedback(String type) {
        lock.lock();
        try {
            switch (type.toLowerCase()) {
                case "good":
                    goodCount.incrementAndGet();
                    break;
                case "average":
                    averageCount.incrementAndGet();
                    break;
                case "poor":
                    poorCount.incrementAndGet();
                    break;
                default:
                    System.out.println("Invalid feedback type: " + type);
                    return;
            }
            totalCount.incrementAndGet();
            System.out.println(type + " feedback submitted! Total: " + totalCount.get());
        } finally {
            lock.unlock();
        }
    }
    
    public static String getStatus() {
        lock.lock();
        try {
            return String.format("{\"good\":%d,\"average\":%d,\"poor\":%d,\"total\":%d}",
                    goodCount.get(), averageCount.get(), poorCount.get(), totalCount.get());
        } finally {
            lock.unlock();
        }
    }
    
    private static void displayStatus() {
        lock.lock();
        try {
            System.out.printf("Current Feedback - Good: %d, Average: %d, Poor: %d, Total: %d%n",
                    goodCount.get(), averageCount.get(), poorCount.get(), totalCount.get());
        } finally {
            lock.unlock();
        }
    }
    
    static class FeedbackHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Handle CORS
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                String[] parts = body.split("=");
                if (parts.length == 2 && "type".equals(parts[0])) {
                    String feedbackType = parts[1];
                    
                    try {
                        submitFeedback(feedbackType);
                        String response = "{\"status\":\"success\",\"message\":\"Feedback submitted\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
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
            // Handle CORS
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            try {
                String response = getStatus();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
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
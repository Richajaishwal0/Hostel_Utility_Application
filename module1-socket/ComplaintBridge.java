import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

public class ComplaintBridge {
    private ComplaintWebClient socketClient;
    
    public ComplaintBridge() {
        this.socketClient = new ComplaintWebClient();
    }
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        ComplaintBridge bridge = new ComplaintBridge();
        
        server.createContext("/complaint", bridge.new ComplaintHandler());
        server.createContext("/complaints", bridge.new ComplaintListHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Complaint Bridge Server started on port 8082");
        System.out.println("Bridging HTTP requests to Socket Server on port 8080");
    }
    
    class ComplaintHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read request body
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                BufferedReader br = new BufferedReader(isr);
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    body.append(line);
                }
                
                // Parse JSON-like data (simple parsing)
                String bodyStr = body.toString();
                String room = extractValue(bodyStr, "room");
                String category = extractValue(bodyStr, "category");
                String description = extractValue(bodyStr, "description");
                
                // Call socket client
                String result = socketClient.submitComplaint(room, category, description);
                
                // Send response
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, result.length());
                OutputStream os = exchange.getResponseBody();
                os.write(result.getBytes());
                os.close();
            }
        }
        
        private String extractValue(String json, String key) {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return "";
            return json.substring(start, end);
        }
    }
    
    class ComplaintListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            if ("GET".equals(exchange.getRequestMethod())) {
                String result = socketClient.getComplaints();
                
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, result.length());
                OutputStream os = exchange.getResponseBody();
                os.write(result.getBytes());
                os.close();
            }
        }
    }
}
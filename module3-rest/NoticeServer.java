import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

public class NoticeServer {
    private static Map<Integer, Notice> notices = new ConcurrentHashMap<>();
    private static int noticeCounter = 1;
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        
        server.createContext("/notices", new NoticeHandler());
        server.createContext("/admin", new AdminHandler());
        server.createContext("/", new CorsHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Notice Board REST Server started on port 8081");
    }
    
    static class NoticeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder response = new StringBuilder("[");
                for (Notice notice : notices.values()) {
                    response.append(notice.toJson()).append(",");
                }
                if (response.length() > 1) response.setLength(response.length() - 1);
                response.append("]");
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            }
        }
    }
    
    static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                BufferedReader br = new BufferedReader(isr);
                String body = br.readLine();
                
                String[] parts = body.split("&");
                String title = URLDecoder.decode(parts[0].split("=")[1], "UTF-8");
                String message = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");
                
                Notice notice = new Notice(noticeCounter++, title, message);
                notices.put(notice.getId(), notice);
                
                String response = "Notice added successfully";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    
    static class CorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }
    
    static class Notice {
        private int id;
        private String title;
        private String message;
        private Date date;
        
        public Notice(int id, String title, String message) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.date = new Date();
        }
        
        public int getId() { return id; }
        
        public String toJson() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("{\"id\":%d,\"title\":\"%s\",\"message\":\"%s\",\"date\":\"%s\"}", 
                id, title, message, sdf.format(date));
        }
    }
}
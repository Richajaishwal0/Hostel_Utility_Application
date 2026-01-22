import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RoomInfoBridge {
    private RoomInfoService service;
    
    public RoomInfoBridge() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (RoomInfoService) registry.lookup("RoomInfoService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {
        RoomInfoBridge bridge = new RoomInfoBridge();
        HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);
        
        server.createContext("/room", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.contains("number=")) {
                String error = "{\"error\":\"Missing room number\"}";
                exchange.sendResponseHeaders(400, error.length());
                exchange.getResponseBody().write(error.getBytes());
                exchange.getResponseBody().close();
                return;
            }
            
            String roomNumber = query.split("=")[1];
            
            try {
                RoomInfo room = bridge.service.getRoomInfo(roomNumber);
                String response = room != null ? 
                    String.format("{\"room\":\"%s\",\"occupants\":%s,\"warden\":\"%s\",\"contact\":\"%s\"}", 
                        room.roomNumber, 
                        "[\"" + String.join("\",\"", room.occupants) + "\"]",
                        room.wardenName, room.wardenContact) :
                    "{\"error\":\"Room not found\"}";
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } catch (Exception e) {
                String error = "{\"error\":\"RMI call failed: " + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            }
            exchange.getResponseBody().close();
        });
        
        server.createContext("/rooms", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = bridge.service.getAllRooms().toString();
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } catch (Exception e) {
                String error = "[]";
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            }
            exchange.getResponseBody().close();
        });
        
        server.start();
        System.out.println("RMI Bridge Server started on port 8083");
    }
}
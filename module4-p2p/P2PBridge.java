import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2PBridge {
    private P2PPeer localPeer;
    private static final int BRIDGE_PORT = 8084;
    
    public P2PBridge() {
        // Create a local peer for the bridge
        localPeer = new P2PPeer(9999); // Use a dedicated port for bridge
        new Thread(localPeer::start).start();
        
        // Wait for peer to start and discover network
        try { 
            Thread.sleep(3000); // Give more time for peer discovery
        } catch (InterruptedException e) {}
    }
    
    public static void main(String[] args) throws IOException {
        P2PBridge bridge = new P2PBridge();
        HttpServer server = HttpServer.create(new InetSocketAddress(BRIDGE_PORT), 0);
        
        server.createContext("/p2p/upload", bridge::handleUpload);
        server.createContext("/p2p/search", bridge::handleSearch);
        server.createContext("/p2p/download", bridge::handleDownload);
        server.createContext("/p2p/files", bridge::handleListFiles);
        server.createContext("/p2p/peers", bridge::handleListPeers);
        
        server.setExecutor(null);
        server.start();
        System.out.println("P2P Bridge Server started on port " + BRIDGE_PORT);
    }
    
    private void handleUpload(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            
            if (contentType != null && contentType.contains("multipart/form-data")) {
                // Extract boundary
                String boundary = "--" + contentType.split("boundary=")[1];
                
                // Read all data as bytes
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream inputStream = exchange.getRequestBody();
                byte[] data = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                
                byte[] fullContent = buffer.toByteArray();
                String boundaryStr = boundary + "\r\n";
                String endBoundaryStr = boundary + "--";
                
                // Find the start and end of file content
                int startIndex = findBytes(fullContent, boundaryStr.getBytes());
                if (startIndex == -1) {
                    sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Boundary not found\"}");
                    return;
                }
                
                // Find filename in headers
                String fileName = "uploaded_file.txt";
                int headerEnd = findBytes(fullContent, "\r\n\r\n".getBytes(), startIndex);
                if (headerEnd != -1) {
                    String headers = new String(fullContent, startIndex, headerEnd - startIndex);
                    if (headers.contains("filename=")) {
                        int fnStart = headers.indexOf("filename=\"") + 10;
                        int fnEnd = headers.indexOf("\"", fnStart);
                        if (fnStart > 9 && fnEnd > fnStart) {
                            fileName = headers.substring(fnStart, fnEnd);
                        }
                    }
                    
                    // Extract file content
                    int contentStart = headerEnd + 4; // Skip \r\n\r\n
                    int endIndex = findBytes(fullContent, ("\r\n" + boundary).getBytes(), contentStart);
                    if (endIndex == -1) {
                        endIndex = fullContent.length;
                    }
                    
                    if (endIndex > contentStart) {
                        byte[] fileContent = new byte[endIndex - contentStart];
                        System.arraycopy(fullContent, contentStart, fileContent, 0, fileContent.length);
                        
                        boolean success = localPeer.uploadFile(fileName, fileContent);
                        String response = String.format(
                            "{\"success\": %b, \"message\": \"%s\"}",
                            success, success ? "File uploaded successfully (" + fileContent.length + " bytes)" : "Upload failed"
                        );
                        sendJsonResponse(exchange, 200, response);
                        return;
                    }
                }
                
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Could not parse file content\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid content type\"}");
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
    
    private int findBytes(byte[] source, byte[] target) {
        return findBytes(source, target, 0);
    }
    
    private int findBytes(byte[] source, byte[] target, int startIndex) {
        for (int i = startIndex; i <= source.length - target.length; i++) {
            boolean found = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }
    
    private void handleSearch(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String fileName = "";
            
            if (query != null && query.contains("file=")) {
                fileName = URLDecoder.decode(query.split("file=")[1].split("&")[0], "UTF-8");
            }
            
            List<P2PPeer.PeerFileInfo> results = localPeer.searchFile(fileName);
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < results.size(); i++) {
                P2PPeer.PeerFileInfo info = results.get(i);
                json.append(String.format(
                    "{\"fileName\":\"%s\",\"peerAddress\":\"%s\",\"fileSize\":%d,\"isLocal\":%b}",
                    info.fileName, info.peerAddress, info.fileSize, info.isLocal
                ));
                if (i < results.size() - 1) json.append(",");
            }
            
            json.append("]");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }
    
    private void handleDownload(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            if (query == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            
            String fileName = "";
            String peerAddress = "";
            
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("fileName".equals(keyValue[0])) {
                        fileName = URLDecoder.decode(keyValue[1], "UTF-8");
                    } else if ("peerAddress".equals(keyValue[0])) {
                        peerAddress = URLDecoder.decode(keyValue[1], "UTF-8");
                    }
                }
            }
            
            if (fileName.isEmpty() || peerAddress.isEmpty()) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            
            // Download file from peer
            boolean success = localPeer.downloadFile(fileName, peerAddress);
            
            if (success) {
                // Serve the downloaded file
                String filePath = "downloads/" + fileName;
                File file = new File(filePath);
                
                if (file.exists()) {
                    exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                    exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                    exchange.sendResponseHeaders(200, file.length());
                    
                    try (FileInputStream fis = new FileInputStream(file);
                         OutputStream os = exchange.getResponseBody()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } else {
                exchange.sendResponseHeaders(500, -1);
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
    
    private void handleListFiles(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<P2PPeer.PeerFileInfo> allFiles = localPeer.listAllFiles();
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < allFiles.size(); i++) {
                P2PPeer.PeerFileInfo info = allFiles.get(i);
                json.append(String.format(
                    "{\"fileName\":\"%s\",\"peerAddress\":\"%s\",\"fileSize\":%d,\"isLocal\":%b}",
                    info.fileName, info.peerAddress, info.fileSize, info.isLocal
                ));
                if (i < allFiles.size() - 1) json.append(",");
            }
            
            json.append("]");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }
    
    private void handleListPeers(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("GET".equals(exchange.getRequestMethod())) {
            Set<String> peers = localPeer.getKnownPeers();
            StringBuilder json = new StringBuilder("[");
            
            int i = 0;
            for (String peer : peers) {
                json.append("\"").append(peer).append("\"");
                if (i < peers.size() - 1) json.append(",");
                i++;
            }
            
            json.append("]");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }
    
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.length());
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}
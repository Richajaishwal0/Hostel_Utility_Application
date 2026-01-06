import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class P2PPeer {
    private int port;
    private Map<String, String> localFiles;
    private Set<String> knownPeers;
    private ServerSocket serverSocket;
    
    public P2PPeer(int port) {
        this.port = port;
        this.localFiles = new ConcurrentHashMap<>();
        this.knownPeers = ConcurrentHashMap.newKeySet();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("P2P Peer started on port " + port);
            
            ExecutorService executor = Executors.newFixedThreadPool(5);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new PeerHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addFile(String fileName, String filePath) {
        localFiles.put(fileName, filePath);
        System.out.println("Added file: " + fileName);
    }
    
    public void addPeer(String peerAddress) {
        knownPeers.add(peerAddress);
    }
    
    public List<String> searchFile(String fileName) {
        List<String> results = new ArrayList<>();
        
        // Check local files
        if (localFiles.containsKey(fileName)) {
            results.add("localhost:" + port);
        }
        
        // Query known peers
        for (String peer : knownPeers) {
            try {
                String[] parts = peer.split(":");
                Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                out.println("SEARCH|" + fileName);
                String response = in.readLine();
                if ("FOUND".equals(response)) {
                    results.add(peer);
                }
                
                socket.close();
            } catch (IOException e) {
                // Peer unavailable
            }
        }
        
        return results;
    }
    
    public boolean downloadFile(String fileName, String peerAddress) {
        try {
            String[] parts = peerAddress.split(":");
            Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println("DOWNLOAD|" + fileName);
            String response = in.readLine();
            
            if (response.startsWith("FILE|")) {
                String content = response.substring(5);
                // Save file locally
                try (FileWriter writer = new FileWriter("downloads/" + fileName)) {
                    writer.write(content);
                }
                addFile(fileName, "downloads/" + fileName);
                socket.close();
                return true;
            }
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    class PeerHandler implements Runnable {
        private Socket socket;
        
        public PeerHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                String request = in.readLine();
                String[] parts = request.split("\\|");
                
                if ("SEARCH".equals(parts[0])) {
                    String fileName = parts[1];
                    if (localFiles.containsKey(fileName)) {
                        out.println("FOUND");
                    } else {
                        out.println("NOT_FOUND");
                    }
                } else if ("DOWNLOAD".equals(parts[0])) {
                    String fileName = parts[1];
                    if (localFiles.containsKey(fileName)) {
                        try {
                            String content = new String(java.nio.file.Files.readAllBytes(
                                java.nio.file.Paths.get(localFiles.get(fileName))));
                            out.println("FILE|" + content);
                        } catch (IOException e) {
                            out.println("ERROR");
                        }
                    } else {
                        out.println("NOT_FOUND");
                    }
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java P2PPeer <port>");
            return;
        }
        
        P2PPeer peer = new P2PPeer(Integer.parseInt(args[0]));
        
        // Create downloads directory
        new File("downloads").mkdirs();
        
        // Start peer in background
        new Thread(peer::start).start();
        
        // Add some sample files
        peer.addFile("notes.txt", "sample_notes.txt");
        peer.addFile("assignment.pdf", "sample_assignment.pdf");
    }
}
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

public class P2PPeer {
    private int port;
    private Map<String, FileInfo> localFiles;
    private Set<String> knownPeers;
    private ServerSocket serverSocket;
    private boolean running = true;
    
    public P2PPeer(int port) {
        this.port = port;
        this.localFiles = new ConcurrentHashMap<>();
        this.knownPeers = ConcurrentHashMap.newKeySet();
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        new File("shared").mkdirs();
        new File("downloads").mkdirs();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("🌐 P2P Peer started on port " + port);
            System.out.println("📁 Shared folder: ./shared/");
            System.out.println("📥 Downloads folder: ./downloads/");
            
            // Auto-discover peers
            discoverPeers();
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(new PeerHandler(clientSocket));
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void discoverPeers() {
        // Auto-discover common peer ports
        int[] commonPorts = {9001, 9002, 9003, 9004};
        for (int peerPort : commonPorts) {
            if (peerPort != port) {
                String peerAddress = "localhost:" + peerPort;
                // Try multiple times with delay
                for (int attempt = 0; attempt < 3; attempt++) {
                    if (isPeerAlive(peerAddress)) {
                        knownPeers.add(peerAddress);
                        System.out.println("🤝 Discovered peer: " + peerAddress);
                        break;
                    }
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
        }
        System.out.println("🌐 Peer discovery complete. Found " + knownPeers.size() + " peers.");
    }
    
    private boolean isPeerAlive(String peerAddress) {
        try {
            String[] parts = peerAddress.split(":");
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])), 1000);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    
    public void addFile(String fileName, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            FileInfo fileInfo = new FileInfo(fileName, filePath, file.length());
            localFiles.put(fileName, fileInfo);
            System.out.println("📄 Added file: " + fileName + " (" + file.length() + " bytes)");
        }
    }
    
    public boolean uploadFile(String fileName, byte[] content) {
        try {
            String filePath = "shared/" + fileName;
            Files.write(Paths.get(filePath), content);
            addFile(fileName, filePath);
            System.out.println("⬆️ Uploaded: " + fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<PeerFileInfo> searchFile(String fileName) {
        List<PeerFileInfo> results = new ArrayList<>();
        
        // Check local files
        if (localFiles.containsKey(fileName)) {
            FileInfo fileInfo = localFiles.get(fileName);
            results.add(new PeerFileInfo(fileName, "localhost:" + port, fileInfo.size, true));
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
                if (response != null && response.startsWith("FOUND|")) {
                    String[] responseData = response.split("\\|");
                    long fileSize = Long.parseLong(responseData[2]);
                    results.add(new PeerFileInfo(fileName, peer, fileSize, false));
                }
                
                socket.close();
            } catch (IOException | NumberFormatException e) {
                // Peer unavailable or invalid response
            }
        }
        
        return results;
    }
    
    public List<PeerFileInfo> listAllFiles() {
        List<PeerFileInfo> allFiles = new ArrayList<>();
        Set<String> processedFiles = new HashSet<>();
        
        // Add local files
        for (Map.Entry<String, FileInfo> entry : localFiles.entrySet()) {
            String fileName = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            allFiles.add(new PeerFileInfo(fileName, "localhost:" + port, fileInfo.size, true));
            processedFiles.add(fileName);
        }
        
        // Query peers for their files
        for (String peer : knownPeers) {
            try {
                String[] parts = peer.split(":");
                Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                out.println("LIST_FILES");
                String response;
                while ((response = in.readLine()) != null && !"END".equals(response)) {
                    String[] fileData = response.split("\\|");
                    if (fileData.length >= 3) {
                        String fileName = fileData[0];
                        long fileSize = Long.parseLong(fileData[1]);
                        if (!processedFiles.contains(fileName)) {
                            allFiles.add(new PeerFileInfo(fileName, peer, fileSize, false));
                            processedFiles.add(fileName);
                        }
                    }
                }
                
                socket.close();
            } catch (IOException | NumberFormatException e) {
                // Peer unavailable
            }
        }
        
        return allFiles;
    }
    
    public boolean downloadFile(String fileName, String peerAddress) {
        try {
            String[] parts = peerAddress.split(":");
            Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("DOWNLOAD|" + fileName);
            
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String response = reader.readLine();
            
            if ("FILE_START".equals(response)) {
                String sizeStr = reader.readLine();
                long fileSize = Long.parseLong(sizeStr);
                
                // Read file content
                byte[] buffer = new byte[4096];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int totalRead = 0;
                
                while (totalRead < fileSize) {
                    int bytesRead = in.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalRead)));
                    if (bytesRead == -1) break;
                    baos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
                
                // Save file
                String downloadPath = "downloads/" + fileName;
                Files.write(Paths.get(downloadPath), baos.toByteArray());
                addFile(fileName, downloadPath);
                
                socket.close();
                System.out.println("⬇️ Downloaded: " + fileName + " (" + fileSize + " bytes)");
                return true;
            }
            
            socket.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public Set<String> getKnownPeers() {
        return new HashSet<>(knownPeers);
    }
    
    public Map<String, FileInfo> getLocalFiles() {
        return new HashMap<>(localFiles);
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                if (request == null) return;
                
                String[] parts = request.split("\\|");
                String command = parts[0];
                
                switch (command) {
                    case "SEARCH":
                        handleSearch(parts, out);
                        break;
                    case "DOWNLOAD":
                        handleDownload(parts, socket);
                        break;
                    case "LIST_FILES":
                        handleListFiles(out);
                        break;
                    case "PING":
                        out.println("PONG");
                        break;
                    default:
                        out.println("UNKNOWN_COMMAND");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void handleSearch(String[] parts, PrintWriter out) {
            if (parts.length < 2) {
                out.println("INVALID_REQUEST");
                return;
            }
            
            String fileName = parts[1];
            if (localFiles.containsKey(fileName)) {
                FileInfo fileInfo = localFiles.get(fileName);
                out.println("FOUND|" + fileName + "|" + fileInfo.size);
            } else {
                out.println("NOT_FOUND");
            }
        }
        
        private void handleDownload(String[] parts, Socket socket) throws IOException {
            if (parts.length < 2) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("INVALID_REQUEST");
                return;
            }
            
            String fileName = parts[1];
            if (localFiles.containsKey(fileName)) {
                FileInfo fileInfo = localFiles.get(fileName);
                File file = new File(fileInfo.path);
                
                if (file.exists()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("FILE_START");
                    out.println(String.valueOf(file.length()));
                    
                    // Send file content
                    try (FileInputStream fis = new FileInputStream(file);
                         OutputStream os = socket.getOutputStream()) {
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    }
                    
                    System.out.println("📤 Sent file: " + fileName + " to " + socket.getRemoteSocketAddress());
                } else {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("FILE_NOT_FOUND");
                }
            } else {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("FILE_NOT_FOUND");
            }
        }
        
        private void handleListFiles(PrintWriter out) {
            for (Map.Entry<String, FileInfo> entry : localFiles.entrySet()) {
                String fileName = entry.getKey();
                FileInfo fileInfo = entry.getValue();
                out.println(fileName + "|" + fileInfo.size + "|" + fileInfo.path);
            }
            out.println("END");
        }
    }
    
    // Data classes
    static class FileInfo {
        String name;
        String path;
        long size;
        
        public FileInfo(String name, String path, long size) {
            this.name = name;
            this.path = path;
            this.size = size;
        }
    }
    
    public static class PeerFileInfo {
        public String fileName;
        public String peerAddress;
        public long fileSize;
        public boolean isLocal;
        
        public PeerFileInfo(String fileName, String peerAddress, long fileSize, boolean isLocal) {
            this.fileName = fileName;
            this.peerAddress = peerAddress;
            this.fileSize = fileSize;
            this.isLocal = isLocal;
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java P2PPeer <port>");
            return;
        }
        
        int port = Integer.parseInt(args[0]);
        P2PPeer peer = new P2PPeer(port);
        
        // Start peer
        new Thread(peer::start).start();
        
        // Keep main thread alive
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            peer.stop();
        }
    }
}
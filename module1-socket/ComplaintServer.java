import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

public class ComplaintServer {
    private static List<Complaint> complaints = Collections.synchronizedList(new ArrayList<>());
    private static int complaintCounter = 1;
    private static final String DATA_FILE = "complaints.dat";
    
    public static void main(String[] args) {
        loadComplaints();
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Complaint Server started on port 8080");
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static class ClientHandler implements Runnable {
        private Socket socket;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                String request = in.readLine();
                if (request == null) {
                    return;
                }
                String[] parts = request.split("\\|");
                
                if (parts[0].equals("LIST")) {
                    StringBuilder response = new StringBuilder("LIST|");
                    if (complaints.isEmpty()) {
                        response.append("");
                    } else {
                        for (Complaint c : complaints) {
                            response.append(c.toString()).append(";");
                        }
                    }
                    out.println(response.toString());
                    return;
                }
                
                if (parts.length < 4) {
                    out.println("ERROR|Invalid request format");
                    return;
                }
                
                if (parts[0].equals("SUBMIT")) {
                    Complaint complaint = new Complaint(
                        complaintCounter++,
                        parts[1], // room
                        parts[2], // category
                        parts[3]  // description
                    );
                    complaints.add(complaint);
                    saveComplaints();
                    System.out.println("New complaint received: #" + complaint.getId() + " from Room " + complaint.getRoom());
                    out.println("SUCCESS|Complaint #" + complaint.getId() + " submitted successfully");

                } else {
                    out.println("ERROR|Unknown command");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    static class Complaint implements Serializable {
        private int id;
        private String room;
        private String category;
        private String description;
        private Date timestamp;
        
        public Complaint(int id, String room, String category, String description) {
            this.id = id;
            this.room = room;
            this.category = category;
            this.description = description;
            this.timestamp = new Date();
        }
        
        public int getId() { return id; }
        public String getRoom() { return room; }
        
        @Override
        public String toString() {
            return id + "," + room + "," + category + "," + description + "," + timestamp;
        }
    }
    
    private static void saveComplaints() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(complaints));
            oos.writeInt(complaintCounter);
        } catch (IOException e) {}
    }
    
    private static void loadComplaints() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            complaints.addAll((List<Complaint>) ois.readObject());
            complaintCounter = ois.readInt();
        } catch (Exception e) {}
    }
}
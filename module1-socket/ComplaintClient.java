import java.io.*;
import java.net.*;

public class ComplaintClient {
    private String serverHost;
    private int serverPort;
    
    public ComplaintClient(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
    }
    
    public String submitComplaint(String room, String category, String description) {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("SUBMIT|" + room + "|" + category + "|" + description);
            return in.readLine();
            
        } catch (IOException e) {
            return "ERROR|Connection failed";
        }
    }
    
    public String getComplaints() {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("LIST");
            return in.readLine();
            
        } catch (IOException e) {
            return "ERROR|Connection failed";
        }
    }
}
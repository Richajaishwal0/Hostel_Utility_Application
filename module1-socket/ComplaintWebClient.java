import java.io.*;
import java.net.*;

public class ComplaintWebClient {
    private String serverHost = "localhost";
    private int serverPort = 8080;
    
    public String submitComplaint(String room, String category, String description) {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("SUBMIT|" + room + "|" + category + "|" + description);
            return in.readLine();
            
        } catch (IOException e) {
            return "ERROR|Connection failed: " + e.getMessage();
        }
    }
    
    public String getComplaints() {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("LIST");
            return in.readLine();
            
        } catch (IOException e) {
            return "ERROR|Connection failed: " + e.getMessage();
        }
    }
    
    // For testing from command line
    public static void main(String[] args) {
        ComplaintWebClient client = new ComplaintWebClient();
        
        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println("  java ComplaintWebClient submit <room> <category> <description>");
            System.out.println("  java ComplaintWebClient list");
            return;
        }
        
        if ("submit".equals(args[0]) && args.length == 4) {
            String result = client.submitComplaint(args[1], args[2], args[3]);
            System.out.println(result);
        } else if ("list".equals(args[0])) {
            String result = client.getComplaints();
            System.out.println(result);
        }
    }
}
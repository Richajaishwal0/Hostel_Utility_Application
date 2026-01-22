import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.Serializable;

public class RoomInfoServer extends UnicastRemoteObject implements RoomInfoService {
    private Map<String, RoomInfo> roomDatabase;
    
    protected RoomInfoServer() throws RemoteException {
        super();
        initializeRoomData();
    }
    
    private void initializeRoomData() {
        roomDatabase = new HashMap<>();
        roomDatabase.put("101", new RoomInfo("101", Arrays.asList("Arjun Sharma", "Vikram Patel", "Rohit Gupta", "Amit Kumar"), "Mr. Rajesh Singh", "9876543210"));
        roomDatabase.put("102", new RoomInfo("102", Arrays.asList("Priya Agarwal", "Sneha Reddy", "Kavya Nair", "Pooja Joshi"), "Mr. Rajesh Singh", "9876543210"));
        roomDatabase.put("103", new RoomInfo("103", Arrays.asList("Ravi Mehta", "Suresh Yadav", "Kiran Jain", "Deepak Verma"), "Mr. Rajesh Singh", "9876543210"));
        roomDatabase.put("104", new RoomInfo("104", Arrays.asList("Anita Sharma", "Meera Iyer", "Divya Pillai", "Rashmi Sinha"), "Mr. Rajesh Singh", "9876543210"));
        roomDatabase.put("105", new RoomInfo("105", Arrays.asList("Sanjay Tiwari", "Manoj Singh", "Ajay Pandey", "Vinod Mishra"), "Mr. Rajesh Singh", "9876543210"));
        roomDatabase.put("106", new RoomInfo("106", Arrays.asList("Sunita Devi", "Rekha Kumari", "Geeta Rani", "Sushma Bai"), "Ms. Lakshmi Devi", "9876543211"));
        roomDatabase.put("107", new RoomInfo("107", Arrays.asList("Rahul Agrawal", "Nikhil Bansal", "Sachin Goyal", "Tarun Saxena"), "Ms. Lakshmi Devi", "9876543211"));
        roomDatabase.put("108", new RoomInfo("108", Arrays.asList("Nisha Chandra", "Ritu Malhotra", "Swati Kapoor", "Preeti Arora"), "Ms. Lakshmi Devi", "9876543211"));
        roomDatabase.put("109", new RoomInfo("109", Arrays.asList("Ashok Rao", "Prakash Jha", "Ramesh Chand", "Sunil Das"), "Ms. Lakshmi Devi", "9876543211"));
        roomDatabase.put("110", new RoomInfo("110", Arrays.asList("Madhuri Bhatt", "Shanti Devi", "Kamala Soni", "Usha Gupta"), "Ms. Lakshmi Devi", "9876543211"));
    }
    
    @Override
    public RoomInfo getRoomInfo(String roomNumber) throws RemoteException {
        return roomDatabase.get(roomNumber);
    }
    
    @Override
    public List<String> getAllRooms() throws RemoteException {
        return new ArrayList<>(roomDatabase.keySet());
    }
    
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            RoomInfoServer server = new RoomInfoServer();
            registry.bind("RoomInfoService", server);
            System.out.println("RMI Room Info Server started on port 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
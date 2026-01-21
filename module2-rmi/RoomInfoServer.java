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
        roomDatabase.put("101", new RoomInfo("101", Arrays.asList("John Doe", "Jane Smith"), "Mr. Anderson", "9876543210"));
        roomDatabase.put("102", new RoomInfo("102", Arrays.asList("Bob Wilson"), "Mr. Anderson", "9876543210"));
        roomDatabase.put("201", new RoomInfo("201", Arrays.asList("Alice Brown", "Carol White"), "Ms. Johnson", "9876543211"));
        roomDatabase.put("202", new RoomInfo("202", Arrays.asList("David Lee"), "Ms. Johnson", "9876543211"));
    }
    
    @Override
    public RoomInfo getRoomInfo(String roomNumber) throws RemoteException {
        return roomDatabase.get(roomNumber);
    }
    
    @Override
    public List<String> getAllRooms() throws RemoteException {
        return new ArrayList<>(roomDatabase.keySet());
    }
    
    public static class RoomInfo implements Serializable {
        public String roomNumber;
        public List<String> occupants;
        public String wardenName;
        public String wardenContact;
        
        public RoomInfo(String roomNumber, List<String> occupants, String wardenName, String wardenContact) {
            this.roomNumber = roomNumber;
            this.occupants = occupants;
            this.wardenName = wardenName;
            this.wardenContact = wardenContact;
        }
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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class RoomInfoClient {
    private RoomInfoService service;
    
    public RoomInfoClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (RoomInfoService) registry.lookup("RoomInfoService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public RoomInfoService.RoomInfo getRoomInfo(String roomNumber) {
        try {
            return service.getRoomInfo(roomNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<String> getAllRooms() {
        try {
            return service.getAllRooms();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
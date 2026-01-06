import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RoomInfoService extends Remote {
    RoomInfo getRoomInfo(String roomNumber) throws RemoteException;
    List<String> getAllRooms() throws RemoteException;
    
    class RoomInfo implements java.io.Serializable {
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
}
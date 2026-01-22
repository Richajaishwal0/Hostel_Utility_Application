import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.io.Serializable;

public interface RoomInfoService extends Remote {
    RoomInfo getRoomInfo(String roomNumber) throws RemoteException;
    List<String> getAllRooms() throws RemoteException;
}
import java.io.Serializable;
import java.util.List;

public class RoomInfo implements Serializable {
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
package hcmus.angtonyvincent.firebaseauthentication.room;

/**
 * Created by VUDAI on 5/17/2017.
 */

public class RoomInfo {
    protected String m_roomName;
    protected DeviceInRoom m_roomOwner;
    public final String TAG = "RoomInfo";

    public RoomInfo(String roomName, DeviceInRoom roomOwner){
        m_roomOwner = roomOwner;
        m_roomName = roomName;
    }

    public String getroomName(){
        return m_roomName;
    }

    public DeviceInRoom getRoomOwner(){
        return m_roomOwner;
    }

    public boolean equal(RoomInfo room){
        return m_roomName.equals(room.m_roomName);
    }
}
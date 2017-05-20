package hcmus.angtonyvincent.firebaseauthentication.list_room;

import java.net.InetAddress;

import hcmus.angtonyvincent.firebaseauthentication.room.RoomInfo;

/**
 * Created by VUDAI on 5/14/2017.
 */

public interface RoomActionListener {

    public void onRoomFound(InetAddress adr, int port, String nameService);

    public void onRoomLost(InetAddress adr, int port, String nameService);

    public void onRoomCreate(RoomInfo roomName);
}

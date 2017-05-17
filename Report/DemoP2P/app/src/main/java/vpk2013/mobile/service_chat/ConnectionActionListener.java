package vpk2013.mobile.service_chat;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by VUDAI on 5/10/2017.
 */

public interface ConnectionActionListener {
    public void receiveConnectionRequest(Socket clientSocket);

    public void onServiceFound(InetAddress adr, int port, String nameService);

    public void onServiceLost(InetAddress adr, int port, String nameService);
}

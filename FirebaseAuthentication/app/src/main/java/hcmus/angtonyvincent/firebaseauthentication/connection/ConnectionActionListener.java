package hcmus.angtonyvincent.firebaseauthentication.connection;

import java.net.Socket;

/**
 * Created by VUDAI on 5/22/2017.
 */

public interface ConnectionActionListener {
    void onMessageReceived(Socket socket, String message);
}

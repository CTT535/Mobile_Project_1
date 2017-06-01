package hcmus.angtonyvincent.firebaseauthentication.connection;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import hcmus.angtonyvincent.firebaseauthentication.room.RoomActivity;

/**
 * Created by VUDAI on 5/17/2017.
 */

public class Connection {

    protected static Context m_context;
    private static boolean m_serverStarted = false;
    private static Thread m_serverThread;
    private static  ServerSocket m_serverSocket;
    private static String TAG = "Connection";
    public static int PORT = 54321;

    public Connection(Context context){
        m_context = context;
    }

    public static int registreServer(int port){
        if (m_serverStarted){
            return m_serverSocket.getLocalPort() ;
        }

        Log.d(TAG, "register server");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            m_serverSocket = serverSocket;
            m_serverThread = new Thread(new ServerThread(serverSocket));
            m_serverStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return serverSocket.getLocalPort();
        }
    }

    public static void startServer(){
        Log.d(TAG, "start server, server thread state: " + m_serverThread.getState().toString());
        if(m_serverThread != null && !m_serverThread.isAlive()) {
            Log.d(TAG, "server thread ready to use");
            m_serverThread.start();
        }
    }

    public static class ServerThread implements Runnable {
        ServerSocket mServerSocket;
        private static String TAG = "ServerThread";
        public ServerThread (ServerSocket serverSocket){
            mServerSocket = serverSocket;
        }

        @Override
        public void run() {
            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                while (!Thread.currentThread().isInterrupted() && m_context instanceof RoomActivity) {
                    Log.d(TAG, "ServerSocket Created, awaiting connection on port " + mServerSocket.getLocalPort());
                    Socket socket = mServerSocket.accept();
                    Log.d(TAG, "Connected.");

                    //new a thread treat the request
                    new Thread(new ReceiveTask(socket)).start();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }

    public static void sendMessage(String message, InetAddress adr, int port){
        final InetAddress address = adr;
        final int port1 = port;
        final String msg = message;
        new Thread(new Runnable() {
            @Override
            public void run() {
            Socket socket = null;
            try {
                Log.d(TAG, "send message (" + msg + ") to " + address.toString() + "(port " + port1 + ")");
                socket = new Socket(address, port1);
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(msg);
                out.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        }).start();
    }

    public static void tearDownServer(){
        Log.d(TAG, "tear down server");
        if (m_serverSocket != null && !m_serverSocket.isClosed()){
            try {
                m_serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        m_serverThread.interrupt();
        m_serverStarted = false;
    }

    private static class ReceiveTask implements Runnable {
        Socket m_socket;
        public ReceiveTask (Socket socket){
            this.m_socket = socket;
        }

        @Override
        public void run() {
            try {
                //read the request
                BufferedReader input;
                input = new BufferedReader(new InputStreamReader(
                        m_socket.getInputStream()));

                String messageStr = null;
                messageStr = input.readLine();
                if (messageStr != null) {
                    Log.d(TAG, "message received from " + m_socket.getInetAddress().toString() + ": " + messageStr);
                    //treat the request
                    if (m_context instanceof  ConnectionActionListener) {
                        ((ConnectionActionListener) m_context).onMessageReceived(m_socket, messageStr);
                    }
                } else {
                    Log.d(TAG, "The nulls! The nulls!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

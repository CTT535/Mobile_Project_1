package vpk2013.mobile.service_chat;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by VUDAI on 5/10/2017.
 */

public class DeviceListFragment extends ListFragment {
    private List<DeviceInfo> m_peers = new ArrayList<DeviceInfo>();
    View mContentView = null;
    private DeviceInfo m_deviceSelected;
    private final String TAG = "DeviceListFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "on tactivity create");
        super.onActivityCreated(savedInstanceState);
        if (getListAdapter() == null) {
            this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, m_peers));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }


    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final DeviceInfo device = (DeviceInfo) getListAdapter().getItem(position);
        //((DeviceActionListener) getActivity()).showDetails(device);
        Log.d(TAG, "click on Device: " + device.getDeviceName() + "/" + device.getIpAdress().toString() + "/" + device.getState());
        Toast.makeText(getActivity(), "Device: " + device.getDeviceName() + "/" + device.getIpAdress().toString() + "/" + device.getState(), Toast.LENGTH_SHORT).show();
        if(!device.isConnected()){
            //create a socket request connection to service
            Log.d(TAG, "device clicked is available");
            new ClientTask(device).execute();
        }
        else{
            Log.d(TAG, "device clicked is connected, send message");
            device.sendMessage("hello world");
        }
    }

    public void onRequestConnectionFrom(Socket clientSocket){
        DeviceInfo clientDevice = new DeviceInfo(clientSocket.getInetAddress(), clientSocket.getPort(), "");
        Iterator<DeviceInfo> i = m_peers.iterator();
        while (i.hasNext()) {
            DeviceInfo deviceInList = i.next();
            Log.d(TAG, "device in list:" + deviceInList.getDeviceName());
            if(deviceInList.equal(clientDevice)) {
                if (clientDevice.equal(deviceInList)){
                    deviceInList.onConnectionEstablished(clientSocket);
                    refresh();
                }
            }
        }
    }

    public void refresh(){

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "refresh");
                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            }
        });

    }

    /**
     * Array adapter for ListFragment that maintains DeviceInfo list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<DeviceInfo> {

        private List<DeviceInfo> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<DeviceInfo> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            DeviceInfo device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.getDeviceName());
                }
                if (bottom != null) {
                    bottom.setText(device.getState() + "/" + device.getPort());
                }
            }

            return v;

        }
    }

    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        final Activity a = getActivity();
        a.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(a, "Hello", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addPeer(InetAddress adr, int port, String peerName){
        Log.d(TAG, "addPeer: " + peerName + "/" + adr.toString());
        DeviceInfo device = new DeviceInfo(adr, port, peerName);
        if(!isExistInList(device)){
            m_peers.add(device);
            if (getListAdapter() == null){
                this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, m_peers));
            }
            refresh();
        }
    }

    public void removePeer(InetAddress adr, int port, String name){
        Log.d(TAG, "remove:" + name);
        Iterator<DeviceInfo> i = m_peers.iterator();
        while (i.hasNext()) {
            DeviceInfo deviceInList = i.next();
            Log.d(TAG, "device in list:" + deviceInList.getDeviceName());
            if(deviceInList.equal(new DeviceInfo(adr, port, name))) {
                deviceInList.releaseConnection();
                i.remove();
            }
        }
        refresh();
    }

    public boolean isExistInList(DeviceInfo device){
        for(DeviceInfo deviceInList:m_peers){
            if (device.equal(deviceInList)){
                return true;
            }
        }
        return false;
    }

    public void clearPeers() {
        m_peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    class ClientTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        private DeviceInfo m_device;

        public ClientTask(DeviceInfo device){
            m_device = device;
        }

        protected String doInBackground(String... info) {
            Socket socket = null;
            try {
                Log.d(TAG, "request connection to service: " + m_device);
                socket = new Socket(m_device.getIpAdress(), m_device.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socket != null){
                m_device.onConnectionEstablished(socket);
                refresh();
            }

            return null;
        }

        protected void onPostExecute() {
        }

    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {
        void showDetails(DeviceInfo device);

        void connect(DeviceInfo deviceInfo);

        void disconnect(DeviceInfo deviceInfo);
    }

    protected class DeviceInfo{
        protected String m_deviceName;
        protected InetAddress m_ipAdress;
        protected int m_port;
        protected Socket m_socket;
        protected int m_state;
        public final static int CONNECTED = 1;
        public final static int NOT_CONNECTED = 2;


        public DeviceInfo(InetAddress ipAdress, int port, String deviceName){
            m_ipAdress = ipAdress;
            m_deviceName = deviceName;
            m_port = port;
            m_socket = null;
            m_state = NOT_CONNECTED;
       }

        public boolean isConnected(){
            return m_state == CONNECTED;
        }

        public InetAddress getIpAdress(){
            return  m_ipAdress;
        }

        public String getDeviceName(){
            return m_deviceName;
        }

        public String getState(){
            if (isConnected()){
                return "Connected";
            }
            return "avaible";
        }
        public boolean equal(DeviceInfo device){
            if (device.m_ipAdress == null){
                return m_deviceName.equals(device.getDeviceName());
            }
            return m_deviceName.equals(device.getDeviceName()) || device.m_ipAdress.toString().equals(m_ipAdress.toString());
        }

        public int getPort(){
            return m_port;
        }

        public void onConnectionEstablished(Socket soc){
            Log.d(TAG, "on connection established");
            if(isConnected()){
                Log.d(TAG, "device already connected");
                return;
            }
            m_socket = soc;
            m_state = CONNECTED;
            //create a thread listen and send message
            new ReceivingThread().run();
        }

        public void releaseConnection(){
            m_state = NOT_CONNECTED;
            if(m_socket == null){
                return;
            }
            try {
                m_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = m_socket;
                if (socket == null) {
                    Log.d(TAG, "Socket is null");
                } else if(socket.isClosed()){
                  Log.d(TAG, "Server close");
                    releaseConnection();
                } else if (socket.getOutputStream() == null) {
                    Log.d(TAG, "Socket output stream is null");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(m_socket.getOutputStream())), true);
                out.println(msg);
                out.flush();
            } catch (UnknownHostException e) {
                Log.d(TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(TAG, "Error3", e);
            }
            Log.d(TAG, "Client sent message: " + msg);
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            m_socket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted() && m_socket != null && m_socket.isConnected()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(TAG, "Read from the stream: " + messageStr);
                            updateMessages(messageStr, true);
                        } else {
                            Log.d(TAG, "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    Log.e(TAG, "Server loop error: ", e);
                }
            }
        }

    }
}

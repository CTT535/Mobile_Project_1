package hcmus.angtonyvincent.firebaseauthentication.room;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import hcmus.angtonyvincent.firebaseauthentication.PatternActivity;
import hcmus.angtonyvincent.firebaseauthentication.R;
import hcmus.angtonyvincent.firebaseauthentication.connection.Connection;
import hcmus.angtonyvincent.firebaseauthentication.connection.ConnectionActionListener;
import hcmus.angtonyvincent.firebaseauthentication.list_room.ListRoomActivity;
import hcmus.angtonyvincent.firebaseauthentication.list_room.NsdHelper;

public class RoomActivity extends AppCompatActivity implements ConnectionActionListener {

    private static final String TAG = "RoomActivity";
    TextView tv_roomName;
    ListDeviceInRoomFragment m_deviceList;
    boolean appfinished;
    Connection m_connection;
    static boolean isRoomOwner = false;
    private Button bt_start;
    static DeviceInRoom m_thisDevice;
    static DeviceInRoom m_roomOwner = null;
    boolean outOfroom = true;
    static String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        m_connection = new Connection(this);
        m_connection.registreServer(Connection.PORT);
        m_connection.startServer();
        m_deviceList = (ListDeviceInRoomFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_list_device);

        m_deviceList.clearList();
        Bundle b = getIntent().getBundleExtra("bundle");
        //called from ListRoomActivity => need to update this device and room name
        roomName = b.getString("roomName");
        isRoomOwner = b.getBoolean("isRoomOwner");
        m_thisDevice = new DeviceInRoom(this.getLocalAddress(), Connection.PORT, "", isRoomOwner);

        tv_roomName = (TextView) findViewById(R.id.tv_room_name);
        tv_roomName.setText(roomName);
        bt_start = (Button)findViewById(R.id.bt_start);

        if(isRoomOwner){
            final Activity context = this;
            m_roomOwner = m_thisDevice;
            Log.d(TAG, "is room owner");
            m_deviceList.addDevice(m_thisDevice);
            bt_start.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    m_deviceList.sendMessageToAll(RequestFactory.createRequestStartGame().toString());
                    Intent patternIntent = new Intent(getActivity(), PatternActivity.class);
                    startActivity(patternIntent);
                    appfinished = false;
                    outOfroom = false;
                    finish();
                }
            });
        } else{
            bt_start.setVisibility(View.INVISIBLE);
            Log.d(TAG, "not room owner");
            //called from ListRoomActivity => need to update room owner from bundle
            if(b != null){
                String address = b.getString("rommOwnerAddress");
                int port = b.getInt("roomOwnerPort");
                try {
                    InetAddress adr = InetAddress.getByName(address.substring(1));
                    m_roomOwner = new DeviceInRoom(adr, port, "", true);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            //add me to the list
            m_deviceList.addDevice(m_thisDevice);
            //request the list player in room except this device
            Connection.sendMessage(RequestFactory.createRequestParticipate(m_thisDevice).toString(), m_roomOwner.getIpAdress(), m_roomOwner.getPort());
        }
        appfinished = true;
    }

    public static DeviceInRoom getThisDeviceInRoom(){
        return m_thisDevice;
    }

    public static DeviceInRoom getRoomOwner(){
        return m_roomOwner;
    }

    public boolean isRoomOwner(){
        return isRoomOwner;
    }

    public InetAddress getLocalAddress(){
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        String adr = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        try {
            return InetAddress.getByName(adr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListRoomActivity.class);
        startActivity(intent);
        appfinished = false;
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(outOfroom){
            //stop room service
            NsdHelper.tearDown();
            Connection.sendMessage(RequestFactory.createRequestOutOfRoom(m_thisDevice).toString(), m_roomOwner.getIpAdress(), m_roomOwner.getPort());
            if (appfinished){
                //stop server
                Connection.tearDownServer();
            }
        }
    }

    public Activity getActivity(){
        return this;
    }

    public void onMessageReceived(Socket socket, String msg){
        new Thread(new TreatRequestTask(msg)).start();
    }

    @Override
    public void startActivity(Intent i){
        super.startActivity(i);
        appfinished = false;
        this.finish();
    }

    public class TreatRequestTask implements Runnable {

        String m_request;
        public TreatRequestTask(String request){
            m_request = request;
        }

        @Override
        public void run() {
            try {
                JSONObject jsonObj = new JSONObject(m_request);
                String signal = jsonObj.get("signal").toString();
                Log.d(TAG, "signal: " + signal);
                switch (signal){
                    case RequestFactory.SIGNAL_REQUEST_PATICIPATE:
                        if(isRoomOwner()) {
                            JSONObject newMember = (JSONObject) jsonObj.get("newMember");
                            DeviceInRoom device = new DeviceInRoom(newMember);
                            Connection.sendMessage(RequestFactory.createReponseListDevice(m_deviceList).toString(), device.getIpAdress(), device.getPort());
                            m_deviceList.addDevice(device);
                        }
                        break;
                    case RequestFactory.SIGNAL_REQUEST_START_GAME:
                        Intent patternIntent = new Intent(getActivity(), PatternActivity.class);
                        startActivity(patternIntent);
                        appfinished = false;
                        outOfroom = false;
                        finish();
                        break;
                    case RequestFactory.SIGNAL_GET_LIST_DEVICE:
                        if(!isRoomOwner()) {
                            Log.d(TAG, "GET_LIST_DEVICE");
                            JSONArray listDevice = jsonObj.getJSONArray("listDevice");
                            for (int i = 0; i < listDevice.length(); i++) {
                                m_deviceList.addDevice(new DeviceInRoom((JSONObject) listDevice.get(i)));
                            }
                        }
                        break;
                    case RequestFactory.SIGNAL_REQUEST_GET_OUT:
                        if(isRoomOwner()){
                            DeviceInRoom deviceGetOut = new DeviceInRoom((JSONObject) jsonObj.get("sourceDevice"));
                            m_deviceList.removeDevice(deviceGetOut);
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

package hcmus.angtonyvincent.firebaseauthentication.room;

import android.app.Activity;
import android.content.Intent;
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
import hcmus.angtonyvincent.firebaseauthentication.list_room.ListRoomActivity;
import hcmus.angtonyvincent.firebaseauthentication.list_room.NsdHelper;

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";
    TextView tv_roomName;
    ListDeviceInRoomFragment m_deviceList;
    boolean appfinished;
    Connection m_connection;
    boolean isRoomOwner = false;
    private Button bt_start;
    DeviceInRoom m_thisDevice;
    DeviceInRoom m_roomOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        m_connection = new Connection(this);
        m_connection.registreServer(Connection.PORT);
        m_connection.startServer();
        m_deviceList = (ListDeviceInRoomFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_list_device);

        Bundle b = getIntent().getBundleExtra("bundle");
        String roomName = b.getString("roomName");
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
                    Intent patternIntent = new Intent(context, PatternActivity.class);
                    startActivity(patternIntent);
                    context.finish();
                }
            });
        } else{
            bt_start.setVisibility(View.INVISIBLE);
            Log.d(TAG, "not room owner");
            //add room owner to list device in the room
            String roomOwnerName = b.getString("roomOwnerName");
            String address = b.getString("rommOwnerAddress");
            int port = b.getInt("roomOwnerPort");

            try {
                InetAddress adr = InetAddress.getByName(address.substring(1));
                m_roomOwner = new DeviceInRoom(adr, port, "", true);
                //add me to the list
                m_deviceList.addDevice(m_thisDevice);
                Connection.sendMessage(RequestFactory.createRequestParticipate(m_thisDevice).toString(), adr, port);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }
        appfinished = true;
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
        NsdHelper.tearDown();
        Connection.sendMessage(RequestFactory.createRequestOutOfRoom(m_thisDevice).toString(), m_roomOwner.getIpAdress(), m_roomOwner.getPort());
        if (appfinished){
            Connection.tearDownServer();
        }
    }

    public Activity getActivity(){
        return this;
    }

    public void onMessageReceived(Socket socket, String msg){
        new Thread(new TreatRequestTask(msg)).start();
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

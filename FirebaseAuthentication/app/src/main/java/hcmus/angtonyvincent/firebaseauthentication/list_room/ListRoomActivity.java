package hcmus.angtonyvincent.firebaseauthentication.list_room;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.InetAddress;

import hcmus.angtonyvincent.firebaseauthentication.R;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomActivity;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomInfo;


/**
 * Created by VUDAI on 5/14/2017.
 */

public class ListRoomActivity extends AppCompatActivity implements RoomActionListener {

    private ListRoomFragment m_roomList;
    private final String TAG = "ListRoomActivity";
    private NsdHelper m_nsdHelper;
    private Button m_bRegistre;
    //private Button m_bUnregistre;

    protected boolean appfinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create list room activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_room);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        m_roomList = (ListRoomFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_list_room);

        m_nsdHelper = new NsdHelper(this);
        m_nsdHelper.initializeNsd();

        m_bRegistre = (Button) findViewById(R.id.bt_registre);
        m_bRegistre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_nsdHelper.registerService();
            }
        });

        /*m_bUnregistre = (Button) findViewById(R.id.bt_unregistre);
        m_bUnregistre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_nsdHelper.tearDown();
            }
        });*/
        appfinished = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (m_nsdHelper != null) {
            m_nsdHelper.stopDiscovery();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_roomList.clearPeers();
        if (m_nsdHelper != null) {
            m_nsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appfinished){
            m_nsdHelper.tearDown();
        }
    }

    @Override
    public void onRoomFound(InetAddress adr, int port, String nameRoom) {
        m_roomList.addRoom(adr, port, nameRoom);
    }

    @Override
    public void onRoomLost(InetAddress adr, int port, String nameRoom) {
        m_roomList.removeRoom(nameRoom);
    }

    @Override
    public void onRoomCreate(RoomInfo roomInfo) {
        Log.i(TAG, "start room activity");
        Bundle b = new Bundle();
        b.putString("roomName", roomInfo.getroomName());
        b.putBoolean("isRoomOwner", true);
        Intent listRoomIntent = new Intent(this, RoomActivity.class);
        listRoomIntent.putExtra("bundle", b);
        startActivity(listRoomIntent);
    }

    @Override
    public void startActivity(Intent i){
        super.startActivity(i);
        appfinished = false;
        this.finish();
    }
}

package vpk2013.mobile.service_chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ConnectionActionListener {
    private DeviceListFragment m_deviceList;
    protected ArrayList<String> listDeviceItems=new ArrayList<String>();
    private final String TAG = "MainActivity";
    private NsdHelper m_nsdHelper;
    private Button m_bDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_deviceList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.device_list);

        m_nsdHelper = new NsdHelper(this);
        m_nsdHelper.initializeNsd();
        m_nsdHelper.registerService();

        m_bDiscovery = (Button) findViewById(R.id.device_discovery);
        if (m_bDiscovery == null){
            Log.d(TAG, "discovery button null");
        }
        m_bDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_nsdHelper.discoverServices();
            }
        });
    }

    @Override
    public void receiveConnectionRequest(Socket clientSocket) {
        //update list device when receiving a request connection
        m_deviceList.onRequestConnectionFrom(clientSocket);
    }

    @Override
    public void onServiceFound(InetAddress adr, int port, String nameService) {
        m_deviceList.addPeer(adr, port, nameService);
    }

    @Override
    public void onServiceLost(InetAddress adr, int port, String nameService) {
        m_deviceList.removePeer(adr, port, nameService);
    }
    @Override
    protected void onPause() {
        if (m_nsdHelper != null) {
            m_nsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (m_nsdHelper != null) {
            m_nsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        m_nsdHelper.tearDown();
        super.onDestroy();
    }
}
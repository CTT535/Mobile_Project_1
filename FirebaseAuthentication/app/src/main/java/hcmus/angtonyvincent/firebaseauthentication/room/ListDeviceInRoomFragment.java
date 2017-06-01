package hcmus.angtonyvincent.firebaseauthentication.room;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hcmus.angtonyvincent.firebaseauthentication.R;
import hcmus.angtonyvincent.firebaseauthentication.connection.Connection;

/**
 * Created by VUDAI on 5/14/2017.
 */

public class ListDeviceInRoomFragment extends ListFragment {
    private static List<DeviceInRoom> m_devices = new ArrayList<DeviceInRoom>();
    View mContentView = null;
    private static String TAG = "ListDeviceFragment";
    static Context m_activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "on activity create");
        super.onActivityCreated(savedInstanceState);
        m_activity = getActivity();
        if (getListAdapter() == null) {
            this.setListAdapter(new DeviceListAdapter(getActivity(), R.layout.item_device, m_devices));
        }
    }

    public static void sendMessageToAll(String msg){
        Log.d(TAG, "send message to all ");
        if(m_devices == null){
            return;
        }
        for(DeviceInRoom deviceInList:m_devices){
            Log.d(TAG, "ip this device: " + ((RoomActivity)m_activity).getLocalAddress().toString());
            Log.d(TAG, "ip device in list: " + deviceInList.getIpAdress().toString());
            if (!deviceInList.equal("", ((RoomActivity)m_activity).getLocalAddress())) {
                Connection.sendMessage(msg, deviceInList.getIpAdress(), deviceInList.getPort());
            }
        }
    }

    public static int getNumberDeviceInRoom(){
        if(m_devices == null){
            return 0;
        }
        return m_devices.size();
    }

    public JSONArray toJSONArray(){
        JSONArray innerArray = new JSONArray();
        int i = 0;
        for(DeviceInRoom deviceInList:m_devices){
            innerArray.put(deviceInList.toJSONObject());
        }
        return innerArray;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_list_device, null);
        return mContentView;
    }


    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final DeviceInRoom device = (DeviceInRoom) getListAdapter().getItem(position);
        //do nothing
    }

    public void refresh(){
        ((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public DeviceInRoom getRoomOwner(){
        Iterator<DeviceInRoom> i = m_devices.iterator();
        while (i.hasNext()) {
            DeviceInRoom deviceInList = i.next();
            if(deviceInList.isRoomOwner()) {
                Log.d(TAG, "has room owner");
                return deviceInList;
            }
        }
        return null;
    }

    /**
     * Array adapter for ListFragment that maintains DeviceInRoom list.
     */
    private class DeviceListAdapter extends ArrayAdapter<DeviceInRoom> {

        private List<DeviceInRoom> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public DeviceListAdapter(Context context, int textViewResourceId,
                                   List<DeviceInRoom> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.item_device, null);
            }
            DeviceInRoom device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.getDeviceName());
                }
                if (bottom != null) {
                    if (device.m_isRoomOwner) {
                        bottom.setText(device.getIpAdress().toString() + "\nYou are room owner");
                    }
                    else {
                        bottom.setText(device.getIpAdress().toString());
                    }
                }
            }
            return v;
        }
    }

    public void addDevice(InetAddress adr, int port, String devieName, boolean isRoomOwner){
        Log.d(TAG, "addDevice: " + devieName + "/" + adr.toString());
        final DeviceInRoom device = new DeviceInRoom(adr, port, devieName, isRoomOwner);
        if(getActivity() != null && !isExistInList(device)){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    m_devices.add(device);
                    if (getListAdapter() == null){
                        setListAdapter(new DeviceListAdapter(getActivity(), R.layout.item_device, m_devices));
                    }
                    refresh();
                }
            });
        }
    }

    public void addDevice(DeviceInRoom device){
        final DeviceInRoom dv = device;
        Log.d(TAG, "addDevice from device: " + device.getDeviceName() + "/" + device.getIpAdress().toString() + "/" + device.getPort());
        if(getActivity() != null && !isExistInList(device)){
            Log.d(TAG, "addDevice: " + device.getDeviceName() + "/" + device.getIpAdress());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    m_devices.add(dv);
                    if (getListAdapter() == null){
                        setListAdapter(new DeviceListAdapter(getActivity(), R.layout.item_device, m_devices));
                    }
                    refresh();
                }
            });
        }
    }

    public void removeDevice(String name, InetAddress adr){
        final Iterator<DeviceInRoom> i = m_devices.iterator();
        while (i.hasNext()) {
            DeviceInRoom deviceInList = i.next();
            Log.d(TAG, "device in list:" + deviceInList.getDeviceName());
            if(getActivity() != null && deviceInList.equal(name, adr)) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        i.remove();
                        refresh();
                    }
                });
            }
        }
    }

    public void removeDevice(DeviceInRoom device){
        final Iterator<DeviceInRoom> i = m_devices.iterator();
        while (i.hasNext()) {
            DeviceInRoom deviceInList = i.next();
            Log.d(TAG, "device in list:" + deviceInList.getDeviceName());
            if(getActivity() != null && deviceInList.equal(device.getDeviceName(), device.getIpAdress())) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        i.remove();
                        refresh();
                    }
                });
            }
        }
    }

    public boolean isExistInList(DeviceInRoom device){
        Log.d(TAG, "isExistInList?");
        for(DeviceInRoom deviceInList:m_devices){
            if (deviceInList.equal(device.getDeviceName(), device.getIpAdress())){
                Log.d(TAG, "isExistInList");
                return true;
            }
        }
        return false;
    }

    public void clearList() {
        if(m_devices == null || getListAdapter() ==null){
            return;
        }
        m_devices.clear();
        refresh();
    }
}

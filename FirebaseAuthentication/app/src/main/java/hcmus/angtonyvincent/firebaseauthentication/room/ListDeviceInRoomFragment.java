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
    private List<DeviceInRoom> m_devices = new ArrayList<DeviceInRoom>();
    View mContentView = null;
    private final String TAG = "ListDeviceFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "on activity create");
        super.onActivityCreated(savedInstanceState);
        if (getListAdapter() == null) {
            this.setListAdapter(new DeviceListAdapter(getActivity(), R.layout.item_device, m_devices));
        }
    }

    public void sendMessageToAll(String msg){
        Log.d(TAG, "send message to all ");
        for(DeviceInRoom deviceInList:m_devices){
            Log.d(TAG, "ip this device: " + ((RoomActivity)getActivity()).getLocalAddress().toString());
            Log.d(TAG, "ip device in list: " + deviceInList.getIpAdress().toString());
            if (!deviceInList.equal("", ((RoomActivity)getActivity()).getLocalAddress())) {
                Log.d(TAG, "send message to " + deviceInList.getIpAdress().toString());
                Connection.sendMessage(msg, deviceInList.getIpAdress(), deviceInList.getPort());
            }
        }
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
        Log.d(TAG, "start refresh");
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.d(TAG, "list adapter: " + getListAdapter());
                    ((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
                    Log.d(TAG, "refresh excuted");
                }
            });
        }
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
                    bottom.setText(device.getIpAdress().toString() + "(port " + device.getPort() + ") Room owner: " + device.m_isRoomOwner);
                }
            }
            return v;
        }
    }

    public void addDevice(InetAddress adr, int port, String devieName, boolean isRoomOwner){
        Log.d(TAG, "addDevice: " + devieName + "/" + adr.toString());
        DeviceInRoom device = new DeviceInRoom(adr, port, devieName, isRoomOwner);
        if(!isExistInList(device)){
            m_devices.add(device);
            if (getListAdapter() == null){
                this.setListAdapter(new DeviceListAdapter(getActivity(), R.layout.item_device, m_devices));
            }
            refresh();
        }
    }

    public void addDevice(DeviceInRoom device){
        Log.d(TAG, "addDevice from device: " + device.getDeviceName() + "/" + device.getIpAdress().toString() + "/" + device.getPort());
        if(!isExistInList(device)){
            Log.d(TAG, "addDevice: " + device.getDeviceName() + "/" + device.getIpAdress());
            m_devices.add(device);
            if (getListAdapter() == null){
                this.setListAdapter(new DeviceListAdapter(getActivity(), R.layout.item_device, m_devices));
            }
            refresh();
        }
    }

    public void removeDevice(String name, InetAddress adr){
        Iterator<DeviceInRoom> i = m_devices.iterator();
        while (i.hasNext()) {
            DeviceInRoom deviceInList = i.next();
            Log.d(TAG, "device in list:" + deviceInList.getDeviceName());
            if(deviceInList.equal(name, adr)) {
                i.remove();
                refresh();
            }
        }
    }

    public void removeDevice(DeviceInRoom device){
        Iterator<DeviceInRoom> i = m_devices.iterator();
        while (i.hasNext()) {
            DeviceInRoom deviceInList = i.next();
            Log.d(TAG, "device in list:" + deviceInList.getDeviceName());
            if(deviceInList.equal(device.getDeviceName(), device.getIpAdress())) {
                i.remove();
                refresh();
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
        m_devices.clear();
        ((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
    }

}

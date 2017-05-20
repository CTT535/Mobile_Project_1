package hcmus.angtonyvincent.firebaseauthentication.list_room;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hcmus.angtonyvincent.firebaseauthentication.R;
import hcmus.angtonyvincent.firebaseauthentication.room.DeviceInRoom;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomActivity;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomInfo;

/**
 * Created by VUDAI on 5/14/2017.
 */

public class ListRoomFragment extends ListFragment {
    private List<RoomInfo> m_rooms = new ArrayList<RoomInfo>();
    View mContentView = null;
    private final String TAG = "roomListFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "on tactivity create");
        super.onActivityCreated(savedInstanceState);
        if (getListAdapter() == null) {
            this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.item_room, m_rooms));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_list_room, null);
        return mContentView;
    }


    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final RoomInfo room = (RoomInfo) getListAdapter().getItem(position);
        DeviceInRoom roomOwner = room.getRoomOwner();
        String roomName = room.getroomName();
        //go to the room
        Bundle b = new Bundle();
        b.putString("roomName", room.getroomName());
        b.putBoolean("isRoomOwner", false);
        b.putString("roomOwnerName", roomOwner.getDeviceName());
        b.putString("rommOwnerAddress", roomOwner.getIpAdress().toString());
        b.putInt("roomOwnerPort", roomOwner.getPort());
        Intent listRoomIntent = new Intent(getActivity(), RoomActivity.class);
        listRoomIntent.putExtra("bundle", b);
        startActivity(listRoomIntent);
    }

    public void refresh(){
        Log.d(TAG, "start refresh");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "refresh on excuting");
                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
                Log.d(TAG, "refresh excuted");
            }
        });
    }

    /**
     * Array adapter for ListFragment that maintains RoomInfo list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<RoomInfo> {

        private List<RoomInfo> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<RoomInfo> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.item_room, null);
            }
            RoomInfo room = items.get(position);
            if (room != null) {
                TextView top = (TextView) v.findViewById(R.id.room_name);
                TextView bottom = (TextView) v.findViewById(R.id.room_details);
                if (top != null) {
                    top.setText(room.getroomName());
                }
                if (bottom != null) {
                    bottom.setText(room.getRoomOwner().getIpAdress().toString());
                }
            }

            return v;

        }
    }

    public void addRoom(InetAddress adr, int port, String roomName){
        Log.d(TAG, "addRoom: " + roomName + "/" + adr.toString());
        RoomInfo room = new RoomInfo(roomName, new DeviceInRoom(adr, port, "", true));
        if(!isExistInList(room)){
            m_rooms.add(room);
            if (getListAdapter() == null){
                this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.item_room, m_rooms));
            }
            refresh();
        }
    }

    public void removeRoom(String name){
        Log.d(TAG, "remove:" + name);
        Iterator<RoomInfo> i = m_rooms.iterator();
        while (i.hasNext()) {
            RoomInfo roomInList = i.next();
            Log.d(TAG, "room in list:" + roomInList.getroomName());
            if(roomInList.equal(new RoomInfo(name, null))) {
                i.remove();
                refresh();
            }
        }
    }

    public boolean isExistInList(RoomInfo room){
        for(RoomInfo roomInList:m_rooms){
            if (room.equal(roomInList)){
                return true;
            }
        }
        return false;
    }

    public void clearPeers() {
        m_rooms.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }
}

package hcmus.angtonyvincent.firebaseauthentication.room;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

import hcmus.angtonyvincent.firebaseauthentication.connection.Connection;

/**
 * Created by VUDAI on 5/17/2017.
 */

public class DeviceInRoom {
    protected String m_deviceName;
    protected InetAddress m_ipAdress;
    protected int m_port;
    boolean m_isRoomOwner;
    public final String TAG = "DeviceInRoom";

    public DeviceInRoom(InetAddress ipAdress, int port, String deviceName, boolean isRoomOwner){
        m_ipAdress = ipAdress;
        m_deviceName = deviceName;
        m_port = port;
        m_isRoomOwner = isRoomOwner;
    }

    public DeviceInRoom(JSONObject jsonObject){
        try {
            m_deviceName = jsonObject.get("nameDevice").toString();
            String strAdr = jsonObject.get("inetAddress").toString();
            String intnetFormat = strAdr.substring(1);
            m_ipAdress = InetAddress.getByName(intnetFormat);
            m_port = Integer.parseInt(jsonObject.get("port").toString());
            if(jsonObject.get("isRoomOwner").toString().equals("false")){
                m_isRoomOwner = false;
            }else{
                m_isRoomOwner = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public boolean isRoomOwner(){
        return m_isRoomOwner;
    }

    public InetAddress getIpAdress(){
        return  m_ipAdress;
    }

    public String getDeviceName(){
        return m_deviceName;
    }

    public boolean equal(String nameDevice, InetAddress adr){
        if (adr == null){
            return m_deviceName.equals(nameDevice);
        }
        return m_ipAdress.toString().equals(adr.toString());
    }

    public int getPort(){
        return m_port;
    }

    public void sendMessage(String msg) {
        Connection.sendMessage(msg, this.m_ipAdress, this.m_port);
    }

    public JSONObject toJSONObject(){
        try {
            JSONObject outerObject = new JSONObject();
            outerObject.put("inetAddress", this.getIpAdress().toString());
            outerObject.put("nameDevice", m_deviceName);
            outerObject.put("port", m_port);
            outerObject.put("isRoomOwner", m_isRoomOwner);
            return outerObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

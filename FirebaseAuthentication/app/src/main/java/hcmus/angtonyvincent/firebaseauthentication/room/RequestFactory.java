package hcmus.angtonyvincent.firebaseauthentication.room;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by VUDAI on 5/20/2017.
 */

public class RequestFactory {

    public static final String SIGNAL_REQUEST_PATICIPATE = "01";
    public static final String SIGNAL_REQUEST_START_GAME = "02";
    public static final String SIGNAL_REQUEST_GET_OUT = "04";
    public static final String SIGNAL_RESPONSE_LIST_DEVICE = "03"; // send a response with the list of member in room 03
    public static final String SIGNAL_GET_LIST_DEVICE = "03";      // receive the list 03


    public static JSONObject createRequestParticipate(DeviceInRoom srcDevice){
        JSONObject obj = new JSONObject();
        try {
            obj.put("signal", SIGNAL_REQUEST_PATICIPATE);
            obj.put("newMember", srcDevice.toJSONObject());
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject createRequestStartGame(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("signal", SIGNAL_REQUEST_START_GAME);
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject createReponseListDevice(ListDeviceInRoomFragment listDevice){
        JSONObject obj = new JSONObject();
        try {
            obj.put("signal", SIGNAL_RESPONSE_LIST_DEVICE);
            obj.put("listDevice", listDevice.toJSONArray());
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject createRequestOutOfRoom(DeviceInRoom srcDevice){
        JSONObject obj = new JSONObject();
        try {
            obj.put("signal", SIGNAL_REQUEST_GET_OUT);
            obj.put("sourceDevice", srcDevice.toJSONObject());
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

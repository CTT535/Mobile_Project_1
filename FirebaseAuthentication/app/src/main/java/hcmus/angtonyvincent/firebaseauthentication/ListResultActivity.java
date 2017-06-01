package hcmus.angtonyvincent.firebaseauthentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hcmus.angtonyvincent.firebaseauthentication.connection.ConnectionActionListener;
import hcmus.angtonyvincent.firebaseauthentication.room.DeviceInRoom;
import hcmus.angtonyvincent.firebaseauthentication.room.ListDeviceInRoomFragment;
import hcmus.angtonyvincent.firebaseauthentication.room.RequestFactory;

public class ListResultActivity extends AppCompatActivity implements ConnectionActionListener{
    static Activity instance;
    static List<Result> m_listResult;
    static final String TAG = "ListResult";
    static String strResult = "";
    static TextView result;
    static Button continueButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_result);
        instance = this;
        result = (TextView)findViewById(R.id.tv_result);
        result.setText(strResult);

        continueButton = (Button) findViewById(R.id.result_continue_button);
        continueButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent playIntent = new Intent(ListResultActivity.this, MainActivity.class);
                startActivity(playIntent);
                finish();
            }
        });
    }

    public static void addResult(Result res){
        Log.d(TAG, "add result: " + res.toString());
        Log.d(TAG, "ip address: " + res.getDevice().getIpAdress().toString());
        Log.d(TAG, "level: " + res.getLevel().toString());
        Log.d(TAG, "end time: " + res.getTime().toString());
        strResult += res.getDevice().getIpAdress() + " " + res.getLevel().toString() + "\n";
        if(m_listResult == null){
            m_listResult = new ArrayList<>();
        }
        if(instance != null){
            //this activity is running
            instance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    result.setText(strResult);
                }
            });
        }
        m_listResult.add(res);
        if(m_listResult.size() == ListDeviceInRoomFragment.getNumberDeviceInRoom()){
            // find the winner
            Collections.sort(m_listResult, new Comparator<Result>(){
                @Override
                public int compare(Result obj1, Result obj2) {
                    // ## descending order
                    return obj2.CompareTo(obj1);
                }
            });
            //winner is the first element
            //if this activity is running, update on screen
            if(instance != null){
                for (Result i: m_listResult) {
                    strResult += i.getDevice().getIpAdress() + " " + i.getLevel().toString() + "\n";
                }
            }
        }
    }

    public static void clear(){
        if(m_listResult != null) {
            m_listResult.clear();
        }
        //this activity is running then update on screen
        if(instance != null){
            instance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    result.setText(strResult);
                }
            });
        }
        strResult = "";
    }

    @Override
    public void onMessageReceived(Socket socket, String message) {
        new Thread(new TreatRequestTask(message)).start();
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
                    case RequestFactory.SIGNAL_NOTIFICATE_RESULT:
                        JSONObject srcDevice = (JSONObject) jsonObj.get("sourceDevice");
                        int level = jsonObj.getInt("level");
                        int timeGameEnd = jsonObj.getInt("time");
                        ListResultActivity.addResult(new ListResultActivity.Result(new DeviceInRoom(srcDevice), level, timeGameEnd));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Result{
        DeviceInRoom m_device;
        int m_level;
        int m_timeEndGame;

        Result(DeviceInRoom device, int level, int timeEndGame){
            m_device = device;
            m_level = level;
            m_timeEndGame = timeEndGame;
        }

        public int CompareTo(Result res){
            if(m_level > res.m_level){
                return 1;
            }else if(m_level == res.m_level){
                if(m_timeEndGame > res.m_timeEndGame){
                    return 1;
                }else{
                    return -1;
                }
            }
            return -1;
        }

        public DeviceInRoom getDevice(){
            return m_device;
        }

        public Integer getLevel(){
            return m_level;
        }

        public Integer getTime(){
            return m_timeEndGame;
        }
    }
}

package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.List;

import hcmus.angtonyvincent.firebaseauthentication.connection.ConnectionActionListener;
import hcmus.angtonyvincent.firebaseauthentication.room.DeviceInRoom;
import hcmus.angtonyvincent.firebaseauthentication.room.ListDeviceInRoomFragment;
import hcmus.angtonyvincent.firebaseauthentication.room.RequestFactory;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomActivity;

import static hcmus.angtonyvincent.firebaseauthentication.PatternActivity.LEVEL_MESSAGE;
import static hcmus.angtonyvincent.firebaseauthentication.PatternActivity.PATTERN_MESSAGE;

public class GameplayActivity extends AppCompatActivity implements ConnectionActionListener {

    private Button surrender;
    private TextView level;
    private TextView time;

    private CountDownTimer timer;

    private PatternDrawer patternDrawer;

    private static String TAG = "GameplayActivity";

    private String correctPattern;
    private int currentLevel;

    public static final String SUCCESS_MESSAGE = "SUCCESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get the Intent that started this activity and extract the string
        Bundle extras = getIntent().getExtras();
        currentLevel = extras.getInt(LEVEL_MESSAGE);
        correctPattern = extras.getString(PATTERN_MESSAGE);

        time = (TextView) findViewById(R.id.gameplay_time_text);
        level = (TextView) findViewById(R.id.gameplay_level_text);
        level.setText("Level " + currentLevel);

        surrender = (Button) findViewById(R.id.gameplay_surrender_button);
        surrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel(); // cancel the countdown

                if(MainActivity.getPlayMode() == MainActivity.MODE_MULTI_PLAYER){
                    ListResultActivity.addResult(new ListResultActivity.Result(RoomActivity.getThisDeviceInRoom(), currentLevel, (int)System.currentTimeMillis()));
                    ListDeviceInRoomFragment.sendMessageToAll(RequestFactory.createSignalResultNotification(RoomActivity.getRoomOwner(), currentLevel, (int) System.currentTimeMillis()).toString());
                    Intent surrenderIntent = new Intent(GameplayActivity.this, ListResultActivity.class);
                    startActivity(surrenderIntent);
                    finish();
                }
                else {
                    Intent surrenderIntent = new Intent(GameplayActivity.this, ResultActivity.class);
                    startActivity(surrenderIntent);
                    finish();
                }
            }
        });

        patternDrawer = (PatternDrawer) findViewById(R.id.pattern);
        patternDrawer.setOnPatternListener(new PatternDrawer.OnPatternListener() {
            @Override
            public void onPatternDetected(List<PatternDrawer.Cell> pattern, String SimplePattern) {
                Log.e("SimplePattern", SimplePattern);
                // There are 2 solutions for each pattern
                if (SimplePattern.equals(correctPattern)
                        || SimplePattern.equals(new StringBuffer(correctPattern).reverse().toString())) {
                    currentLevel++; // level up

                    timer.cancel(); // cancel the countdown

                    patternDrawer.setDisplayMode(PatternDrawer.DisplayMode.Correct);

                    Intent continueIntent = new Intent(GameplayActivity.this, PatternActivity.class);
                    continueIntent.putExtra(SUCCESS_MESSAGE, currentLevel);
                    startActivity(continueIntent);
                    finish();
                } else {
                    patternDrawer.setDisplayMode(PatternDrawer.DisplayMode.Wrong);
                }
                super.onPatternDetected(pattern, SimplePattern);
            }
        });

        // 10 seconds coutdowm timer
        timer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                time.setText(millisUntilFinished / 1000 + "s left");
            }

            public void onFinish() {
                time.setText("Time over");

                if(MainActivity.getPlayMode() == MainActivity.MODE_MULTI_PLAYER){
                    ListResultActivity.addResult(new ListResultActivity.Result(RoomActivity.getThisDeviceInRoom(), currentLevel, (int)System.currentTimeMillis()));
                    ListDeviceInRoomFragment.sendMessageToAll(RequestFactory.createSignalResultNotification(RoomActivity.getRoomOwner(), currentLevel, (int) System.currentTimeMillis()).toString());
                    Intent surrenderIntent = new Intent(GameplayActivity.this, ListResultActivity.class);
                    startActivity(surrenderIntent);
                    finish();
                }
                else {
                    Intent timeUpIntent = new Intent(GameplayActivity.this, ResultActivity.class);
                    startActivity(timeUpIntent);
                    finish();
                }
            }
        };
        timer.start();
    } // onCreate

    @Override
    public void onBackPressed () {
        // do nothing
    } // onBackPressed

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
}
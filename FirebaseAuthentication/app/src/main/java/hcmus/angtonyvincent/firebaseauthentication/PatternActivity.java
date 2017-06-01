package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.Random;

import hcmus.angtonyvincent.firebaseauthentication.connection.Connection;
import hcmus.angtonyvincent.firebaseauthentication.connection.ConnectionActionListener;
import hcmus.angtonyvincent.firebaseauthentication.room.DeviceInRoom;
import hcmus.angtonyvincent.firebaseauthentication.room.ListDeviceInRoomFragment;
import hcmus.angtonyvincent.firebaseauthentication.room.RequestFactory;
import hcmus.angtonyvincent.firebaseauthentication.room.RoomActivity;

public class PatternActivity extends AppCompatActivity implements ConnectionActionListener {

    private TextView level;
    private TextView time;
    private ImageView image;
    private CountDownTimer timer;
    private Button continueBtn;

    private int currentLevel;
    private String correctPattern;

    private final int MAX_LEVEL = 10;
    public static final String LEVEL_MESSAGE = "LEVEL";
    public static final String PATTERN_MESSAGE = "PATTERN";
    private static String TAG = "PatternActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle extras = getIntent().getExtras();

        if(MainActivity.getPlayMode() == MainActivity.MODE_MULTI_PLAYER){
            //inform that this activity will receive the request
            new Connection(this);
            //called from RoomActivity
            if(extras == null){
                //reset result list
                ListResultActivity.clear();
            }
        }

        if (extras != null) {
            // this activity is called from GameActivity
            currentLevel = extras.getInt(GameplayActivity.SUCCESS_MESSAGE);

            if (currentLevel > MAX_LEVEL) {
                if(MainActivity.getPlayMode() == MainActivity.MODE_MULTI_PLAYER) {
                    ListResultActivity.addResult(new ListResultActivity.Result(RoomActivity.getThisDeviceInRoom(), currentLevel, (int) System.currentTimeMillis()));
                    ListDeviceInRoomFragment.sendMessageToAll(RequestFactory.createSignalResultNotification(RoomActivity.getRoomOwner(), currentLevel, (int) System.currentTimeMillis()).toString());
                    Intent winIntent = new Intent(this, ListResultActivity.class);
                    startActivity(winIntent);
                    finish();
                }
                else {
                    Intent quitIntent = new Intent(PatternActivity.this, ResultActivity.class);
                    startActivity(quitIntent);
                    finish();
                }

                return; // exit onCreate
            }
        } else {
            // this activity is called from MainActivity
            currentLevel = 1;
        }

        time = (TextView) findViewById(R.id.pattern_time_text);
        level = (TextView) findViewById(R.id.pattern_level_text);
        level.setText("Level " + currentLevel);

        continueBtn = (Button) findViewById(R.id.pattern_continue_button);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel(); // cancel the countdown

                Intent intent = new Intent(PatternActivity.this, GameplayActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(LEVEL_MESSAGE, currentLevel);
                extras.putString(PATTERN_MESSAGE, correctPattern);
                intent.putExtras(extras);
                startActivity(intent);
                finish();
            }
        });

        image = (ImageView) findViewById(R.id.pattern_image);
        displayPattern();

        // 5 seconds coutdowm timer
        timer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                time.setText(millisUntilFinished / 1000 + "s left");
            }

            public void onFinish() {
                time.setText("Time over");

                Intent intent = new Intent(PatternActivity.this, GameplayActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(LEVEL_MESSAGE, currentLevel);
                extras.putString(PATTERN_MESSAGE, correctPattern);
                intent.putExtras(extras);
                startActivity(intent);
                finish();
            }
        };
        timer.start();
    } // onCreate

    @Override
    public void onBackPressed() {
        // do nothing
    } // onBackPressed

    public  void displayPattern() {
        boolean head = new Random().nextBoolean(); // Flip a coin

        switch (currentLevel) {
            case 1:
                if (head) {
                    // 1
                    correctPattern = "1538";
                }
                else {
                    // Y
                    correctPattern = "4258";
                }
                break;

            case 2:
                if (head) {
                    // L
                    correctPattern = "14789";
                }
                else {
                    // A
                    correctPattern = "74269";
                }
                break;

            case 3:
                if (head) {
                    // C
                    correctPattern = "3214789";
                }
                else {
                    // U
                    correctPattern = "1478963";
                }
                break;

            case 4:
                if(head) {
                    // Z
                    correctPattern = "1235789";
                }
                else {
                    // N
                    correctPattern = "7415963";
                }
                break;

            case 5:
                if(head) {
                    // S
                    correctPattern = "321456987";
                }
                else {
                    // G
                    correctPattern = "321478965";
                }
                break;

            case 6:
                if(head) {
                    // P
                    correctPattern = "4563217";
                }
                else {
                    // B
                    correctPattern = "74123568";
                }
                break;

            case 7:
                if(head) {
                    correctPattern = "15896247";
                }
                else {
                    correctPattern = "68321547";
                }
                break;

            case 8:
                if(head) {
                    correctPattern = "76183";
                }
                else {
                    correctPattern = "741685239";
                }
                break;

            case 9:
                if (head) {
                    correctPattern = "4231786";
                }
                else {
                    correctPattern = "65213987";
                }
                break;

            case 10:
                if (head) {
                    correctPattern = "18349276";
                }
                else {
                    correctPattern = "427538619";
                }
                break;
        }

        // set the pattern to display
        image.setImageResource(getResources().getIdentifier("p" + correctPattern, "drawable", getPackageName()));
    } // displayPattern

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

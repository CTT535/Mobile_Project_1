package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class PatternActivity extends AppCompatActivity {

    private TextView time;
    private ImageView image;
    private CountDownTimer timer;

    private int currentLevel;
    private String correctPattern;

    private final int MAX_LEVEL = 10;
    public static final String LEVEL_MESSAGE = "LEVEL";
    public static final String PATTERN_MESSAGE = "PATTERN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // this activity is called from GameActivity
            currentLevel = extras.getInt(GameplayActivity.SUCCESS_MESSAGE);

            if (currentLevel > MAX_LEVEL) {
                Intent quitIntent = new Intent(PatternActivity.this, ResultActivity.class);
                startActivity(quitIntent);
                finish();
                
                return; // exit onCreate
            }
        } else {
            // this activity is called from MainActivity
            currentLevel = 1;
        }

        time = (TextView) findViewById(R.id.pattern_time_text);
        image = (ImageView) findViewById(R.id.pattern_image);

        switch (currentLevel) {
            case 1:
                correctPattern = "14789";
                image.setImageResource(R.drawable.p14789);
                break;
            case 2:
                correctPattern = "1235789";
                image.setImageResource(R.drawable.p1235789);
                break;
            case 3:
                correctPattern = "7415963";
                image.setImageResource(R.drawable.p7415963);
                break;
            case 4:
                correctPattern = "321456987";
                image.setImageResource(R.drawable.p321456987);
                break;
            case 5:
                correctPattern = "15896247";
                image.setImageResource(R.drawable.p15896247);
                break;
            case 6:
                correctPattern = "68321547";
                image.setImageResource(R.drawable.p68321547);
                break;
            case 7:
                correctPattern = "741236985";
                image.setImageResource(R.drawable.p741236985);
                break;
            case 8:
                correctPattern = "76183";
                image.setImageResource(R.drawable.p76183);
                break;
            case 9:
                correctPattern = "18349276";
                image.setImageResource(R.drawable.p18349276);
                break;
            case 10:
                correctPattern = "741685239";
                image.setImageResource(R.drawable.p741685239);
                break;
        }

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
}

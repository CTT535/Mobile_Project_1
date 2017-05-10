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

import java.util.List;

import static hcmus.angtonyvincent.firebaseauthentication.PatternActivity.LEVEL_MESSAGE;
import static hcmus.angtonyvincent.firebaseauthentication.PatternActivity.PATTERN_MESSAGE;

public class GameplayActivity extends AppCompatActivity {

    private Button surrender;
    private TextView level;
    private TextView time;

    private CountDownTimer timer;

    private PatternDrawer patternDrawer;

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
                Intent surrenderIntent = new Intent(GameplayActivity.this, ResultActivity.class);
                startActivity(surrenderIntent);
                finish();
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

                Intent timeUpItent = new Intent(GameplayActivity.this, ResultActivity.class);
                startActivity(timeUpItent);
                finish();
            }
        };
        timer.start();
    } // onCreate

    @Override
    public void onBackPressed () {
        // do nothing
    } // onBackPressed

}
package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class PatternActivity extends AppCompatActivity {

    private TextView timeText;
    private ImageView patternImage;
    private Level[] levels;
    private int currentLevel;

    private final int MAX_LEVEL = 10;
    public static final String LEVEL_MESSAGE = "LEVEL";
    public static final String PATTERN_MESSAGE = "PATTERN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        levels = new Level[MAX_LEVEL];
        levels[0] = new Level(1, "14789");
        levels[1] = new Level(1, "1235789");
        levels[2] = new Level(1, "7415963");
        levels[3] = new Level(1, "321456987");
        levels[4] = new Level(2, "15896247");
        levels[5] = new Level(2, "68321547");
        levels[6] = new Level(2, "741236985");
        levels[7] = new Level(3, "76183");
        levels[8] = new Level(3, "18349276");
        levels[9] = new Level(3, "741685239");

        currentLevel = 1;

        timeText = (TextView) findViewById(R.id.pattern_time_text);

        patternImage = (ImageView) findViewById(R.id.pattern_image);
        patternImage.setImageResource(R.drawable.p14789);

        // 5 seconds coutdowm timer
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                timeText.setText(millisUntilFinished / 1000 + "s left");
            }

            public void onFinish() {
                Intent intent = new Intent(PatternActivity.this, GameplayActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(LEVEL_MESSAGE, currentLevel);
                extras.putString(PATTERN_MESSAGE, levels[currentLevel - 1].getPattern());
                intent.putExtras(extras);
                startActivity(intent);
                finish();
            }
        }.start();
    }
}

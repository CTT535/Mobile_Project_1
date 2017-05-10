package hcmus.angtonyvincent.firebaseauthentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class GameplayActivity extends AppCompatActivity {

    private Button surrender;
    private TextView level;

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
        currentLevel = extras.getInt(PatternActivity.LEVEL_MESSAGE);
        correctPattern = extras.getString(PatternActivity.PATTERN_MESSAGE);

        level = (TextView) findViewById(R.id.gameplay_level_text);
        level.setText("LEVEL " + currentLevel);

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
                // There are two solution for a pattern
                if (SimplePattern.equals(correctPattern)
                        || SimplePattern.equals(new StringBuffer(correctPattern).reverse().toString())) {

                    currentLevel++;
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
    } // onCreate

    @Override
    public void onBackPressed () {
        // do nothing
    } // onBackPressed
}
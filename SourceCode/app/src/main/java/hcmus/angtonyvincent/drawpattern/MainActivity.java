package hcmus.angtonyvincent.drawpattern;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button surrender;
    TextView instruction;

    private String correctPattern;
    private String[] simplePattern2 = {"15", "59", "35", "57", "25", "58", "45", "56"};
    private String[] simplePattern3 = {"159", "951", "357", "753","258", "852","456", "654"};
    private String[] simplePattern4 = {"1245", "2356", "4578", "5689", "1254", "2365", "4587", "5698"};
    private String[] simplePattern5 = {"12357", "75321", "32159", "95123", "78951", "15987", "98753", "35789"};
    private String[] simplePattern6 = {"123456", "321654", "456789", "654987", "123654", "321456", "456987", "654789"};
    private String[] simplePattern7 = {"1236987", "3214789", "1478963", "3698741", "4269871", "6247893", "1235789", "3215987"};
    private String[] simplePattern8 = {"12369874", "36987412", "98741236", "74123698", "12365478", "32145698", "78965412", "98745632"};
    private String[] simplePattern9 = {"123456789", "321654987", "123698745", "321478965", "123654789", "321456987", "784951623", "326159487"};
    private MaterialLockView materialLockView;

    private int level;
    private int passedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        level = 1;
        passedPoint = 2;

        instruction = (TextView) findViewById(R.id.textViewInstruction);

        surrender = (Button) findViewById(R.id.buttonSurrender);
        surrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPattern();
            }
        });

        newPattern();

        materialLockView = (MaterialLockView) findViewById(R.id.pattern);
        materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                Log.e("SimplePattern", SimplePattern);
                if (SimplePattern.equals(correctPattern)) {

                    materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Correct);

                    level++; // level up

                    newPattern();

                } else {

                    materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);

                }
                super.onPatternDetected(pattern, SimplePattern);
            }
        });
    }

    public void newPattern() {
        correctPattern = randomPattern();
        instruction.setText("Level " + level + "\nDraw this pattern : " + correctPattern);
    }

    public String randomPattern() {
        passedPoint = level / 4 + 2; // min = 2
        if (passedPoint > 9) {
            passedPoint = 9; // max = 9
        }

        Random r = new Random();
        int i = r.nextInt(8); // 0 => 7

        String result = "";

        switch (passedPoint) {
            case 2:
                result = simplePattern2[i];
                break;

            case 3:
                result = simplePattern3[i];
                break;

            case 4:
                result = simplePattern4[i];
                break;

            case 5:
                result = simplePattern5[i];
                break;

            case 6:
                result = simplePattern6[i];
                break;

            case 7:
                result = simplePattern7[i];
                break;

            case 8:
                result = simplePattern8[i];
                break;

            case 9:
                result = simplePattern9[i];
                break;
        }

        return result;
    }
}

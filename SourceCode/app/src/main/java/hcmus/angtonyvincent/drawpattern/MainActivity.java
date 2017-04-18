package hcmus.angtonyvincent.drawpattern;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button refresh;
    TextView instruction;
    private String correctPattern;
    private MaterialLockView materialLockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        instruction = (TextView) findViewById(R.id.textViewInstruction);

        refresh = (Button) findViewById(R.id.buttonRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {
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
        instruction.setText(correctPattern);
    }

    public String randomPattern() {
        String result = "";

        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = 1; i < 10; i++) {
            list.add(new Integer(i));
        }

        Collections.shuffle(list);

        for (int i = 0; i < 3; i++) {
            result += list.get(i).toString();
        }

        return result;
    }
}

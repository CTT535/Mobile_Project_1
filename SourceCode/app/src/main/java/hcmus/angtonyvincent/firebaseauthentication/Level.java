package hcmus.angtonyvincent.firebaseauthentication;

/**
 * Created by Ang Tony Vincent on 5/10/2017.
 */

public class Level {
    private int mDificulty;
    private String mPattern;

    Level(int dificulty, String pattern) {
        setDificulty(dificulty);
        setPattern(pattern);
    }

    public int getDificulty() {
        return mDificulty;
    }

    public void setDificulty(int dificulty) {
        mDificulty = dificulty;
    }

    public String getPattern() {
        return mPattern;
    }

    public void setPattern(String pattern) {
        mPattern = pattern;
    }

    public String getImageResource() {
        return "p" + mPattern;
    }
}

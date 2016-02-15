package zhang.jian.greetingapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import zhang.jian.greetingapp.R;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int SPLASH_TIME_OUT = 1000;
    private boolean mIsBackButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        launchMainActivity();
    }

    private void launchMainActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!mIsBackButtonPressed){
                    startMainActivity();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        mIsBackButtonPressed = true;
        finish();
    }
}

package com.example.administrator.my.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.example.administrator.my.R;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout sl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initData();
        initAnimation();
        newThread();

    }

    private void initData() {
        sl_root = (LinearLayout) findViewById(R.id.sl_root);
    }

    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(3000);
        sl_root.startAnimation(alphaAnimation);
    }
    private void newThread() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                enterHome();
            }
        }.start();
    }

    private void enterHome() {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

}

package com.example.administrator.my.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.InputStream;

import com.example.administrator.my.R;
import com.example.administrator.my.utils.SpUtil;

public class SplashActivity extends AppCompatActivity {
    private static final int ENTER_HOME = 100;
    private TextView tv_version_name;
    private RelativeLayout rl_root;
    private Handler mHandler = new Handler() {
        @Override
        //alt+ctrl+向下箭头,向下拷贝相同代码
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ENTER_HOME:
                    //进入应用程序主界面,activity跳转过程
                    enterHome();
                    break;
            }
        }
    };
    private InputStream stream;
    private FileOutputStream fos;
    private LinearLayout sl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //初始化UI
        initUI();
        //初始化数据
        initData();
        //初始化动画
        initAnimation();
        newThread();
    }
    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(3000);
        sl_root.startAnimation(alphaAnimation);
    }

    private void initUI() {
        sl_root = (LinearLayout) findViewById(R.id.sl_root);
    }

    private void initData() {
        mHandler.sendEmptyMessageDelayed(ENTER_HOME, 3000);
    }

    private void newThread() {
        final Message msg = Message.obtain();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(msg);
            }
        }.start();
    }
    private void enterHome() {
        boolean is_first_enter = SpUtil.getBoolean("is_first_enter", true);
        if(is_first_enter){
            Intent intent = new Intent(this, GuideActivity.class);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }
}


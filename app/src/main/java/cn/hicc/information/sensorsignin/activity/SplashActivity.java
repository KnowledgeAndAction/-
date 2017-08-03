package cn.hicc.information.sensorsignin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.hicc.information.sensorsignin.R;
import cn.hicc.information.sensorsignin.utils.SpUtil;

/**
 * 启动页——周凯歌
 */
public class SplashActivity extends AppCompatActivity {

    private static final int ENTER_HOME = 100;
    private RelativeLayout sl_root;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ENTER_HOME:
                    // 进入应用程序主界面,activity跳转过程
                    enterHome();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        // 初始化UI
        initUI();

        initData();

        // 初始化动画
        initAnimation();

        // 开启一个子线程
        newThread();
    }

    // 初始化动画
    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(3000);
        sl_root.startAnimation(alphaAnimation);
    }

    // 初始化UI
    private void initUI() {
        sl_root = (RelativeLayout) findViewById(R.id.sl_root);
    }

    private void initData() {
        mHandler.sendEmptyMessageDelayed(ENTER_HOME, 3000);
    }

    // 开启一个子线程
    private void newThread() {
        final Message msg = Message.obtain();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e)   {
                    e.printStackTrace();
                }
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    // 进入应用
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


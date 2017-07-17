package cn.hicc.information.sensorsignin.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.hicc.information.sensorsignin.R;
import com.sensoro.cloud.SensoroManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.hicc.information.sensorsignin.fragment.ActivityFragment;
import cn.hicc.information.sensorsignin.fragment.HistoryFragment;
import cn.hicc.information.sensorsignin.fragment.SettingFragment;
import cn.hicc.information.sensorsignin.model.ExitEvent;
import cn.hicc.information.sensorsignin.model.TabItem;
import cn.hicc.information.sensorsignin.service.SensorService;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import cn.hicc.information.sensorsignin.view.MyTabLayout;

public class MainActivity extends AppCompatActivity {

    private SensoroManager sensoroManager;
    private MyTabLayout myTablayout_bottom;
    private ViewPager viewPager;
    private ArrayList<TabItem> tabs;
    private static Boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensoroManager = SensoroManager.getInstance(MainActivity.this);


        // 初始化控件
        initWidget();

        // 检查蓝牙是否可用
        checkBluetooth();

        // 注册监听退出登录的事件
        EventBus.getDefault().register(this);
    }

    // 检查蓝牙是否可用
    private void checkBluetooth() {
        if (!sensoroManager.isBluetoothEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, 0);
        } else {
            // 开启服务
            startService(new Intent(this, SensorService.class));
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        viewPager = (ViewPager) findViewById(R.id.viewPager_top);
        myTablayout_bottom = (MyTabLayout) findViewById(R.id.myTablayout_bottom);

        initLayout();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                // 蓝牙可用
                if (sensoroManager.isBluetoothEnabled()) {
                    startService(new Intent(MainActivity.this, SensorService.class));
                }
                break;
        }
    }

    /**
     * 初始化布局
     */
    private void initLayout() {
        tabs = new ArrayList<>();
        tabs.add(new TabItem(R.drawable.bottom_activity_selector, R.string.tab_activity, ActivityFragment.class));
        tabs.add(new TabItem(R.drawable.bottom_history_selector, R.string.tab_history, HistoryFragment.class));
        tabs.add(new TabItem(R.drawable.bottom_setting_selector, R.string.tab_setting, SettingFragment.class));
        myTablayout_bottom.initData(tabs, new MyTabLayout.OnTabClickListener() {
            @Override
            public void onTabClick(TabItem tabItem) {
                viewPager.setCurrentItem(tabs.indexOf(tabItem));
            }
        });
        myTablayout_bottom.setCurrentTab(0);

        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                myTablayout_bottom.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 设置底部条目及对应界面的适配器
     */
    class FragmentAdapter extends FragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                return tabs.get(position).tagFragmentClz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }
    }

    // 接收退出登录的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ExitEvent event) {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        stopService(new Intent(this,SensorService.class));
    }

    // 监听返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            exitBy2Click();
        }
        return false;
    }

    // 双击退出程序
    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            ToastUtil.show("再按一次退出程序");
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
        }
    }

}

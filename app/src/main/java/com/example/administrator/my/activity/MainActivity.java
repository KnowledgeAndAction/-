package com.example.administrator.my.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.my.R;
import com.example.administrator.my.fragment.ActivityFragment;
import com.example.administrator.my.fragment.HistoryFragment;
import com.example.administrator.my.fragment.SettingFragment;
import com.example.administrator.my.model.ExitEvent;
import com.example.administrator.my.model.TabItem;
import com.example.administrator.my.utils.Logs;
import com.example.administrator.my.utils.SpUtil;
import com.example.administrator.my.view.MyTabLayout;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SensoroManager sensoroManager;
    private MyTabLayout myTablayout_bottom;
    private ViewPager viewPager;
    private ArrayList<TabItem> tabs;
    private boolean isFirst = true;
    private String serialNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensoroManager = SensoroManager.getInstance(MainActivity.this);

        // 设置sdk
        setSDK();

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
            // 开启SDK
            startSDK();
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


    // 开启SDK
    private void startSDK() {
        /**
         * 设置启用云服务 (上传传感器数据，如电量、UMM等)。如果不设置，默认为关闭状态。
         **/
        sensoroManager.setCloudServiceEnable(true);
        /**
         * 启动 SDK 服务
         **/
        try {
            sensoroManager.startService();
        } catch (Exception e) {
            e.printStackTrace(); // 捕获异常信息
        }
    }

    /**
     * 设置SDK
     */
    private void setSDK() {
        BeaconManagerListener beaconManagerListener = new BeaconManagerListener() {
            /**
             * 发现传感器
             */
            @Override
            public void onNewBeacon(Beacon beacon) {
                //序列号
                serialNumber = beacon.getSerialNumber();
                SpUtil.putString("serialNumber", serialNumber);

                if (isFirst) {
                    Logs.d("发现云子");
                    Intent intent = new Intent();
                    intent.setAction("GET_YUNZI_ID");
                    intent.putExtra("yunzi", serialNumber);
                    sendBroadcast(intent);
                    isFirst = false;
                } else if (SpUtil.getBoolean("destroy", true)) {
                    Logs.d("发现云子");
                    Intent intent = new Intent();
                    intent.setAction("GET_YUNZI_ID");
                    intent.putExtra("yunzi", serialNumber);
                    sendBroadcast(intent);
                } else if (!SpUtil.getString("serialNumber", "").equals(serialNumber)) {
                    Logs.d("发现云子");
                    Intent intent = new Intent();
                    intent.setAction("GET_YUNZI_ID");
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onGoneBeacon(Beacon beacon) {
            }

            /**
             * 传感器更新
             */
            @Override
            public void onUpdateBeacon(final ArrayList<Beacon> beacons) {
                String[] yunziIds = new String[beacons.size()];
                int i = 0;
                for (Beacon beacon : beacons) {
                    yunziIds[i] = beacon.getSerialNumber();
                    i++;
                }
                System.out.println(yunziIds.length);
                Intent intent = new Intent();
                intent.setAction("SET_BROADCST_OUT");
                intent.putExtra("sensor2ID", yunziIds);
                sendBroadcast(intent);
            }
        };

        sensoroManager.setBeaconManagerListener(beaconManagerListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                // 蓝牙可用
                if (sensoroManager.isBluetoothEnabled()) {
                    startSDK();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ExitEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (sensoroManager != null) {
            sensoroManager.stopService();
        }
    }

}

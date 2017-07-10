package com.example.administrator.my.activity;

import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.example.administrator.my.R;
import com.example.administrator.my.db.MyDatabaseHelper;
import com.example.administrator.my.fragment.ActivityFragment;
import com.example.administrator.my.fragment.HistoryFragment;
import com.example.administrator.my.fragment.SettingFragment;
import com.example.administrator.my.model.TabItem;
import com.example.administrator.my.utils.Logs;
import com.example.administrator.my.utils.SpUtil;
import com.example.administrator.my.view.MyTabLayout;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    private SensoroManager sensoroManager;
    private MyTabLayout myTablayout_bottom;
    private ViewPager viewPager;
    private ArrayList<TabItem> tabs;

    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sensoroManager = SensoroManager.getInstance(MainActivity.this);

        //设置sdk
        setSDK();

        //初始化控件
        initWidget();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        viewPager = (ViewPager) findViewById(R.id.viewPager_top);
        myTablayout_bottom = (MyTabLayout) findViewById(R.id.myTablayout_bottom);

        initData();
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
                //TODO 该写这里

//                light = beacon.getLight();
                String serialNumber = beacon.getSerialNumber();//序列号
                SpUtil.putString("serialNumber",serialNumber);
//                accuracy = beacon.getAccuracy() * 100+"";    //距离
                //信号强度
//                rssi = beacon.getRssi() + "";

                if (isFirst) {
                    Logs.d("发现云子");
                    Intent intent = new Intent();
                    intent.setAction("GET_YUNZI_ID");
                    intent.putExtra("yunzi", serialNumber);
                    sendBroadcast(intent);
                    isFirst = false;
                } else if (SpUtil.getBoolean("destroy",true)){
                    Logs.d("发现云子");
                    Intent intent = new Intent();
                    intent.setAction("GET_YUNZI_ID");
                    intent.putExtra("yunzi", serialNumber);
                    sendBroadcast(intent);
                } else if (!SpUtil.getString("serialNumber","").equals(serialNumber)) {
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
                for (Beacon beacon : beacons) {
                    Intent intent = new Intent();
                    intent.setAction("SET_BROADCST_OUT");
                    sendBroadcast(intent);

//                    System.out.println("健康jfk速度jfk基督教jfk的角度看jfk觉得");
                }
            }
        };

        sensoroManager.setBeaconManagerListener(beaconManagerListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //检查蓝牙是否可用
        if (!sensoroManager.isBluetoothEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, 0);
        } else {
            // 开启SDK
            startSDK();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 判断蓝牙是否开启
        boolean isBTEnable = openBluetooth();
        if (isBTEnable) {
            // 开启SDK
            startSDK();
        }
    }

    /**
     * 打开蓝牙对话框
     */
    private boolean openBluetooth() {
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        boolean status = bluetoothAdapter.isEnabled();
        if (!status) {
            Builder builder = new Builder(this);
            builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent,1);
                }
            }).setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setTitle(R.string.ask_bt_open);
            builder.show();
        }

        return status;
    }

    /**
     * 设置底部条目及对应界面的适配器      暂定
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

    /**
     * 设置底部条目及对应界面的适配器    暂定
     */
    private void initData() {
        tabs = new ArrayList<>();
        tabs.add(new TabItem(R.mipmap.ic_launcher_round, R.string.tab_activity, ActivityFragment.class));
        tabs.add(new TabItem(R.mipmap.ic_launcher_round, R.string.tab_history, HistoryFragment.class));
        tabs.add(new TabItem(R.mipmap.ic_launcher_round, R.string.tab_setting, SettingFragment.class));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(sensoroManager!=null){
            sensoroManager.stopService();
        }
        System.exit(0);

    }

}

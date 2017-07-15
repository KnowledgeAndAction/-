package cn.hicc.information.sensorsignin.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.hicc.information.sensorsignin.R;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.fragment.ActivityFragment;
import cn.hicc.information.sensorsignin.fragment.HistoryFragment;
import cn.hicc.information.sensorsignin.fragment.SettingFragment;
import cn.hicc.information.sensorsignin.model.DestroyFragment;
import cn.hicc.information.sensorsignin.model.ExitEvent;
import cn.hicc.information.sensorsignin.model.SignActive;
import cn.hicc.information.sensorsignin.model.TabItem;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import cn.hicc.information.sensorsignin.view.MyTabLayout;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    private SensoroManager sensoroManager;
    private MyTabLayout myTablayout_bottom;
    private ViewPager viewPager;
    private ArrayList<TabItem> tabs;
    private String serialNumber;
    // 存储扫描到的云子id
    private List<String> oldSerialNumber = new ArrayList<>();
    private MyDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensoroManager = SensoroManager.getInstance(MainActivity.this);
        //上传数据
        initData();
        // 设置sdk
        setSDK();

        // 初始化控件
        initWidget();

        // 检查蓝牙是否可用
        checkBluetooth();

        // 注册监听退出登录的事件
        EventBus.getDefault().register(this);
    }
//上传未存储到网络的数据
    private void initData() {
        database = MyDatabase.getInstance();
        List<SignActive> unSaveActives = database.getUnSaveActives();
        for (final SignActive unSaveActive : unSaveActives) {
            OkHttpUtils.get()
                    .url(Constant.API_URL+"api/TSign/InsertSign")
                    .addParams("account",unSaveActive.getNumber())
                    .addParams("activityid",unSaveActive.getActiveId()+"")
                    .addParams("intime",unSaveActive.getInTime())
                    .addParams("outtime",unSaveActive.getOutTime())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("sucessed")) {
                                    Logs.d("上传成功");
                                    database.updateSignActive(unSaveActive.getActiveId(),true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
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
            Logs.d("开启sensoro服务");
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
                // 序列号
                serialNumber = beacon.getSerialNumber();
                Logs.d("serialNumber:" + serialNumber);
                // 如果存储的云子id中不包含此次发现的  就将新的添加到集合中，发送广播
                if (!oldSerialNumber.contains(serialNumber)) {
                    oldSerialNumber.add(serialNumber);
                    Logs.d("发现新云子:" + serialNumber);
                    Intent intent = new Intent();
                    intent.putExtra("yunzi",serialNumber);
                    intent.setAction("GET_YUNZI_ID");
                    sendBroadcast(intent);
                }

                // 签到 签离界面需要的广播
                Intent intent = new Intent();
                intent.setAction("SET_BROADCST_OUT");
                intent.putExtra("sensor2ID", beacon.getSerialNumber());
                sendBroadcast(intent);
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
                    // 签到 签离界面需要的广播
                    Intent intent = new Intent();
                    intent.setAction("SET_BROADCST_OUT");
                    intent.putExtra("sensor2ID", beacon.getSerialNumber());
                    sendBroadcast(intent);
                }
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

    // 接收退出登录的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ExitEvent event) {
        finish();
    }

    // 接收fragment销毁时的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DestroyFragment destroyFragment) {
        Logs.d("活动页销毁了");
        // 将集合清空
        oldSerialNumber.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (sensoroManager != null) {
            Logs.d("MainActivity销毁了，停止服务");
            sensoroManager.stopService();
        }
        // 如果activity销毁，就将集合清空
        oldSerialNumber.clear();
    }

}

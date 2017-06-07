package com.example.administrator.my.activity;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.example.administrator.my.fragment.ActivityFragment;
import com.example.administrator.my.fragment.HistoryFragment;
import com.example.administrator.my.MyTabLayout;
import com.example.administrator.my.R;
import com.example.administrator.my.fragment.SettingFragment;
import com.example.administrator.my.TabItem;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends FragmentActivity {

    private ProgressDialog progressDialog;
    private SensoroManager sensoroManager;
    private String tmpString;
    private String lightString;
    private Integer temp;
    private boolean isShow = true;
    private MyTabLayout myTablayout_bottom;
    private ViewPager viewPager;
    ActionBar actionBar;
    String beaconFilter;
    String matchFormat;
    SharedPreferences sharedPreferences;

    public static final String TAG_FRAG_BEACONS = "TAG_FRAG_BEACONS";

    LinearLayout actionBarMainLayout;
    private ArrayList<TabItem> tabs;
    CopyOnWriteArrayList<Beacon> beacons;
    ArrayList<OnBeaconChangeListener> beaconListeners;

    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    BeaconManagerListener beaconManagerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensoroManager = SensoroManager.getInstance(MainActivity.this);
        //初始化控件
        initWidget();
        initData();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        isShow = true;
        //设置sdk
        setSDK();
//        showDialog();
        //开启SDK
//        startSDK();
        viewPager = (ViewPager) findViewById(R.id.viewPager_top);
        myTablayout_bottom = (MyTabLayout) findViewById(R.id.myTablayout_bottom);

    }


    /*
      * Start sensoro service.
      * 							开启服务
      */
    private void startSensoroService() {
        // set a tBeaconManagerListener.
        sensoroManager.setBeaconManagerListener(beaconManagerListener);
        try {
            sensoroManager.startService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置SDK
     */
    private void setSDK() {
        BeaconManagerListener beaconManagerListener = new BeaconManagerListener() {
            private Integer temperature;

            /**
             * 发现传感器
             * @param beacon
             */
            @Override
            public void onNewBeacon(Beacon beacon) {
                /*
				 * A new beacon appears.
				 */
                String key = getKey(beacon);
                boolean state = sharedPreferences.getBoolean(key, false);
//                if (state) {
//					/*
//					 * show notification
//					 */
//
//                    showNotification(beacon, true);
//                }
//                closeDialog();
//                isShow = false;
//                light = beacon.getLight();
//                serialNumber = beacon.getSerialNumber();//序列号
//                accuracy = beacon.getAccuracy() * 100+"";    //距离
//                //信号强度
//                rssi = beacon.getRssi() + "";
//                getTemperature(beacon);                    //温度
//                getLight(beacon);                           //光照


//                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
//                intent.putExtra("light", light);
//                intent.putExtra("tmpString", tmpString);  //温度
//                intent.putExtra("lightString", lightString);   //光照
//                intent.putExtra("accuracy", accuracy);         //距离
//                intent.putExtra("rssi", rssi);                   //信号强度
//                intent.putExtra("serialNumber", serialNumber);//序列号
//                startActivity(intent);
            }

            @Override
            public void onGoneBeacon(Beacon beacon) {
            }

            /**
             * 传感器更新
             * @param beacons
             */
            @Override
            public void onUpdateBeacon(final ArrayList<Beacon> beacons) {


                for (Beacon beacon : beacons) {
                    if (isShow) {
                        isShow = false;
                        closeDialog();
                    }
                }
            }
        };
        sensoroManager.setBeaconManagerListener(beaconManagerListener);
    }

    public String getKey(Beacon beacon) {
        if (beacon == null) {
            return null;
        }
        String key = beacon.getProximityUUID() + beacon.getMajor() + beacon.getMinor() + beacon.getSerialNumber();

        return key;

    }

    /**
     * 获取光照数值
     *
     * @param beacon
     */
    private void getLight(final Beacon beacon) {
        Double light = beacon.getLight();
        if (light == null) {
            lightString = getString(R.string.closed);
        } else {
            lightString = new DecimalFormat("#0.00").format(light) + " " + getString(R.string.lx);
        }
    }


    /**
     * 获取温度
     *
     * @param beacon
     */
    private void getTemperature(Beacon beacon) {
        temp = beacon.getTemperature();
        if (temp == null) {
            tmpString = getString(R.string.closed);
        } else {
            tmpString = temp + " " + getString(R.string.degree);
        }

    }

    /**
     * 弹出对话框
     */
    private void showDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    /**
     * 关闭对话框
     */
    private void closeDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //检查蓝牙是否可用
        isBlueEnable();
//        if (!sensoroManager.isBluetoothEnabled()) {
//            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(bluetoothIntent, 0);
//        }

    }

    @Override
    protected void onResume() {
        boolean isBTEnable = isBlueEnable(); //判断蓝牙是否开启
        if (isBTEnable) {
            startSensoroService();				//开启服务
        }
//        handler.post(runnable);
        super.onResume();

    }

    /**
     *判断蓝牙是否可用
     * @return
     */
    private boolean isBlueEnable() {
        bluetoothManager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        boolean status = bluetoothAdapter.isEnabled();
        if (!status) {
            Builder builder = new Builder(this);
            builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
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
    private void initData(){
        tabs=new ArrayList<TabItem>();
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
    /*
	 * Beacon Change Listener.Use it to notificate updating of beacons.
	 */
    public interface OnBeaconChangeListener {
        public void onBeaconChange(ArrayList<Beacon> beacons);
    }

    /*
     * Register beacon change listener.
     */
    public void registerBeaconChangerListener(OnBeaconChangeListener onBeaconChangeListener) {
        if (beaconListeners == null) {
            return;
        }
        beaconListeners.add(onBeaconChangeListener);
    }

    /*
     * Unregister beacon change listener.
     */
    public void unregisterBeaconChangerListener(OnBeaconChangeListener onBeaconChangeListener) {
        if (beaconListeners == null) {
            return;
        }
        beaconListeners.remove(onBeaconChangeListener);
    }
}

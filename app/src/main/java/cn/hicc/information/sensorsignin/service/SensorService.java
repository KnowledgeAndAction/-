package cn.hicc.information.sensorsignin.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.SpUtil;

public class SensorService extends Service {

    private SensoroManager sensoroManager;
    private List<String> oldSerialNumber = new ArrayList<>();
    private String startTime = "";
    private boolean isCheck = false;
    private boolean isOpen = false;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isOpen = true;
        Logs.d("onCreate服务创建");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.d("onStartCommand服务连接");
        SpUtil.remove("yunziId");
        SpUtil.remove("endTime");
        sensoroManager = SensoroManager.getInstance(this);
        setSDK();
        startSDK();
        checkTime();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOpen = false;
        Logs.d("onDestroy服务销毁");
        if (sensoroManager != null) {
            sensoroManager.stopService();
            Logs.d("停止sensoro服务");
        }
        // 如果activity销毁，就将集合清空
        oldSerialNumber.clear();
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
                String serialNumber = beacon.getSerialNumber();
                Logs.d("service serialNumber:" + serialNumber);
                // 如果存储的云子id中不包含此次发现的  就将新的添加到集合中，发送广播
                if (!oldSerialNumber.contains(serialNumber)) {
                    oldSerialNumber.add(serialNumber);
                    Logs.d("service发现新云子:" + serialNumber);
                    Intent intent = new Intent();
                    intent.putExtra("yunzi", serialNumber);
                    intent.setAction("GET_YUNZI_ID");
                    sendBroadcast(intent);
                }

                // 签到 签离界面需要的广播
                Intent intent = new Intent();
                intent.setAction("SET_BROADCST_OUT");
                intent.putExtra("sensor2ID", beacon.getSerialNumber());
                sendBroadcast(intent);

                if (SpUtil.getString("yunziId","").equals(beacon.getSerialNumber())) {
                    isCheck = false;
                }
            }

            @Override
            public void onGoneBeacon(Beacon beacon) {
                Logs.d("service一个云子消失了:" + beacon.getSerialNumber());
                if (oldSerialNumber.contains(beacon.getSerialNumber())) {
                    // TODO 暂时不做动态的
                    //oldSerialNumber.remove(beacon.getSerialNumber());
                    Intent intent = new Intent();
                    intent.setAction("SENSOR_GONE");
                    intent.putExtra("sensorNumber", beacon.getSerialNumber());
                    sendBroadcast(intent);
                }

                if (SpUtil.getString("yunziId","").equals(beacon.getSerialNumber())) {
                    isCheck = true;
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//("HH:mm:ss")(小时：分钟：秒)
                    startTime = df.format(new Date());
                }
            }

            /**
             * 传感器更新
             */
            @Override
            public void onUpdateBeacon(final ArrayList<Beacon> beacons) {
                for (Beacon beacon : beacons) {
                    Logs.d("service云子更新：" + beacon.getSerialNumber());
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

    private void checkTime() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    while (isOpen) {
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                        String endTime = df.format(new Date());
                        Logs.d("开始时间：" + startTime);
                        Logs.d("结束时间：" + endTime);

                        if (isCheck && !startTime.equals("")) {
                            long start = df.parse(startTime).getTime();
                            long end = df.parse(endTime).getTime();
                            // 如果超过10分钟未检测到云子  就自动签离  1000*60*10 10分钟
                            Log.d("SIGN_TAG","时间间隔"+ (end-start));
                            // TODO 设置时间
                            if (Math.abs(end-start) > 1000*60*10) {
                                // 自动签离
                                Log.d("SIGN_TAG","发送自动签离的广播");
                                Intent intent = new Intent();
                                intent.setAction("SET_BROADCST_OUT");
                                intent.putExtra("isLeave", true);
                                sendBroadcast(intent);
                            }
                        }

                        // 判断是否到了活动结束时间
                        if (!SpUtil.getString("endTime","").equals("")) {
                            String endTime1 = SpUtil.getString("endTime", "").replace("T", " ").substring(0, 16);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            String presentTime = sdf.format(new Date());//当前时间

                            long end = sdf.parse(endTime1).getTime();
                            long time = sdf.parse(presentTime).getTime();

                            if (time > end) {
                                // 自动签离
                                Log.d("SIGN_TAG","发送自动签离的广播");
                                Intent intent = new Intent();
                                intent.setAction("SET_BROADCST_OUT");
                                intent.putExtra("isLeave", true);
                                sendBroadcast(intent);
                            }
                        }

                        // 每隔10秒检测一次
                        Thread.sleep(1000*10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

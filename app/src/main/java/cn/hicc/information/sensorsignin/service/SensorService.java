package cn.hicc.information.sensorsignin.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cn.hicc.information.sensorsignin.utils.Logs;

public class SensorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.d("服务启动了");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d("服务销毁了");
    }
}

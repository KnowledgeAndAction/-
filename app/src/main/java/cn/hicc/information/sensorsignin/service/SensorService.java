package cn.hicc.information.sensorsignin.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SensorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

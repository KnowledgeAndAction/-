package com.example.administrator.my.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.my.R;
import com.example.administrator.my.activity.DetailActivity;
import com.example.administrator.my.model.Active;
import com.example.administrator.my.utils.ToastUtil;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.beacon.kit.SensoroBeaconManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * 活动对应界面——周凯歌
 */

public class ActivityFragment extends BaseFragment{
    private List<Active> mActiveList = new ArrayList<>();
    private ListView listView;
    private Boolean sharedPreferences;
    private SensoroBeaconManager sensoroManager;

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
                ToastUtil.show("饭店客房看电视金发科技看到解放军");
                /*
				 * A new beacon appears.
				 */
                String key = getKey(beacon);
//                boolean state = sharedPreferences.getBoolean(key, false);
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
                Intent intent=new Intent("com.example.broadcasttest.MY_BROADCAST");
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
//                    if (isShow) {
//                        isShow = false;
//                        closeDialog();
//                    }
                }
            }
        };
        sensoroManager.setBeaconManagerListener(beaconManagerListener);
    }

    public String getKey(Beacon beacon) {
        if (beacon == null) {
            return null;
        }
        String key = beacon.getProximityUUID() + beacon.getMajor() + beacon.getMinor()
                + beacon.getSerialNumber();

        return key;

    }
    @Override
    public void fetchData() {
       /* OkHttpUtils
                .get()
                .url("http://123.434.3544")
                .addParams("sensoroId","ddasd2324235345345s")
                .build()
                .execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int i) {

            }

            @Override
            public void onResponse(String s, int i) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean flag = jsonObject.getBoolean("flag");
                    if (flag) {
                        JSONArray result = jsonObject.getJSONArray("result");
                        for (int j = 0; j < result.length(); j++) {
                            JSONObject activity = result.getJSONObject(j);
                            String name = activity.getString("activityName");
                            String time = activity.getString("activityTime");
                            String location = activity.getString("activityLocation");

                            Active active = new Active();
                            active.setActiveName(name);
                            active.setActiveTime(time);
                            active.setActiveLocation(location);
                            mActiveList.add(active);


                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });*/
        for (int i = 0; i<50; i++) {
            Active active = new Active();
            active.setActiveName("讲座");
            active.setActiveLocation("a2 203");
            active.setActiveTime("12:30");
            mActiveList.add(active);
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater
            , @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bottom_active, container, false);

        listView = (ListView) view.findViewById(R.id.lv_active);
        MyAdapter adapter = new MyAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Active active = mActiveList.get(position);
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("active",active);
                startActivity(intent);
            }
        });
        return view;
    }
    public class MyBroadcast extends BroadcastReceiver{

        //云子更新信息广播接受
        @Override
        public void onReceive(Context context, Intent intent) {
            ToastUtil.show("发现云子");
        }
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mActiveList.size();
        }

        @Override
        public Active getItem(int position) {
            return mActiveList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_activity_list,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
                viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_name.setText(getItem(position).getActiveName());
            viewHolder.tv_location.setText(getItem(position).getActiveLocation());
            viewHolder.tv_time.setText(getItem(position).getActiveTime());

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_location;
        TextView tv_time;
    }
}

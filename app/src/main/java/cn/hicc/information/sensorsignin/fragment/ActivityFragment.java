package cn.hicc.information.sensorsignin.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.activity.DetailActivity3;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 活动对应界面——周凯歌
 */

public class ActivityFragment extends BaseFragment {
    private List<Active> mActiveList = new ArrayList<>();
    private ListView listView;
    private MyBroadcast myBroadcast;
    private String yunziId;
    private MyAdapter adapter;
    private SensorGoneBroadcast sensorGoneBroadcast;
    private MyUpdataBroadcast myUpdataBroadcast;

    @Override
    public void fetchData() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active, container, false);

        // 注册广播接收者
        initBroadcast();

        initView(view);

        return view;
    }

    private void initView(View view) {
        listView = (ListView) view.findViewById(R.id.lv_active);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Active active = mActiveList.get(position);
                Intent intent = new Intent(getContext(), DetailActivity3.class);
                intent.putExtra("active", active);
                intent.putExtra("yunziId", yunziId);
                startActivity(intent);
            }
        });
    }

    // 注册广播接收者
    private void initBroadcast() {
        // 注册发现云子广播接收者
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GET_YUNZI_ID");
        getContext().registerReceiver(myBroadcast, intentFilter);

        // 注册云子消失广播接收者
        sensorGoneBroadcast = new SensorGoneBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SENSOR_GONE");
        getContext().registerReceiver(sensorGoneBroadcast, filter);

        // 注册云子更新信息广播接收者
        myUpdataBroadcast = new MyUpdataBroadcast();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("SET_BROADCST_OUT");
        getContext().registerReceiver(myUpdataBroadcast, intentFilter2);
    }

    // 从网络获取活动信息
    private void getActive(final String yunziId) {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/GetActivity")
                .addParams("sensorId", yunziId)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        ToastUtil.show("获取活动失败，请稍后重试"+e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            boolean sucessed = jsonObject.getBoolean("sucessed");
                            if (sucessed) {
                                JSONArray data = jsonObject.getJSONArray("data");
                                for (int j = 0; j < data.length(); j++) {
                                    JSONObject activity = data.getJSONObject(j);
                                    String name = activity.getString("ActivityName");
                                    String des = activity.getString("ActivityDescription");
                                    String time = activity.getString("Time");
                                    String location = activity.getString("Location");
                                    long activeId = activity.getLong("Nid");
                                    String endTime = activity.getString("EndTime");
                                    String rule = activity.getString("Rule");

                                    // 获取当前时间，判断该活动是否已经失效，不失效时才添加到集合中
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String presentTime = sdf.format(new java.util.Date());
                                    if (sdf.parse(presentTime).getTime() <= sdf.parse(endTime.replace("T", " ").substring(0, 19)).getTime()) {
                                        if (mActiveList.contains(getActiveForSensor(yunziId))) {
                                            Active activeForSensor = getActiveForSensor(yunziId);
                                            activeForSensor.setActiveId(activeId);
                                            activeForSensor.setSersorID(yunziId);
                                            activeForSensor.setActiveName(name);
                                            activeForSensor.setActiveTime(time);
                                            activeForSensor.setActiveDes(des);
                                            activeForSensor.setActiveLocation(location);
                                            activeForSensor.setEndTime(endTime);
                                            activeForSensor.setRule(Integer.parseInt(rule));
                                        } else {
                                            Active active = new Active();
                                            active.setActiveId(activeId);
                                            active.setSersorID(yunziId);
                                            active.setActiveName(name);
                                            active.setActiveTime(time);
                                            active.setActiveDes(des);
                                            active.setActiveLocation(location);
                                            active.setEndTime(endTime);
                                            active.setRule(Integer.parseInt(rule));
                                            mActiveList.add(active);
                                        }
                                    } else {
                                        Logs.d("这个活动过期了:"+yunziId);
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            } else {
                                Logs.d("这个云子上没有活动："+yunziId);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Logs.d("异常");
                        }
                    }
                });
    }

    // 云子消失广播接收者
    public class SensorGoneBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logs.d("接收到了云子消失的广播");
            String sensorNumber = intent.getStringExtra("sensorNumber");
            Active active = getActiveForSensor(sensorNumber);
            if (active != null) {
                // TODO 暂时不动态更新
                //mActiveList.remove(active);
                //adapter.notifyDataSetChanged();
            }
        }
    }

    // 云子更新信息广播接收者
    public class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logs.d("发现云子:"+yunziId);
            yunziId = intent.getStringExtra("yunzi");
            // 根据云子id从网络获取具体活动信息
            getActive(yunziId);
        }
    }

    // 云子更新信息广播接收者
    public class MyUpdataBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("sensor2ID");
            //getActive(id);
        }
    }

    // listview适配器
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_activity_list, parent, false);
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
            viewHolder.tv_time.setText(getItem(position).getActiveTime().replace("T", " ").substring(0, 16));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_location;
        TextView tv_time;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消注册广播
        if (myBroadcast != null) {
            getContext().unregisterReceiver(myBroadcast);
        }
        if (sensorGoneBroadcast != null) {
            getContext().unregisterReceiver(sensorGoneBroadcast);
        }
        if (myUpdataBroadcast != null) {
            getContext().unregisterReceiver(myUpdataBroadcast);
        }
        mActiveList.clear();
    }

    // 根据云子id获取对应的活动对象
    private Active getActiveForSensor(String number) {
        for (Active active : mActiveList) {
            if (number.equals(active.getSersorID())) {
                return active;
            }
        }
        return null;
    }
}

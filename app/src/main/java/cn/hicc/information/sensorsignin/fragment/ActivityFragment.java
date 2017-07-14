package cn.hicc.information.sensorsignin.fragment;

import android.app.ProgressDialog;
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

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.activity.DetailActivity;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.model.DestroyFragment;
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
    private ProgressDialog progressDialog;

    @Override
    public void fetchData() {
    }

    private void getActive(final String yunziId) {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/GetActivity")
                .addParams("sensorId", yunziId)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {

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

                                    Active active = new Active();
                                    active.setSersorID(yunziId);
                                    active.setActiveName(name);
                                    active.setActiveTime(time);
                                    active.setActiveDes(des);
                                    active.setActiveLocation(location);
                                    mActiveList.add(active);
                                }

                                MyAdapter adapter = new MyAdapter();
                                listView.setAdapter(adapter);
                                closeDialog();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bottom_active, container, false);


        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GET_YUNZI_ID");
        getContext().registerReceiver(myBroadcast, intentFilter);

        listView = (ListView) view.findViewById(R.id.lv_active);

//        showDialog();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Active active = mActiveList.get(position);
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("active", active);
                intent.putExtra("yunziId", yunziId);
                startActivity(intent);
            }
        });

        return view;
    }

    public class MyBroadcast extends BroadcastReceiver {
        //云子更新信息广播接受
        @Override
        public void onReceive(Context context, Intent intent) {
            yunziId = intent.getStringExtra("yunzi");
            getActive("1");

            ToastUtil.show("发现云子:"+yunziId);
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
            Logs.i("取消注册广播");
            getContext().unregisterReceiver(myBroadcast);
        }
        mActiveList.clear();
        EventBus.getDefault().post(new DestroyFragment());
    }


    /**
     * 弹出对话框
     */
    private void showDialog() {
        progressDialog = new ProgressDialog(getContext());
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
}

package cn.hicc.information.sensorsignin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.model.HistoryActive;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 历史记录详细数据
 */
public class HistoryActivity extends AppCompatActivity {

    private List<HistoryActive> mActiveList = new ArrayList<>();
    private ListView listView;
    private SwipeRefreshLayout mSwipeLayout;
    private MyAdapter adapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();

        mSwipeLayout.setRefreshing(true);
        getActive();
    }

    // 获取历史活动
    private void getActive() {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TSign/GetSign")
                .addParams("userId", SpUtil.getString(Constant.ACCOUNT, ""))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        mSwipeLayout.setRefreshing(false);
                        ToastUtil.show("历史记录响应失败"+e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            boolean sucessed = jsonObject.getBoolean("sucessed");
                            mActiveList.clear();
                            if (sucessed) {
                                JSONArray data = jsonObject.getJSONArray("data");
                                if (data.length() == 0) {
                                    ToastUtil.show("没有历史记录");
                                } else {
                                    for (int j = 0; j < data.length(); j++) {
                                        JSONObject hActivity = data.getJSONObject(j);
                                        String hActivityId = hActivity.getString("ActivityId");
                                        String hStudnetNum = hActivity.getString("StudnetNum");
                                        String hInTime = hActivity.getString("InTime");
                                        String hOutTime = hActivity.getString("OutTime");
                                        String hActivityDescription = hActivity.getString("ActivityDescription");
                                        String hTime = hActivity.getString("Time");
                                        String hLocation = hActivity.getString("Location");
                                        String hActivityName = hActivity.getString("ActivityName");

                                        // 如果是完成签离的活动才展示
                                        if (!hInTime.equals(hOutTime)) {
                                            HistoryActive historyActive = new HistoryActive();
                                            historyActive.sethActivityId(hActivityId);
                                            historyActive.sethStudnetNum(hStudnetNum);
                                            historyActive.sethInTime(hInTime);
                                            historyActive.sethOutTime(hOutTime);
                                            historyActive.setActivityDescription(hActivityDescription);
                                            historyActive.sethActivityName(hActivityName);
                                            historyActive.sethLocation(hLocation);
                                            historyActive.sethTime(hTime);
                                            historyActive.setEndTime(hActivity.getString("EndTime"));
                                            mActiveList.add(historyActive);
                                        }
                                    }
                                }
                            } else {
                                ToastUtil.show("获取历史记录失败");
                            }
                            mSwipeLayout.setRefreshing(false);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mSwipeLayout.setRefreshing(false);
                            ToastUtil.show("获取历史记录失败："+e.toString());
                        }
                    }
                });
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("历史详细信息");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = (ListView) findViewById(R.id.lv_history);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActive();
            }
        });
        adapter = new MyAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryActive historyActive = mActiveList.get(position);
                Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                intent.putExtra("ActivityId", historyActive);
                startActivity(intent);
            }
        });
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mActiveList.size();
        }

        @Override
        public HistoryActive getItem(int position) {
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
                convertView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.item_activity_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
                viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_name.setText(getItem(position).gethActivityName());
            viewHolder.tv_location.setText(getItem(position).getLocation().replace("T", " "));
            viewHolder.tv_time.setText(getItem(position).gethInTime().replace("T", " ").substring(0, 16));

            return convertView;
        }
    }
    static class ViewHolder {
        TextView tv_name;
        TextView tv_location;
        TextView tv_time;
    }
}

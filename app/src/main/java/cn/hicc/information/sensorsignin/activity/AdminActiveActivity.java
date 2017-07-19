package cn.hicc.information.sensorsignin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 查看管理员添加的活动——陈帅
 */

public class AdminActiveActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView lv_active_info;
    private SwipeRefreshLayout swipe_refresh;
    private MyAdapter myAdapter;
    private List<Active> mActiveList = new ArrayList<>();
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add:
                    startActivity(new Intent(AdminActiveActivity.this, AddActiveActivity.class));
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_active);

        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("管理活动");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lv_active_info = (ListView) findViewById(R.id.lv_active_info);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        // 设置listview点击事件
        lv_active_info.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到修改活动界面
                Intent intent = new Intent(AdminActiveActivity.this,ChangeActiveActivity.class);
                // 把活动传过去
                intent.putExtra("active",mActiveList.get(position));
                startActivity(intent);
            }
        });

        // 配置swipeRefresh
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary , R.color.colorAccent, R.color.colorPrimaryDark);
        // 设置刷新事件
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActive();
            }
        });

        // 设置开始就刷新
        swipe_refresh.setRefreshing(true);
        myAdapter = new MyAdapter();
        lv_active_info.setAdapter(myAdapter);
        // 获取活动
        getActive();
    }

    // TODO 获取活动
    private void getActive() {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/GetActivity1")
                .addParams("account", SpUtil.getString(Constant.ACCOUNT, ""))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        ToastUtil.show("获取活动失败，请稍后重试:" + e.toString());
                        swipe_refresh.setRefreshing(false);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        try {
                            mActiveList.clear();
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
                                    int rule = activity.getInt("Rule");

                                    Active active = new Active();
                                    active.setActiveId(activeId);
                                    active.setActiveName(name);
                                    active.setActiveTime(time);
                                    active.setActiveDes(des);
                                    active.setActiveLocation(location);
                                    active.setRule(rule);
                                    mActiveList.add(active);
                                }
                            } else {
                                ToastUtil.show("暂无数据");
                            }
                            myAdapter.notifyDataSetChanged();
                            swipe_refresh.setRefreshing(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtil.show("获取活动失败，请稍后重试:" + e.toString());
                            swipe_refresh.setRefreshing(false);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipe_refresh.setRefreshing(true);
        // 获取活动
        getActive();
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
                convertView = LayoutInflater.from(AdminActiveActivity.this).inflate(R.layout.item_activity_list, parent, false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tool_bar, menu);
        return true;
    }
}

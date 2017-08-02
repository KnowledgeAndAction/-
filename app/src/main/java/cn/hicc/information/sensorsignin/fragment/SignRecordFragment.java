package cn.hicc.information.sensorsignin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.activity.SignRecordActivity;
import cn.hicc.information.sensorsignin.adapter.SignRecordRecyclerAdapter;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 签到记录
 */

public class SignRecordFragment extends BaseFragment {

    private SwipeRefreshLayout swipe_refresh;
    private List<Active> mActiveList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SignRecordRecyclerAdapter myAdapter;

    @Override
    public void fetchData() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_active, container, false);

        initView(view);

        return view;
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new SignRecordRecyclerAdapter(mActiveList);
        recyclerView.setAdapter(myAdapter);

        // 设置listview点击事件
        myAdapter.setItemClickListener(new SignRecordRecyclerAdapter.OnRecyclerViewOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到修改活动界面
                Intent intent = new Intent(getContext(), SignRecordActivity.class);
                intent.putExtra("id",mActiveList.get(position).getActiveId());
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


        // 获取活动
        getActive();
    }

    private void getActive() {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/GetActivityByAccount")
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
                                    String endTime = activity.getString("EndTime");

                                    Active active = new Active();
                                    active.setActiveId(activeId);
                                    active.setActiveName(name);
                                    active.setActiveTime(time);
                                    active.setActiveDes(des);
                                    active.setActiveLocation(location);
                                    active.setRule(rule);
                                    active.setEndTime(endTime);
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
}

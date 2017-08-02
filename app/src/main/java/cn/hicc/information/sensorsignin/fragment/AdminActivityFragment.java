package cn.hicc.information.sensorsignin.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import cn.hicc.information.sensorsignin.activity.ChangeActiveActivity;
import cn.hicc.information.sensorsignin.adapter.RecyclerAdapter;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 管理活动
 */

public class AdminActivityFragment extends BaseFragment {

    private SwipeRefreshLayout swipe_refresh;
    private List<Active> mActiveList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapter myAdapter;
    private ProgressDialog progressDialog;

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
        myAdapter = new RecyclerAdapter(mActiveList);
        recyclerView.setAdapter(myAdapter);

        // 设置listview点击事件
        myAdapter.setItemClickListener(new RecyclerAdapter.OnRecyclerViewOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到修改活动界面
                Intent intent = new Intent(getContext(),ChangeActiveActivity.class);
                // 把活动传过去
                intent.putExtra("active",mActiveList.get(position));
                startActivity(intent);
            }
        });
        // 设置删除活动点击事件
        myAdapter.setDeleteClickListener(new RecyclerAdapter.OnRecyclerViewDeleteClickListener() {
            @Override
            public void onDeleteClick(View view, int position) {
                showConfirmDialog(position, (int) mActiveList.get(position).getActiveId());
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

    @Override
    public void onResume() {
        super.onResume();
        Logs.d("fragment刷新执行了吗");
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

    // 删除活动
    private void deleteActive(final int position, int id) {
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TActivity/Delprofessional")
                .addParams("Nid", id+"")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.show("删除失败，请稍后重试：" + e.toString());
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean sucessed = jsonObject.getBoolean("sucessed");
                            if (sucessed) {
                                ToastUtil.show("删除活动成功");
                                mActiveList.remove(position);
                                myAdapter.notifyDataSetChanged();
                            } else {
                                ToastUtil.show("删除活动失败");
                            }
                            closeProgressDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtil.show("删除失败，请稍后重试：" + e.toString());
                            closeProgressDialog();
                        }
                    }
                });
    }

    // 显示确认对话框
    protected void showConfirmDialog(final int position, final int id) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        // 设置对话框左上角图标
        builder.setIcon(R.mipmap.logo);
        // 设置不能取消
        builder.setCancelable(false);
        // 设置对话框标题
        builder.setTitle("删除活动");
        // 设置对话框内容
        builder.setMessage("您确认删除该活动？");
        // 设置积极的按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 提交活动
                showProgressDialog();
                deleteActive(position, id);
                dialog.dismiss();
            }
        });
        // 设置消极的按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setMessage("修改中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

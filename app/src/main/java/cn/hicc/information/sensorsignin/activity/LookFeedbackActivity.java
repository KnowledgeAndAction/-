package cn.hicc.information.sensorsignin.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.adapter.FeedbackAdapter;
import cn.hicc.information.sensorsignin.model.FeedBack;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 查看意见反馈——陈帅
 */
public class LookFeedbackActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private List<FeedBack> feedbackInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe_refresh;
    private FeedbackAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_feed_back);

        initView();
    }

    // 从网络获取数据
    private void initData() {
        feedbackInfoList.clear();
        OkHttpUtils
                .get()
                .url("http://123.206.57.216:8080/SchoolTestInterface/getfeedback.do")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        swipe_refresh.setRefreshing(false);
                        ToastUtil.show("加载数据失败，请稍后重试:" +e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        getJson(response);
                    }
                });
    }

    // 解析json数据
    private void getJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean("sucessed")) {
                JSONArray data = jsonObject.getJSONArray("data");
                for (int i=0; i<data.length(); i++) {
                    JSONObject object = data.getJSONObject(i);
                    String account = object.getString("account");
                    String msg = object.getString("msg");
                    String phoneBrand = object.getString("phoneBrand");
                    String phoneBrandType = object.getString("phoneBrandType");
                    String androidVersion = object.getString("androidVersion");

                    FeedBack feedBack = new FeedBack();
                    feedBack.setAccount(account);
                    feedBack.setMsg(msg);
                    feedBack.setPhoneBrand(phoneBrand);
                    feedBack.setPhoneBrandType(phoneBrandType);
                    feedBack.setAndroidVersion(androidVersion);

                    feedbackInfoList.add(feedBack);
                }

                swipe_refresh.setRefreshing(false);
                myAdapter.notifyDataSetChanged();
            } else {
                swipe_refresh.setRefreshing(false);
                ToastUtil.show("加载数据失败:");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            swipe_refresh.setRefreshing(false);
            ToastUtil.show("加载数据失败，请稍后重试:" + e.toString());
        }
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("查看意见反馈");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new FeedbackAdapter(feedbackInfoList);
        recyclerView.setAdapter(myAdapter);

        // 配置swipeRefresh
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary , R.color.colorAccent, R.color.colorPrimaryDark);
        // 设置刷新事件
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });

        // 设置开始就刷新
        swipe_refresh.setRefreshing(true);

        initData();
    }
}

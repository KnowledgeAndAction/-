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

import cn.hicc.information.sensorsignin.adapter.SignInfoAdapter;
import cn.hicc.information.sensorsignin.model.SignInfo;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 管理员查看签到记录界面——陈帅
 */
public class SignRecordActivity extends AppCompatActivity {

    private long activeId;
    private Toolbar toolbar;
    private List<SignInfo> signInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe_refresh;
    private SignInfoAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_record);

        // 从上一个界面接收这个活动的id
        activeId = getIntent().getLongExtra("id", 0);

        initView();
    }

    // 从网络获取数据
    private void initData() {
        signInfoList.clear();
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TSign/GetSignByActivityId")
                .addParams("activityId",activeId+"")
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
                for (int i=0,j=0; i<data.length(); i++) {
                    JSONObject object = data.getJSONObject(i);
                    String in = object.getString("InTime");
                    String out = object.getString("OutTime");
                    if (!in.equals(out)) {
                        SignInfo signInfo = new SignInfo();
                        signInfo.setInTime(in);
                        signInfo.setOutTime(out);
                        signInfoList.add(signInfo);
                        getName(signInfoList.get(j), object.getString("StudnetNum"));
                        j++;
                    }
                }

                swipe_refresh.setRefreshing(false);
                myAdapter.notifyDataSetChanged();
            } else {
                swipe_refresh.setRefreshing(false);
                ToastUtil.show("加载数据失败:" +jsonObject.getString("Msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            swipe_refresh.setRefreshing(false);
            ToastUtil.show("加载数据失败，请稍后重试:" + e.toString());
        }
    }

    // 获取对应学号姓名
    private void getName(final SignInfo signInfo, final String account) {
        OkHttpUtils
                .get()
                .url("http://123.206.57.216:8080/SchoolTestInterface/getInfo.do")
                .addParams("Account",account)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        signInfo.setName(account);
                        Logs.d("获取姓名失败:" + e.toString());
                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("sucessed")) {
                                signInfo.setName(jsonObject.getString("data"));
                            } else {
                                signInfo.setName(account);
                                Logs.d("获取姓名失败");
                            }
                            myAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            signInfo.setName(account);
                            Logs.d("获取姓名失败:" + e.toString());
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("签到记录");
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
        myAdapter = new SignInfoAdapter(signInfoList);
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

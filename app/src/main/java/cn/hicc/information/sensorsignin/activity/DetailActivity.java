package cn.hicc.information.sensorsignin.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;

/**
 * 此界面为活动详情——周凯歌
 */
public class DetailActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView activityLocation;
    private TextView activityDes;
    private TextView activtyName;
    private Active active;
    private MyBroadcast myBroadcast;
    private String sensor2ID;
    private String yunziId;
    private boolean isCan = false;
    private List<String> sensorList = new ArrayList<>();
    private MyDatabase database;
    private Toolbar toolbar;
    private TextView tv_time;
    private SwipeRefreshLayout refreshLayout;
    private CollapsingToolbarLayout toolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tow);

        database = MyDatabase.getInstance();

        Intent intent = getIntent();

        active = (Active) intent.getSerializableExtra("active");
        yunziId = intent.getStringExtra("yunziId");

        //初始化数据
        initData();

        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setEnabled(false);

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        // 设置标题和背景图
        toolbarLayout.setTitle(active.getActiveName());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        activityLocation = (TextView) findViewById(R.id.tv_location);
        activityDes = (TextView) findViewById(R.id.tv_des);
        loginButton = (Button) findViewById(R.id.loginButton);  //签到按钮
        tv_time = (TextView) findViewById(R.id.tv_time);

        activityLocation.setText("地点：" + active.getActiveLocation());
        activityDes.setText(active.getActiveDes());
        tv_time.setText("时间：" + active.getActiveTime().substring(11,16));

        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SET_BROADCST_OUT");
        registerReceiver(myBroadcast, intentFilter);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果还没有签到过
                if (!database.isSign(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId())) {
                    if (TimeCompare(active.getActiveTime().replace("T", " ").substring(0, 16))) {
                        // 如果能检测到云子 可以签到
                        if (isCan) {
                            Intent intent = new Intent(DetailActivity.this, MoveActivity.class);
                            intent.putExtra("activeName", active.getActiveName());
                            intent.putExtra("location", active.getActiveLocation());
                            intent.putExtra("activityDes", active.getActiveDes());
                            intent.putExtra("activeId", active.getActiveId());
                            intent.putExtra("yunziId", yunziId);

                            startActivity(intent);
                            finish();
                        } else {
                            ToastUtil.show("暂时无法进行签到，请稍后重试，并确保您在活动地点附近");
                        }
                    } else {
                        ToastUtil.show("未到签到时间");
                    }
                } else {
                    ToastUtil.show("您已经签到过");
                }
            }
        });

    }

    public class MyBroadcast extends BroadcastReceiver {
        //云子更新信息广播接受
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取接收到的云子id，添加到集合中
            sensor2ID = intent.getStringExtra("sensor2ID");
            sensorList.add(sensor2ID);
            // 如果当集合中包含进入活动的云子id，就可以签到
            isCan = sensorList.contains(yunziId);
        }
    }

    /**
     * 判断是否到了签到时间   true/false
     *
     * @param signTime 需要签到的时间
     */
    private boolean TimeCompare(String signTime) {
        //格式化时间
        SimpleDateFormat currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String presentTime = currentTime.format(new java.util.Date());//当前时间
        try {
            java.util.Date beginTime = currentTime.parse(signTime);
            java.util.Date endTime = currentTime.parse(presentTime);
            if (endTime.getTime() >= beginTime.getTime()) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBroadcast != null) {
            unregisterReceiver(myBroadcast);
        }
    }
}

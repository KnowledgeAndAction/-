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
import java.util.Date;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;

/**
 * 此界面为活动详情——周凯歌
 */
public class DetailActivity extends AppCompatActivity {

    private Active active;
    private MyBroadcast myBroadcast;
    private String sensor2ID;
    private String yunziId;
    private boolean isCan = false;
    private List<String> sensorList = new ArrayList<>();
    private MyDatabase database;
    private int clickCount = 0;
    private int clickDebug = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //初始化数据
        initData();

        // 初始化控件
        initView();
    }

    // 初始化控件
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setEnabled(false);

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        // 设置标题和背景图
        toolbarLayout.setTitle(active.getActiveName());

        TextView activityLocation = (TextView) findViewById(R.id.tv_location);
        TextView activityDes = (TextView) findViewById(R.id.tv_des);
        Button signButton = (Button) findViewById(R.id.loginButton);
        TextView tv_time = (TextView) findViewById(R.id.tv_time);
        TextView tv_end_time = (TextView) findViewById(R.id.tv_end_time);

        activityLocation.setText("地点：" + active.getActiveLocation());
        activityDes.setText("    "+active.getActiveDes());
        tv_time.setText("开始时间：" + active.getActiveTime().replace("T", " ").substring(0, 16));
        tv_end_time.setText("结束时间：" + active.getEndTime().replace("T", " ").substring(0, 16));

        // 签到按钮点击事件
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 签到
                signIn();
            }
        });
    }

    // 初始化数据
    private void initData() {
        database = MyDatabase.getInstance();

        Intent intent = getIntent();

        active = (Active) intent.getSerializableExtra("active");
        yunziId = intent.getStringExtra("yunziId");

        // 注册云子更新信息广播接收者
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SET_BROADCST_OUT");
        registerReceiver(myBroadcast, intentFilter);
    }

    // 签到逻辑
    private void signIn() {
        // 获取当前时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = df.format(new Date());
        switch (active.getRule()) {
            // 日常活动
            case 1:
                // 如果当前签到时间距离上一次签到时间不超过2个小时，就不能再次签到
                try {
                    Logs.i("数据库中的时间："+database.getRecentSignTime(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId()));
                    Logs.i("当前时间:"+df.parse(current).getTime());
                    if (( (df.parse(current).getTime() - database.getRecentSignTime(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId()))
                            > 1000*60*60*2) || clickDebug > 29) {
                        if (TimeCompare(active.getActiveTime().replace("T", " ").substring(0, 19))) {
                            // TODO 如果能检测到云子 可以签到 为了优化用户体验，当连续点击15次，可以签到
                            if (isCan || clickCount > 14) {
                                Intent intent = new Intent(DetailActivity.this, MoveActivity.class);
                                intent.putExtra("activeName", active.getActiveName());
                                intent.putExtra("location", active.getActiveLocation());
                                intent.putExtra("activityDes", active.getActiveDes());
                                intent.putExtra("activeId", active.getActiveId());
                                intent.putExtra("yunziId", yunziId);
                                intent.putExtra("endTime", active.getEndTime());

                                startActivity(intent);
                                finish();
                            } else {
                                ToastUtil.show("暂时无法进行签到，请稍后重试，并确保您在活动地点附近");
                                clickCount++;
                            }
                        } else {
                            ToastUtil.show("不符合签到时间");
                        }
                    } else {
                        clickDebug++;
                        ToastUtil.show("您最近已经签到过，请下次再签到");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            // 普通活动
            case 0:
                // 如果还没有签到过
                if (!database.isSign(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId())) {
                    if (TimeCompare(active.getActiveTime().replace("T", " ").substring(0, 19))) {
                        // 如果能检测到云子 可以签到
                        if (isCan) {
                            Intent intent = new Intent(DetailActivity.this, MoveActivity.class);
                            intent.putExtra("activeName", active.getActiveName());
                            intent.putExtra("location", active.getActiveLocation());
                            intent.putExtra("activityDes", active.getActiveDes());
                            intent.putExtra("activeId", active.getActiveId());
                            intent.putExtra("yunziId", yunziId);
                            intent.putExtra("endTime", active.getEndTime());

                            startActivity(intent);
                            finish();
                        } else {
                            ToastUtil.show("暂时无法进行签到，请稍后重试，并确保您在活动地点附近");
                            clickCount++;
                        }
                    } else {
                        ToastUtil.show("不符合签到时间");
                    }
                } else {
                    ToastUtil.show("您已经签到过");
                }
                break;
        }
    }

    // 云子更新信息广播接收者
    public class MyBroadcast extends BroadcastReceiver {
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
     * 判断是否到了签到时间
     * @param signTime 需要签到的时间
     */
    private boolean TimeCompare(String signTime) {
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 当前时间
        String presentTime = sdf.format(new java.util.Date());
        try {
            java.util.Date beginTime = sdf.parse(signTime);
            java.util.Date current = sdf.parse(presentTime);
            java.util.Date aEndTime = sdf.parse(active.getEndTime().replace("T", " ").substring(0, 19));
            // 可以提前10分钟签到     并且不能超过要活动结束时间
            if (((current.getTime() + 1000*60*10) >= beginTime.getTime()) && (current.getTime() < aEndTime.getTime())) {
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

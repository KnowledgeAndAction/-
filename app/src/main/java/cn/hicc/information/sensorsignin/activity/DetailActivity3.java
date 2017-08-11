package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.model.SignItem;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 此界面为活动详情——陈帅
 */
public class DetailActivity3 extends AppCompatActivity {

    private Active active;
    private MyBroadcast myBroadcast;
    private String sensor2ID;
    private String yunziId;
    private boolean isCan = false;
    private List<String> sensorList = new ArrayList<>();
    private MyDatabase database;
    private int clickCount = 0;
    private int clickDebug = 0;
    private ProgressDialog progressDialog;
    private int mNid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail3);

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
        TextView tv_time = (TextView) findViewById(R.id.tv_time);
        TextView tv_end_time = (TextView) findViewById(R.id.tv_end_time);
        Button signButton = (Button) findViewById(R.id.loginButton);

        activityLocation.setText("地点：" + active.getActiveLocation());
        activityDes.setText("    " + active.getActiveDes());
        tv_time.setText("开始时间：" + active.getActiveTime().replace("T", " ").substring(0, 16));
        tv_end_time.setText("结束时间：" + active.getEndTime().replace("T", " ").substring(0, 16));

        // 签到按钮点击事件
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 签到
                showSignConfirmDialog();
            }
        });
    }

    // 签到逻辑
    private void signIn() {
        switch (active.getRule()) {
            // 日常活动
            case 1:
                // 如果当前签到时间距离上一次签到时间不超过24个小时，就不能再次签到
                if (database.isRecentSign(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId()) || clickDebug > 24) {
                    if (TimeCompareDay(active.getActiveTime().replace("T", " ").substring(11, 19))) {
                        // TODO 如果能检测到云子 可以签到 为了优化用户体验，当连续点击15次，可以签到
                        if (isCan || clickCount > 14) {
                            signInForService();
                        } else {
                            ToastUtil.show("暂时无法进行签到，请稍后重试，并确保您在活动地点附近");
                            clickCount++;
                        }
                    } else {
                        ToastUtil.show("不符合签到时间");
                    }
                } else if (database.isSignOut(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId(), new SignItem()) == 0){
                    ToastUtil.show("您还没有签离，请先签离该活动");
                } else {
                    clickDebug++;
                    ToastUtil.show("您今天已经签到过，请明天在来吧");
                }
                break;
            // 普通活动
            case 0:
                int flag = database.isSign2(SpUtil.getString(Constant.ACCOUNT, ""), active.getActiveId());
                switch (flag) {
                    // 如果还没有签到过
                    case 0:
                        // 如果符合时间
                        if (TimeCompare(active.getActiveTime().replace("T", " ").substring(0, 19))) {
                            // 如果能检测到云子 可以签到
                            if (isCan || clickCount > 14) {
                                signInForService();
                            } else {
                                ToastUtil.show("暂时无法进行签到，请稍后重试，并确保您在活动地点附近");
                                clickCount++;
                            }
                        } else {
                            ToastUtil.show("不符合签到时间");
                        }
                        break;
                    // 已经签到过
                    case 1:
                        ToastUtil.show("您已经签到过");
                        break;
                    // 没有签离
                    case 2:
                        ToastUtil.show("您还没有签离，请先签离");
                        break;
                }
                break;
        }
    }

    // 发送签到数据到网络
    private void signInForService() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String inTime = df.format(new Date());
        showDialog("签到中...");
        // get
        /*OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TSign/InsertSign")
                .addParams("account", SpUtil.getString("account", ""))
                .addParams("activityid", active.getActiveId() + "")
                .addParams("intime", inTime)
                .addParams("outtime", inTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        closeDialog();
                        ToastUtil.show("签到失败:" + e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        closeDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.getBoolean("sucessed")) {
                                mNid = jsonObject.getInt("data");
                                saveSignInData(inTime);
                                Intent intent = new Intent(DetailActivity3.this, MoveActivity2.class);
                                intent.putExtra("active",active);
                                intent.putExtra("yunziId", yunziId);

                                startActivity(intent);
                                finish();
                            } else {
                                ToastUtil.show("签到失败："+ jsonObject.getString("Msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtil.show("签到失败:" + e.toString());
                        }
                    }
                });*/

        // post
        OkHttpUtils
                .post()
                .url(Constant.API_URL + "api/TSign/InsertSignPost")
                .addParams("StudnetNum", SpUtil.getString("account", ""))
                .addParams("ActivityId", active.getActiveId() + "")
                .addParams("InTime", inTime)
                .addParams("OutTime", inTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        closeDialog();
                        ToastUtil.show("签到失败:" + e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        closeDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.getBoolean("sucessed")) {
                                mNid = jsonObject.getInt("data");
                                saveSignInData(inTime);
                                Intent intent = new Intent(DetailActivity3.this, MoveActivity2.class);
                                intent.putExtra("active",active);
                                intent.putExtra("yunziId", yunziId);

                                startActivity(intent);
                                finish();
                            } else {
                                ToastUtil.show("签到失败："+ jsonObject.getString("Msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtil.show("签到失败:" + e.toString());
                        }
                    }
                });
    }

    // 保存数据到本地数据库
    private void saveSignInData(String inTime) {
        SignItem signItem = new SignItem();

        signItem.setNumber(SpUtil.getString(Constant.ACCOUNT,""));
        signItem.setActiveId((int) active.getActiveId());
        signItem.setInTime(inTime);
        signItem.setOutTime(inTime);
        signItem.setNid(mNid);

        database.saveSignItem(signItem);
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

    // 判断是否到了签到时间  signTime 需要签到的时间
    private boolean TimeCompare(String signTime) {
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 当前时间
        String presentTime = sdf.format(new Date());
        try {
            Date beginTime = sdf.parse(signTime);
            Date current = sdf.parse(presentTime);
            Date aEndTime = sdf.parse(active.getEndTime().replace("T", " ").substring(0, 19));
            // 可以提前10分钟签到     并且不能超过要活动结束时间
            if (((current.getTime() + 1000 * 60 * 10) >= beginTime.getTime()) && (current.getTime() < aEndTime.getTime())) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 判断日常活动是否在指定时间范围内
    private boolean TimeCompareDay(String signTime) {
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        // 当前时间
        String presentTime = sdf.format(new Date());
        try {
            Date beginTime = sdf.parse(signTime);
            Date current = sdf.parse(presentTime);
            Date aEndTime = sdf.parse(active.getEndTime().replace("T", " ").substring(11, 19));
            // 可以提前10分钟签到     并且不能超过要活动结束时间
            if (((current.getTime() + 1000 * 60 * 10) >= beginTime.getTime()) && (current.getTime() < aEndTime.getTime())) {
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

    private void showDialog(String title) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(title);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // 显示确认签到对话框
    private void showSignConfirmDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        //设置对话框左上角图标
        builder.setIcon(R.mipmap.logo2);
        //设置对话框标题
        builder.setTitle("确定要签到");
        //设置文本内容
        builder.setMessage("您确定要对该活动签到");
        //设置积极的按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signIn();
            }
        });
        //设置消极的按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}

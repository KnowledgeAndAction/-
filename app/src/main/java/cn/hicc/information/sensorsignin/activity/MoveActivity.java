package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.model.SignActive;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 此界面为签到界面——周凯歌
 */
public class MoveActivity extends AppCompatActivity {

    private TextView tv_inTime;
    private String inTime;
    private TextView tv_activityName;
    private String activeName;
    private MyBroadcast myBroadcast;
    private String outTime;
    private String location;
    private String activityDes;
    private String yunziId;
    private boolean isCan = false;
    private List<String> sensorList = new ArrayList<>();
    private long activeId;
    private MyDatabase database;
    private ProgressDialog progressDialog;
    private boolean isClick = false;
    private TextView tv_total_time;
    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        // 获取数据库
        database = MyDatabase.getInstance();

        getIntentData();

        //初始化控件
        initView();

        //获取签到时间
        getInTime();

        // 注册自动签离广播接收者
        initBroadcast();

        // 更新计时
        updataTime();
    }

    // 注册自动签离广播接收者
    private void initBroadcast() {
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SET_BROADCST_OUT");
        registerReceiver(myBroadcast, intentFilter);
    }

    // 获取上一个页面的数据
    private void getIntentData() {
        Intent intent = getIntent();
        activeName = intent.getStringExtra("activeName");
        location = intent.getStringExtra("location");
        activityDes = intent.getStringExtra("activityDes");
        yunziId = intent.getStringExtra("yunziId");
        activeId = intent.getLongExtra("activeId",0);
        String endTime = intent.getStringExtra("endTime");
        SpUtil.putString("yunziId",yunziId);
        SpUtil.putString("endTime",endTime);

        // 判断是不是对一个新的活动签离 如果是将以前的数据清空
        if (SpUtil.getInt("MoveActiveId",-1) != (int) activeId) {
            SpUtil.remove("time");
            SpUtil.remove("MoveActiveId");
        }
    }

    // 更新计时
    private void updataTime() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        Thread.sleep(1000);
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                        // 获取当前时间
                        long now = System.currentTimeMillis();
                        // 计算从签到时间开始，经过了多长时间
                        long total = now - df.parse(inTime).getTime();
                        // 将毫秒转换成时间格式
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(total);
                        // 计算天数
                        long days = total / (1000 * 60 * 60 * 24);
                        // 计算小时数
                        final long hours = (total-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
                        // 转换成时间格式
                        final String totalTime = df.format(calendar.getTime());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_total_time.setText(hours + ":" + totalTime.substring(totalTime.indexOf(":")+1));
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    // 获取签到时间
    private void getInTime(){
        if (SpUtil.getString("time","").equals("")) {
            // ("HH:mm:ss")(小时：分钟：秒)
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            inTime = df.format(new Date());
            tv_inTime.setText("签到时间：" + inTime);
        } else {
            inTime = SpUtil.getString("time","");
            tv_inTime.setText("签到时间：" + inTime);
            SpUtil.remove("time");
            SpUtil.remove("MoveActiveId");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpUtil.putString("time",inTime);
        SpUtil.putInt("MoveActiveId",(int)activeId);
    }

    // 获取签离时间
    private void getOutTime() {
        // ("HH:mm:ss")(小时：分钟：秒)
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        outTime = df.format(new Date());
    }

    // 初始化控件
    private void initView() {
        tv_inTime = (TextView) findViewById(R.id.tv_inTime);
        tv_activityName = (TextView) findViewById(R.id.tv_activeName);
        tv_total_time = (TextView) findViewById(R.id.tv_total_time);
        Button moveButton = (Button) findViewById(R.id.moveButton);

        tv_activityName.setText(activeName);

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOutTime();
                // TODO 如果可以签离 为了优化用户体验，当连续点击10次，可以签到
                if(isCan || clickCount > 9){
                    isClick = true;
                    // 发送时间数据
                    signForService();
                }else {
                    clickCount++;
                    ToastUtil.show("暂时无法签离，请稍后重试，并确保您在活动地点附近");
                }
            }
        });
    }

    // 保存数据到本地
    private void saveSignData(int save) {
        SignActive signActive = new SignActive();
        signActive.setActiveId(activeId);
        signActive.setSave(save);
        signActive.setInTime(inTime);
        signActive.setNumber(SpUtil.getString(Constant.ACCOUNT,""));
        signActive.setOutTime(outTime);

        database.saveSignActive(signActive);

        // 签离后将保存的活动结束时间清空
        SpUtil.remove("endTime");
    }

    // 发送时间数据
    private void signForService(){
        showDialog();
        OkHttpUtils
                .get()
                .url(Constant.API_URL+"api/TSign/InsertSign")
                .addParams("account", SpUtil.getString("account",""))
                .addParams("activityid",activeId+"")
                .addParams("intime",inTime)
                .addParams("outtime", outTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        closeDialog();
                        ToastUtil.show("签离成功:" + e.toString());
                        saveSignData(0);
                        finish();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        closeDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.getBoolean("sucessed")) {
                                saveSignData(1);
                                ToastUtil.show("签离成功");
                                finish();
                            } else {
                                saveSignData(0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            saveSignData(0);
                        }
                    }
                });
    }

    // 自动签离广播接收者
    public class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取接收到的云子id，添加到集合中
            String sensor2ID = intent.getStringExtra("sensor2ID");
            Logs.d("签离界面接收到了云子id消息:" + sensor2ID);
            sensorList.add(sensor2ID);
            // 如果当集合中包含进入活动的云子id，就可以签离
            isCan = sensorList.contains(yunziId);

            // 离开时间超过10分钟，自动签离
            boolean isLeave = intent.getBooleanExtra("isLeave", false);
            if (isLeave) {
                isClick = true;
                getOutTime();
                signForService();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.d("onDestroy");
        if(myBroadcast != null){
            unregisterReceiver(myBroadcast);
        }
        // 在页面销毁时，如果没有点击过签离按钮，就自动签离
        if (!isClick) {
            getOutTime();
            Logs.d("onDestroy");
            signForService();
        }
        SpUtil.remove("yunziId");
        SpUtil.remove("time");
        SpUtil.remove("MoveActiveId");
    }

    // 重写返回键  使其回到桌面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // 通过隐示意图 开启桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("签离中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

package com.example.administrator.my.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.my.R;
import com.example.administrator.my.utils.Constant;
import com.example.administrator.my.utils.SpUtil;
import com.example.administrator.my.utils.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;

/**
 * 此界面为签到界面——周凯歌
 */
public class MoveActivity extends AppCompatActivity {

    private String mHour;
    private int mMinute;
    private TextView tv_inTime;
    private String mMinute1;
    private String inTime;
    private TextView tv_activityName;
    private String activeName;
    private MyBroadcast myBroadcast;
    private String outTime;
    private boolean isHere=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
        Intent intent = getIntent();
        activeName = intent.getStringExtra("activeName");
        //初始化控件
        initView();
        //获取数据
        getData();
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SET_BROADCST_OUT");
        registerReceiver(myBroadcast, intentFilter);
    }
    /**
     * 获取数据的方法
     */
    private void getData() {
        //获取签到时间
        getInTime();
    }

    /**
     * 获取签到时间
     */
    private void getInTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("HH:mm:ss")(小时：分钟：秒)
        inTime = df.format(new Date());
        tv_inTime.setText(inTime);
        SpUtil.putString("inTime",inTime);
    }
    /**
     * 获取签离时间
     */
    private void getOutTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("HH:mm:ss")(小时：分钟：秒)
        outTime = df.format(new Date());
        SpUtil.putString("outTime",outTime);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        tv_inTime = (TextView) findViewById(R.id.tv_inTime);
        tv_activityName = (TextView) findViewById(R.id.tv_activeName);
        tv_activityName.setText(activeName);
        tv_inTime.setText(mHour+mMinute);
        Button moveButton = (Button) findViewById(R.id.moveButton);
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOutTime();
                //发送时间数据
                if(isHere){
                    setTime();
                }else {
                    ToastUtil.show("请稍后重试");
                }
            }
        });
    }



    /**
     * 发送时间数据
     */
    private void setTime(){
        OkHttpUtils
                .get()
                .url(Constant.API_URL+"api/TSign/InsertSign")
                .addParams("account", SpUtil.getString("account",""))
                .addParams("activityid","1")
                .addParams("intime",inTime)
                .addParams("outtime", outTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        ToastUtil.show("保存失败");
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        ToastUtil.show("保存成功");
                        finish();
                    }
                });
    }
    public class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isHere=true;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myBroadcast!=null){
            unregisterReceiver(myBroadcast);
        }

    }
}

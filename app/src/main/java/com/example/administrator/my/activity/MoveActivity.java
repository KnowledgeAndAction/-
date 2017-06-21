package com.example.administrator.my.activity;

import android.content.Intent;
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
    }
    /**
     * 获取数据的方法
     */
    private void getData() {
        //获取当前时间
        getTime();
    }
    /**
     * 获取系统时间
     */
    private void getTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("HH:mm:ss")(小时：分钟：秒)
        inTime = df.format(new Date());
        tv_inTime.setText(inTime);
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
                startActivity(new Intent(MoveActivity.this,MainActivity.class));
                //发送时间数据
                setTime();
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
//        api/TSign/InsertSign?account={account}&activityid={activityid}&intime={intime}&outtime={outtime}
                .addParams("account", SpUtil.getString("account",""))
                .addParams("activityid","1")
                .addParams("intime",inTime)
                .addParams("outtime",inTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        ToastUtil.show("保存失败");
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        ToastUtil.show("保存成功");
                    }
                });
    }
}

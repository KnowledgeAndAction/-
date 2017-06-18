package com.example.administrator.my.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.my.R;
import com.example.administrator.my.utils.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
        //初始化控件
        initData();
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
        /*long time=System.currentTimeMillis();
        final Calendar mCalendar=Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        mHour = mCalendar.get(Calendar.HOUR) + "";//小时
        //分钟
        mMinute1 = mCalendar.get(Calendar.MINUTE) + "";*/

    }

    /**
     * 初始化控件
     */
    private void initData() {
        tv_inTime = (TextView) findViewById(R.id.tv_inTime);
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
                .url("http://123.123")
                .addParams("inTime",inTime)
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

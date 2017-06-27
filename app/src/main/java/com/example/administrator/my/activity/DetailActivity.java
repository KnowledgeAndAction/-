package com.example.administrator.my.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.my.R;
import com.example.administrator.my.model.Active;
import com.example.administrator.my.utils.ToastUtil;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 此界面为活动详情——周凯歌
 */
public class DetailActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView activityLocation;
    private TextView activityDes;
    private TextView activtyName;
    private Active active;
    private String activeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Intent intent = getIntent();

        active = (Active) intent.getSerializableExtra("active");
        Log.d("考虑", "" + active);
        //初始化数据
        initData();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        activtyName = (TextView) findViewById(R.id.tv_name);
        activityLocation = (TextView) findViewById(R.id.tv_location);
        activityDes = (TextView) findViewById(R.id.tv_des);

        activeName = active.getActiveName();

        activtyName.setText(activeName);
        activityLocation.setText(active.getActiveLocation());
        activityDes.setText(active.getActiveDes());

        loginButton = (Button) findViewById(R.id.loginButton);  //登录按钮
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TimeCompare(active.getActiveTime().replace("T"," ").substring(0,16))){
                    Intent intent = new Intent(DetailActivity.this, MoveActivity.class);
                    intent.putExtra("activeName", active.getActiveName());
                    intent.putExtra("location",active.getActiveLocation());
                    intent.putExtra("activityDes",active.getActiveDes());
                    startActivity(intent);
                    finish();
                }else {
                    ToastUtil.show("未到签到时间");
                }

            }
        });

    }

    /**
     *判断是否到了签到时间   true/false
     * @param signTime  需要签到的时间
     */
    private boolean TimeCompare(String signTime){
        //格式化时间
        SimpleDateFormat currentTime= new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String presentTime = currentTime.format(new java.util.Date());//当前时间
        try {
            java.util.Date beginTime = currentTime.parse(signTime);
            java.util.Date endTime = currentTime.parse(presentTime);
            if(endTime.getTime()>=beginTime.getTime()) {
//                System.out.println("运行了运行了运行了运行了运行了");
                return true;
            }else{
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }
}

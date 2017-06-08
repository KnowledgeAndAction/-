package com.example.administrator.my.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.administrator.my.R;
import com.example.administrator.my.utils.ToastUtil;

/**
 * 此界面为活动详情——周凯歌
 */
public class DetailActivity extends AppCompatActivity {

    private EditText activityName;
    private EditText activityDes;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //初始化数据
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        activityName = (EditText) findViewById(R.id.activityName);//活动名称
        activityDes = (EditText) findViewById(R.id.activityDes);//描述
        loginButton = (Button) findViewById(R.id.loginButton);  //登录按钮
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, MoveActivity.class);
                startActivity(intent);
                ToastUtil.show(getApplicationContext(),"签到成功");
            }
        });

    }
}

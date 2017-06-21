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
                Intent intent = new Intent(DetailActivity.this, MoveActivity.class);
                intent.putExtra("activeName", active.getActiveName());
                startActivity(intent);
                finish();
            }
        });

    }
}

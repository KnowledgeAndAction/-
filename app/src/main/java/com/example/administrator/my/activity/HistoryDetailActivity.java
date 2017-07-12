package com.example.administrator.my.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.administrator.my.R;
import com.example.administrator.my.bean.HistoryActivity;

/**
 * 历史活动详情——崔国钊
 */
public class HistoryDetailActivity extends AppCompatActivity {
    private HistoryActivity historyActivity;
    private TextView tv_details;
    private TextView tv_hintime;
    private TextView tv_houttime;
    private TextView tv_hname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        Intent intent = getIntent();
        historyActivity = (HistoryActivity) intent.getSerializableExtra("hActivityId");
        initVie();
    }

    private void initVie() {
        tv_details = (TextView) findViewById(R.id.tv_details);
        tv_hintime = (TextView) findViewById(R.id.tv_hintime);
        tv_houttime = (TextView) findViewById(R.id.tv_houttime);
        tv_hname = (TextView) findViewById(R.id.tv_hname);

        tv_details.setText(historyActivity.getActivityDescription());
        tv_hintime.setText(historyActivity.gethInTime().replace("T"," "));
        tv_houttime.setText(historyActivity.gethOutTime().replace("T",""));
        tv_hname.setText(historyActivity.gethActivityName());

    }


}

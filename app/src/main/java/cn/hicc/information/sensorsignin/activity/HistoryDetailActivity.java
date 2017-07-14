package cn.hicc.information.sensorsignin.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import cn.hicc.information.sensorsignin.model.HistoryActivity;

/**
 * 历史活动详情——崔国钊
 */
public class HistoryDetailActivity extends AppCompatActivity {
    private HistoryActivity historyActivity;
    private TextView tv_details;
    private TextView tv_hintime;
    private TextView tv_houttime;
    private TextView tv_hname;
    private TextView tv_hlocation;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        Intent intent = getIntent();
        historyActivity = (HistoryActivity) intent.getSerializableExtra("ActivityId");
        initVie();
    }

    private void initVie() {
        tv_details = (TextView) findViewById(R.id.tv_details);
        tv_hintime = (TextView) findViewById(R.id.tv_hintime);
        tv_houttime = (TextView) findViewById(R.id.tv_houttime);
        tv_hname = (TextView) findViewById(R.id.tv_hname);
        tv_hlocation = (TextView) findViewById(R.id.tv_hlocation);
        toolbar= (Toolbar) findViewById(R.id.toolbar);

        tv_details.setText("  " + historyActivity.getActivityDescription());
        tv_hintime.setText(historyActivity.gethInTime().replace("T", " "));
        tv_houttime.setText(historyActivity.gethOutTime().replace("T", " "));
        tv_hname.setText(historyActivity.gethActivityName());
        tv_hlocation.setText(historyActivity.getLocation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("历史记录详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


}

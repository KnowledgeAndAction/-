package cn.hicc.information.sensorsignin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hicc.information.sensorsignin.R;

import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;

/**
 * 管理员界面——陈帅
 */

public class AdminActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initView();
    }

    private void initView() {
        Button bt_look = (Button) findViewById(R.id.bt_look);
        Button bt_exit = (Button) findViewById(R.id.bt_exit);

        bt_look.setOnClickListener(this);
        bt_exit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_look:
                startActivity(new Intent(this, AdminActiveActivity.class));
                break;
            case R.id.bt_exit:
                startActivity(new Intent(this, LoginActivity.class));
                SpUtil.putBoolean(Constant.IS_REMBER_PWD,false);
                finish();
                break;
        }
    }
}

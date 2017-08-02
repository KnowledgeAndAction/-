package cn.hicc.information.sensorsignin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import cn.hicc.information.sensorsignin.model.ExitEvent;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.MD5Util;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 修改密码界面——崔国钊
 */
public class ChangePswActivity extends AppCompatActivity {
    private EditText et_old;
    private EditText et_new;
    private EditText et_new_true;
    private Button bt_pwd_change;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);
        initDate();
        change();//设置按钮点击事件
    }

    private void change() {
        bt_pwd_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old_pwd = et_old.getText().toString().trim();
                String new_pwd = et_new.getText().toString().trim();
                String new_pwd_true = et_new_true.getText().toString().trim();
                final String old_psd = MD5Util.strToMD5(old_pwd);
                if (!old_psd.equals(SpUtil.getString(Constant.PASS_WORD, ""))) {
                    ToastUtil.show("密码不正确");
                } else if (old_psd.equals("") || new_pwd.equals("")) {
                    ToastUtil.show("密码不能为空！");
                } else if (new_pwd.equals(old_pwd)) {
                    ToastUtil.show("新密码与旧密码相同！");
                } else if (new_pwd_true.equals("")) {
                    ToastUtil.show("请确认密码");
                } else if (new_pwd_true.equals(new_pwd)) {
                    checkPassWorld(new_pwd);
                } else {
                    ToastUtil.show("密码不一致！");
                }

            }

            //检查是否修改成功
            private void checkPassWorld(String new_password) {
                final String MD5Pass = MD5Util.strToMD5(new_password);
                Logs.d(MD5Pass);
                OkHttpUtils
                        .post()
                        //.url(Constant.API_URL + "api/TStudentLogin/updstupassword")
                        .url("http://123.206.57.216:8080/SchoolTestInterface/changePassword.do")
                        .addParams("Account", SpUtil.getString(Constant.ACCOUNT, ""))
                        .addParams("Password", MD5Pass)
                        //.addParams("Level","0")
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                ToastUtil.show("修改失败：" + e.toString());
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                getJSON(response);
                            }
                        });
            }

            private void getJSON(String response) {
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    boolean sucessed = jsonObject.getBoolean("sucessed");
                    if (sucessed){
                        EventBus.getDefault().post(new ExitEvent());
                        Intent intent=new Intent(ChangePswActivity.this,LoginActivity.class);
                        startActivity(intent);
                        SpUtil.putBoolean(Constant.IS_REMBER_PWD,false);
                        finish();
                        ToastUtil.show("修改成功");
                    }else {
                        ToastUtil.show("网络错误，请重新修改");
                    }
                } catch (JSONException e) {
                    ToastUtil.show("修改失败"+e.toString());
                    e.printStackTrace();
                }
            }
        });
    }


    private void initDate() {
        et_old = (EditText) findViewById(R.id.et_old);
        et_new = (EditText) findViewById(R.id.et_new);
        et_new_true = (EditText) findViewById(R.id.et_new_true);
        bt_pwd_change = (Button) findViewById(R.id.pwd_change);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("修改密码");
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

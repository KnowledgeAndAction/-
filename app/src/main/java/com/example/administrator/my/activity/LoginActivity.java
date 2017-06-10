package com.example.administrator.my.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.administrator.my.R;
import com.example.administrator.my.utils.Constant;
import com.example.administrator.my.utils.SpUtil;
import com.example.administrator.my.utils.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * 登录
 */
public class LoginActivity extends AppCompatActivity {

    private EditText et_account;
    private EditText et_password;
    private CheckBox cb_remember;
    private Button bt_login;
    private static final int USER_ORDINARY = 0;
    private static final int USER_ADMIN = 1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        initView();

        login();
    }

    private void login() {
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = et_account.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                if (!account.equals("") && !password.equals("")) {
                    showProgressDialog();
                    checkLogin(account, password);
                } else {
                    ToastUtil.show("账号或密码不能为空");
                }
            }
        });
    }

    private void checkLogin(final String account, final String password) {
        OkHttpUtils.get().url(Constant.TEST_URL+"login.do")
                .addParams("Account",account)
                .addParams("Password",password)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.show("登录失败，请稍后重试");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        // 解析json数据
                        getJson(response,account,password);
                    }
                });
    }

    private void getJson(String response, String account, String password) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean flag = jsonObject.getBoolean("flag");
            if (flag) {
                // 检测是否记住密码
                if (cb_remember.isChecked()) {
                    SpUtil.putString("account",account);
                    SpUtil.putString("password",password);
                    SpUtil.putBoolean("check",true);
                }

                closeProgressDialog();

                JSONObject result = jsonObject.getJSONObject("result");
                int type = result.getInt("type");
                if (type == USER_ORDINARY) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else if (type == USER_ADMIN) {
                    startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                    finish();
                }
            } else {
                closeProgressDialog();
                ToastUtil.show("账号或密码错误");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        et_account = (EditText) findViewById(R.id.et_account);
        et_password = (EditText) findViewById(R.id.et_password);
        cb_remember = (CheckBox) findViewById(R.id.cb_remember);
        bt_login = (Button) findViewById(R.id.bt_login);

        et_account.setText(SpUtil.getString("account",""));
        et_password.setText(SpUtil.getString("password",""));
        cb_remember.setChecked(SpUtil.getBoolean("check",false));
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("登录中");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

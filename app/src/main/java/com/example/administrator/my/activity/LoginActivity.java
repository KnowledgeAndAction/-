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
import com.example.administrator.my.utils.MD5Util;
import com.example.administrator.my.utils.SpUtil;
import com.example.administrator.my.utils.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * 登录——陈帅
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

        // 登录按钮点击事件
        login();
    }

    // 登录按钮点击事件
    private void login() {
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = et_account.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                // 如果账号密码不为空，检查是否正确
                if (!account.equals("") && !password.equals("")) {
                    showProgressDialog();
                    checkLogin(account, password);
                } else {
                    ToastUtil.show("账号或密码不能为空");
                }
            }
        });
    }

    // 检查是否登录成功
    private void checkLogin(final String account, final String password) {
        // 对密码md5加密
        String MD5Pass = MD5Util.strToMD5(password);
        // 发送请求
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TStudentLogin/GetStudentType")
                .addParams("Account",account)
                .addParams("pwd", MD5Pass)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        closeProgressDialog();
                        ToastUtil.show("登录失败：" + e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        // 解析json数据
                        getJson(response,account,password);
                    }
                });
    }

    // 解析json数据
    private void getJson(String response, String account, String password) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean sucessed = jsonObject.getBoolean("sucessed");
            if (sucessed) {
                // 检测是否记住密码
                if (cb_remember.isChecked()) {
                    SpUtil.putString("account",account);
                    SpUtil.putString("password",password);
                }
                SpUtil.putBoolean("check",cb_remember.isChecked());

                // 登录成功
                int type = jsonObject.getInt("data");
                closeProgressDialog();
                // 根据用户类型，跳转到不同页面
                if (type == USER_ORDINARY) {
                    // 跳转到普通用户界面
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else if (type == USER_ADMIN) {
                    // 跳转到管理员用户界面
                    startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                    finish();
                }
            } else {
                // 登录失败
                closeProgressDialog();
                ToastUtil.show("账号或密码错误");
            }
        } catch (JSONException e) {
            // json解析异常
            e.printStackTrace();
            closeProgressDialog();
            ToastUtil.show("登录失败：" + e.toString());
        }
    }

    // 初始化控件
    private void initView() {
        et_account = (EditText) findViewById(R.id.et_account);
        et_password = (EditText) findViewById(R.id.et_password);
        cb_remember = (CheckBox) findViewById(R.id.cb_remember);
        bt_login = (Button) findViewById(R.id.bt_login);

        if (SpUtil.getBoolean("check",false)) {
            et_account.setText(SpUtil.getString("account",""));
            et_password.setText(SpUtil.getString("password",""));
            cb_remember.setChecked(SpUtil.getBoolean("check",false));
        }
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

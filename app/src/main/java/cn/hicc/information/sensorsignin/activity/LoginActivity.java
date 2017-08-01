package cn.hicc.information.sensorsignin.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.MD5Util;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import cn.hicc.information.sensorsignin.view.CustomVideoView;
import okhttp3.Call;

/**
 * 登录——陈帅
 */
public class LoginActivity extends AppCompatActivity {
    private Drawable seen,unseen;
    private EditText et_account;
    private EditText et_password;
    private CheckBox cb_remember;
    private Button bt_login;
    private static final int USER_ORDINARY = 0;
    private static final int USER_ADMIN = 1;
    private ProgressDialog progressDialog;
    private TextInputLayout text_input_account;
    private TextInputLayout text_input_pass;
    private CustomVideoView videoview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_tow);

        // 初始化控件
        initView();

        initVideo();

        // 检查是否已经登录
        checkIsEnter();

        // 登录按钮点击事件
        login();

        // 检查权限
        checkPermission();
    }

    private void initVideo() {
        //加载视频资源控件
        videoview = (CustomVideoView) findViewById(R.id.videoview);
        //设置播放加载路径
        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        //播放
        videoview.start();
        //循环播放
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoview.start();
            }
        });
    }

    //返回重启加载
    @Override
    protected void onRestart() {
        initVideo();
        super.onRestart();
    }

    //防止锁屏或者切出的时候，音乐在播放
    @Override
    protected void onStop() {
        videoview.stopPlayback();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        if (videoview != null) {
            videoview.suspend();
        }
    }

    private void checkPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ToastUtil.show("我们需要访问您的位置，来确定活动地点");
                            return;
                        }
                    }
                } else {
                    ToastUtil.show("出了个小错误");
                    finish();
                }
                break;
        }
    }

    // 登录按钮点击事件
    private void login() {
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLogin = true;
                String account = et_account.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (account.equals("")) {
                    isLogin = false;
                    text_input_account.setError("用户名为空");
                }
                if (password.equals("")) {
                    isLogin = false;
                    text_input_pass.setError("密码为空");
                }
                // 如果账号密码不为空，检查是否正确
                if (isLogin) {
                    showProgressDialog();
                    checkLogin(account, password);
                }
            }
        });
    }

    // 检查是否登录成功
    private void checkLogin(final String account, String password) {
        // 对密码md5加密
        final String MD5Pass = MD5Util.strToMD5(password);
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
                        getJson(response,account,MD5Pass);
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
                checkUp(account,password);

                // 登录成功
                int type = jsonObject.getInt("data");
                SpUtil.putInt(Constant.USER_TYPE, type);
                closeProgressDialog();

                // 根据用户类型，跳转到不同页面
                enterApp(type);
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

    // 进入应用
    private void enterApp(int type) {
        if (type == USER_ORDINARY) {
            // 跳转到普通用户界面
            startActivity(new Intent(getApplicationContext(), MainActivityTow.class));
            finish();
        } else if (type == USER_ADMIN) {
            // 跳转到管理员用户界面
            startActivity(new Intent(getApplicationContext(), AdminActivity.class));
            finish();
        }
    }

    //检查是否勾选记住密码
    private void checkUp(String userName,String mPwd) {
        if (cb_remember.isChecked()) {
            SpUtil.putString(Constant.ACCOUNT,userName);
            SpUtil.putString(Constant.PASS_WORD,mPwd);
            SpUtil.putBoolean(Constant.IS_REMBER_PWD,true);
        } else {
            SpUtil.putString(Constant.ACCOUNT,userName);
            SpUtil.putString(Constant.PASS_WORD,mPwd);
            SpUtil.putBoolean(Constant.IS_REMBER_PWD,false);
        }
    }

    // 检查是否已经登陆
    private void checkIsEnter() {
        if (SpUtil.getBoolean(Constant.IS_REMBER_PWD,false)) {
            enterApp(SpUtil.getInt(Constant.USER_TYPE,0));
        }
    }

    // 初始化控件
    private void initView() {
        //Resources res = getResources();
        //seen=res.getDrawable(R.mipmap.seen);
        //unseen=res.getDrawable(R.mipmap.unseen);
        et_account = (EditText) findViewById(R.id.et_account);
        et_password = (EditText) findViewById(R.id.et_password);
        cb_remember = (CheckBox) findViewById(R.id.cb_remember);
        bt_login = (Button) findViewById(R.id.bt_login);

        text_input_account = (TextInputLayout) findViewById(R.id.text_input_account);
        text_input_pass = (TextInputLayout) findViewById(R.id.text_input_pass);

        //seen.setBounds(0,0,seen.getMinimumWidth(),seen.getMinimumHeight());
        //seen.getBounds().set(0,1,0,0);
        et_account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    text_input_account.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    text_input_pass.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (!SpUtil.getBoolean(Constant.IS_REMBER_PWD,false)) {
            et_account.setText(SpUtil.getString(Constant.ACCOUNT,""));
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("登录中");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

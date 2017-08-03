package cn.hicc.information.sensorsignin.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.MD5Util;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import cn.hicc.information.sensorsignin.view.CustomVideoView;
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
    private TextInputLayout text_input_account;
    private TextInputLayout text_input_pass;
    private CustomVideoView videoview;
    private ProgressDialog downloadProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        // 初始化控件
        initView();

        // 设置背景视频
        initVideo();

        // 检查是否已经登录
        checkIsEnter();

        // 登录按钮点击事件
        login();

        // 检查权限
        checkPermission();

        // 检测更新
        checkVersionCode();
    }

    // 检测更新
    private void checkVersionCode() {
        // 发送GET请求
        OkHttpUtils
                .get()
                .url("http://123.206.57.216:8080/test/update.json")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Logs.i("获取最新app信息失败："+e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Logs.i(response);
                        Logs.i("获取最新app信息成功");
                        // 解析json
                        getAppInfoJson(response);
                    }
                });
    }

    // 解析服务器返回的app信息数据
    private void getAppInfoJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean falg = jsonObject.getBoolean("falg");
            if (falg) {
                JSONObject data = jsonObject.getJSONObject("data");
                double v = Double.valueOf(data.getString("appVersion"));
                int version = (int) v;
                // 如果服务器的版本号大于本地的  就更新
                if(version > getVersionCode()){
                    // 获取下载地址
                    String mAppUrl = data.getString("appUrl");
                    // 获取新版app描述
                    String appDescribe = data.getString("appDescribe");
                    // 如果sd卡可用
                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                        // 展示下载对话框
                        showUpDataDialog(appDescribe, mAppUrl);
                    }
                }
            } else {
                Logs.i("获取最新app信息失败："+jsonObject.getString("data"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logs.i("解析最新app信息失败："+e.toString());
        }
    }

    // 显示更新对话框
    protected void showUpDataDialog(String description, final String appUrl) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        // 设置对话框左上角图标
        builder.setIcon(R.mipmap.logo);
        // 设置不能取消
        builder.setCancelable(false);
        // 设置对话框标题
        builder.setTitle("发现新版本");
        // 设置对话框内容
        builder.setMessage(description);
        // 设置积极的按钮
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 下载apk
                downLoadApk(appUrl);
                // 显示一个进度条对话框
                showDownloadProgressDialog();
            }
        });
        // 设置消极的按钮
        builder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // 下载文件
    private void downLoadApk(String appUrl) {
        OkHttpUtils
                .get()
                .url(appUrl)
                .build()
                .execute(new FileCallBack(getExternalFilesDir("apk").getPath(),"智慧校园.apk") {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.show("下载失败："+e.toString());
                        Logs.i("下载失败："+e.toString()+","+id);
                        downloadProgressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        ToastUtil.show("下载成功,保存路径:");
                        Logs.i("下载成功,保存路径:");
                        // 安装应用
                        installApk(response);
                        downloadProgressDialog.dismiss();
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        // 设置进度
                        downloadProgressDialog.setProgress((int) (100 * progress));
                    }
                });
    }

    // 下载的进度条对话框
    protected void showDownloadProgressDialog() {
        downloadProgressDialog = new ProgressDialog(this);
        downloadProgressDialog.setIcon(R.mipmap.logo);
        downloadProgressDialog.setTitle("下载安装包中");
        downloadProgressDialog.setCanceledOnTouchOutside(false);
        downloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                return;
            }
        });
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.show();
    }

    // 安装应用
    protected void installApk(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //文件作为数据源
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent,1);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // 获取本应用版本号
    private int getVersionCode() {
        // 拿到包管理者
        PackageManager pm = getPackageManager();
        // 获取包的基本信息
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            // 返回应用的版本号
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                checkVersionCode();
        }
    }

    // 初始化背景视频
    private void initVideo() {
        //加载视频资源控件
        videoview = (CustomVideoView) findViewById(R.id.videoview);
        //设置播放加载路径
        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video3));
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

    // 检查权限
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

    // 权限请求结果
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

    // TODO 检查是否登录成功 临时接口
    private void checkLogin(final String account, String password) {
        // 对密码md5加密
        final String MD5Pass = MD5Util.strToMD5(password);
        // 发送请求
        OkHttpUtils
                .get()
                //.url(Constant.API_URL + "api/TStudentLogin/GetStudentType")
                .url("http://123.206.57.216:8080/SchoolTestInterface/login.do")
                .addParams("Account",account)
                //.addParams("pwd", MD5Pass)
                .addParams("Password", MD5Pass)
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
                        //getJson(response,account,MD5Pass);
                        getTemporaryJson(response,account,MD5Pass);
                    }
                });
    }

    // 解析临时数据
    private void getTemporaryJson(String response, String account, String password) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean sucessed = jsonObject.getBoolean("sucessed");
            if (sucessed) {
                // 检测是否记住密码
                checkUp(account,password);

                // 登录成功
                JSONObject data = jsonObject.getJSONObject("data");
                SpUtil.putInt(Constant.USER_TYPE, data.getInt("type"));
                SpUtil.putString(Constant.USER_NAME, data.getString("name"));
                SpUtil.putInt(Constant.USER_GRADE, data.getInt("GradeCode"));
                SpUtil.putString(Constant.USER_CLASS, data.getString("ClassDescription"));
                String imageUrl = data.getString("NewImage");
                if (!imageUrl.equals("null")) {
                    SpUtil.putString(Constant.USER_IMAGE, "http://123.206.57.216:8080/StudentImage/" + imageUrl);
                } else {
                    imageUrl = data.getString("OldImage");
                    SpUtil.putString(Constant.USER_IMAGE, "http://123.206.57.216:8080/OldImage/" + imageUrl);
                }

                // 根据用户类型，跳转到不同页面
                enterApp(data.getInt("type"));
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

    // 解析json数据
    private void getJson(String response, String account, String password) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean sucessed = jsonObject.getBoolean("sucessed");
            if (sucessed) {
                // 检测是否记住密码
                checkUp(account,password);

                // 获取用户姓名
                getUserName(account);

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

    // 获取用户姓名
    private void getUserName(String account) {
        // 发送请求
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TStudentInfo/GetStudentInfo")
                .addParams("studentNu",account)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        closeProgressDialog();
                        Logs.d("获取学生姓名失败"+e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("sucessed")) {
                                JSONObject data = jsonObject.getJSONObject("data");
                                SpUtil.putString(Constant.USER_NAME, data.getString("StudentName"));
                                SpUtil.putInt(Constant.USER_GRADE, data.getInt("GradeCode"));
                                SpUtil.putString(Constant.USER_CLASS, data.getString("ClassDescription"));
                                String imageUrl = data.getString("NewImage");
                                if (!imageUrl.equals("null")) {
                                    SpUtil.putString(Constant.USER_IMAGE, "http://home.hicc.cn/StudentImage/" + imageUrl);
                                } else {
                                    imageUrl = data.getString("OldImage");
                                    SpUtil.putString(Constant.USER_IMAGE, "http://home.hicc.cn/OldImage/" + imageUrl);
                                }
                            } else {
                                Logs.d("获取学生姓名失败" + jsonObject.getString("Msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Logs.d("获取学生姓名失败"+e.toString());
                        }
                    }
                });
    }

    // 进入应用
    private void enterApp(int type) {
        if (type == USER_ORDINARY) {
            // 跳转到普通用户界面
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            closeProgressDialog();
        } else if (type == USER_ADMIN) {
            // 跳转到管理员用户界面
            startActivity(new Intent(getApplicationContext(), AdminActivity.class));
            finish();
            closeProgressDialog();
        }
    }

    // 检查是否勾选记住密码
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

        et_account = (EditText) findViewById(R.id.et_account);
        et_password = (EditText) findViewById(R.id.et_password);
        cb_remember = (CheckBox) findViewById(R.id.cb_remember);
        bt_login = (Button) findViewById(R.id.bt_login);

        //ImageView iv_bg = (ImageView) findViewById(R.id.iv_bg);
        //Glide.with(this).load(R.drawable.a).asGif().into(iv_bg);

        text_input_account = (TextInputLayout) findViewById(R.id.text_input_account);
        text_input_pass = (TextInputLayout) findViewById(R.id.text_input_pass);

        // 设置输入框监听事件，当有内容时，将错误提示清除
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
        // 设置输入框监听事件，当有内容时，将错误提示清除
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

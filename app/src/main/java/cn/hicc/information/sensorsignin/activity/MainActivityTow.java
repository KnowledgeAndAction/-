package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

import com.hicc.information.sensorsignin.R;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.sensoro.cloud.SensoroManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.hicc.information.sensorsignin.fragment.ActivityFragment;
import cn.hicc.information.sensorsignin.fragment.HistoryFragment;
import cn.hicc.information.sensorsignin.fragment.SettingFragment;
import cn.hicc.information.sensorsignin.model.ExitEvent;
import cn.hicc.information.sensorsignin.model.TabItem;
import cn.hicc.information.sensorsignin.service.SensorService;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.StatusBarUtils;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

public class MainActivityTow extends AppCompatActivity {

    private SensoroManager sensoroManager;
    private ViewPager viewPager;
    private ArrayList<TabItem> tabs;
    private static Boolean isExit = false;
    private ProgressDialog progressDialog;
    private BottomBar bottomBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tow);

        sensoroManager = SensoroManager.getInstance(MainActivityTow.this);

        // 初始化控件
        initWidget();

        // 检查蓝牙是否可用
        checkBluetooth();

        // 注册监听退出登录的事件
        EventBus.getDefault().register(this);

        // 检测更新
        checkVersionCode();
    }

    // 检测更新
    private void checkVersionCode() {
        // 发送GET请求
        OkHttpUtils
                .get()
                .url("http://123.206.57.216:8080/test/update.json")
                .addParams("appId", "1")
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
                showProgressDialog();
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
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        ToastUtil.show("下载成功,保存路径:");
                        Logs.i("下载成功,保存路径:");
                        // 安装应用
                        installApk(response);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        // 设置进度
                        progressDialog.setProgress((int) (100 * progress));
                    }
                });
    }

    // 下载的进度条对话框
    protected void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.mipmap.logo);
        progressDialog.setTitle("下载安装包中");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                return;
            }
        });
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
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

    // 检查蓝牙是否可用
    private void checkBluetooth() {
        if (!sensoroManager.isBluetoothEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, 0);
        } else {
            // 开启服务
            startService(new Intent(this, SensorService.class));
        }
    }

    // 初始化控件
    private void initWidget() {
        viewPager = (ViewPager) findViewById(R.id.viewPager_top);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("活动");
        toolbar.setBackgroundColor(Color.parseColor("#03A9F4"));
        setSupportActionBar(toolbar);

        initLayout();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                // 蓝牙可用
                if (sensoroManager.isBluetoothEnabled()) {
                    startService(new Intent(MainActivityTow.this, SensorService.class));
                }
                break;
            case 1:
                checkVersionCode();
        }
    }

    // 初始化布局
    private void initLayout() {
        tabs = new ArrayList<>();
        tabs.add(new TabItem(R.drawable.bottom_activity_selector, R.string.tab_activity, ActivityFragment.class));
        tabs.add(new TabItem(R.drawable.bottom_history_selector, R.string.tab_history, HistoryFragment.class));
        tabs.add(new TabItem(R.drawable.bottom_setting_selector, R.string.tab_setting, SettingFragment.class));
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_active:
                        viewPager.setCurrentItem(0);
                        toolbar.setTitle("活动");
                        toolbar.setBackgroundColor(Color.parseColor("#03A9F4"));
                        StatusBarUtils.setWindowStatusBarColor(MainActivityTow.this, "#03A9F4");
                        break;
                    case R.id.tab_history:
                        viewPager.setCurrentItem(1);
                        toolbar.setTitle("历史");
                        toolbar.setBackgroundColor(Color.parseColor("#5D4037"));
                        StatusBarUtils.setWindowStatusBarColor(MainActivityTow.this, "#5D4037");
                        break;
                    case R.id.tab_setting:
                        viewPager.setCurrentItem(2);
                        toolbar.setTitle("设置");
                        toolbar.setBackgroundColor(Color.parseColor("#045563"));
                        StatusBarUtils.setWindowStatusBarColor(MainActivityTow.this, "#045563");
                        break;
                }
            }
        });

        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomBar.selectTabAtPosition(position,true);
                switch (position) {
                    case 0:
                        toolbar.setTitle("活动");
                        toolbar.setBackgroundColor(Color.parseColor("#03A9F4"));
                        StatusBarUtils.setWindowStatusBarColor(MainActivityTow.this, "#03A9F4");
                        break;
                    case 1:
                        toolbar.setTitle("历史");
                        toolbar.setBackgroundColor(Color.parseColor("#5D4037"));
                        StatusBarUtils.setWindowStatusBarColor(MainActivityTow.this, "#5D4037");
                        break;
                    case 2:
                        toolbar.setTitle("设置");
                        toolbar.setBackgroundColor(Color.parseColor("#045563"));
                        StatusBarUtils.setWindowStatusBarColor(MainActivityTow.this, "#045563");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 设置底部条目及对应界面的适配器
     */
    class FragmentAdapter extends FragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                return tabs.get(position).tagFragmentClz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }
    }

    // 接收退出登录的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ExitEvent event) {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        stopService(new Intent(this,SensorService.class));
    }

    // 监听返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            exitBy2Click();
        }
        return false;
    }

    // 双击退出程序
    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            ToastUtil.show("再按一次退出程序");
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
        }
    }

}

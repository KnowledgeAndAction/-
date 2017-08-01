package cn.hicc.information.sensorsignin.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import cn.hicc.information.sensorsignin.MyApplication;
import cn.hicc.information.sensorsignin.activity.ChangePswActivity;
import cn.hicc.information.sensorsignin.activity.LoginActivity;
import cn.hicc.information.sensorsignin.activity.MainActivity;
import cn.hicc.information.sensorsignin.model.ExitEvent;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 设置界面——崔国钊
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener {
    private TextView tv_changePassWorld;
    private TextView tv_checkToUpdata;
    private TextView tv_esc;
    private ProgressDialog progressDialog;

    private Dialog dialog = new Dialog(MyApplication.getContext());

    @Override
    public void fetchData() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bottom_setting, container, false);
        inItUI(view);
        return view;
    }

    /**
     * 实例化按钮
     */
    private void inItUI(View view) {
        tv_changePassWorld = (TextView) view.findViewById(R.id.tv_changePassWorld);
        tv_checkToUpdata = (TextView) view.findViewById(R.id.tv_checkToUpdate);
        tv_esc = (TextView) view.findViewById(R.id.tv_esc);

        tv_changePassWorld.setOnClickListener(this);
        tv_checkToUpdata.setOnClickListener(this);
        tv_esc.setOnClickListener(this);
    }

    //设置按钮的点击事件。
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tv_changePassWorld:
                Intent intent = new Intent(getContext(), ChangePswActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_checkToUpdate:
                showProgressDialogs();
                // 发送GET请求
                OkHttpUtils
                        .get()
                        .url("http://123.206.57.216:8080/test/update.json")
                        .addParams("appId", "1")
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                closeProgressDialog();
                                Logs.i("获取最新app信息失败：" + e.toString());
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Logs.i(response);
                                Logs.i("获取最新app信息成功");
                                closeProgressDialog();
                                // 解析json
                                getAppInfoJson(response);
                            }
                        });
                break;
            case R.id.tv_esc:
                showConfirmDialog();
                break;
        }
    }

    private void showConfirmDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        //设置对话框左上角图标
        builder.setIcon(R.mipmap.logo);
        //设置对话框标题
        builder.setTitle("是否退出");
        //设置文本内容
        builder.setMessage("您将会退出应用");
        //设置积极的按钮
        builder.setPositiveButton("确认退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EventBus.getDefault().post(new ExitEvent());
                startActivity(new Intent(getContext(), LoginActivity.class));
                SpUtil.putBoolean(Constant.IS_REMBER_PWD, false);
            }
        });
        //设置消极的按钮
        builder.setNegativeButton("暂不推出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

//    private void show_dialog(String s) {
//        dialog = new Dialog(getContext());
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view, null);
//        // 获取对话框的文本内容控件
//        TextView tv_content = (TextView) view.findViewById(R.id.tv_action);
//        // 给控件设置文本，这里根据你传过来的值进行设置
//        tv_content.setText(s);
//        // 设置Dialog没有标题，这个一定要在设置内容之前定义
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        // 给对话框的内容设置一个自定义的view
//        dialog.setContentView(view);
//        // 显示对话框
//        dialog.show();
//
//    }

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
                if (version > getVersionCode()) {
                    // 获取下载地址
                    String mAppUrl = data.getString("appUrl");
                    // 获取新版app描述
                    String appDescribe = data.getString("appDescribe");
                    // 如果sd卡可用
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        // 展示下载对话框
                        showUpDataDialog(appDescribe, mAppUrl);
                    }
                } else {
                    ToastUtil.show("您的版本已经是最新的啦");
                }
            } else {
                Logs.i("获取最新app信息失败：" + jsonObject.getString("data"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logs.i("解析最新app信息失败：" + e.toString());
        }
    }

    // 显示更新对话框
    protected void showUpDataDialog(String description, final String appUrl) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        //设置对话框左上角图标
        builder.setIcon(R.mipmap.logo);
        //设置对话框标题
        builder.setTitle("发现新版本");
        //设置对话框内容
        builder.setMessage(description);
        //设置积极的按钮
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //下载apk
                downLoadApk(appUrl);
                // 显示一个进度条对话框
                showProgressDialog();
            }
        });
        //设置消极的按钮
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
                .execute(new FileCallBack(MyApplication.getContext().getExternalFilesDir("apk").getPath(), "智慧校园.apk") {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.show("下载失败：" + e.toString());
                        Logs.i("下载失败：" + e.toString() + "," + id);
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
        progressDialog = new ProgressDialog(getContext());
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
        startActivityForResult(intent, 1);
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    // 获取本应用版本号
    private int getVersionCode() {
        // 拿到包管理者
        PackageManager pm = MyApplication.getContext().getPackageManager();
        // 获取包的基本信息
        try {
            PackageInfo info = pm.getPackageInfo(MyApplication.getContext().getPackageName(), 0);
            // 返回应用的版本号
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void showProgressDialogs() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setMessage("检测更新中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

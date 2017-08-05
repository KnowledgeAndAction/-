package cn.hicc.information.sensorsignin.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.model.Active;
import cn.hicc.information.sensorsignin.model.SignItem;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.Logs;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 此界面为签到界面——陈帅
 */
public class MoveActivity2 extends AppCompatActivity {

    private TextView tv_inTime;
    private String inTime;

    private String activeName;
    private MyBroadcast myBroadcast;
    private String outTime;
    private String location;
    private String yunziId;
    private boolean isCan = false;
    private List<String> sensorList = new ArrayList<>();
    private long activeId;
    private MyDatabase database;
    private ProgressDialog progressDialog;
    private boolean isClick = false;
    private TextView tv_total_time;
    private int clickCount = 0;
    private boolean stop = true;
    private int mNid;
    private String endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move2);

        // 获取数据库
        database = MyDatabase.getInstance();

        getIntentData();

        //初始化控件
        initView();

        // 注册自动签离广播接收者
        initBroadcast();

        // 更新计时
        updataTime();

        // 保存未签离的数据
        saveUnSignOutData();
    }

    // 清除未签离的活动数据
    private void clearUnSignOutData() {
        SpUtil.remove(Constant.SIGN_OUT_YUNZIID);
        SpUtil.remove(Constant.SIGN_OUT_ACTIVE_NAME);
        SpUtil.remove(Constant.SIGN_OUT_LOCATION);
        SpUtil.remove(Constant.SIGN_OUT_ACTIVE_ID);
        SpUtil.remove(Constant.SIGN_OUT_ENDTIME);
    }

    // 保存未签离的数据
    private void saveUnSignOutData() {
        SpUtil.putString(Constant.SIGN_OUT_YUNZIID,yunziId);
        SpUtil.putString(Constant.SIGN_OUT_ACTIVE_NAME,activeName);
        SpUtil.putString(Constant.SIGN_OUT_LOCATION,location);
        SpUtil.putInt(Constant.SIGN_OUT_ACTIVE_ID, (int) activeId);
        SpUtil.putString(Constant.SIGN_OUT_ENDTIME,endTime);
    }

    // 注册自动签离广播接收者
    private void initBroadcast() {
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SET_BROADCST_OUT");
        registerReceiver(myBroadcast, intentFilter);
    }

    // 获取上一个页面的数据
    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            Active active = (Active) intent.getSerializableExtra("active");
            yunziId = intent.getStringExtra("yunziId");

            activeName = active.getActiveName();
            location = active.getActiveLocation();
            activeId = active.getActiveId();
            endTime = active.getEndTime();

            SpUtil.putString("yunziId",yunziId);
            SpUtil.putString("endTime",endTime);
        }
    }

    // 更新计时
    private void updataTime() {
        // 如果还没有签离
        final SignItem signItem = new SignItem();
        int flag = database.isSignOut(SpUtil.getString(Constant.ACCOUNT, ""), activeId, signItem);
        tv_inTime.setText("签到时间：" + signItem.getInTime().substring(11));
        mNid = signItem.getNid();
        if (flag == 0) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    while (stop) {
                        try {
                            Thread.sleep(1000);
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            // 获取当前时间
                            long now = System.currentTimeMillis();
                            // 计算从签到时间开始，经过了多长时间
                            long total = now - df.parse(signItem.getInTime()).getTime();
                            // 将毫秒转换成时间格式
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(total);
                            // 计算天数
                            long days = total / (1000 * 60 * 60 * 24);
                            // 计算小时数
                            final long hours = (total - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
                            // 转换成时间格式
                            final String totalTime = df.format(calendar.getTime());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_total_time.setText(hours + ":" + totalTime.substring(totalTime.indexOf(":") + 1));
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }

    // 获取签离时间
    private void getOutTime() {
        // ("HH:mm:ss")(小时：分钟：秒)
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        outTime = df.format(new Date());
    }

    // 初始化控件
    private void initView() {
        TextView tv_activityName = (TextView) findViewById(R.id.tv_activeName);
        TextView tv_location = (TextView) findViewById(R.id.tv_location);
        tv_inTime = (TextView) findViewById(R.id.tv_inTime);
        tv_total_time = (TextView) findViewById(R.id.tv_total_time);
        Button moveButton = (Button) findViewById(R.id.moveButton);

        tv_activityName.setText(activeName);
        tv_location.setText(location);

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 签离逻辑
                showSignOutConfirmDialog();
            }
        });
    }

    private void signOut() {
        getOutTime();
        // TODO 如果可以签离 为了优化用户体验，当连续点击15次，可以签到
        if(isCan || clickCount > 14){
            isClick = true;
            // 发送时间数据
            signForService();
        }else {
            clickCount++;
            ToastUtil.show("暂时无法签离，请稍后重试，并确保您在活动地点附近");
        }
    }

    // 发送时间数据
    private void signForService(){
        showDialog();
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TSign/UpdOutTime")
                .addParams("nid", mNid+"")
                .addParams("outtime", outTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        closeDialog();
                        ToastUtil.show("签离失败:" + e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        closeDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.getBoolean("sucessed")) {
                                database.updateSignOutTime(mNid,outTime);
                                stop = false;
                                ToastUtil.show("签离成功");

                                // 签离后将保存的活动结束时间清空
                                SpUtil.remove("endTime");
                                clearUnSignOutData();

                                finish();
                            } else {
                                ToastUtil.show("签离失败："+ jsonObject.getString("Msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtil.show("签离失败:" + e.toString());
                        }
                    }
                });
    }

    // 自动签离广播接收者
    public class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取接收到的云子id，添加到集合中
            String sensor2ID = intent.getStringExtra("sensor2ID");
            Logs.d("签离界面接收到了云子id消息:" + sensor2ID);
            sensorList.add(sensor2ID);
            // 如果当集合中包含进入活动的云子id，就可以签离
            isCan = sensorList.contains(yunziId);

            // 离开时间超过10分钟，自动签离
            boolean isLeave = intent.getBooleanExtra("isLeave", false);
            if (isLeave) {
                isClick = true;
                getOutTime();
                signForService();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myBroadcast != null){
            unregisterReceiver(myBroadcast);
        }
        // 在页面销毁时，如果没有点击过签离按钮，就自动签离
        if (!isClick) {
            getOutTime();
            signForService();
        }
        SpUtil.remove("yunziId");
    }

    // 重写返回键  使其回到桌面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // 通过隐示意图 开启桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("签离中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // 显示确认签离对话框
    private void showSignOutConfirmDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        //设置对话框左上角图标
        builder.setIcon(R.mipmap.logo);
        //设置对话框标题
        builder.setTitle("确定要签离");
        //设置文本内容
        builder.setMessage("您确定要对该活动签离");
        //设置积极的按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signOut();
            }
        });
        //设置消极的按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}

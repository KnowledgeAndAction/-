package cn.hicc.information.sensorsignin.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hicc.information.sensorsignin.db.MyDatabaseHelper;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;

/**
 * 此界面为签到界面——周凯歌
 */
public class MoveActivity extends AppCompatActivity {

    private String mHour;
    private int mMinute;
    private TextView tv_inTime;
    private String mMinute1;
    private String inTime;
    private TextView tv_activityName;
    private String activeName;
    private MyBroadcast myBroadcast;
    private String outTime;
    private MyDatabaseHelper dbHelper;
    private String location;
    private String activityDes;
    private String yunziId;
    private boolean isCan = false;
    private List<String> sensorList = new ArrayList<>();
    private long activeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
        //创建数据库
        dbHelper = new MyDatabaseHelper(this,"userInformation.db",null,1);
        dbHelper.getWritableDatabase();

        Intent intent = getIntent();
        activeName = intent.getStringExtra("activeName");
        location = intent.getStringExtra("location");
        activityDes = intent.getStringExtra("activityDes");
        yunziId = intent.getStringExtra("yunziId");
        activeId = intent.getLongExtra("activeId",0);
        //初始化控件
        initView();
        //获取数据
        getData();
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SET_BROADCST_OUT");
        registerReceiver(myBroadcast, intentFilter);
    }
    /**
     * 获取数据的方法
     */
    private void getData() {
        //获取签到时间
        getInTime();
    }

    /**
     * 获取签到时间
     */
    private void getInTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("HH:mm:ss")(小时：分钟：秒)
        inTime = df.format(new Date());
        tv_inTime.setText(inTime);
        SpUtil.putString("inTime",inTime);
    }
    /**
     * 获取签离时间
     */
    private void getOutTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("HH:mm:ss")(小时：分钟：秒)
        outTime = df.format(new Date());
        SpUtil.putString("outTime",outTime);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        tv_inTime = (TextView) findViewById(R.id.tv_inTime);
        tv_activityName = (TextView) findViewById(R.id.tv_activeName);
        tv_activityName.setText(activeName);
        tv_inTime.setText(mHour+mMinute);
        Button moveButton = (Button) findViewById(R.id.moveButton);
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOutTime();
                //发送时间数据
                if(isCan){
                    //保存签到信息
                    saveSignData();
                    output();
                    setTime();
                }else {
                    ToastUtil.show("请稍后重试");
                }
            }
        });
    }

    private void output() {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor=db.query("History",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String activityName=cursor.getString(cursor.getColumnIndex("activityName"));
                String location=cursor.getString(cursor.getColumnIndex("location"));
                String activityDes=cursor.getString(cursor.getColumnIndex("activityDes"));
                String inTime=cursor.getString(cursor.getColumnIndex("inTime"));
                String outTime=cursor.getString(cursor.getColumnIndex("outTime"));
                System.out.println(activityName);
                System.out.println(location);
                System.out.println(activityDes);
                System.out.println(inTime);
                System.out.println(outTime);
            }while (cursor.moveToNext());
        }
    }

    /**
     * 保存数据到本地
     */
    private void saveSignData() {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("activityName",activeName);
        values.put("location",location);
        values.put("activityDes",activityDes);
        values.put("inTime",inTime);
        values.put("outTime",outTime);
        db.insert("History",null,values);
    }


    /**
     * 发送时间数据
     */
    private void setTime(){
        OkHttpUtils
                .get()
                .url(Constant.API_URL+"api/TSign/InsertSign")
                .addParams("account", SpUtil.getString("account",""))
                .addParams("activityid",activeId+"")
                .addParams("intime",inTime)
                .addParams("outtime", outTime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        ToastUtil.show("保存失败");
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        ToastUtil.show("保存成功");
                        finish();
                    }
                });
    }

    public  class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取接收到的云子id，添加到集合中
            String sensor2ID = intent.getStringExtra("sensor2ID");
            sensorList.add(sensor2ID);
            // 如果当集合中包含进入活动的云子id，就可以签离
            isCan = sensorList.contains(yunziId);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myBroadcast!=null){
            unregisterReceiver(myBroadcast);
        }

    }
}

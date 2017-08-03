package cn.hicc.information.sensorsignin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.MyApplication;
import cn.hicc.information.sensorsignin.model.SignActive;

/**
 * Created by 陈帅 on 2017/7/15/015.
 * 数据库
 */

public class MyDatabase {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "sign_activity_info";

    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private final SQLiteDatabase db;
    private static MyDatabase myDatabase;

    private MyDatabase(Context context) {
        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context, DB_NAME, null, VERSION);
        db = myDatabaseHelper.getWritableDatabase();
    }

    public synchronized static MyDatabase getInstance() {
        if (myDatabase == null) {
            myDatabase = new MyDatabase(MyApplication.getContext());
        }
        return myDatabase;
    }

    // 保存签到活动
    public void saveSignActive(SignActive signActive) {
        if (signActive != null) {
            ContentValues values = new ContentValues();
            values.put("number", signActive.getNumber());
            values.put("activeId", signActive.getActiveId());
            values.put("inTime", signActive.getInTime());
            values.put("outTime", signActive.getOutTime());
            values.put("save", signActive.getSave());
            db.insert("History",null,values);
        }
    }

    // 更新签到活动状态
    public void updateSignActive(long activeId, boolean save) {
        ContentValues values = new ContentValues();
        values.put("save", save);
        db.update("History", values, "activeId = ?", new String[]{String.valueOf(activeId)});
    }

    // 获得所有未保存成功的活动
    public List<SignActive> getUnSaveActives() {
        List<SignActive> list = new ArrayList<>();
        Cursor cursor = db.query("History", null, "save = ?", new String[]{"0"}, null, null, null);
        while (cursor.moveToNext()){
            SignActive signActive = new SignActive();
            signActive.setActiveId(cursor.getLong(cursor.getColumnIndex("activeId")));
            signActive.setNumber(cursor.getString(cursor.getColumnIndex("number")));
            signActive.setInTime(cursor.getString(cursor.getColumnIndex("inTime")));
            signActive.setOutTime(cursor.getString(cursor.getColumnIndex("outTime")));
            signActive.setSave(cursor.getInt(cursor.getColumnIndex("save")));
            list.add(signActive);
        }
        cursor.close();
        return list;
    }

    // 判断一个用户是否已经对一个活动签到了
    public boolean isSign(String number, long activeId) {
        Cursor cursor = db.query("History", new String[]{"activeId"}, "number = ?", new String[]{number}, null, null, null);
        while (cursor.moveToNext()){
            if (cursor.getLong(cursor.getColumnIndex("activeId")) == activeId) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    // 获取对一个活动最近签到的时间
    public long getRecentSignTime(String number, long activeId) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        long big = -1;
        Cursor cursor = db.query("History", new String[]{"outTime"}, "number=? and activeId=?", new String[]{number,String.valueOf(activeId)}, null, null, null);
        while (cursor.moveToNext()) {
            String outTime = cursor.getString(cursor.getColumnIndex("outTime"));
            try {
                if (big < df.parse(outTime).getTime()) {
                    big = df.parse(outTime).getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return big;
    }
}

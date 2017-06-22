package com.example.administrator.my.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/6/22.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String HISTORY_TABLE = "create table History("
            + "id integer primary key autoincrement,"
            + "userNumber text,"
            + "activityName text,"
            + "location text,"
            + "activityDes text,"
            + "rule text,"
            + "inTime text,"
            + "outTime text)";
    private Context mContext;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

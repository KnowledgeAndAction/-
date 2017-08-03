package cn.hicc.information.sensorsignin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 陈帅 on 2017/6/22.
 * 数据库创建类
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String HISTORY_TABLE = "create table History("
            + "id integer primary key autoincrement,"
            + "number text,"
            + "activeId integer,"
            + "save integer,"
            + "inTime text,"
            + "outTime text)";

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

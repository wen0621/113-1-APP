package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StdDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "menu.db";
    private static final int DATABASE_VERSION = 2; // 升級版本號
    private static final String TAG = "StdDBHelper";

    // SQL 語句: 建立新表
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS menu_items (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "price REAL NOT NULL, " +
            "quantity INTEGER NOT NULL, " +
            "order_time DATETIME DEFAULT (datetime('now', 'localtime'))" +
            ")";

    // 改表名
    private static final String RENAME_OLD_TABLE = "ALTER TABLE menu_items RENAME TO menu_items_old";

    // 遷移舊資料
    private static final String INSERT_OLD_DATA = "INSERT INTO menu_items (name, price, quantity, order_time) " +
            "SELECT name, price, quantity, order_time FROM menu_items_old";

    // 刪除舊表
    private static final String DROP_OLD_TABLE = "DROP TABLE IF EXISTS menu_items_old";

    public StdDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
            Log.d(TAG, "資料庫建立成功。");
        } catch (Exception e) {
            Log.e(TAG, "建立資料庫時發生錯誤：", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                // 1) 改名舊表
                db.execSQL(RENAME_OLD_TABLE);
                Log.d(TAG, "舊表格已重新命名為 'menu_items_old'。");

                // 2) 建新表
                db.execSQL(CREATE_TABLE);
                Log.d(TAG, "新表格 'menu_items' 建立完成。");

                // 3) 搬移舊資料
                db.execSQL(INSERT_OLD_DATA);
                Log.d(TAG, "資料已從 'menu_items_old' 遷移到 'menu_items'。");

                // 4) 刪除舊表
                db.execSQL(DROP_OLD_TABLE);
                Log.d(TAG, "舊表格 'menu_items_old' 已刪除。");
            } catch (Exception e) {
                Log.e(TAG, "升級資料庫時發生錯誤：", e);
            }
        }
    }
}

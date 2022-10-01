package com.example.listview4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    private static final String Database_name = "order.db";
    private static final int Version = 1;
    private String Create_orderSQL = "CREATE TABLE order_list( _id INTEGER, foodname TEXT PRIMARY KEY, price INTEGER, " +
            "volumn INTEGER, created_time TIMESTAMP default CURRENT_TIMESTAMP);";
    private String Drop_orderSQL = "DROP TABLE IF EXISTS order_list";
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context){
        super(context, Database_name, null, Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Create_orderSQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Drop_orderSQL);
        onCreate(db);
    }
}

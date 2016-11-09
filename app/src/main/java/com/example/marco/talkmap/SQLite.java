package com.example.marco.talkmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by marco on 16/10/30.
 */
public class SQLite extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "talkmsg.db";    //資料庫名稱
    private static final int DATABASE_VERSION = 1;    //資料庫版本

    private SQLiteDatabase db;

    //=================================即時庫存=====================================
    private static final String MSG = "msg";
    private static final String MSG_NAME = "msg_name";
    private static final String MSG_MSG = "msg_msg";
    private static final String MSG_TIME = "msg_time";


    private static final String CREATE_TABLE_MSG =
            "CREATE TABLE " + MSG + " (" +
                    MSG_NAME + " TEXT NOT NULL, " +
                    MSG_MSG + " TEXT NOT NULL, " +
                    MSG_TIME + " TEXT NOT NULL)";


    public SQLite(Context context) {    //建構子
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    synchronized public void close(SQLiteDatabase sqldb) {
        sqldb.close();
    }


    synchronized public void insert_msg(String name, String msg, String time) {

      if(time  != "")
      {
          SQLiteDatabase sqldb = getReadableDatabase();
          System.out.println("有重複資料");
          String record = "SELECT *  FROM " + MSG + " WHERE "+MSG_TIME +" = '"+time+"'";
          Cursor cursor = sqldb.rawQuery(record, null);
          System.out.println("有重複資料："+cursor);
          while (cursor.moveToNext()) {
              return;
          }
          ContentValues cv = new ContentValues();
          cv.put(MSG_NAME, name);
          cv.put(MSG_MSG, msg);
          cv.put(MSG_TIME, time);
          sqldb.insert(MSG, null, cv);
          close(sqldb);

      }



    }

    synchronized public void delete_msg() {
        SQLiteDatabase sqldb = getWritableDatabase();
        sqldb.delete(MSG, null, null);
        close(sqldb);

    }

    synchronized public ArrayList select_msg() {

        ArrayList<Obj_Msg> list = new ArrayList<Obj_Msg>();
        SQLiteDatabase sqldb = getReadableDatabase();
         String record = "SELECT *  FROM " + MSG + " ORDER BY  MSG_TIME  desc";
       // String record = "SELECT *  FROM " + MSG;

        Cursor cursor = sqldb.rawQuery(record, null);
        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getColumnCount() - 2; i += 3) {


                System.out.println("撈資料1:"+cursor.getString(i + 0));
                System.out.println("撈資料2:"+cursor.getString(i + 1));
                System.out.println("撈資料3:"+cursor.getString(i + 2));



                Obj_Msg om = new Obj_Msg();
                om.setName(cursor.getString(i + 0));
                om.setMsg(cursor.getString(i + 1));
                om.setTime(cursor.getString(i + 2));
                list.add(om);
            }
        }
        cursor.close();
        close(sqldb);
        return list;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MSG);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //oldVersion=舊的資料庫版本；newVersion=新的資料庫版本
//        db.execSQL("DROP TABLE IF EXISTS config");    //刪除舊有的資料表
//        onCreate(db);
    }
}

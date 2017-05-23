package com.korbi.todolist;

/**
 * Created by korbi on 13.03.17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskDbHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "TaskDataBase.db";
    public static final int DB_VERSION = 1;

    public static final String TASK_TABLE = "tasks";
    public static final String COL_ID = "_id";
    public static final String COL_TASK = "title";
    public static final String COL_DONE = "done";
    public TaskDbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTable = "CREATE TABLE " + TASK_TABLE + " ( " + COL_ID + " INTEGER PRIMARY KEY, " +
                COL_TASK + " TEXT, " + COL_DONE + " INTEGER NOT NULL)";

        db.execSQL(createTable);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
        onCreate(db);
    }

    public void addTask(Task task)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TASK, task.getTaskname());
        values.put(COL_DONE, task.getState());

        db.insert(TASK_TABLE, null, values);
        db.close();
    }

    public Task getTask(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TASK_TABLE, new String[]
                {COL_ID, COL_TASK, COL_DONE}, COL_ID + "=?", new String[]
                    {String.valueOf(id)}, null, null,
                    null, null);

        if (cursor != null) cursor.moveToFirst();

        Task task = new Task(cursor.getInt(1), cursor.getString(1), cursor.getInt(2));
        return task;
    }

    public List<Task> getAllTasks()
    {
        List<Task> taskList = new ArrayList<Task>();

        String selectQuery = "SELECT  * FROM " + TASK_TABLE + " ORDER BY " + COL_DONE + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Task task = new Task(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));

                taskList.add(task);
            } while (cursor.moveToNext());
        }

        return taskList;
    }

    public int updateTask(Task task)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TASK, task.getTaskname());
        values.put(COL_DONE, task.getState());

        return db.update(TASK_TABLE, values, COL_ID + " =?",
                new String[] { String.valueOf(task.getId()) });
    }

    public void deleteTask(Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TASK_TABLE, COL_ID + " = ?",
                new String[]{String.valueOf(id)});

        db.close();
    }

    public int getLatestID()
    {
        int latestID = 0;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT max(" + COL_ID + ") FROM tasks", null);
        cursor.moveToFirst();
        latestID = cursor.getInt(0);

        return latestID;
    }
}

package com.korbi.todolist.database;

/**
 * Created by korbi on 13.03.17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.korbi.todolist.logic.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;



public class TaskDbHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "TaskDataBase.db";
    private static final int DB_VERSION = 1;

    private static final String TASK_TABLE = "tasks";
    private static final String COL_ID = "_id";
    private static final String COL_TASK = "title";
    private static final String COL_DONE = "done";
    private static final String COL_DEADLINE = "deadline";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_TIME_IS_SET = "time_is_set";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public TaskDbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTable = "CREATE TABLE " + TASK_TABLE + " ( " + COL_ID + " INTEGER PRIMARY KEY, "
                + COL_TASK + " TEXT, " + COL_DEADLINE + " TEXT, " + COL_PRIORITY + " INTEGER, "
                + COL_DONE + " INTEGER NOT NULL, " + COL_TIME_IS_SET + " INTEGER NOT NULL)";

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
        values.put(COL_DEADLINE, dateFormatter.format(task.getDeadline()));
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_TIME_IS_SET, task.getTimeIsSet());

        db.insert(TASK_TABLE, null, values);
        db.close();
    }

    public List<Task> getAllTasks()
    {
        List<Task> taskList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TASK_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Task task = new Task(cursor.getInt(0), cursor.getString(1),
                        parseDate(cursor.getString(2)), cursor.getInt(3), cursor.getInt(4),
                        cursor.getInt(5));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public List<Task> getUncompletedTasks()
    {
        List<Task> taskList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TASK_TABLE + " Where " + COL_DONE + " != 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Task task = new Task(cursor.getInt(0), cursor.getString(1),
                        parseDate(cursor.getString(2)), cursor.getInt(3), cursor.getInt(4),
                        cursor.getInt(5));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public Task getTask(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TASK_TABLE, new String[]
                    {COL_ID, COL_TASK, COL_DONE}, COL_ID + "=?", new String[]
                    {String.valueOf(id)}, null, null,
                null, null);

        if (cursor != null) cursor.moveToFirst();

        Task task = new Task(cursor.getInt(0), cursor.getString(1),
                parseDate(cursor.getString(2)), cursor.getInt(3), cursor.getInt(4),
                cursor.getInt(5));
        return task;
    }

    public int updateTask(Task task)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TASK, task.getTaskname());
        values.put(COL_DONE, task.getState());
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_DEADLINE, dateFormatter.format(task.getDeadline()));
        values.put(COL_TIME_IS_SET, task.getTimeIsSet());

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
        int latestID;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT max(" + COL_ID + ") FROM tasks", null);
        cursor.moveToFirst();
        latestID = cursor.getInt(0);

        cursor.close();
        return latestID;
    }

    private Date parseDate(String datestring)
    {
        Date date = null;

        try
        {
            date = dateFormatter.parse(datestring);
            Log.d("Date", datestring + date.toString());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return date;
    }
}

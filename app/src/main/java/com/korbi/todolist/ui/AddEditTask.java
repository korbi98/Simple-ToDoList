package com.korbi.todolist.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.korbi.todolist.logic.Task;
import com.korbi.todolist.widget.ToDoListWidget;
import com.korbi.todolist.todolist.R;

public class AddEditTask extends AppCompatActivity
{
    private EditText newTaskEntry;
    private CheckBox createEventCheckBox;
    private SeekBar selectPriority;
    private TextView deadlineLabel;
    private Button resetDeadlineButton;

    SimpleDateFormat dateFormat;
    SimpleDateFormat dateTimeFormat;

    Calendar c = Calendar.getInstance();

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private int position;
    private int isDateOrTimeSet = Task.NO_DEADLINE;


    private void createCustomActionBar(String activityTitle)
    {
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.custom_action_bar, null
        );

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);

        TextView title = (TextView) findViewById(R.id.CustomActionBarTitle);
        title.setText(activityTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        createEventCheckBox = (CheckBox) findViewById(R.id.CreateEventCheckBox);
        newTaskEntry = (EditText) findViewById(R.id.NewTaskName);
        deadlineLabel = (TextView) findViewById(R.id.DeadlineTextView);
        selectPriority = (SeekBar) findViewById(R.id.SelectPriority);
        resetDeadlineButton = (Button) findViewById(R.id.resetDate);
        createEventCheckBox.setEnabled(false);
        resetDeadlineButton.setEnabled(false);



        settings = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        editor = settings.edit();

        if(settings.getInt(Settings.STANDARD_PRIORITY_SETTING, 1) == 3)
        {
            selectPriority.setProgress(settings.getInt(Settings.PREVIOUS_PRIORITY, 1));
        }
        else selectPriority.setProgress(settings.getInt(Settings.STANDARD_PRIORITY_SETTING, 1));

        createCustomActionBar(getString(R.string.add_task_activity_title));

        setCalendar(0, 0, 0, 0, 0);

        newTaskEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    save(getCurrentFocus());
                    return true;
                }

                return false;
            }
        });

        dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        prefill();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    public void onStop()
    {
        updateWidget();
        super.onStop();
    }

    public void save(View v)
    {
        editor.putInt(Settings.PREVIOUS_PRIORITY, selectPriority.getProgress());
        editor.commit();

        if(getIntent().getBooleanExtra("prefillBool", false))
        {
            saveChanges();
        }
        else saveNewTask();
    }

    private void saveNewTask()
    {

        if (newTaskEntry.getText().toString().trim().length() > 0)
        {

            int id = MainActivity.db.getLatestID() + 1;

            if (c.get(Calendar.YEAR) < 100) c.set(Calendar.YEAR, 2200);

            Task newTask = new Task(id, newTaskEntry.getText().toString(),
                    c.getTime(), selectPriority.getProgress(), 0, isDateOrTimeSet);

            Log.d("addCall", String.valueOf(c.get(Calendar.DAY_OF_YEAR)));

            MainActivity.db.addTask(newTask);
            MainActivity.taskItems.add(newTask);

            if (newTask.getTimeIsSet() != Task.NO_DEADLINE) createCalendarEvent(newTask);

            updateWidget();
            finish();
        }
        else
        {
            Toast.makeText(getApplicationContext(), R.string.empty_name_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void saveChanges()
    {
        if (newTaskEntry.getText().toString().trim().length() > 0)
        {
            if (c.get(Calendar.YEAR) < 100) c.set(Calendar.YEAR, 2200);

            MainActivity.taskItems.get(position).setTaskname(newTaskEntry.getText().toString());
            MainActivity.taskItems.get(position).setPriority(selectPriority.getProgress());
            MainActivity.taskItems.get(position).setDeadline(c.getTime());
            MainActivity.taskItems.get(position).setTimeIsSet(isDateOrTimeSet);

            MainActivity.db.updateTask(MainActivity.taskItems.get(position));

            if (MainActivity.taskItems.get(position).getTimeIsSet() != Task.NO_DEADLINE) createCalendarEvent(MainActivity.taskItems.get(position));

            Log.d("cTime", String.valueOf(c.getTime()));
            Log.d("tTime", String.valueOf(MainActivity.taskItems.get(position).getDeadline()));
            Log.d("changeDay", String.valueOf(c.get(Calendar.DAY_OF_YEAR)));

            updateWidget();
            finish();
        }
        else
        {
            Toast.makeText(getApplicationContext(), R.string.empty_name_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void cancel(View v)
    {
        finish();
    }

    public void setDate(View v)
    {
        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day)
                    {
                        Calendar calTest = Calendar.getInstance();
                        calTest.set(Calendar.YEAR, year);
                        calTest.set(Calendar.MONTH, month);
                        calTest.set(Calendar.DATE, day);

                        c.setTime(calTest.getTime()); // weird bug when setting c directly, that's why calTest is here

                        isDateOrTimeSet = Task.JUST_DATE;
                        updateDeadlineSection();
                    }

                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        datePickerDialog.show();
    }

    public void setTime(View v)
    {
        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        final TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener()
                {

                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute)
                    {
                        Calendar cal = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);

                        if (c.getTimeInMillis() - System.currentTimeMillis() < 1000)
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_time),
                                    Toast.LENGTH_LONG).show();

                            c.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
                            c.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                        }
                        isDateOrTimeSet = Task.DATE_AND_TIME;
                        updateDeadlineSection();
                    }

                }, hour, minute, false);

        timePickerDialog.show();
    }

    public void setDeadline(View v)
    {
        if (settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false)) setTime(v);
        setDate(v);
    }

    public void resetDeadline(View v)
    {
        setCalendar(0, 0, 0, 0, 0);
        isDateOrTimeSet = 0;

        updateDeadlineSection();
    }

    public void setCalendar(int taskDay, int taskMonth, int taskYear, int taskHour, int taskMinute)
    {
        c.set(Calendar.YEAR, taskYear);
        c.set(Calendar.MONTH, taskMonth);
        c.set(Calendar.DATE, taskDay);
        c.set(Calendar.HOUR_OF_DAY, taskHour);
        c.set(Calendar.MINUTE, taskMinute);
    }

    private void createCalendarEvent(Task task)
    {
        if (createEventCheckBox.isChecked())
        {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, task.getTaskname())
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Created by ToDoList");

            if (MainActivity.settings.getBoolean(INPUT_METHOD_SERVICE, false))
            {
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, task.getDeadline().getTime())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, task.getDeadline().getTime()
                            + 3600*1000); //To add one hour in milliseconds
            }

            startActivity(intent);
        }
    }

    private void updateDeadlineSection()
    {
        if (isDateOrTimeSet == Task.DATE_AND_TIME && settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false))
        {
            createEventCheckBox.setEnabled(true);
            resetDeadlineButton.setEnabled(true);
            deadlineLabel.setText(dateTimeFormat.format(c.getTime()));
        }
        else if (isDateOrTimeSet != Task.NO_DEADLINE)
        {
            createEventCheckBox.setEnabled(true);
            resetDeadlineButton.setEnabled(true);
            deadlineLabel.setText(dateFormat.format(c.getTime()));
        }
        else
        {
            createEventCheckBox.setEnabled(false);
            resetDeadlineButton.setEnabled(false);
            deadlineLabel.setText(null);
        }
    }

    private void prefill()
    {
        if (getIntent().getBooleanExtra("prefillBool", false))
        {
            position = getIntent().getIntExtra("position", 0);

            newTaskEntry.setText(MainActivity.taskItems.get(position).getTaskname());
            selectPriority.setProgress(MainActivity.taskItems.get(position).getPriority());
            c.setTime(MainActivity.taskItems.get(position).getDeadline());
            Log.d("prefill", String.valueOf(c.get(Calendar.DAY_OF_YEAR)));
            isDateOrTimeSet = MainActivity.taskItems.get(position).getTimeIsSet();
            updateDeadlineSection();
        }
    }

    public void updateWidget()
    {
        Intent intent = new Intent(this, ToDoListWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ToDoListWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
    }
}




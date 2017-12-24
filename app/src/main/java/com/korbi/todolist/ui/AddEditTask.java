package com.korbi.todolist.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.korbi.todolist.database.TaskDbHelper;
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
    final int REQUEST_CODE = 1;
    private String currentCategory;

    private TaskDbHelper db;


    private void createCustomActionBar(String activityTitle)
    {
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.custom_action_bar, null
        );

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);

        TextView title = findViewById(R.id.CustomActionBarTitle);
        title.setText(activityTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        Bundle bundle = getIntent().getExtras();

        createEventCheckBox = findViewById(R.id.CreateEventCheckBox);
        newTaskEntry = findViewById(R.id.NewTaskName);
        deadlineLabel = findViewById(R.id.DeadlineTextView);
        selectPriority = findViewById(R.id.SelectPriority);
        resetDeadlineButton = findViewById(R.id.resetDate);
        createEventCheckBox.setEnabled(false);
        resetDeadlineButton.setEnabled(false);

        db = new TaskDbHelper(this);
        currentCategory = bundle.getString(Settings.CURRENT_CATEGORY, db.getTaskCategory(1));
        if (currentCategory == null) currentCategory = db.getTaskCategory(1);

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

                if (actionId == EditorInfo.IME_ACTION_DONE) // saves when enter is pressed
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

    private void saveNewTask() // saves the new task
    {

        if (newTaskEntry.getText().toString().trim().length() > 0)
        {

            int id = db.getLatestID() + 1;

            if (c.get(Calendar.YEAR) < 100) c.set(Calendar.YEAR, 2200);

            Task newTask = new Task(id, newTaskEntry.getText().toString(),
                    c.getTime(), selectPriority.getProgress(), 0, isDateOrTimeSet, currentCategory);

            db.addTask(newTask);

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

    private void saveChanges() // updates the task that was edited
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

    private void updateDeadlineSection() // toggles whether the reset button is enabled or not
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

    private void prefill() //when you edit a task, this method sets the priority bar, editTextView etc. to the values of the task to edit
    {
        if (getIntent().getBooleanExtra("prefillBool", false))
        {
            position = getIntent().getIntExtra("position", 0);

            newTaskEntry.setText(MainActivity.taskItems.get(position).getTaskname());
            selectPriority.setProgress(MainActivity.taskItems.get(position).getPriority());
            c.setTime(MainActivity.taskItems.get(position).getDeadline());
            isDateOrTimeSet = MainActivity.taskItems.get(position).getTimeIsSet();
            updateDeadlineSection();
        }
    }

    public void speechToTask(View v)
    {
        String DIALOG_TEXT = getString(R.string.speech_to_task_text);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_to_task_text));

        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "error",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        String resultSpeech;
        super.onActivityResult(requestCode, resultCode, intent);
        ArrayList<String> speech;
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE)
        {
            speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            resultSpeech = speech.get(0);

            if(resultSpeech.contains(getString(R.string.speech_command_important)))
            {
                String regex = "\\s*\\bimportant\\b\\s*";
                selectPriority.setProgress(Task.PRIORITY_HIGH);
                resultSpeech = resultSpeech.replaceFirst(regex, "");
                regex = "\\s*\\bwichtig\\b\\s*";
                resultSpeech = resultSpeech.replaceFirst(regex, "");
            }
            newTaskEntry.setText(resultSpeech);
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




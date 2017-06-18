package com.korbi.todolist.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.korbi.todolist.todolist.R;
import com.korbi.todolist.widget.ToDoListWidget;


public class Settings extends AppCompatActivity
{
    public static final String INCLUDE_TIME_SETTING = "createEventSetting";
    public static final String SET_DIVIDERS = "dividers";
    public static final String STANDARD_PRIORITY_SETTING = "standartPriority";
    public static final String REMAINING_TIME_INSTEAD_OF_DATE = "dateortime";
    public static final String PREVIOUS_PRIORITY = "previouspriority";
    public static final String DEADLINE_FIRST = "deadlinefirst";

    private CheckBox includeTimeCheckBox;
    private CheckBox setDividers;
    private CheckBox dateOrTime;
    private CheckBox deadlineFirst;
    private Spinner defaultPriority;

    private SharedPreferences settings = MainActivity.settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editor = settings.edit();

        includeTimeCheckBox = (CheckBox) findViewById(R.id.IncludeTimeInDeadline);
        setDividers = (CheckBox) findViewById(R.id.setDividersCheckbox);
        dateOrTime = (CheckBox) findViewById(R.id.dateOrTimeCheckbox);
        deadlineFirst = (CheckBox) findViewById(R.id.deadline_before_priority);
        defaultPriority = (Spinner) findViewById(R.id.defaultPrioritySpinner);

        includeTimeCheckBox.setChecked(settings.getBoolean(INCLUDE_TIME_SETTING, false));
        setDividers.setChecked(settings.getBoolean(SET_DIVIDERS, false));
        dateOrTime.setChecked(settings.getBoolean(REMAINING_TIME_INSTEAD_OF_DATE, false));
        deadlineFirst.setChecked(settings.getBoolean(DEADLINE_FIRST, false));
        defaultPriority.setSelection(settings.getInt(STANDARD_PRIORITY_SETTING, 0));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop()
    {
        save();
        super.onStop();
    }

    @Override
    public void onPause()
    {
        save();
        super.onPause();
    }

    public void save()
    {
        editor.putBoolean(INCLUDE_TIME_SETTING, includeTimeCheckBox.isChecked());
        editor.putBoolean(SET_DIVIDERS, setDividers.isChecked());
        editor.putBoolean(REMAINING_TIME_INSTEAD_OF_DATE, dateOrTime.isChecked());
        editor.putBoolean(DEADLINE_FIRST, deadlineFirst.isChecked());
        editor.putInt(STANDARD_PRIORITY_SETTING, defaultPriority.getSelectedItemPosition());
        editor.commit();
        MainActivity.adapter.notifyDataSetChanged();
        updateWidget();
        finish();
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

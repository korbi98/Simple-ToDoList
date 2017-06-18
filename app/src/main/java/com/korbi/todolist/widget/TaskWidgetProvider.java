package com.korbi.todolist.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.korbi.todolist.logic.Task;
import com.korbi.todolist.database.TaskDbHelper;
import com.korbi.todolist.ui.Settings;
import com.korbi.todolist.todolist.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskWidgetProvider implements RemoteViewsService.RemoteViewsFactory
{
    private static TaskDbHelper db;
    private List<Task> tasks;
    private Context context;
    SharedPreferences settings;

    private SimpleDateFormat yearOnly = new SimpleDateFormat(("yyyy"));
    private SimpleDateFormat showFullDate = new SimpleDateFormat(("dd.MM.yy"));
    private SimpleDateFormat showTime = new SimpleDateFormat(("HH:mm"));

    public TaskWidgetProvider(Context context, Intent intent)
    {
        this.context = context;
        settings = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

    }


    @Override
    public void onCreate()
    {
        db = new TaskDbHelper(context);
        tasks = db.getUncompletedTasks();
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount()
    { return tasks.size();}

    @Override
    public RemoteViews getViewAt(int position)
    {

        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widgetlist_items);

        createView(row, position);

        row.setOnClickFillInIntent(R.id.widget_list_row, new Intent());

        return row;
    }

    public void createView(RemoteViews row, int position)
    {
        sort();
        Task task = tasks.get(position);

        row.setTextViewText(R.id.widget_list_task_view, tasks.get(position).getTaskname());
        row.setTextColor(R.id.widget_list_deadline, ContextCompat.getColor(context, R.color.colorPrimary));
        row.setTextColor(R.id.widget_list_task_view, ContextCompat.getColor(context, R.color.colorPrimary));
        row.setInt(R.id.widget_list_row, "setBackgroundResource", R.color.widgetItemBackground);
        setDeadlineRemainingTime(row, position);

        switch (task.getPriority())
        {
            case Task.PRIORITY_LOW:
                row.setInt(R.id.widgetPriorityIndicator, "setBackgroundResource", R.color.colorPriorityLow);
                break;

            case Task.PRIORITY_NORMAL:
                row.setInt(R.id.widgetPriorityIndicator, "setBackgroundResource", R.color.colorPriorityNormal);
                break;
            case Task.PRIORITY_HIGH:
                row.setInt(R.id.widgetPriorityIndicator, "setBackgroundResource", R.color.colorPriorityHigh);
                row.setTextViewText(R.id.widget_list_task_view, Html.fromHtml("<b>" + tasks.get(position).getTaskname() + "</b>"));
                break;
        }
    }

    @Override
    public RemoteViews getLoadingView()
    { return(null); }

    @Override
    public int getViewTypeCount()
    { return(1); }

    @Override
    public long getItemId(int position)
    { return(position); }

    @Override
    public boolean hasStableIds()
    { return(true); }

    @Override
    public void onDataSetChanged()
    {
        tasks = db.getUncompletedTasks();
        sort();
    }

    public void sort()
    {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2)
            {

                int sortState = t1.getState() - t2.getState();      // highest sorting rule

                if (sortState != 0)
                {
                    return sortState;
                }

                if (settings.getBoolean(Settings.DEADLINE_FIRST, false))
                {
                    if (t1.getDeadline().compareTo(t2.getDeadline()) != 0)
                    {
                        return t1.getDeadline().compareTo(t2.getDeadline());
                    }

                    int sortPriority = t2.getPriority() - t1.getPriority();

                    if (sortPriority != 0)
                    {
                        return sortPriority;
                    }
                }
                else
                {
                    int sortPriority = t2.getPriority() - t1.getPriority();

                    if (sortPriority != 0) {
                        return sortPriority;
                    }
                    if (t1.getDeadline().compareTo(t2.getDeadline()) != 0) {
                        return t1.getDeadline().compareTo(t2.getDeadline());
                    }
                }
                return t1.getId() - t2.getId(); // least important sorting rule
            }
        });
    }

    private void setDeadlineRemainingTime(RemoteViews row, int position)
    {
        Task task = tasks.get(position);
        Calendar taskCal = Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();
        taskCal.setTime(task.getDeadline());
        currentCal.setTimeInMillis(System.currentTimeMillis());

        boolean timeIsSet = task.getTimeIsSet() == Task.DATE_AND_TIME;
        boolean deadlineIsSet = task.getTimeIsSet() != Task.NO_DEADLINE;
        boolean includeTime = settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false) && timeIsSet;
        boolean showRemainingTime = settings.getBoolean(Settings.REMAINING_TIME_INSTEAD_OF_DATE, false);
        String deadlineHour = showTime.format(task.getDeadline());
        String deadlineDate = showFullDate.format(task.getDeadline());
        int remainingDays = (taskCal.get(Calendar.YEAR) - currentCal.get(Calendar.YEAR)) * 365 +
                taskCal.get(Calendar.DAY_OF_YEAR) - currentCal.get(Calendar.DAY_OF_YEAR);
        int remainingTime = taskCal.get(Calendar.HOUR_OF_DAY) - currentCal.get(Calendar.HOUR_OF_DAY);

        String deadlineStr = "";

        Log.d("isDedlineSet", String.valueOf(task.getTimeIsSet()));

        if (deadlineIsSet && !showRemainingTime)
        {
            if (includeTime)
            {
                if (remainingDays == 0) deadlineStr = deadlineHour;
                else if (remainingDays == 1)
                    deadlineStr = context.getString(R.string.tomorow);
                else deadlineStr = deadlineDate;
            }
            else
            {
                if (remainingDays == 0) deadlineStr = context.getString(R.string.today);
                else if (remainingDays == 1) deadlineStr = context.getString(R.string.tomorow);
                else deadlineStr = deadlineDate;
            }
        }
        else if (deadlineIsSet)
        {
            if (includeTime && remainingDays == 0)
            {
                deadlineStr = String.valueOf(remainingTime) + context.getString(R.string.hours_till_deadline);
            }
            else
            {
                if (remainingDays == 0) deadlineStr = context.getString(R.string.today);
                else if (remainingDays == 1) deadlineStr = context.getString(R.string.tomorow);
                else
                {
                    deadlineStr = String.valueOf(remainingDays) +
                            context.getString(R.string.days_till_deadline);
                }
            }
        }
        row.setTextViewText(R.id.widget_list_deadline, deadlineStr);

        if (includeTime && remainingTime < 0 && remainingDays == 0)
            row.setTextColor(R.id.widget_list_deadline, Color.RED);
        else if (remainingDays < 0) row.setTextColor(R.id.widget_list_deadline, Color.RED);
    }
}

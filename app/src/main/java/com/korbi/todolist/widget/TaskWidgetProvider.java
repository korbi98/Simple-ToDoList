package com.korbi.todolist.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.korbi.todolist.logic.Task;
import com.korbi.todolist.database.TaskDbHelper;
import com.korbi.todolist.ui.Settings;
import com.korbi.todolist.todolist.R;

import java.text.SimpleDateFormat;
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

    public void sort() {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {

                int sortState = t1.getState() - t2.getState();      // highest sorting rule

                if (sortState != 0) {
                    return sortState;
                }

                int sortPriority = t2.getPriority() - t1.getPriority();

                if (sortPriority != 0) {
                    return sortPriority;
                }

                if (t1.getDeadline().compareTo(t2.getDeadline()) != 0) {
                    return t1.getDeadline().compareTo(t2.getDeadline());
                }

                return t1.getId() - t2.getId(); // least important sorting rule
            }
        });
    }

    private void setDeadlineRemainingTime(RemoteViews row, int position)
    {
        Task task = tasks.get(position);
        int taskYear = Integer.parseInt(yearOnly.format(task.getDeadline()));
        boolean includeTime = settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false);

        if (taskYear < 2900)
        {
            if (settings.getBoolean(Settings.DATE_INSTEAD_OF_REMAINING_TIME, false))
            {
                String deadlineStr;

                if (includeTime)
                {
                    if (showFullDate.format(task.getDeadline()) == showFullDate.format(System.currentTimeMillis()))
                    {
                        deadlineStr = showTime.format(task.getDeadline()); // if current date == deadline date show only time
                    }
                    else deadlineStr = showFullDate.format(task.getDeadline()); //else show date as for time there is not enough space in the widget
                }
                else
                {
                    if (showFullDate.format(task.getDeadline()) == showFullDate.format(System.currentTimeMillis()))
                    {
                        deadlineStr = context.getString(R.string.today); //if current date == deadline date show "today"
                    }
                    else deadlineStr = showFullDate.format(task.getDeadline()); //else show date
                }
                row.setTextViewText(R.id.widget_list_deadline, deadlineStr);

                if (task.getDeadline().getTime() - System.currentTimeMillis() < 0) //if time difference is negative make string red
                {
                    row.setTextColor(R.id.widget_list_deadline, Color.RED);
                }
            }
            else {

                long deadline = task.getDeadline().getTime() / (24 * 3600 * 1000)
                        - System.currentTimeMillis() / (24 * 3600 * 1000);

                String remainingTimeStr = context.getString(R.string.daysTillDeadline);

                if (deadline <= 1 && deadline > -1 &&
                        settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false) == true) {
                    remainingTimeStr = context.getString(R.string.hoursTillDeadline);
                    deadline = task.getDeadline().getTime() / (3600 * 1000)
                            - System.currentTimeMillis() / (3600 * 1000);

                    row.setTextViewText(R.id.widget_list_deadline, String.valueOf(deadline) + remainingTimeStr);
                    if (deadline < 0) {
                        row.setTextColor(R.id.widget_list_deadline, Color.RED);
                    }
                } else if (deadline < 0) {
                    row.setTextColor(R.id.widget_list_deadline, Color.RED);
                }

                row.setTextViewText(R.id.widget_list_deadline, String.valueOf(deadline) + remainingTimeStr);
            }

        } else row.setTextViewText(R.id.widget_list_deadline, "");
    }
}

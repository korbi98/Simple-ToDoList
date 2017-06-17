package com.korbi.todolist.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.korbi.todolist.ui.AddEditTask;
import com.korbi.todolist.ui.MainActivity;
import com.korbi.todolist.todolist.R;

/**
 * Implementation of App Widget functionality.
 */
public class ToDoListWidget extends AppWidgetProvider
{
    public static final String TRIGGER_UPDATE = "updatetaskwidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {

        Intent serviceIntent = new Intent(context, WidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //List<Task> tasks = MainActivity.taskItems;

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);

        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.to_do_list_widget);
        widget.setRemoteAdapter(R.id.widget_list, serviceIntent);

        Intent addTask = new Intent(context, AddEditTask.class);
        Intent openMainActivity = new Intent(context, MainActivity.class);

        PendingIntent pendingAddTaskIntent = PendingIntent.getActivity(context, 0, addTask, 0);
        widget.setOnClickPendingIntent(R.id.widgetAddTask, pendingAddTaskIntent);

        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, 0, openMainActivity, 0);
        widget.setOnClickPendingIntent(R.id.todoListWidget, pendingMainActivityIntent);

        widget.setPendingIntentTemplate(R.id.widget_list, pendingMainActivityIntent);

        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        Log.d("broadcast", String.valueOf(intent.getAction()));

        if (TRIGGER_UPDATE.equals(intent.getAction()))
        {
            onUpdate(context, appWidgetManager, intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}


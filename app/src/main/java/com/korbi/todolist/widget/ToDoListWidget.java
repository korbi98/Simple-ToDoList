package com.korbi.todolist.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.korbi.todolist.ui.AddEditTask;
import com.korbi.todolist.ui.ChooseWidgetCategory;
import com.korbi.todolist.ui.MainActivity;
import com.korbi.todolist.todolist.R;
import com.korbi.todolist.ui.Settings;

/**
 * Implementation of App Widget functionality.
 */
public class ToDoListWidget extends AppWidgetProvider
{
    public static final String APP_ID = "appid";
    private static SharedPreferences settings;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        settings = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        Intent serviceIntent = new Intent(context, WidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);

        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.to_do_list_widget);
        widget.setRemoteAdapter(R.id.widget_list, serviceIntent);
        widget.setTextViewText(R.id.widget_title, settings.getString(Settings.WIDGET_CATEGORY+String.valueOf(appWidgetId), "ToDoList"));

        Intent addTask = new Intent(context, AddEditTask.class);
        Intent openMainActivity = new Intent(context, MainActivity.class);
        Intent chooseCategory = new Intent(context, ChooseWidgetCategory.class);
        addTask.putExtra(Settings.CURRENT_CATEGORY, settings.getString(Settings.CURRENT_CATEGORY, "fail"));
        chooseCategory.putExtra(APP_ID, appWidgetId);

        PendingIntent pendingAddTaskIntent = PendingIntent.getActivity(context, appWidgetId, addTask, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widgetAddTask, pendingAddTaskIntent);

        PendingIntent pendingChooseCategory = PendingIntent.getActivity(context,appWidgetId,chooseCategory, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widget_title, pendingChooseCategory);

        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, 0, openMainActivity, 0);
        widget.setOnClickPendingIntent(R.id.todoListWidget, pendingMainActivityIntent);

        widget.setPendingIntentTemplate(R.id.widget_list, pendingMainActivityIntent);

        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
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


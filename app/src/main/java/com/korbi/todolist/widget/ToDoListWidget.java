/*
 * Copyright 2019 Korbinian Moser
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.korbi.todolist.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.korbi.todolist.todolist.R;
import com.korbi.todolist.ui.AddEditTask;
import com.korbi.todolist.ui.ChooseWidgetCategory;
import com.korbi.todolist.ui.MainActivity;
import com.korbi.todolist.ui.SettingsActivity;

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
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        String title = settings.getString(SettingsActivity.WIDGET_CATEGORY + String.valueOf(appWidgetId), "ToDoList");

        Intent serviceIntent = new Intent(context, WidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);

        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.to_do_list_widget);
        widget.setRemoteAdapter(R.id.widget_list, serviceIntent);
        widget.setTextViewText(R.id.widget_title, title);

        Intent addTask = new Intent(context, AddEditTask.class);
        Intent openMainActivity = new Intent(context, MainActivity.class);
        Intent chooseCategory = new Intent(context, ChooseWidgetCategory.class);
        addTask.putExtra(SettingsActivity.CURRENT_CATEGORY, title);
        openMainActivity.putExtra(SettingsActivity.CURRENT_CATEGORY, title);
        chooseCategory.putExtra(APP_ID, appWidgetId);

        PendingIntent pendingAddTaskIntent = PendingIntent.getActivity(context, appWidgetId, addTask, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widgetAddTask, pendingAddTaskIntent);

        PendingIntent pendingChooseCategory = PendingIntent.getActivity(context,appWidgetId,chooseCategory, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widget_title, pendingChooseCategory);

        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, appWidgetId, openMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
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


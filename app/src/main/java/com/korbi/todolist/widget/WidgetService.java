package com.korbi.todolist.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by korbi on 6/15/17.
 */

public class WidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return(new TaskWidgetProvider(this.getApplicationContext(), intent));
    }
}

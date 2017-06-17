package com.korbi.todolist.ui;

import android.support.v7.widget.DefaultItemAnimator;

/**
 * Created by dime on 05/11/14.
 */
public class ToDoListAnimator extends DefaultItemAnimator {
    @Override
    public long getRemoveDuration() {
        return 400;
    }

    @Override
    public long getChangeDuration() {
        return 400;
    }

    @Override
    public long getAddDuration() {
        return 500;
    }
}

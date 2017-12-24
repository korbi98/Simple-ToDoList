package com.korbi.todolist.ui;

import android.support.v7.widget.DefaultItemAnimator;


//sets the duration of the add and remove animations of the recyclerView
public class ToDoListAnimator extends DefaultItemAnimator {
    @Override
    public long getRemoveDuration() {
        return 350;
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

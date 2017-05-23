package com.korbi.todolist;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.korbi.todolist.todolist.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by korbi on 12.03.17.
 */

public class ToDoListAdapter extends BaseAdapter
{
    static class ViewHolder
    {
        TextView task_name;
        CheckBox done_box;
    }

    private final List<Task> tasks;
    private final LayoutInflater inflator;
    private Task task;

    public ToDoListAdapter(List<Task> tasks, Context context)
    {
        this.tasks = tasks;
        inflator = LayoutInflater.from(context);
    }

    public int getCount()
    {
        return tasks.size();
    }

    public Object getItem(int position)
    {
        return tasks.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void clear() //removes all completed tasks
    {
        for(int i = 0; i < tasks.size(); i++)
        {
            Log.d("Tasks.size()", String.valueOf(tasks.size()));
            Log.d("i = ", String.valueOf(i));
            if (tasks.get(i).getState() == 1)
            {
                MainActivity.db.deleteTask(tasks.get(i).getId());
                tasks.remove(tasks.get(i));
                i--;

            }
        }
    }

    public void sort()
    {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getState() - t2.getState();
            }
        });
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        final ToDoListAdapter.ViewHolder holder;

        if (convertView == null)
        {
            convertView = inflator.inflate(R.layout.list_layout, parent, false);
            holder = new ToDoListAdapter.ViewHolder();
            holder.task_name = (TextView) convertView.findViewById(R.id.list_task_view);
            holder.done_box = (CheckBox) convertView.findViewById(R.id.list_checkbox);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ToDoListAdapter.ViewHolder) convertView.getTag();
        }

        task = tasks.get(position);

        holder.done_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                tasks.get(position).setState(isChecked ? 1:0);

                if(isChecked)
                {
                    holder.task_name.setPaintFlags(holder.task_name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else
                {
                    holder.task_name.setPaintFlags(0);
                }

                sort();

                MainActivity.db.updateTask(tasks.get(position));
                MainActivity.adapter.notifyDataSetChanged();
            }
        });

        holder.task_name.setText(task.getTaskname());
        holder.done_box.setChecked(task.getState() != 0);

        return convertView;
    }

}



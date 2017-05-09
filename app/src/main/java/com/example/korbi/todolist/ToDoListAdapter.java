package com.example.korbi.todolist;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

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

        task = (Task) getItem(position);

        holder.done_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                tasks.get(position).setState(isChecked);
                MainActivity.db.updateTask(tasks.get(position));

                if(isChecked)
                {
                    holder.task_name.setPaintFlags(holder.task_name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else
                {
                    holder.task_name.setPaintFlags(0);
                }
            }
        });

        holder.task_name.setText(task.getTaskname());
        holder.done_box.setChecked(task.getState());

        return convertView;
    }

}



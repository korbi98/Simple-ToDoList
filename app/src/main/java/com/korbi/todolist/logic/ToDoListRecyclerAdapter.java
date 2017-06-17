package com.korbi.todolist.logic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.os.Handler;

import com.korbi.todolist.ui.AddEditTask;
import com.korbi.todolist.ui.MainActivity;
import com.korbi.todolist.ui.Settings;
import com.korbi.todolist.todolist.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by korbi on 6/9/17.
 */

public class ToDoListRecyclerAdapter extends RecyclerView.Adapter<ToDoListRecyclerAdapter.TaskViewHolder>
{

    private final List<Task> tasks;
    private Task task;

    private Context context;

    private SharedPreferences settings = MainActivity.settings;
    private SimpleDateFormat yearOnly = new SimpleDateFormat(("yyyy"));
    private SimpleDateFormat showFullDate = new SimpleDateFormat(("dd.MM.yy"));
    private SimpleDateFormat showTime = new SimpleDateFormat(("HH:mm"));


    public class TaskViewHolder extends RecyclerView.ViewHolder
    {
        TextView task_name, task_deadline;
        CheckBox isDone;
        View divider;
        View priorityIndicator;

        private TaskViewHolder(View view)
        {
            super(view);
            task_name = (TextView) itemView.findViewById(R.id.list_task_view);
            task_deadline = (TextView) itemView.findViewById(R.id.list_deadline);
            isDone = (CheckBox) itemView.findViewById(R.id.list_checkbox);
            divider = itemView.findViewById(R.id.listDivider);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent i = new Intent(context, AddEditTask.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("position", getAdapterPosition());
                    i.putExtra("prefillBool", true);
                    context.startActivity(i);

                    return false;
                }
            });
        }
    }

    public ToDoListRecyclerAdapter(List tasks, Context context)
    {
        this.tasks = tasks;
        this.context = context;
    }


    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder h, int position)
    {
        task = tasks.get(position);
        int taskYear = Integer.parseInt(yearOnly.format(task.getDeadline()));

        h.task_name.setText(task.getTaskname());

        if((task.getState() != 0))
        {
            h.task_name.setPaintFlags(h.task_name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.task_name.setAlpha(0.5f);
            h.task_deadline.setAlpha(0f);
            h.isDone.setAlpha(0.5f);
        }
        else
        {
            h.task_name.setPaintFlags(0);
            h.task_name.setAlpha(1f);
            h.task_deadline.setAlpha(1f);
            h.isDone.setAlpha(1f);
        }

        h.isDone.setOnCheckedChangeListener(null);
        h.isDone.setChecked(task.getState() != 0);

        h.isDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                tasks.get(h.getAdapterPosition()).setState(isChecked ? 1:0);

                MainActivity.db.updateTask(tasks.get(h.getAdapterPosition()));
                notifyItemRangeChanged(0, tasks.size());
                sort();


            }
        });



        setDeadlineRemainingTime(h, taskYear);
        setItemColor(h);
        setDividers(h);

    }

    @Override
    public int getItemCount()
    {
        return tasks.size();
    }

    private void setDeadlineRemainingTime(ToDoListRecyclerAdapter.TaskViewHolder holder, int taskYear)
    {
        boolean includeTime = settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false);

        if (taskYear < 2900)
        {

            if (settings.getBoolean(Settings.DATE_INSTEAD_OF_REMAINING_TIME, false))
            {
                String deadlineStr;

                if (includeTime)
                {
                    if (showFullDate.format(task.getDeadline()).equals(showFullDate.format(System.currentTimeMillis())))
                    {
                        deadlineStr = showTime.format(task.getDeadline()); // if current date == deadline date show only time
                    }
                    else deadlineStr = showFullDate.format(task.getDeadline()) + "\n" + showTime.format(task.getDeadline()); //else show date + time
                }
                else
                {
                    if (showFullDate.format(task.getDeadline()).equals(showFullDate.format(System.currentTimeMillis())))
                    {
                        deadlineStr = context.getString(R.string.today); //if current date == deadline date show "today"
                    }
                    else deadlineStr = showFullDate.format(task.getDeadline()); //else show date
                }
                holder.task_deadline.setText(deadlineStr);

                if (task.getDeadline().getTime() - System.currentTimeMillis() < 0) //if time difference is negative make string red
                {
                    holder.task_deadline.setTextColor(Color.RED);
                }
            }
            else
            {
                long deadline = task.getDeadline().getTime() / (24 * 3600 * 1000)
                        - System.currentTimeMillis() / (24 * 3600 * 1000);

                String remainingTimeStr = context.getString(R.string.daysTillDeadline);

                if (deadline <= 1 && deadline > -1 && includeTime)
                {
                    remainingTimeStr = context.getString(R.string.hoursTillDeadline);
                    deadline = task.getDeadline().getTime() / (3600 * 1000)
                            - System.currentTimeMillis() / (3600 * 1000);

                    holder.task_deadline.setText(String.valueOf(deadline) + remainingTimeStr);
                    if (deadline < 0) {
                        holder.task_deadline.setTextColor(Color.RED);
                    }
                } else if (deadline < 0) {
                    holder.task_deadline.setTextColor(Color.RED);
                }
                holder.task_deadline.setText(String.valueOf(deadline) + remainingTimeStr);
            }

        } else holder.task_deadline.setText("");
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

                int sortPriority = t2.getPriority() - t1.getPriority();

                if (sortPriority != 0)
                {
                    return sortPriority;
                }

                if (t1.getDeadline().compareTo(t2.getDeadline()) != 0)
                {
                    return t1.getDeadline().compareTo(t2.getDeadline());
                }

                return t1.getId() - t2.getId(); // least important sorting rule
            }
        });
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

    private void setItemColor(ToDoListRecyclerAdapter.TaskViewHolder holder)
    {
        if (task.getState() == 0) {
            switch (task.getPriority()) {
                case Task.PRIORITY_LOW:
                    holder.priorityIndicator.setBackgroundResource(R.color.colorPriorityLow);
                    //holder.itemView.setBackgroundResource(R.color.colorPriorityLow);
                    holder.task_name.setTypeface(null, Typeface.NORMAL);
                    break;

                case Task.PRIORITY_NORMAL:
                    holder.priorityIndicator.setBackgroundResource(R.color.colorPriorityNormal);
                    //holder.itemView.setBackgroundResource(R.color.colorPriorityNormal);
                    holder.task_name.setTypeface(null, Typeface.NORMAL);
                    break;

                case Task.PRIORITY_HIGH:
                    holder.priorityIndicator.setBackgroundResource(R.color.colorPriorityHigh);
                    //holder.itemView.setBackgroundResource(R.color.colorPriorityHigh);
                    holder.task_name.setTypeface(null, Typeface.BOLD);
            }
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            holder.itemView.setBackgroundResource(typedValue.resourceId);
        }
        else
        {
            holder.priorityIndicator.setBackgroundResource(R.color.colorPriorityLabelDone);
            holder.itemView.setBackgroundResource(R.color.colorCompletedTask);
        }
    }

    private void setDividers(ToDoListRecyclerAdapter.TaskViewHolder holder)
    {
        if(MainActivity.settings.getBoolean(Settings.SET_DIVIDERS, false))
        {
            holder.divider.setVisibility(View.VISIBLE);
        }
        else holder.divider.setVisibility(View.GONE);
    }

}



package com.korbi.todolist.logic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.korbi.todolist.todolist.R;
import com.korbi.todolist.ui.AddEditTask;
import com.korbi.todolist.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private void setDeadlineRemainingTime(ToDoListRecyclerAdapter.TaskViewHolder holder, int taskYear) //This method sets the text of the textview, that shows the deadline of the task
    {
        Calendar taskCal = Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();
        taskCal.setTime(task.getDeadline());
        currentCal.setTimeInMillis(System.currentTimeMillis());

        boolean timeIsSet = task.getTimeIsSet() == Task.DATE_AND_TIME;
        boolean deadlineIsSet = task.getTimeIsSet() != Task.NO_DEADLINE;
        boolean includeTime = settings.getBoolean(context.getString(R.string.settings_include_time_in_deadline_key), false) && timeIsSet;
        boolean showRemainingTime = settings.getBoolean(context.getString(R.string.settings_date_or_remaining_time_key), false);
        String deadlineHour = showTime.format(task.getDeadline());
        String deadlineDate = showFullDate.format(task.getDeadline());
        int remainingDays = (taskCal.get(Calendar.YEAR) - currentCal.get(Calendar.YEAR)) * 365 +
                taskCal.get(Calendar.DAY_OF_YEAR) - currentCal.get(Calendar.DAY_OF_YEAR);
        int remainingTime = taskCal.get(Calendar.HOUR_OF_DAY) - currentCal.get(Calendar.HOUR_OF_DAY);

        String deadlineStr = "";

        if (deadlineIsSet && !showRemainingTime) // String depends whether you choose to show the date or the remaining time in the settings
        {
            if (includeTime)
            {
                if (remainingDays == 0) deadlineStr = deadlineHour;
                else if (remainingDays == 1)
                    deadlineStr = context.getString(R.string.tomorow) + "\n"
                            + deadlineHour;
                else deadlineStr = deadlineDate + "\n" + deadlineHour;
            }
            else
            {
                if (remainingDays == 0) deadlineStr = context.getString(R.string.today);
                else if (remainingDays == 1) deadlineStr = context.getString(R.string.tomorow);
                else deadlineStr = deadlineDate;
            }
        }
        else if (deadlineIsSet)
        {
            if (includeTime && remainingDays == 0)
            {
                deadlineStr = String.valueOf(remainingTime) + context.getString(R.string.hours_till_deadline);
            }
            else
            {
                if (remainingDays == 0) deadlineStr = context.getString(R.string.today);
                else if (remainingDays == 1) deadlineStr = context.getString(R.string.tomorow);
                else
                {
                    deadlineStr = String.valueOf(remainingDays) +
                            context.getString(R.string.days_till_deadline);
                }
            }
        }
        holder.task_deadline.setText(deadlineStr);

        if (includeTime && remainingTime < 0 && remainingDays == 0)
            holder.task_deadline.setTextColor(Color.RED);
        else if (remainingDays < 0) holder.task_deadline.setTextColor(Color.RED);
    }

    public void sort() //Sorts the tasks in the right order (Priority first and then deadline)
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

                if (settings.getBoolean(context.getString(R.string.settings_deadline_before_priority_key), false)) // deadline first and then priority
                {
                    if (t1.getDeadline().compareTo(t2.getDeadline()) != 0)
                    {
                        return t1.getDeadline().compareTo(t2.getDeadline());
                    }

                    int sortPriority = t2.getPriority() - t1.getPriority();

                    if (sortPriority != 0)
                    {
                        return sortPriority;
                    }
                }
                else
                {
                    int sortPriority = t2.getPriority() - t1.getPriority();

                    if (sortPriority != 0) {
                        return sortPriority;
                    }
                    if (t1.getDeadline().compareTo(t2.getDeadline()) != 0) {
                        return t1.getDeadline().compareTo(t2.getDeadline());
                    }
                }
                return t1.getId() - t2.getId(); // least important sorting rule
            }
        });
    }

    public void clear() //removes all completed tasks
    {
        for(int i = 0; i < tasks.size(); i++)
        {
            if (tasks.get(i).getState() == 1)
            {
                MainActivity.db.deleteTask(tasks.get(i).getId());
                tasks.remove(tasks.get(i));
                i--;

            }
        }
    }

    private void setItemColor(ToDoListRecyclerAdapter.TaskViewHolder holder) // sets the color indicating the priority of the task
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
        if (MainActivity.settings.getBoolean(context.getString(R.string.settings_show_dividers_key), true))
        {
            holder.divider.setVisibility(View.VISIBLE);
        }
        else holder.divider.setVisibility(View.GONE);
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView task_name, task_deadline;
        CheckBox isDone;
        View divider;
        View priorityIndicator;

        private TaskViewHolder(View view) {
            super(view);
            task_name = itemView.findViewById(R.id.list_task_view);
            task_deadline = itemView.findViewById(R.id.list_deadline);
            isDone = itemView.findViewById(R.id.list_checkbox);
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
}



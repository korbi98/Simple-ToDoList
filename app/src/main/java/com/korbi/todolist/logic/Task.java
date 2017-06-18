package com.korbi.todolist.logic;

import java.util.Date;

/**
 * Created by korbi on 12.03.17.
 */

public class Task
{
    private int id;
    private String taskname;
    private int isDone;
    private Date deadline;
    private int priority;
    private int timeIsSet;

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;

    public static final int NO_DEADLINE = 0;
    public static final int JUST_DATE = 1;
    public static final int DATE_AND_TIME = 2;

    public Task(int id, String taskname, Date deadline, int priority, int isDone, int timeIsSet)
    {
        this.id = id;
        this.taskname = taskname;
        this.deadline = deadline;
        this.priority = priority;
        this.isDone = isDone;
        this.timeIsSet = timeIsSet;
    }

    public int getState()
    {
        return isDone;
    }

    public String getTaskname()
    {
        return taskname;
    }

    public void setTaskname(String taskname)
    {
        this.taskname = taskname;
    }

    public void setState(int state)
    {
        this.isDone = state;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setDeadline(Date deadline)
    {
        this.deadline = deadline;
    }

    public Date getDeadline()
    {
        return deadline;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getTimeIsSet()
    {
        return timeIsSet;
    }

    public void setTimeIsSet(int timeIsSet)
    {
        this.timeIsSet = timeIsSet;
    }
}
